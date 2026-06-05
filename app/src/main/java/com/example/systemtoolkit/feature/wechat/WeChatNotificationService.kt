package com.example.systemtoolkit.feature.wechat

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.systemtoolkit.R
import com.example.systemtoolkit.feature.updateblocker.SystemUpdatePackages

class WeChatNotificationService : NotificationListenerService() {

    private var lastAlertTime = 0L
    private val minIntervalMs = 3000L
    private lateinit var barrageManager: BarrageManager

    override fun onCreate() {
        super.onCreate()
        barrageManager = BarrageManager(this)
        startForegroundService()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // 屏蔽系统更新通知
        if (isSystemUpdateNotification(sbn)) {
            cancelNotification(sbn.key)
            return
        }

        if (sbn.packageName != WECHAT_PACKAGE) return

        // 跳过来电/视频通话 — 微信自己处理正常
        if (isCallNotification(sbn)) return

        // 游戏横屏模式：弹幕通知，不响铃不震动
        if (isLandscapeGamingMode()) {
            val title = sbn.notification.extras?.getString(Notification.EXTRA_TITLE) ?: "微信"
            val text = sbn.notification.extras?.getString(Notification.EXTRA_TEXT) ?: ""
            barrageManager.show("$title: $text")
            return
        }

        val ringerMode = getRingerMode()
        if (!shouldAlert(ringerMode)) return

        val wechatInForeground = isWeChatInForeground()

        if (wechatInForeground) {
            // 应用内：只震动（系统通知渠道，跟随系统震动）
            postAlertNotification(silent = true)
        } else {
            // 后台/锁屏/桌面：系统通知渠道播放声音+震动，完全跟随系统设置
            postAlertNotification(silent = false)
        }

        lastAlertTime = System.currentTimeMillis()
    }

    override fun onDestroy() {
        barrageManager.dismiss()
        super.onDestroy()
    }

    // ---------- 游戏模式检测 ----------

    private fun isLandscapeGamingMode(): Boolean {
        if (!barrageManager.canDraw()) return false
        if (!barrageManager.isLandscape()) return false
        val foregroundPkg = getCurrentForegroundPackage() ?: return false
        return GamePackages.contains(this, foregroundPkg)
    }

    private fun getCurrentForegroundPackage(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null
        val now = System.currentTimeMillis()
        val stats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            now - 5_000,
            now
        )
        return stats
            .filter { it.lastTimeUsed >= now - 2_000 }
            .maxByOrNull { it.lastTimeUsed }
            ?.packageName
    }

    // ---------- 系统更新通知屏蔽 ----------

    private fun isSystemUpdateNotification(sbn: StatusBarNotification): Boolean {
        if (sbn.packageName in SystemUpdatePackages.allBlockedPackages()) return true
        val title = sbn.notification.extras?.getString(Notification.EXTRA_TITLE) ?: ""
        val text = sbn.notification.extras?.getString(Notification.EXTRA_TEXT) ?: ""
        return SystemUpdatePackages.notificationKeywords.any {
            title.contains(it) || text.contains(it)
        }
    }

    // ---------- 前台检测 ----------

    private fun isWeChatInForeground(): Boolean {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return false
        val now = System.currentTimeMillis()
        val stats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            now - 5_000,
            now
        )
        return stats.any { it.packageName == WECHAT_PACKAGE && it.lastTimeUsed >= now - 2_000 }
    }

    // ---------- 通知分类 ----------

    private fun isCallNotification(sbn: StatusBarNotification): Boolean {
        if (sbn.notification.category == Notification.CATEGORY_CALL) return true
        val title = sbn.notification.extras?.getString(Notification.EXTRA_TITLE) ?: ""
        val text = sbn.notification.extras?.getString(Notification.EXTRA_TEXT) ?: ""
        return CALL_KEYWORDS.any { title.contains(it) || text.contains(it) }
    }

    // ---------- 铃声模式 ----------

    private fun getRingerMode(): Int {
        val am = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return AudioManager.RINGER_MODE_NORMAL
        return am.ringerMode
    }

    private fun shouldAlert(ringerMode: Int): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastAlertTime < minIntervalMs) return false

        // 勿扰模式：不提醒
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return false
        if (nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL) return false

        return ringerMode == AudioManager.RINGER_MODE_NORMAL ||
               ringerMode == AudioManager.RINGER_MODE_VIBRATE ||
               ringerMode == AudioManager.RINGER_MODE_SILENT
    }

    // ---------- 系统通知提醒 ----------

    private fun postAlertNotification(silent: Boolean) {
        val channelId = if (silent) VIBRATE_CHANNEL else ALERT_SOUND_CHANNEL
        val notification = Notification.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(ALERT_NOTIFICATION_ID, notification)
        nm.cancel(ALERT_NOTIFICATION_ID)
    }

    // ---------- 前台服务 ----------

    private fun startForegroundService() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 前台服务通道（低优先级，不发声）
        val fgChannel = NotificationChannel(
            CHANNEL_ID, "服务运行中", NotificationManager.IMPORTANCE_LOW
        ).apply { description = "用于保持通知监听服务运行" }
        nm.createNotificationChannel(fgChannel)

        // 提醒通道：后台通知（声音+震动，跟随系统设置）
        val soundChannel = NotificationChannel(
            ALERT_SOUND_CHANNEL, "通知提醒", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "后台/锁屏时的声音和震动提醒"
            setSound(
                android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            enableVibration(true)
        }
        nm.createNotificationChannel(soundChannel)

        // 提醒通道：前台仅震动（不发声）
        val vibrateChannel = NotificationChannel(
            VIBRATE_CHANNEL, "震动提醒", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "应用内或静音模式下的仅震动提醒"
            setSound(null, null)
            enableVibration(true)
        }
        nm.createNotificationChannel(vibrateChannel)

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.wechat_foreground_notification_title))
            .setContentText(getString(R.string.wechat_foreground_notification_text))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
        startForeground(FOREGROUND_ID, notification)
    }

    companion object {
        private const val WECHAT_PACKAGE = "com.tencent.mm"
        private const val CHANNEL_ID = "wechat_notifier_channel"
        private const val ALERT_SOUND_CHANNEL = "alert_sound_channel"
        private const val VIBRATE_CHANNEL = "vibrate_only_channel"
        private const val FOREGROUND_ID = 1
        private const val ALERT_NOTIFICATION_ID = 99
        private val CALL_KEYWORDS = arrayOf("语音", "视频", "通话", "来电", "语音通话", "视频通话")
    }
}
