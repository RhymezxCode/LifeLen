package com.lifelen.core.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.lifelen.core.data.image.ImageStore
import com.lifelen.core.model.Identification
import com.lifelen.core.model.NutritionInfo
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.model.Scan
import com.lifelen.core.model.ScanCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultHistoryRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val mapper = ScanMapper(Json { ignoreUnknownKeys = true })
    private lateinit var dao: FakeScanDao
    private lateinit var repo: DefaultHistoryRepository

    private fun scan(
        id: String,
        title: String,
        category: ScanCategory,
        createdAt: Long,
        favorite: Boolean = false,
    ) = Scan(
        id = id,
        imagePath = "/does/not/exist/$id.jpg",
        identification = Identification(title, category, "summary", 0.9f),
        nutrition = if (category == ScanCategory.FOOD) NutritionInfo("1", 100, 1.0, 1.0, 1.0) else null,
        price = if (category == ScanCategory.ELECTRONICS) PriceInfo("$", 10.0, 20.0, 15.0) else null,
        createdAt = createdAt,
        isFavorite = favorite,
    )

    @Before
    fun setUp() = runTest {
        dao = FakeScanDao()
        repo = DefaultHistoryRepository(dao, mapper, ImageStore(context))
        listOf(
            scan("1", "MacBook Air", ScanCategory.ELECTRONICS, createdAt = 300L, favorite = true),
            scan("2", "Jollof rice", ScanCategory.FOOD, createdAt = 200L),
            scan("3", "iMac", ScanCategory.ELECTRONICS, createdAt = 100L),
        ).forEach { dao.upsert(mapper.toEntity(it)) }
    }

    @Test
    fun `observeHistory maps entities to domain sorted newest first`() = runTest {
        val history = repo.observeHistory().first()
        assertEquals(listOf("1", "2", "3"), history.map { it.id })
        assertEquals("MacBook Air", history.first().title)
    }

    @Test
    fun `observeFavorites returns only favourites`() = runTest {
        val favs = repo.observeFavorites().first()
        assertEquals(listOf("1"), favs.map { it.id })
    }

    @Test
    fun `search filters by title fragment`() = runTest {
        val results = repo.search("macbook").first()
        assertEquals(listOf("1"), results.map { it.id })
    }

    @Test
    fun `search filters by category`() = runTest {
        val results = repo.search("food").first()
        assertEquals(listOf("2"), results.map { it.id })
    }

    @Test
    fun `getScan returns the mapped domain scan`() = runTest {
        val scan = repo.getScan("2")!!
        assertEquals("Jollof rice", scan.title)
        assertEquals(ScanCategory.FOOD, scan.category)
    }

    @Test
    fun `getScan returns null for a missing id`() = runTest {
        assertNull(repo.getScan("nope"))
    }

    @Test
    fun `toggleFavorite updates the stored flag`() = runTest {
        repo.toggleFavorite("2", true)
        assertTrue(dao.getById("2")!!.isFavorite)
        assertEquals(listOf("1", "2"), repo.observeFavorites().first().map { it.id })
    }

    @Test
    fun `delete removes the row without throwing on a missing image file`() = runTest {
        repo.delete("1")
        assertNull(dao.getById("1"))
        assertEquals(listOf("2", "3"), repo.observeHistory().first().map { it.id })
    }

    @Test
    fun `clearAll empties the history`() = runTest {
        repo.clearAll()
        assertTrue(repo.observeHistory().first().isEmpty())
    }
}
