package com.verusminer.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.verusminer.app.MainActivity
import com.verusminer.app.R
import com.verusminer.app.data.MinerConfig
import com.verusminer.app.data.MiningStats
import com.verusminer.app.viewmodel.ParcelableMinerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class MiningService : Service() {
    
    private val binder = MiningBinder()
    private var miningProcess: Process? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    
    private val _miningStats = MutableStateFlow(MiningStats())
    val miningStats: StateFlow<MiningStats> = _miningStats
    
    private val _isMining = MutableStateFlow(false)
    val isMining: StateFlow<Boolean> = _isMining
    
    private var startTime = 0L
    
    inner class MiningBinder : Binder() {
        fun getService(): MiningService = this@MiningService
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action}")
        when (intent?.action) {
            ACTION_START_MINING -> {
                val parcelableConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_CONFIG, ParcelableMinerConfig::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<ParcelableMinerConfig>(EXTRA_CONFIG)
                }
                
                if (parcelableConfig == null) {
                    Log.e(TAG, "Config is null, cannot start mining")
                    return START_NOT_STICKY
                }
                
                val config = MinerConfig(
                    walletAddress = parcelableConfig.walletAddress,
                    poolUrl = parcelableConfig.poolUrl,
                    workerName = parcelableConfig.workerName,
                    cpuThreads = parcelableConfig.cpuThreads,
                    algorithm = parcelableConfig.algorithm
                )
                
                Log.d(TAG, "Starting mining with config: $config")
                startForeground(NOTIFICATION_ID, createNotification())
                startMining(config)
            }
            ACTION_STOP_MINING -> {
                Log.d(TAG, "Stopping mining")
                stopMining()
                stopSelf()
            }
        }
        return START_STICKY
    }
    
    private fun startMining(config: MinerConfig) {
        Log.d(TAG, "startMining called")
        if (_isMining.value) {
            Log.d(TAG, "Already mining, ignoring")
            return
        }
        
        serviceScope.launch {
            try {
                Log.d(TAG, "Extracting ccminer binary...")
                val ccminerPath = extractCCMiner()
                if (ccminerPath == null) {
                    Log.e(TAG, "Failed to extract ccminer")
                    _isMining.value = false
                    return@launch
                }
                Log.d(TAG, "CCMiner extracted to: $ccminerPath")
                
                val configPath = createConfigFile(config)
                Log.d(TAG, "Config created at: $configPath")
                
                val command = listOf(
                    ccminerPath,
                    "-a", config.algorithm,
                    "-o", "stratum+tcp://${config.poolUrl}",
                    "-u", "${config.walletAddress}.${config.workerName}",
                    "-p", "x",
                    "-t", config.cpuThreads.toString()
                )
                
                Log.d(TAG, "Starting ccminer with command: ${command.joinToString(" ")}")
                
                val processBuilder = ProcessBuilder(command)
                processBuilder.directory(filesDir)
                processBuilder.redirectErrorStream(true)
                
                miningProcess = processBuilder.start()
                _isMining.value = true
                startTime = System.currentTimeMillis()
                
                Log.d(TAG, "Mining process started!")
                
                monitorMiningOutput()
                updateStatsLoop()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error starting mining", e)
                e.printStackTrace()
                _isMining.value = false
            }
        }
    }
    
    private fun stopMining() {
        _isMining.value = false
        miningProcess?.destroy()
        miningProcess = null
        _miningStats.value = MiningStats()
    }
    
    private fun extractCCMiner(): String? {
        return try {
            Log.d(TAG, "Detecting ABI...")
            val abi = when {
                Build.SUPPORTED_ABIS.contains("arm64-v8a") -> "arm64-v8a"
                Build.SUPPORTED_ABIS.contains("armeabi-v7a") -> "armeabi-v7a"
                else -> {
                    Log.e(TAG, "Unsupported ABI: ${Build.SUPPORTED_ABIS.joinToString()}")
                    return null
                }
            }
            Log.d(TAG, "Using ABI: $abi")
            
            val ccminerFile = File(filesDir, "ccminer")
            
            if (ccminerFile.exists()) {
                Log.d(TAG, "CCMiner already extracted, reusing")
                return ccminerFile.absolutePath
            }
            
            Log.d(TAG, "Extracting ccminer from assets...")
            val assetPath = "ccminer/$abi/ccminer"
            Log.d(TAG, "Asset path: $assetPath")
            
            assets.open(assetPath).use { input ->
                ccminerFile.outputStream().use { output ->
                    val bytescopied = input.copyTo(output)
                    Log.d(TAG, "Copied $bytescopied bytes")
                }
            }
            
            Log.d(TAG, "Setting executable permissions...")
            Runtime.getRuntime().exec("chmod 755 ${ccminerFile.absolutePath}").waitFor()
            
            Log.d(TAG, "CCMiner ready at: ${ccminerFile.absolutePath}")
            ccminerFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting ccminer", e)
            e.printStackTrace()
            null
        }
    }
    
    private fun createConfigFile(config: MinerConfig): String {
        val configFile = File(filesDir, "config.json")
        val configContent = """
            {
                "algo": "${config.algorithm}",
                "url": "stratum+tcp://${config.poolUrl}",
                "user": "${config.walletAddress}.${config.workerName}",
                "pass": "x",
                "threads": ${config.cpuThreads}
            }
        """.trimIndent()
        
        configFile.writeText(configContent)
        return configFile.absolutePath
    }
    
    private fun monitorMiningOutput() {
        serviceScope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(miningProcess?.inputStream))
                var line: String?
                
                while (reader.readLine().also { line = it } != null && _isMining.value) {
                    line?.let { parseMiningOutput(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun parseMiningOutput(line: String) {
        try {
            Log.d(TAG, "Miner output: $line")
            when {
                line.contains("accepted", ignoreCase = true) -> {
                    _miningStats.value = _miningStats.value.copy(
                        acceptedShares = _miningStats.value.acceptedShares + 1
                    )
                    Log.d(TAG, "Share accepted! Total: ${_miningStats.value.acceptedShares}")
                }
                line.contains("rejected", ignoreCase = true) -> {
                    _miningStats.value = _miningStats.value.copy(
                        rejectedShares = _miningStats.value.rejectedShares + 1
                    )
                    Log.d(TAG, "Share rejected! Total: ${_miningStats.value.rejectedShares}")
                }
                line.contains("H/s", ignoreCase = true) || line.contains("MH/s", ignoreCase = true) -> {
                    val hashrate = extractHashrate(line)
                    if (hashrate > 0) {
                        _miningStats.value = _miningStats.value.copy(hashrate = hashrate)
                        Log.d(TAG, "Hashrate: $hashrate H/s")
                    }
                }
                line.contains("diff", ignoreCase = true) -> {
                    val diff = extractDifficulty(line)
                    if (diff > 0) {
                        _miningStats.value = _miningStats.value.copy(difficulty = diff)
                        Log.d(TAG, "Difficulty: $diff")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing output", e)
            e.printStackTrace()
        }
    }
    
    private fun extractHashrate(line: String): Double {
        val regex = """(\d+\.?\d*)\s*(H/s|KH/s|MH/s)""".toRegex(RegexOption.IGNORE_CASE)
        val match = regex.find(line) ?: return 0.0
        
        val value = match.groupValues[1].toDoubleOrNull() ?: 0.0
        val unit = match.groupValues[2].uppercase()
        
        return when (unit) {
            "H/S" -> value
            "KH/S" -> value * 1000
            "MH/S" -> value * 1000000
            else -> value
        }
    }
    
    private fun extractDifficulty(line: String): Double {
        val regex = """diff[^0-9]*(\d+\.?\d*)""".toRegex(RegexOption.IGNORE_CASE)
        return regex.find(line)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
    }
    
    private fun updateStatsLoop() {
        serviceScope.launch {
            while (_isMining.value) {
                val uptime = System.currentTimeMillis() - startTime
                _miningStats.value = _miningStats.value.copy(uptime = uptime)
                
                updateNotification()
                
                delay(1000)
            }
        }
    }
    
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "VerusMiner::MiningWakeLock"
        ).apply {
            acquire(10 * 60 * 60 * 1000L)
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.mining_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Verus mining status notifications"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.mining_notification_title))
            .setContentText(getString(R.string.mining_notification_text))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification() {
        val stats = _miningStats.value
        val hashrate = when {
            stats.hashrate >= 1000000 -> "%.2f MH/s".format(stats.hashrate / 1000000)
            stats.hashrate >= 1000 -> "%.2f KH/s".format(stats.hashrate / 1000)
            else -> "%.2f H/s".format(stats.hashrate)
        }
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Mining Active")
            .setContentText("$hashrate | Accepted: ${stats.acceptedShares}")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopMining()
        wakeLock?.release()
    }
    
    companion object {
        private const val TAG = "MiningService"
        private const val CHANNEL_ID = "mining_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_START_MINING = "com.verusminer.app.START_MINING"
        const val ACTION_STOP_MINING = "com.verusminer.app.STOP_MINING"
        const val EXTRA_CONFIG = "miner_config"
    }
}
