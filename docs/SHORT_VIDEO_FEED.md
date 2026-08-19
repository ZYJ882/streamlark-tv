# 沉浸式短视频推荐流

StreamLark TV 的短视频模式使用 `FeedProvider` 与 `FeedSession` 分离内容来源、游标状态和播放器 UI。首版默认启用 `DemoFeedProvider`，因此可以在不登录、不上传 Cookie、不开启明文流量的情况下验证连续播放体验。

```text
授权内容 Provider / 本地演示目录
              │
              ▼
       FeedProvider
              │ initial / loadMore
              ▼
        FeedSession
              │ current / next / previous
              ▼
  ImmersiveFeedActivity + Media3
```

| 操作 | 遥控器行为 | 结果 |
|---|---|---|
| 上键 | `DPAD_UP` | 切换至上一条，保持当前会话顺序。 |
| 下键 | `DPAD_DOWN` | 切换至下一条；接近末尾时尝试调用 Provider 追加内容。 |
| 左/右键 | `DPAD_LEFT` / `DPAD_RIGHT` | 分别快退、快进 10 秒。 |
| 确认键 | `DPAD_CENTER` / Enter | 暂停或继续播放。 |
| 返回键 | Back | 离开沉浸式刷流，已观看条目保留进度。 |

为了支持 Android 5.0，连续播放采用单一 Media3 `ExoPlayer` 实例和 `FeedSession` 游标，而不是依赖 Android 8.0+ 的画中画、Jetpack Compose Pager 或系统手势导航。播放器在切换条目时保存当前进度，创建下一条媒体项并保留相同的控制器和错误处理逻辑。

## Provider 接入边界

任何生产 Provider 都必须满足以下要求：使用平台正式开放 API、获授权的服务端中转或用户自有媒体；将令牌保存在 Android Keystore；明确告知数据用途；遵守来源平台的使用规则。Provider 不应接受浏览器导出的 Cookie、设备指纹绕过参数、反爬绕过脚本或未经授权的私有接口。
