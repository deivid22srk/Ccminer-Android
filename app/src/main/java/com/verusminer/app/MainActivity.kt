package com.verusminer.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.verusminer.app.data.DefaultPools
import com.verusminer.app.ui.theme.VerusMinerTheme
import com.verusminer.app.viewmodel.MiningViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        setContent {
            VerusMinerTheme {
                val viewModel: MiningViewModel = viewModel()
                
                DisposableEffect(Unit) {
                    viewModel.bindService(this@MainActivity)
                    onDispose {
                        viewModel.unbindService(this@MainActivity)
                    }
                }
                
                MiningScreen(
                    viewModel = viewModel,
                    onRequestBatteryOptimization = { requestBatteryOptimization() }
                )
            }
        }
    }
    
    private fun requestBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiningScreen(
    viewModel: MiningViewModel,
    onRequestBatteryOptimization: () -> Unit
) {
    val minerConfig by viewModel.minerConfig.collectAsState()
    val miningStats by viewModel.miningStats.collectAsState()
    val isMining by viewModel.isMining.collectAsState()
    val scope = rememberCoroutineScope()
    
    var showSettings by remember { mutableStateOf(false) }
    
    val gradientColors = listOf(
        Color(0xFF1A237E),
        Color(0xFF0D47A1),
        Color(0xFF01579B)
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Verus Miner",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                    IconButton(onClick = onRequestBatteryOptimization) {
                        Icon(Icons.Default.BatteryChargingFull, "Battery")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A237E),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        if (isMining) {
                            viewModel.stopMining(androidx.compose.ui.platform.LocalContext.current)
                        } else {
                            viewModel.startMining(androidx.compose.ui.platform.LocalContext.current)
                        }
                    }
                },
                containerColor = if (isMining) Color(0xFFD32F2F) else Color(0xFF388E3C),
                contentColor = Color.White
            ) {
                val scale by animateFloatAsState(
                    targetValue = if (isMining) 1f else 1.1f,
                    label = "scale"
                )
                Icon(
                    imageVector = if (isMining) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isMining) "Stop" else "Start",
                    modifier = Modifier.scale(scale)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MiningStatusCard(
                    isMining = isMining,
                    stats = miningStats
                )
                
                ConfigCard(
                    config = minerConfig,
                    onWalletChange = { viewModel.updateWalletAddress(it) },
                    onPoolChange = { viewModel.updatePoolUrl(it) },
                    onWorkerChange = { viewModel.updateWorkerName(it) },
                    onThreadsChange = { viewModel.updateCpuThreads(it) },
                    enabled = !isMining
                )
                
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    
    if (showSettings) {
        SettingsDialog(
            config = minerConfig,
            onDismiss = { showSettings = false },
            onThreadsChange = { viewModel.updateCpuThreads(it) }
        )
    }
}

@Composable
fun MiningStatusCard(
    isMining: Boolean,
    stats: com.verusminer.app.data.MiningStats
) {
    val statusColor by animateColorAsState(
        targetValue = if (isMining) Color(0xFF4CAF50) else Color(0xFFB0BEC5),
        label = "statusColor"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(statusColor)
                )
                Text(
                    text = if (isMining) "Mining Active" else "Stopped",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            StatRow(
                icon = Icons.Default.Speed,
                label = "Hashrate",
                value = formatHashrate(stats.hashrate),
                color = Color(0xFF2196F3)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatColumn(
                    label = "Accepted",
                    value = "${stats.acceptedShares}",
                    color = Color(0xFF4CAF50)
                )
                StatColumn(
                    label = "Rejected",
                    value = "${stats.rejectedShares}",
                    color = Color(0xFFF44336)
                )
                StatColumn(
                    label = "Uptime",
                    value = formatUptime(stats.uptime),
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigCard(
    config: com.verusminer.app.data.MinerConfig,
    onWalletChange: (String) -> Unit,
    onPoolChange: (String) -> Unit,
    onWorkerChange: (String) -> Unit,
    onThreadsChange: (Int) -> Unit,
    enabled: Boolean
) {
    var walletAddress by remember(config.walletAddress) { 
        mutableStateOf(config.walletAddress) 
    }
    var workerName by remember(config.workerName) { 
        mutableStateOf(config.workerName) 
    }
    var expanded by remember { mutableStateOf(false) }
    var selectedPool by remember(config.poolUrl) { 
        mutableStateOf(DefaultPools.pools.find { it.fullAddress == config.poolUrl } ?: DefaultPools.pools[0]) 
    }
    var threads by remember(config.cpuThreads) { 
        mutableFloatStateOf(config.cpuThreads.toFloat()) 
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Configuration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
            
            OutlinedTextField(
                value = walletAddress,
                onValueChange = { 
                    walletAddress = it
                    onWalletChange(it)
                },
                label = { Text("Wallet Address") },
                leadingIcon = { Icon(Icons.Default.Wallet, null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                singleLine = true
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded && enabled,
                onExpandedChange = { if (enabled) expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = "${selectedPool.name} (${selectedPool.region})",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Mining Pool") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = enabled
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DefaultPools.pools.forEach { pool ->
                        DropdownMenuItem(
                            text = { Text("${pool.name} - ${pool.region}") },
                            onClick = {
                                selectedPool = pool
                                onPoolChange(pool.fullAddress)
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = workerName,
                onValueChange = { 
                    workerName = it
                    onWorkerChange(it)
                },
                label = { Text("Worker Name") },
                leadingIcon = { Icon(Icons.Default.Edit, null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                singleLine = true
            )
            
            Column {
                Text(
                    text = "CPU Threads: ${threads.roundToInt()}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = threads,
                    onValueChange = { threads = it },
                    onValueChangeFinished = { 
                        onThreadsChange(threads.roundToInt()) 
                    },
                    valueRange = 1f..Runtime.getRuntime().availableProcessors().toFloat(),
                    steps = Runtime.getRuntime().availableProcessors() - 2,
                    enabled = enabled
                )
                Text(
                    text = "Max: ${Runtime.getRuntime().availableProcessors()} cores",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun SettingsDialog(
    config: com.verusminer.app.data.MinerConfig,
    onDismiss: () -> Unit,
    onThreadsChange: (Int) -> Unit
) {
    var threads by remember { mutableFloatStateOf(config.cpuThreads.toFloat()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Advanced Settings") },
        text = {
            Column {
                Text("CPU Threads: ${threads.roundToInt()}")
                Slider(
                    value = threads,
                    onValueChange = { threads = it },
                    valueRange = 1f..Runtime.getRuntime().availableProcessors().toFloat(),
                    steps = Runtime.getRuntime().availableProcessors() - 2
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onThreadsChange(threads.roundToInt())
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun StatRow(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun StatColumn(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

fun formatHashrate(hashrate: Double): String {
    return when {
        hashrate >= 1000000 -> "%.2f MH/s".format(hashrate / 1000000)
        hashrate >= 1000 -> "%.2f KH/s".format(hashrate / 1000)
        hashrate > 0 -> "%.2f H/s".format(hashrate)
        else -> "0 H/s"
    }
}

fun formatUptime(uptime: Long): String {
    val seconds = (uptime / 1000) % 60
    val minutes = (uptime / (1000 * 60)) % 60
    val hours = (uptime / (1000 * 60 * 60))
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}
