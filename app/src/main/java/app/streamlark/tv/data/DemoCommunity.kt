package app.streamlark.tv.data

import app.streamlark.tv.model.ContentCollection
import app.streamlark.tv.model.CreatorProfile
import app.streamlark.tv.model.VideoItem

/**
 * Local presentation data for community-style TV pages. A production provider
 * can replace this object through [AuthorizedContentProvider] after approval.
 */
object DemoCommunity {
    val profiles: List<CreatorProfile> = listOf(
        CreatorProfile(
            id = "streamlark-studio",
            displayName = "StreamLark Studio",
            handle = "@streamlark",
            bio = "为旧款电视设备设计的沉浸式内容体验实验室。",
            followerLabel = "12.8 万关注",
            accentColorHex = "#405B96"
        ),
        CreatorProfile(
            id = "daily-lab",
            displayName = "日常研究所",
            handle = "@daily_lab",
            bio = "把可持续的日常灵感带到你的大屏。",
            followerLabel = "8.6 万关注",
            accentColorHex = "#6E7144"
        ),
        CreatorProfile(
            id = "curious-channel",
            displayName = "好奇频道",
            handle = "@curious_channel",
            bio = "用通俗的方式讲清楚复杂问题。",
            followerLabel = "23.4 万关注",
            accentColorHex = "#466B72"
        )
    )

    private val videoOwners: Map<String, String> = mapOf(
        "lark-01" to "streamlark-studio",
        "lark-02" to "streamlark-studio",
        "lark-03" to "daily-lab",
        "lark-04" to "curious-channel",
        "lark-05" to "streamlark-studio",
        "lark-06" to "streamlark-studio",
        "lark-07" to "daily-lab",
        "lark-08" to "curious-channel"
    )

    val collections: List<ContentCollection> = listOf(
        ContentCollection(
            id = "collection-evening",
            ownerId = "streamlark-studio",
            title = "大屏夜间放映",
            subtitle = "适合客厅慢下来看的 4 条内容",
            videoIds = listOf("lark-01", "lark-02", "lark-05", "lark-06"),
            accentColorHex = "#405B96"
        ),
        ContentCollection(
            id = "collection-everyday",
            ownerId = "daily-lab",
            title = "轻松的居住实验",
            subtitle = "关于厨房、收纳和日常节奏",
            videoIds = listOf("lark-03", "lark-07"),
            accentColorHex = "#6E7144"
        ),
        ContentCollection(
            id = "collection-curiosity",
            ownerId = "curious-channel",
            title = "十分钟好奇心",
            subtitle = "从气候到编码的知识短片",
            videoIds = listOf("lark-04", "lark-08"),
            accentColorHex = "#466B72"
        )
    )

    fun profile(id: String?): CreatorProfile? = profiles.firstOrNull { it.id == id }

    fun profileFor(video: VideoItem): CreatorProfile? = profile(videoOwners[video.id])

    fun works(profileId: String): List<VideoItem> =
        DemoCatalog.all.filter { videoOwners[it.id] == profileId }

    fun collectionsFor(profileId: String): List<ContentCollection> =
        collections.filter { it.ownerId == profileId }

    fun collection(id: String?): ContentCollection? = collections.firstOrNull { it.id == id }

    fun collectionVideos(collection: ContentCollection): List<VideoItem> =
        collection.videoIds.mapNotNull(DemoCatalog::find)
}
