# 系统工具箱

Android 系统修复与增强工具集。首个工具解决 Flyme10 / Android 14 上微信消息不响铃不震动的问题。

## 当前功能

### 微信通知提醒

| 场景 | 铃声 | 震动 |
|---|---|---|
| 微信在后台 / 锁屏 / 桌面 | ✅ | ✅ |
| 微信在前台（聊天界面内） | ❌ | ✅ |
| 震动模式 | ❌ | ✅ |
| 静音模式 | ❌ | ❌ |
| 勿扰模式 | ❌ | ❌ |
| 微信语音/视频电话 | ❌ | ❌ |

- 前台服务保活，不会被系统杀死
- 3 秒防抖，避免连续消息轰炸
- 来电自动过滤，不与微信自带来电提醒冲突

## 构建

```bash
./gradlew assembleRelease
```

要求：Android SDK 34、JDK 17。

## 安装

1. 从 [Releases](https://github.com/cheng-xl/SystemToolkit/releases) 下载最新 APK
2. 安装后打开 App → 点击「打开通知权限设置」→ 找到「系统工具箱」→ 打开
3. 点击「打开使用情况访问」→ 找到「系统工具箱」→ 允许
4. 点击「关闭电池优化」→ 选择「不优化」

## 项目结构

```
app/src/main/java/com/example/systemtoolkit/
├── MainActivity.kt              # 仪表盘
├── model/
│   ├── Tool.kt                   # 工具数据类
│   └── FeatureRegistry.kt        # 工具注册中心（加新功能只改这里）
├── ui/
│   └── ToolAdapter.kt            # 列表适配器
└── feature/
    └── wechat/                    # 微信通知提醒
        ├── WeChatNotifierActivity.kt
        └── WeChatNotificationService.kt
```

## 添加新工具

1. 新建 `feature/xxx/XxxActivity.kt`
2. 在 `FeatureRegistry.kt` 中加一行注册
3. 在 `AndroidManifest.xml` 中声明 Activity

无需改动 `MainActivity`、`ToolAdapter` 等已有代码。
