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
import androidx.core.app.NotificationCompat
import com.verusminer.app.MainActivity
import com.verusminer.app.R
import com.verusminer.app.data.MinerConfig
import com.verusminer.app.data.MiningStats
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
        when (intent?.action) {
            ACTION_START_MINING -> {
                val config = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_CONFIG, MinerConfig::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_CONFIG)
                } ?: return START_NOT_STICKY
                
                startForeground(NOTIFICATION_ID, createNotification())
                startMining(config)
            }
            ACTION_STOP_MINING -> {
                stopMining()
                stopSelf()
            }
        }
        return START_STICKY
    }
    
    private fun startMining(config: MinerConfig) {
        if (_isMining.value) return
        
        serviceScope.launch {
            try {
                val ccminerPath = extractCCMiner()
                if (ccminerPath == null) {
                    _isMining.value = false
                    return@launch
                }
                
                val configPath = createConfigFile(config)
                
                val processBuilder = ProcessBuilder(
                    ccminerPath,
                    "-a", config.algorithm,
                    "-o", "stratum+tcp://${config.poolUrl}",
                    "-u", "${config.walletAddress}.${config.workerName}",
                    "-p", "x",
                    "-t", config.cpuThreads.toString()
                )
                
                processBuilder.directory(filesDir)
                processBuilder.redirectErrorStream(true)
                
                miningProcess = processBuilder.start()
                _isMining.value = true
                startTime = System.currentTimeMillis()
                
                monitorMiningOutput()
                updateStatsLoop()
                
            } catch (e: Exception) {
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
            val abi = when {
                Build.SUPPORTED_ABIS.contains("arm64-v8a") -> "arm64-v8a"
                Build.SUPPORTED_ABIS.contains("armeabi-v7a") -> "armeabi-v7a"
                else -> return null
            }
            
            val ccminerFile = File(filesDir, "ccminer")
            
            assets.open("ccminer/$abi/ccminer").use { input ->
                ccminerFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            Runtime.getRuntime().exec("chmod 755 ${ccminerFile.absolutePath}").waitFor()
            
            ccminerFile.absolutePath
        } catch (e: Exception) {
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
            when {
                line.contains("accepted", ignoreCase = true) -> {
                    _miningStats.value = _miningStats.value.copy(
                        acceptedShares = _miningStats.value.acceptedShares + 1
                    )
                }
                line.contains("rejected", ignoreCase = true) -> {
                    _miningStats.value = _miningStats.value.copy(
                        rejectedShares = _miningStats.value.rejectedShares + 1
                    )
                }
                line.contains("H/s", ignoreCase = true) || line.contains("MH/s", ignoreCase = true) -> {
                    val hashrate = extractHashrate(line)
                    if (hashrate > 0) {
                        _miningStats.value = _miningStats.value.copy(hashrate = hashrate)
                    }
                }
                line.contains("diff", ignoreCase = true) -> {
                    val diff = extractDifficulty(line)
                    if (diff > 0) {
                        _miningStats.value = _miningStats.value.copy(difficulty = diff)
                    }
                }
            }
        } catch (e: Exception) {
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
        private const val CHANNEL_ID = "mining_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_START_MINING = "com.verusminer.app.START_MINING"
        const val ACTION_STOP_MINING = "com.verusminer.app.STOP_MINING"
        const val EXTRA_CONFIG = "miner_config"
    }
}
