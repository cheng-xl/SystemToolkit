package com.example.systemtoolkit.model

import com.example.systemtoolkit.feature.wechat.WeChatNotifierActivity

object FeatureRegistry {
    val tools: List<Tool> = listOf(
        Tool(
            id = "wechat_notifier",
            name = "微信通知提醒",
            description = "修复微信消息无铃声/震动的问题，支持前台静默",
            targetActivity = WeChatNotifierActivity::class.java
        )
        // 后续新功能在此添加即可，无需改动其他代码
    )
}
