package com.example.systemtoolkit.feature.updateblocker

object SystemUpdatePackages {

    // Flyme / Meizu 特有
    val flymePackages = listOf(
        "com.meizu.flyme.update",
        "com.meizu.flyme.service",
        "com.meizu.safe"
    )

    // 通用 / 其他厂商
    val genericPackages = listOf(
        "com.android.updater",
        "com.google.android.gms.update",
        "com.android.dynsystem"
    )

    // 通知标题关键词兜底
    val notificationKeywords = listOf(
        "系统更新",
        "System Update",
        "OTA",
        "固件更新"
    )

    fun allBlockedPackages(): List<String> = flymePackages + genericPackages
}
