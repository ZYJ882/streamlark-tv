package app.streamlark.tv.model

/**
 * A provider-neutral piece of playable content. Demo content is intentionally
 * local metadata plus public sample streams; external providers can implement
 * the same contract without exposing session credentials to UI code.
 */
data class VideoItem(
    val id: String,
    val title: String,
    val creator: String,
    val durationLabel: String,
    val category: ContentCategory,
    val description: String,
    val sourceUrl: String,
    val accentColorHex: String,
    val badge: String = "演示"
)

enum class ContentCategory(val label: String) {
    DISCOVER("发现"),
    MUSIC("音乐"),
    LIFESTYLE("生活"),
    KNOWLEDGE("知识")
}

