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
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.systemtoolkit.R
import com.google.android.material.button.MaterialButton

class WeChatNotifierActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var usageStatsStatus: TextView
    private lateinit var overlayStatus: TextView
    private lateinit var gamePkgList: TextView
    private lateinit var btnOpenSettings: MaterialButton
    private lateinit var btnUsageStats: MaterialButton
    private lateinit var btnBattery: MaterialButton
    private lateinit var btnOverlay: MaterialButton
    private lateinit var btnAddGame: MaterialButton

    private val postNotificationsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wechat_notifier)

        statusText = findViewById(R.id.status_text)
        usageStatsStatus = findViewById(R.id.usage_stats_status)
        overlayStatus = findViewById(R.id.overlay_status)
        gamePkgList = findViewById(R.id.game_pkg_list)
        btnOpenSettings = findViewById(R.id.btn_open_settings)
        btnUsageStats = findViewById(R.id.btn_usage_stats)
        btnBattery = findViewById(R.id.btn_battery)
        btnOverlay = findViewById(R.id.btn_overlay)
        btnAddGame = findViewById(R.id.btn_add_game)

        btnOpenSettings.setOnClickListener { openNotificationSettings() }
        btnUsageStats.setOnClickListener { openUsageStatsSettings() }
        btnBattery.setOnClickListener { openBatterySettings() }
        btnOverlay.setOnClickListener { openOverlaySettings() }
        btnAddGame.setOnClickListener { showAddGameDialog() }

        requestPostNotifications()
    }

    override fun onResume() {
        super.onResume()
        updateNotificationStatus()
        updateUsageStatsStatus()
        updateOverlayStatus()
        updateGameList()
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

    // ---------- 悬浮窗权限 ----------

    private fun updateOverlayStatus() {
        val granted = Settings.canDrawOverlays(this)
        if (granted) {
            overlayStatus.text = getString(R.string.overlay_granted)
            overlayStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            btnOverlay.isEnabled = false
            btnOverlay.text = "已授权"
        } else {
            overlayStatus.text = getString(R.string.overlay_denied)
            overlayStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            btnOverlay.isEnabled = true
            btnOverlay.text = getString(R.string.btn_overlay)
        }
    }

    private fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "无法打开悬浮窗设置", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------- 游戏包名管理 ----------

    private fun updateGameList() {
        val pkgs = GamePackages.getAll(this)
        if (pkgs.isEmpty()) {
            gamePkgList.text = getString(R.string.game_list_empty)
        } else {
            gamePkgList.text = pkgs.joinToString("\n") { "✖  $it  [点击删除]" }
        }
        gamePkgList.setOnClickListener { removeGamePackage() }
    }

    private fun showAddGameDialog() {
        val input = EditText(this).apply {
            hint = "输入游戏包名，如 com.tencent.tmgp.sgame"
        }
        AlertDialog.Builder(this)
            .setTitle("添加游戏")
            .setMessage("请输入游戏的包名（可在应用详情或 Play 商店中找到）")
            .setView(input)
            .setPositiveButton("添加") { _, _ ->
                val pkg = input.text.toString().trim()
                if (pkg.isNotEmpty()) {
                    GamePackages.add(this, pkg)
                    updateGameList()
                    Toast.makeText(this, "已添加 $pkg", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun removeGamePackage() {
        val pkgs = GamePackages.getAll(this).toList()
        if (pkgs.isEmpty()) return
        AlertDialog.Builder(this)
            .setTitle("删除游戏")
            .setItems(pkgs.toTypedArray()) { _, which ->
                GamePackages.remove(this, pkgs[which])
                updateGameList()
                Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show()
            }
            .show()
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
