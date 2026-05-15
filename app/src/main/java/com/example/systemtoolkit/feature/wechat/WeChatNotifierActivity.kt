package com.example.systemtoolkit.feature.wechat

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.systemtoolkit.R
import com.google.android.material.button.MaterialButton

class WeChatNotifierActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var usageStatsStatus: TextView
    private lateinit var btnOpenSettings: MaterialButton
    private lateinit var btnUsageStats: MaterialButton
    private lateinit var btnBattery: MaterialButton

    private val postNotificationsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wechat_notifier)

        statusText = findViewById(R.id.status_text)
        usageStatsStatus = findViewById(R.id.usage_stats_status)
        btnOpenSettings = findViewById(R.id.btn_open_settings)
        btnUsageStats = findViewById(R.id.btn_usage_stats)
        btnBattery = findViewById(R.id.btn_battery)

        btnOpenSettings.setOnClickListener { openNotificationSettings() }
        btnUsageStats.setOnClickListener { openUsageStatsSettings() }
        btnBattery.setOnClickListener { openBatterySettings() }

        requestPostNotifications()
    }

    override fun onResume() {
        super.onResume()
        updateNotificationStatus()
        updateUsageStatsStatus()
    }

    // ---------- 通知监听权限 ----------

    private fun updateNotificationStatus() {
        val enabled = isNotificationListenerEnabled()
        if (enabled) {
            statusText.text = getString(R.string.status_enabled)
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
        } else {
            statusText.text = getString(R.string.status_disabled)
            statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        val targetName = ComponentName(
            this,
            WeChatNotificationService::class.java
        ).flattenToString()
        return flat.split(":").any { it == targetName }
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "无法打开通知监听设置", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------- 使用情况访问权限 ----------

    private fun updateUsageStatsStatus() {
        val granted = isUsageStatsGranted()
        if (granted) {
            usageStatsStatus.text = getString(R.string.usage_stats_granted)
            usageStatsStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            btnUsageStats.isEnabled = false
            btnUsageStats.text = getString(R.string.btn_usage_stats_done)
        } else {
            usageStatsStatus.text = getString(R.string.usage_stats_denied)
            usageStatsStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            btnUsageStats.isEnabled = true
            btnUsageStats.text = getString(R.string.btn_usage_stats)
        }
    }

    @Suppress("DEPRECATION")
    private fun isUsageStatsGranted(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as? android.app.AppOpsManager
            ?: return false
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    private fun openUsageStatsSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "无法打开使用情况访问设置", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------- 电池优化 ----------

    private fun openBatterySettings() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            if (fallback.resolveActivity(packageManager) != null) {
                startActivity(fallback)
            } else {
                Toast.makeText(this, "无法打开电池优化设置", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------- Android 13+ 通知权限 ----------

    private fun requestPostNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                postNotificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
