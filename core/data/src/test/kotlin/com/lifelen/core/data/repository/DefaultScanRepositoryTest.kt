package com.lifelen.core.data.repository

import com.lifelen.core.common.result.DataResult
import com.lifelen.core.data.handler.CategoryHandlerRegistry
import com.lifelen.core.data.handler.ElectronicsHandler
import com.lifelen.core.data.handler.FoodHandler
import com.lifelen.core.data.handler.GenericHandler
import com.lifelen.core.data.handler.PricingSynthesizer
import com.lifelen.core.data.model.ScanOptions
import com.lifelen.core.data.qwen.AnalysisParser
import com.lifelen.core.data.session.CaptureDraft
import com.lifelen.core.model.Identification
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.model.Scan
import com.lifelen.core.model.ScanCategory
import com.lifelen.core.network.QwenClient
import com.lifelen.core.network.util.ImageEncoder
import com.lifelen.core.search.SearchClient
import com.lifelen.core.search.SearchResult
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Robolectric-backed because identify() base64-encodes via `android.util.Base64`. */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DefaultScanRepositoryTest {

    private class FakeQwenClient(
        var onAnalyze: () -> String = { IDENTIFY_JSON },
        var onChat: () -> String = { PRICE_JSON },
    ) : QwenClient {
        override suspend fun analyzeImage(
            imageDataUrl: String,
            systemPrompt: String,
            userPrompt: String,
        ): String = onAnalyze()

        override suspend fun chat(systemPrompt: String, userPrompt: String): String = onChat()
    }

    private class FakeSearchClient : SearchClient {
        override suspend fun searchShopping(query: String): List<SearchResult> = listOf(
            SearchResult("MacBook Air on Amazon", "amazon.com", "https://a", "$849"),
            SearchResult("MacBook Air on eBay", "ebay.com", "https://b", "$799"),
        )
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val parser = AnalysisParser(json)
    private val mapper = ScanMapper(json)
    private lateinit var qwen: FakeQwenClient
    private lateinit var dao: FakeScanDao
    private lateinit var repo: DefaultScanRepository

    private fun buildRepo(qwenClient: FakeQwenClient): DefaultScanRepository {
        val pricing = PricingSynthesizer(FakeSearchClient(), qwenClient, parser)
        val registry = CategoryHandlerRegistry(
            setOf(GenericHandler(), FoodHandler(), ElectronicsHandler(pricing)),
        )
        return DefaultScanRepository(
            qwenClient = qwenClient,
            imageEncoder = ImageEncoder(),
            parser = parser,
            registry = registry,
            mapper = mapper,
            scanDao = dao,
            ioDispatcher = UnconfinedTestDispatcher(),
        )
    }

    @Before
    fun setUp() {
        dao = FakeScanDao()
        qwen = FakeQwenClient()
        repo = buildRepo(qwen)
    }

    private val draft = CaptureDraft(id = "x", imagePath = "/tmp/x.jpg", bytes = byteArrayOf(1, 2, 3))

    @Test
    fun `identify returns Success with the parsed category and enriched price`() = runTest {
        val result = repo.identify(draft, ScanOptions(pricingEnabled = true))
        assertTrue(result is DataResult.Success)
        val scan = (result as DataResult.Success).data
        assertEquals(ScanCategory.ELECTRONICS, scan.category)
        assertEquals("MacBook", scan.title)
        assertEquals("x", scan.id)
        assertEquals("/tmp/x.jpg", scan.imagePath)
        assertNotNull(scan.price)
        assertEquals(849.0, scan.price!!.lowPrice, 0.0001)
    }

    @Test
    fun `identify with pricing disabled produces no price`() = runTest {
        val result = repo.identify(draft, ScanOptions(pricingEnabled = false))
        assertTrue(result is DataResult.Success)
        assertNull((result as DataResult.Success).data.price)
    }

    @Test
    fun `save writes the scan into the dao`() = runTest {
        val result = repo.identify(draft, ScanOptions(pricingEnabled = true)) as DataResult.Success
        repo.save(result.data)
        assertNotNull(dao.getById("x"))
        assertEquals("MacBook", dao.getById("x")!!.title)
    }

    @Test
    fun `refreshPrice records the prior low as previousLowPrice`() = runTest {
        val existing = Scan(
            id = "e1",
            imagePath = "/tmp/e1.jpg",
            identification = Identification("MacBook", ScanCategory.ELECTRONICS, "", 0.9f),
            price = PriceInfo("$", lowPrice = 700.0, highPrice = 900.0, average = 800.0),
            createdAt = 1L,
        )
        val result = repo.refreshPrice(existing, ScanOptions(pricingEnabled = true))
        assertTrue(result is DataResult.Success)
        val updated = (result as DataResult.Success).data
        assertEquals(existing.price!!.lowPrice, updated.previousLowPrice!!, 0.0001)
        assertEquals(849.0, updated.price!!.lowPrice, 0.0001)
    }

    @Test
    fun `identify wraps a thrown vision error in DataResult Error`() = runTest {
        val failing = FakeQwenClient(onAnalyze = { throw RuntimeException("network down") })
        val failingRepo = buildRepo(failing)
        val result = failingRepo.identify(draft, ScanOptions(pricingEnabled = true))
        assertTrue(result is DataResult.Error)
        assertEquals("network down", (result as DataResult.Error).throwable.message)
    }

    private companion object {
        const val IDENTIFY_JSON =
            """{"title":"MacBook","category":"electronics","confidence":0.9,""" +
                """"attributes":{"Chip":"M2"},"search_query":"macbook"}"""
        const val PRICE_JSON =
            """{"currency":"$","low_price":849,"high_price":999,"average":900,""" +
                """"options":[{"retailer":"Amazon","price":849,"currency":"$","url":"a","condition":"new"}]}"""
    }
}
