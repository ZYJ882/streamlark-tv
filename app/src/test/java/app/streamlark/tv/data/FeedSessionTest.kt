package app.streamlark.tv.data

import app.streamlark.tv.model.ContentCategory
import app.streamlark.tv.model.VideoItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedSessionTest {

    @Test
    fun `session starts at requested item and keeps movement in bounds`() {
        val session = FeedSession(TestProvider, initialVideoId = "two")

        assertEquals("two", session.current?.id)
        assertEquals("one", session.moveBy(-1)?.id)
        assertEquals("one", session.moveBy(-1)?.id)
    }

    @Test
    fun `session appends unique items when cursor reaches feed tail`() {
        val session = FeedSession(PagingProvider)

        session.moveBy(1)
        session.moveBy(1)

        assertEquals(4, session.size)
        assertEquals("three", session.current?.id)
        assertTrue(session.all().map { it.id }.contains("four"))
    }

    private object TestProvider : FeedProvider {
        override val id: String = "test"
        override val displayName: String = "Test"

        override fun loadInitial(): List<VideoItem> = listOf(item("one"), item("two"))

        override fun loadMore(afterId: String?): List<VideoItem> = emptyList()
    }

    private object PagingProvider : FeedProvider {
        override val id: String = "paging"
        override val displayName: String = "Paging"
        private var loaded = false

        override fun loadInitial(): List<VideoItem> = listOf(item("one"), item("two"), item("three"))

        override fun loadMore(afterId: String?): List<VideoItem> {
            return if (!loaded && afterId == "three") {
                loaded = true
                listOf(item("four"))
            } else {
                emptyList()
            }
        }
    }

    private companion object {
        fun item(id: String) = VideoItem(
            id = id,
            title = id,
            creator = "test",
            durationLabel = "00:10",
            category = ContentCategory.DISCOVER,
            description = "test",
            sourceUrl = "https://example.com/$id.m3u8",
            accentColorHex = "#101010"
        )
    }
}
