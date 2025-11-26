package com.verusminer.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "miner_prefs")

class PreferencesManager(private val context: Context) {
    
    companion object {
        private val WALLET_ADDRESS = stringPreferencesKey("wallet_address")
        private val POOL_URL = stringPreferencesKey("pool_url")
        private val WORKER_NAME = stringPreferencesKey("worker_name")
        private val CPU_THREADS = intPreferencesKey("cpu_threads")
    }
    
    val minerConfig: Flow<MinerConfig> = context.dataStore.data.map { prefs ->
        MinerConfig(
            walletAddress = prefs[WALLET_ADDRESS] ?: "",
            poolUrl = prefs[POOL_URL] ?: DefaultPools.pools[0].fullAddress,
            workerName = prefs[WORKER_NAME] ?: "android-miner",
            cpuThreads = prefs[CPU_THREADS] ?: Runtime.getRuntime().availableProcessors()
        )
    }
    
    suspend fun saveWalletAddress(address: String) {
        context.dataStore.edit { prefs ->
            prefs[WALLET_ADDRESS] = address
        }
    }
    
    suspend fun savePoolUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[POOL_URL] = url
        }
    }
    
    suspend fun saveWorkerName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[WORKER_NAME] = name
        }
    }
    
    suspend fun saveCpuThreads(threads: Int) {
        context.dataStore.edit { prefs ->
            prefs[CPU_THREADS] = threads
        }
    }
    
    suspend fun saveConfig(config: MinerConfig) {
        context.dataStore.edit { prefs ->
            prefs[WALLET_ADDRESS] = config.walletAddress
            prefs[POOL_URL] = config.poolUrl
            prefs[WORKER_NAME] = config.workerName
            prefs[CPU_THREADS] = config.cpuThreads
        }
    }
}
