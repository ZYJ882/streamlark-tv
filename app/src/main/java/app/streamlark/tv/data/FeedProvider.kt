package app.streamlark.tv.data

import app.streamlark.tv.model.VideoItem

/**
 * Contract for a legal, authorized short-video source. Implementations must
 * receive content through the provider owner's permitted API or the user's
 * own media library; UI code never requests browser cookies or bypass tokens.
 */
interface FeedProvider {
    val id: String
    val displayName: String

    fun loadInitial(): List<VideoItem>

    /**
     * Returns additional content after [afterId]. Returning an empty list is a
     * valid end-of-feed signal.
     */
    fun loadMore(afterId: String?): List<VideoItem>
}

/**
 * The default provider keeps development deterministic and credential-free.
 */
object DemoFeedProvider : FeedProvider {
    override val id: String = "demo"
    override val displayName: String = "StreamLark 演示推荐流"

    override fun loadInitial(): List<VideoItem> = DemoCatalog.all

    override fun loadMore(afterId: String?): List<VideoItem> = emptyList()
}

object FeedProviderRegistry {
    private var activeProvider: FeedProvider = DemoFeedProvider

    fun active(): FeedProvider = activeProvider

    /**
     * Applications may register only a provider whose endpoint, terms and
     * user authorization have been independently verified by the integrator.
     */
    fun register(provider: FeedProvider) {
        activeProvider = provider
    }
}
