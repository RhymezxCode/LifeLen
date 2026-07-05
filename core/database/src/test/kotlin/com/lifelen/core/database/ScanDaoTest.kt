package com.lifelen.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lifelen.core.database.entity.ScanEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScanDaoTest {

    private lateinit var db: LifeLensDatabase
    private lateinit var dao: ScanDao

    private fun entity(
        id: String,
        title: String,
        category: String,
        createdAt: Long,
        isFavorite: Boolean = false,
    ) = ScanEntity(
        id = id,
        imagePath = "/img/$id.jpg",
        title = title,
        category = category,
        identificationJson = """{"title":"$title","category":"$category","summary":""}""",
        nutritionJson = null,
        priceJson = null,
        createdAt = createdAt,
        isFavorite = isFavorite,
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, LifeLensDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.scanDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `upsert then getById returns the stored row`() = runTest {
        val row = entity("1", "MacBook", "ELECTRONICS", createdAt = 100L)
        dao.upsert(row)
        assertEquals(row, dao.getById("1"))
        assertNull(dao.getById("missing"))
    }

    @Test
    fun `upsert replaces an existing row with the same id`() = runTest {
        dao.upsert(entity("1", "Old", "ELECTRONICS", createdAt = 1L))
        dao.upsert(entity("1", "New", "ELECTRONICS", createdAt = 2L))
        assertEquals("New", dao.getById("1")!!.title)
        assertEquals(1, dao.observeAll().first().size)
    }

    @Test
    fun `observeAll orders by createdAt descending`() = runTest {
        dao.upsert(entity("a", "A", "FOOD", createdAt = 100L))
        dao.upsert(entity("b", "B", "FOOD", createdAt = 300L))
        dao.upsert(entity("c", "C", "FOOD", createdAt = 200L))
        assertEquals(listOf("b", "c", "a"), dao.observeAll().first().map { it.id })
    }

    @Test
    fun `observeFavorites reflects setFavorite`() = runTest {
        dao.upsert(entity("1", "Kept", "ELECTRONICS", createdAt = 10L))
        dao.upsert(entity("2", "Other", "ELECTRONICS", createdAt = 20L))
        assertTrue(dao.observeFavorites().first().isEmpty())

        dao.setFavorite("1", true)
        assertEquals(listOf("1"), dao.observeFavorites().first().map { it.id })

        dao.setFavorite("1", false)
        assertTrue(dao.observeFavorites().first().isEmpty())
    }

    @Test
    fun `search matches on title`() = runTest {
        dao.upsert(entity("1", "MacBook Air", "ELECTRONICS", createdAt = 10L))
        dao.upsert(entity("2", "Jollof rice", "FOOD", createdAt = 20L))
        assertEquals(listOf("1"), dao.search("mac").first().map { it.id })
    }

    @Test
    fun `search matches on category`() = runTest {
        dao.upsert(entity("1", "MacBook Air", "ELECTRONICS", createdAt = 10L))
        dao.upsert(entity("2", "Jollof rice", "FOOD", createdAt = 20L))
        assertEquals(listOf("2"), dao.search("FOOD").first().map { it.id })
    }

    @Test
    fun `deleteById removes only the target row`() = runTest {
        dao.upsert(entity("1", "A", "FOOD", createdAt = 10L))
        dao.upsert(entity("2", "B", "FOOD", createdAt = 20L))
        dao.deleteById("1")
        assertEquals(listOf("2"), dao.observeAll().first().map { it.id })
    }

    @Test
    fun `deleteAll empties the table`() = runTest {
        dao.upsert(entity("1", "A", "FOOD", createdAt = 10L))
        dao.upsert(entity("2", "B", "FOOD", createdAt = 20L))
        dao.deleteAll()
        assertTrue(dao.observeAll().first().isEmpty())
    }
}
