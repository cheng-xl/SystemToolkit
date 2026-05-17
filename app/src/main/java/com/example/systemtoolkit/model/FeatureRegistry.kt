package com.example.systemtoolkit.model

import com.example.systemtoolkit.feature.wechat.WeChatNotifierActivity
import com.example.systemtoolkit.feature.updateblocker.UpdateBlockerActivity

object FeatureRegistry {
    val tools: List<Tool> = listOf(
        Tool(
            id = "wechat_notifier",
            name = "微信通知提醒",
            description = "修复微信消息无铃声/震动的问题，支持前台静默",
            targetActivity = WeChatNotifierActivity::class.java
        ),
        Tool(
            id = "update_blocker",
            name = "系统更新屏蔽",
            description = "永久禁止系统更新，阻断更新推送通知",
            targetActivity = UpdateBlockerActivity::class.java
        )
    )
}
