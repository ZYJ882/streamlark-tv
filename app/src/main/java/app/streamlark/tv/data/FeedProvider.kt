package app.streamlark.tv.data

import app.streamlark.tv.model.ContentCollection
import app.streamlark.tv.model.CreatorProfile
import app.streamlark.tv.model.VideoItem

/**
 * Base contract for a legal, authorized short-video source. Implementations
 * must receive data through the provider owner's permitted API or a user's own
 * media library; UI code never requests browser cookies or bypass tokens.
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
 * Product-level contract for content discovery. A provider that supports user
 * and collection pages must expose only display metadata and playable assets
 * it is allowed to provide to this client.
 */
interface AuthorizedContentProvider : FeedProvider {
    fun search(query: String): List<VideoItem>
    fun profile(id: String?): CreatorProfile?
    fun profileFor(video: VideoItem): CreatorProfile?
    fun works(profileId: String): List<VideoItem>
    fun collectionsFor(profileId: String): List<ContentCollection>
    fun collection(id: String?): ContentCollection?
    fun collectionVideos(collectionId: String): List<VideoItem>
}

/**
 * The default provider keeps development deterministic and credential-free.
 */
object DemoFeedProvider : AuthorizedContentProvider {
    override val id: String = "demo"
    override val displayName: String = "StreamLark 演示推荐流"

    override fun loadInitial(): List<VideoItem> = DemoCatalog.all

    override fun loadMore(afterId: String?): List<VideoItem> = emptyList()

    override fun search(query: String): List<VideoItem> = DemoCatalog.query(query, null)

    override fun profile(id: String?): CreatorProfile? = DemoCommunity.profile(id)

    override fun profileFor(video: VideoItem): CreatorProfile? = DemoCommunity.profileFor(video)

    override fun works(profileId: String): List<VideoItem> = DemoCommunity.works(profileId)

    override fun collectionsFor(profileId: String): List<ContentCollection> =
        DemoCommunity.collectionsFor(profileId)

    override fun collection(id: String?): ContentCollection? = DemoCommunity.collection(id)

    override fun collectionVideos(collectionId: String): List<VideoItem> =
        collection(collectionId)?.let(DemoCommunity::collectionVideos).orEmpty()
}

object FeedProviderRegistry {
    private var activeProvider: AuthorizedContentProvider = DemoFeedProvider

    fun active(): AuthorizedContentProvider = activeProvider

    /**
     * Applications may register only a provider whose endpoint, terms and
     * user authorization have been independently verified by the integrator.
     */
    fun register(provider: AuthorizedContentProvider) {
        activeProvider = provider
    }
}
