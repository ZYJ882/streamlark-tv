package app.streamlark.tv.data

import app.streamlark.tv.model.ContentCategory
import app.streamlark.tv.model.VideoItem

/**
 * Safe first-party demo metadata. Stream URLs are public playback test assets,
 * not scraped platform endpoints and not authenticated user content.
 */
object DemoCatalog {
    private const val ANGEL_ONE_HLS =
        "https://storage.googleapis.com/shaka-demo-assets/angel-one-hls/hls.m3u8"
    private const val SINTEL_HLS =
        "https://storage.googleapis.com/shaka-demo-assets/sintel/hls.m3u8"
    private const val MUX_HLS =
        "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"

    val all: List<VideoItem> = listOf(
        VideoItem(
            id = "lark-01",
            title = "云端日落：大屏沉浸体验",
            creator = "StreamLark Studio",
            durationLabel = "02:18",
            category = ContentCategory.DISCOVER,
            description = "用于验证焦点导航、详情跳转与 HLS 播放流程的演示内容。",
            sourceUrl = ANGEL_ONE_HLS,
            accentColorHex = "#405B96",
            badge = "精选"
        ),
        VideoItem(
            id = "lark-02",
            title = "一首歌的空间感",
            creator = "声场实验室",
            durationLabel = "04:32",
            category = ContentCategory.MUSIC,
            description = "演示音乐分类、横向内容轨道与继续观看状态。",
            sourceUrl = SINTEL_HLS,
            accentColorHex = "#8A4F77",
            badge = "音乐"
        ),
        VideoItem(
            id = "lark-03",
            title = "周末厨房：十分钟轻食",
            creator = "日常研究所",
            durationLabel = "08:05",
            category = ContentCategory.LIFESTYLE,
            description = "演示生活类内容的详情页、收藏与全屏播放入口。",
            sourceUrl = MUX_HLS,
            accentColorHex = "#6E7144",
            badge = "生活"
        ),
        VideoItem(
            id = "lark-04",
            title = "海流如何塑造气候",
            creator = "好奇频道",
            durationLabel = "11:20",
            category = ContentCategory.KNOWLEDGE,
            description = "演示长内容的进度记忆和 Android 5.0 兼容播放器。",
            sourceUrl = ANGEL_ONE_HLS,
            accentColorHex = "#466B72",
            badge = "知识"
        ),
        VideoItem(
            id = "lark-05",
            title = "城市夜行指南",
            creator = "探索事务所",
            durationLabel = "06:48",
            category = ContentCategory.DISCOVER,
            description = "演示推荐流与遥控器确认键打开详情的基本交互。",
            sourceUrl = MUX_HLS,
            accentColorHex = "#755E93",
            badge = "发现"
        ),
        VideoItem(
            id = "lark-06",
            title = "旧唱片的数字修复",
            creator = "模拟信号",
            durationLabel = "05:14",
            category = ContentCategory.MUSIC,
            description = "演示媒体播放、暂停恢复和全屏控制条。",
            sourceUrl = SINTEL_HLS,
            accentColorHex = "#9A633E",
            badge = "音乐"
        ),
        VideoItem(
            id = "lark-07",
            title = "小空间收纳课",
            creator = "居住观察",
            durationLabel = "09:41",
            category = ContentCategory.LIFESTYLE,
            description = "演示内容筛选、收藏状态与本地偏好保存。",
            sourceUrl = ANGEL_ONE_HLS,
            accentColorHex = "#537D67",
            badge = "生活"
        ),
        VideoItem(
            id = "lark-08",
            title = "从零理解视频编码",
            creator = "像素学院",
            durationLabel = "13:06",
            category = ContentCategory.KNOWLEDGE,
            description = "演示搜索结果、详情信息和从上次位置继续播放。",
            sourceUrl = MUX_HLS,
            accentColorHex = "#4F668B",
            badge = "知识"
        )
    )

    fun find(id: String?): VideoItem? = all.firstOrNull { it.id == id }

    fun query(keyword: String, category: ContentCategory?): List<VideoItem> {
        val normalized = keyword.trim().lowercase()
        return all.filter { item ->
            (category == null || item.category == category) &&
                (normalized.isBlank() ||
                    item.title.lowercase().contains(normalized) ||
                    item.creator.lowercase().contains(normalized) ||
                    item.category.label.contains(normalized))
        }
    }
}
