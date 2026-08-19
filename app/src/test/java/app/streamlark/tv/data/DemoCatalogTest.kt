package app.streamlark.tv.data

import app.streamlark.tv.model.ContentCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoCatalogTest {

    @Test
    fun `search finds matching local demo content`() {
        val results = DemoCatalog.query("编码", null)

        assertEquals(1, results.size)
        assertEquals("lark-08", results.first().id)
    }

    @Test
    fun `category filtering only returns selected content type`() {
        val results = DemoCatalog.query("", ContentCategory.MUSIC)

        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.category == ContentCategory.MUSIC })
    }

    @Test
    fun `known demo item can be resolved by id`() {
        assertNotNull(DemoCatalog.find("lark-01"))
        assertTrue(DemoCatalog.find("missing") == null)
    }
}
