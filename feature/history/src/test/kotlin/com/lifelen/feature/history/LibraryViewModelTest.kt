package com.lifelen.feature.history

import app.cash.turbine.test
import com.lifelen.core.data.repository.HistoryRepository
import com.lifelen.core.model.Identification
import com.lifelen.core.model.NutritionInfo
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.model.Scan
import com.lifelen.core.model.ScanCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
private class FakeHistoryRepository(initial: List<Scan>) : HistoryRepository {
    val scans = MutableStateFlow(initial)
    override fun observeHistory(): Flow<List<Scan>> = scans
    override fun observeFavorites(): Flow<List<Scan>> = scans.map { list -> list.filter { it.isFavorite } }
    override fun search(query: String): Flow<List<Scan>> =
        scans.map { list -> list.filter { it.title.contains(query, ignoreCase = true) } }
    override suspend fun getScan(id: String): Scan? = scans.value.firstOrNull { it.id == id }
    override suspend fun toggleFavorite(id: String, isFavorite: Boolean) = Unit
    override suspend fun delete(id: String) {
        scans.value = scans.value.filterNot { it.id == id }
    }

    override suspend fun clearAll() {
        scans.value = emptyList()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val dayMs = 24 * 60 * 60 * 1000L
    private val now = 1_700_000_000_000L // fixed instant, mid-afternoon UTC

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After fun tearDown() = Dispatchers.resetMain()

    private fun scan(id: String, title: String, category: ScanCategory, createdAt: Long, price: Double? = null, kcal: Int? = null) =
        Scan(
            id = id,
            imagePath = "/$id.jpg",
            identification = Identification(title, category, "", 0.9f),
            nutrition = kcal?.let { NutritionInfo("1", it, 0.0, 0.0, 0.0) },
            price = price?.let { PriceInfo("$", it, it, it) },
            createdAt = createdAt,
        )

    private suspend fun app.cash.turbine.ReceiveTurbine<LibraryUiState>.awaitLoaded(): LibraryUiState {
        var state = awaitItem()
        while (state.isLoading) state = awaitItem()
        return state
    }

    @Test
    fun `groups scans by day and reports total count`() = runTest {
        val repo = FakeHistoryRepository(
            listOf(
                scan("1", "MacBook Air", ScanCategory.ELECTRONICS, now, price = 849.0),
                scan("2", "Jollof rice", ScanCategory.FOOD, now - 60_000, kcal = 540),
                scan("3", "AirPods Pro", ScanCategory.ELECTRONICS, now - 2 * dayMs, price = 199.0),
            ),
        )
        LibraryViewModel(repo).uiState.test {
            val state = awaitLoaded()
            assertEquals(3, state.totalCount)
            // Two scans share "today", one is older → at least two groups, newest group first.
            assertTrue(state.groups.size >= 2)
            assertEquals(2, state.groups.first().scans.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `category filter narrows the list`() = runTest {
        val repo = FakeHistoryRepository(
            listOf(
                scan("1", "MacBook", ScanCategory.ELECTRONICS, now, price = 849.0),
                scan("2", "Jollof", ScanCategory.FOOD, now, kcal = 540),
            ),
        )
        val vm = LibraryViewModel(repo)
        vm.uiState.test {
            awaitLoaded()
            vm.onFilter(ScanCategory.FOOD)
            var state = awaitItem()
            // Wait past the transient emission where the filter has flipped but the list hasn't yet.
            while (state.filter != ScanCategory.FOOD ||
                state.groups.flatMap { it.scans }.let { it.isEmpty() || it.any { s -> s.category != ScanCategory.FOOD } }
            ) {
                state = awaitItem()
            }
            val shown = state.groups.flatMap { it.scans }
            assertEquals(1, shown.size)
            assertEquals("Jollof", shown.first().title)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `search matches titles`() = runTest {
        val repo = FakeHistoryRepository(
            listOf(
                scan("1", "MacBook Air", ScanCategory.ELECTRONICS, now, price = 849.0),
                scan("2", "Jollof rice", ScanCategory.FOOD, now, kcal = 540),
            ),
        )
        val vm = LibraryViewModel(repo)
        vm.uiState.test {
            awaitLoaded()
            vm.onQueryChange("mac")
            var state = awaitItem()
            // Wait past the transient emission where the query has changed but the list hasn't yet.
            while (state.query != "mac" ||
                state.groups.flatMap { it.scans }.let { it.isEmpty() || it.any { s -> !s.title.contains("mac", true) } }
            ) {
                state = awaitItem()
            }
            val shown = state.groups.flatMap { it.scans }
            assertEquals(1, shown.size)
            assertEquals("MacBook Air", shown.first().title)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `empty repository yields no groups`() = runTest {
        LibraryViewModel(FakeHistoryRepository(emptyList())).uiState.test {
            val state = awaitLoaded()
            assertEquals(0, state.totalCount)
            assertTrue(state.groups.isEmpty())
            cancelAndConsumeRemainingEvents()
        }
    }
}
