package com.verusminer.app.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.verusminer.app.data.MinerConfig
import com.verusminer.app.data.MiningStats
import com.verusminer.app.data.PreferencesManager
import com.verusminer.app.service.MiningService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@Parcelize
data class ParcelableMinerConfig(
    val walletAddress: String = "",
    val poolUrl: String = "na.vipor.net:5040",
    val workerName: String = "android-miner",
    val cpuThreads: Int = 4,
    val algorithm: String = "verus"
) : Parcelable

fun MinerConfig.toParcelable() = ParcelableMinerConfig(
    walletAddress = walletAddress,
    poolUrl = poolUrl,
    workerName = workerName,
    cpuThreads = cpuThreads,
    algorithm = algorithm
)

class MiningViewModel(application: Application) : AndroidViewModel(application) {
    
    companion object {
        private const val TAG = "MiningViewModel"
    }
    
    private val appContext = application.applicationContext
    private val preferencesManager = PreferencesManager(application)
    private var miningService: MiningService? = null
    private var bound = false
    
    private val _minerConfig = MutableStateFlow(MinerConfig())
    val minerConfig: StateFlow<MinerConfig> = _minerConfig
    
    private val _miningStats = MutableStateFlow(MiningStats())
    val miningStats: StateFlow<MiningStats> = _miningStats
    
    private val _isMining = MutableStateFlow(false)
    val isMining: StateFlow<Boolean> = _isMining
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MiningService.MiningBinder
            miningService = binder.getService()
            bound = true
            
            viewModelScope.launch {
                miningService?.miningStats?.collect { stats ->
                    _miningStats.value = stats
                }
            }
            
            viewModelScope.launch {
                miningService?.isMining?.collect { mining ->
                    _isMining.value = mining
                }
            }
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            miningService = null
            bound = false
        }
    }
    
    init {
        loadConfig()
    }
    
    fun bindService(context: Context) {
        val intent = Intent(context, MiningService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    fun unbindService(context: Context) {
        if (bound) {
            context.unbindService(serviceConnection)
            bound = false
        }
    }
    
    private fun loadConfig() {
        viewModelScope.launch {
            preferencesManager.minerConfig.collect { config ->
                _minerConfig.value = config
            }
        }
    }
    
    fun updateWalletAddress(address: String) {
        viewModelScope.launch {
            preferencesManager.saveWalletAddress(address)
        }
    }
    
    fun updatePoolUrl(url: String) {
        viewModelScope.launch {
            preferencesManager.savePoolUrl(url)
        }
    }
    
    fun updateWorkerName(name: String) {
        viewModelScope.launch {
            preferencesManager.saveWorkerName(name)
        }
    }
    
    fun updateCpuThreads(threads: Int) {
        viewModelScope.launch {
            preferencesManager.saveCpuThreads(threads)
        }
    }
    
    suspend fun startMining(context: Context) {
        val config = minerConfig.first()
        Log.d(TAG, "startMining called with config: $config")
        
        if (config.walletAddress.isBlank()) {
            Log.e(TAG, "Wallet address is empty!")
            return
        }
        
        val parcelableConfig = config.toParcelable()
        Log.d(TAG, "Created parcelable config: $parcelableConfig")
        
        val intent = Intent(context, MiningService::class.java).apply {
            action = MiningService.ACTION_START_MINING
            putExtra(MiningService.EXTRA_CONFIG, parcelableConfig)
        }
        
        Log.d(TAG, "Starting service...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        Log.d(TAG, "Service start command sent")
    }
    
    fun stopMining(context: Context) {
        val intent = Intent(context, MiningService::class.java).apply {
            action = MiningService.ACTION_STOP_MINING
        }
        context.startService(intent)
    }
}
