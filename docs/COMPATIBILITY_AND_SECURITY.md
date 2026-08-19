# Android 5.0 兼容性与安全基线

StreamLark TV 的最低支持版本是 **Android 5.0（API 21）**。首版采用 Android View、RecyclerView 与 Media3，不依赖 Jetpack Compose、Scoped Storage、DataStore 或 Android 8.0 才具备的画中画能力。

| 能力 | Android 5.0–7.x | Android 8.0+ |
|---|---|---|
| 焦点导航、搜索、详情、收藏与历史 | 支持 | 支持 |
| HLS 播放、全屏控制与进度恢复 | 支持 | 支持 |
| 画中画 | 自动降级为全屏播放 | 用户离开时进入画中画 |
| 账户/外部内容 Provider | 不内置 | 不内置 |

项目默认使用本地演示目录和公开播放测试流。任何第三方内容平台的 Provider 必须由对应权利方或已获授权的部署方实现；应用层不得要求、记录、上报或在日志中输出网页 Cookie、授权头或长期会话令牌。

Android Manifest 设置 `usesCleartextTraffic=false`，网络安全配置只信任系统证书，应用关闭系统备份。以后若增加需要保存的用户授权令牌，必须通过 Android Keystore 加密，且不得将令牌与播放历史、崩溃日志或遥测事件关联。
