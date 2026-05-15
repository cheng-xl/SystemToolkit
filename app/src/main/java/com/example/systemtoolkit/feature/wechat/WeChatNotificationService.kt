package com.example.systemtoolkit.feature.wechat

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.systemtoolkit.R

class WeChatNotificationService : NotificationListenerService() {

    private var lastAlertTime = 0L
    private val minIntervalMs = 3000L
    private var currentRingtone: Ringtone? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != WECHAT_PACKAGE) return

        // 跳过来电/视频通话 — 微信自己处理正常
        if (isCallNotification(sbn)) return

        val ringerMode = getRingerMode()
        if (!shouldAlert(ringerMode)) return

        val wechatInForeground = isWeChatInForeground()

        if (wechatInForeground) {
            // 应用内：只震动
            vibrate()
        } else {
            // 锁屏/桌面/后台：发声 + 震动
            if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                playSound()
            }
            vibrate()
        }

        lastAlertTime = System.currentTimeMillis()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn?.packageName == WECHAT_PACKAGE && isCallNotification(sbn)) {
            stopCurrentRingtone()
        }
    }

    override fun onDestroy() {
        stopCurrentRingtone()
        super.onDestroy()
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
        // 系统标准分类
        if (sbn.notification.category == Notification.CATEGORY_CALL) return true

        // 微信不设 category，靠通知标题中的关键词兜底
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

        // 静音模式：不提醒
        if (ringerMode == AudioManager.RINGER_MODE_SILENT) return false

        return ringerMode == AudioManager.RINGER_MODE_NORMAL ||
               ringerMode == AudioManager.RINGER_MODE_VIBRATE
    }

    // ---------- 声音 ----------

    private fun playSound() {
        stopCurrentRingtone()

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        currentRingtone = RingtoneManager.getRingtone(this, uri)
        currentRingtone?.audioAttributes = attrs
        currentRingtone?.play()
    }

    private fun stopCurrentRingtone() {
        currentRingtone?.let {
            if (it.isPlaying) it.stop()
        }
        currentRingtone = null
    }

    // ---------- 震动 ----------

    @Suppress("DEPRECATION")
    private fun vibrate() {
        val pattern = longArrayOf(0, 100, 50, 100)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            val v = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            v?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        }
    }

    // ---------- 前台服务 ----------

    private fun startForegroundService() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "服务运行中",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "用于保持通知监听服务运行"
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)

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
        private const val FOREGROUND_ID = 1
        private val CALL_KEYWORDS = arrayOf("语音", "视频", "通话", "来电", "语音通话", "视频通话")
    }
}
