package com.verusminer.app.data

data class MinerConfig(
    val walletAddress: String = "",
    val poolUrl: String = "na.vipor.net:5040",
    val workerName: String = "android-miner",
    val cpuThreads: Int = 4,
    val algorithm: String = "verus"
)

data class MiningStats(
    val hashrate: Double = 0.0,
    val acceptedShares: Int = 0,
    val rejectedShares: Int = 0,
    val difficulty: Double = 0.0,
    val uptime: Long = 0L,
    val temperature: Float = 0f
)

data class PoolInfo(
    val name: String,
    val url: String,
    val port: Int,
    val region: String
) {
    val fullAddress: String
        get() = "$url:$port"
}

object DefaultPools {
    val pools = listOf(
        PoolInfo("Vipor NA", "na.vipor.net", 5040, "North America"),
        PoolInfo("Vipor EU", "eu.vipor.net", 5040, "Europe"),
        PoolInfo("Vipor ASIA", "asia.vipor.net", 5040, "Asia"),
        PoolInfo("Vipor SA", "sa.vipor.net", 5040, "South America"),
        PoolInfo("Luckpool", "pool.verus.io", 9998, "Global")
    )
}
