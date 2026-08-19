package app.streamlark.tv.data

import app.streamlark.tv.model.VideoItem

/**
 * Stateful feed cursor that is independent from Android UI and Media3.
 * It can be unit-tested and reused by a pager or an immersive player.
 */
class FeedSession(
    private val provider: FeedProvider,
    initialVideoId: String? = null
) {
    private val items = provider.loadInitial().toMutableList()
    private var currentIndex: Int = items.indexOfFirst { it.id == initialVideoId }
        .takeIf { it >= 0 } ?: 0

    val current: VideoItem?
        get() = items.getOrNull(currentIndex)

    val currentPosition: Int
        get() = currentIndex

    val size: Int
        get() = items.size

    fun all(): List<VideoItem> = items.toList()

    fun moveBy(offset: Int): VideoItem? {
        if (items.isEmpty()) return null
        val target = (currentIndex + offset).coerceIn(0, items.lastIndex)
        currentIndex = target
        loadMoreIfNeeded()
        return current
    }

    fun moveTo(videoId: String): VideoItem? {
        val index = items.indexOfFirst { it.id == videoId }
        if (index < 0) return null
        currentIndex = index
        loadMoreIfNeeded()
        return current
    }

    private fun loadMoreIfNeeded() {
        if (items.isEmpty() || currentIndex < items.lastIndex - LOAD_MORE_THRESHOLD) return
        val more = provider.loadMore(items.lastOrNull()?.id)
        val knownIds = items.mapTo(mutableSetOf()) { it.id }
        items += more.filter { it.id !in knownIds }
    }

    private companion object {
        const val LOAD_MORE_THRESHOLD = 2
    }
}
