# 手机与电视双界面路由

StreamLark 采用“共享内容与播放逻辑、按设备能力分离 UI”的策略。该模式参考 VLC Android 的公开实现：VLC 维护独立 `television` 模块与移动端模块，同时复用媒体库和播放逻辑；其启动页根据是否具备 Leanback、触控硬件、特定设备强制规则以及用户偏好选择电视或手机活动。[1] [2]

## StreamLark 判定规则

| 优先级 | 条件 | 结果 |
|---|---|---|
| 1 | 用户已显式选择手机/电视模式 | 使用用户选择的界面。 |
| 2 | 系统包含 `android.software.leanback` | 使用电视界面。 |
| 3 | 设备没有触摸屏特性 | 使用电视界面。 |
| 4 | 其他情况 | 使用手机界面。 |

手机界面使用竖屏、沉浸式媒体、触控上下滑切换和侧边操作栏；电视界面保留横向内容轨道、遥控器焦点和方向键交互。两种界面共用 `AuthorizedContentProvider`、`FeedSession`、本地收藏历史与 Media3 播放链路。

设备自动判定应只决定默认界面，不能取代用户偏好。后续版本将提供“切换到手机/电视界面”的设置项，方便平板、投屏设备和带触控的电视盒子选择适合自己的模式。

## 参考资料

[1] [VideoLAN VLC Android 源码：`StartActivity.kt`](https://github.com/videolan/vlc-android/blob/master/application/vlc-android/src/org/videolan/vlc/StartActivity.kt)  
[2] [VideoLAN VLC Android 源码：`AndroidDevices.kt`](https://github.com/videolan/vlc-android/blob/master/application/resources/src/main/java/org/videolan/resources/AndroidDevices.kt)  
[3] [Android Developers：VLC 为 Android TV 优化导航和布局](https://developer.android.com/stories/apps/vlc-android-tv)  
