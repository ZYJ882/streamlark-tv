package app.streamlark.tv.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthorizedContentProviderTest {

    @Test
    fun `demo provider exposes searchable content`() {
        val results = DemoFeedProvider.search("编码")

        assertEquals(1, results.size)
        assertEquals("lark-08", results.first().id)
    }

    @Test
    fun `every demo video resolves to a presentation profile`() {
        assertTrue(DemoFeedProvider.loadInitial().all { DemoFeedProvider.profileFor(it) != null })
    }

    @Test
    fun `profile collections resolve their declared video items`() {
        val profile = DemoFeedProvider.profile("streamlark-studio")
        assertNotNull(profile)
        val collection = DemoFeedProvider.collectionsFor(profile!!.id).first()

        assertTrue(DemoFeedProvider.collectionVideos(collection.id).isNotEmpty())
    }
}
