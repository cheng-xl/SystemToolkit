# 系统工具箱

Android 系统修复与增强工具集，三个工具解决 Flyme10 / Android 14 常见痛点。

---

## 工具一：微信通知提醒

修复微信消息不响铃不震动的问题。

| 场景 | 铃声 | 震动 |
|---|---|---|
| 后台 / 锁屏 / 桌面 | ✅ | ✅ |
| 聊天界面内（前台） | ❌ | ✅ |
| 静音模式 | ❌ | ✅ |
| 震动模式 | ❌ | ✅ |
| 勿扰模式 | ❌ | ❌ |
| 微信语音/视频电话 | ❌ | ❌ |

- 前台服务保活，防系统回收
- 2 秒防抖，单次震动不重复
- 来电自动拦截，不与微信自带提醒冲突
- 声音和震动完全跟随系统设置（通过 Notification 渠道实现，不会出现双响或双重震动）

## 工具二：系统更新屏蔽

永久禁止系统更新，阻止更新推送。

- **通知拦截**：自动识别系统更新通知并立即消除（基于包名 + 关键词）
- **ADB 命令**：一键复制 `pm disable-user` / `pm enable` 命令到剪贴板
- 覆盖 Flyme/Meizu + 通用系统更新包

## 工具三：游戏横屏弹幕

横屏打游戏时用弹幕代替响铃震动，不打断游戏。

- 弹幕从右滑入、匀速飘过、5 秒消失，不拦截触控
- 三条触发条件：横屏 + 悬浮窗权限已授权 + 前台是已添加的游戏
- 游戏包名需手动输入（附带常见游戏包名参考）

---

## 安装设置

1. 从 [Releases](https://github.com/cheng-xl/SystemToolkit/releases) 下载 APK 安装
2. **微信通知提醒**：进入对应页面，按顺序授予四个权限（通知监听 → 使用情况访问 → 应用管理 → 悬浮窗）
3. **游戏弹幕**：在微信通知提醒页底部添加游戏包名
4. **系统更新屏蔽**：通知拦截自动生效；如需彻底禁用更新，复制 ADB 命令到电脑执行

---

## 构建

```bash
./gradlew assembleRelease
```

要求：Android SDK 34、JDK 17。

---

## 项目结构

```
app/src/main/java/com/example/systemtoolkit/
├── MainActivity.kt
├── model/
│   ├── Tool.kt                     # 工具数据类
│   └── FeatureRegistry.kt          # 注册中心
├── ui/
│   └── ToolAdapter.kt
└── feature/
    ├── wechat/                      # 微信通知提醒 + 游戏弹幕
    │   ├── WeChatNotifierActivity.kt
    │   ├── WeChatNotificationService.kt
    │   ├── BarrageManager.kt        # 弹幕悬浮窗
    │   └── GamePackages.kt          # 游戏包名管理
    └── updateblocker/               # 系统更新屏蔽
        ├── UpdateBlockerActivity.kt
        └── SystemUpdatePackages.kt
```

## 添加新工具

1. 新建 `feature/xxx/XxxActivity.kt`
2. 在 `FeatureRegistry.kt` 中加一行注册
3. 在 `AndroidManifest.xml` 中声明 Activity

无需改动 `MainActivity`、`ToolAdapter` 等已有代码。
