# StreamLark TV 产品能力清单

本版本以独立实现的方式整合了大屏短视频客户端的核心体验：推荐流、沉浸刷流、搜索、详情、收藏、历史、创作者主页与合集。所有页面均以 Android 5.0（API 21）和遥控器焦点导航为基础。

| 能力域 | 当前实现 | 数据边界 |
|---|---|---|
| 推荐与刷流 | 首页内容轨道、沉浸刷流页、上下条切换、Media3 队列与进度保存。 | `AuthorizedContentProvider.loadInitial/loadMore`。 |
| 搜索与筛选 | 首页关键词搜索、分类筛选、无结果提示。 | `AuthorizedContentProvider.search`。 |
| 内容详情 | 描述、播放、收藏、已保存进度、创作者和合集入口。 | Provider 返回的公开视频元数据。 |
| 我的资料库 | 设备本地收藏和观看历史。 | `SharedPreferences`；不保存账户会话。 |
| 创作者主页 | 简介、作品轨道和合集轨道。 | `profile/profileFor/works/collectionsFor`。 |
| 合集详情 | 合集说明、内容轨道与第一条播放入口。 | `collection/collectionVideos`。 |
| 外部账户 | 未实现。 | 不导入网页 Cookie，不调用私有接口，不包含平台保护绕过逻辑。 |

## 授权 Provider 接入

生产 Provider 必须实现 `AuthorizedContentProvider`，并只使用来源方批准的 API、用户自有媒体库或已获授权的服务端。Provider 应将访问令牌隔离在安全存储中，避免将任何敏感值传入页面、日志、崩溃上报或播放历史。

`DemoFeedProvider` 是当前默认实现，用于让所有页面在不登录、不访问第三方账户的情况下完成测试和演示。
