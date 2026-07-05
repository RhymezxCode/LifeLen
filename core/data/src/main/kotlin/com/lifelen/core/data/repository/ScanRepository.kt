package com.lifelen.core.data.repository

import com.lifelen.core.common.di.Dispatcher
import com.lifelen.core.common.di.LifelenDispatcher
import com.lifelen.core.common.result.DataResult
import com.lifelen.core.data.handler.CategoryHandlerRegistry
import com.lifelen.core.data.model.ScanOptions
import com.lifelen.core.data.qwen.AnalysisParser
import com.lifelen.core.data.qwen.QwenPrompts
import com.lifelen.core.data.session.CaptureDraft
import com.lifelen.core.database.ScanDao
import com.lifelen.core.model.Scan
import com.lifelen.core.network.QwenClient
import com.lifelen.core.network.util.ImageEncoder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Identify a captured frame and (separately) persist it. Identification does NOT write to the
 * library — only [save] does — so the result sheet can appear before the user commits to keeping it.
 */
interface ScanRepository {
    /** Runs vision + enrichment on an already-captured [CaptureDraft]. Does not persist. */
    suspend fun identify(draft: CaptureDraft, options: ScanOptions): DataResult<Scan>

    /** Writes the scan to the library (the "Save to library" action). */
    suspend fun save(scan: Scan)

    /** Re-fetches live pricing, recording the prior low as [Scan.previousLowPrice] for the trend pill. */
    suspend fun refreshPrice(scan: Scan, options: ScanOptions): DataResult<Scan>
}

class DefaultScanRepository @Inject constructor(
    private val qwenClient: QwenClient,
    private val imageEncoder: ImageEncoder,
    private val parser: AnalysisParser,
    private val registry: CategoryHandlerRegistry,
    private val mapper: ScanMapper,
    private val scanDao: ScanDao,
    @Dispatcher(LifelenDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) : ScanRepository {

    override suspend fun identify(
        draft: CaptureDraft,
        options: ScanOptions,
    ): DataResult<Scan> = withContext(ioDispatcher) {
        try {
            val dataUrl = imageEncoder.toDataUrl(draft.bytes)
            val raw = qwenClient.analyzeImage(
                imageDataUrl = dataUrl,
                systemPrompt = QwenPrompts.IDENTIFY_SYSTEM,
                userPrompt = QwenPrompts.IDENTIFY_USER,
            )
            val parsed = parser.parseAnalysis(raw)
            val enrichment = registry.handlerFor(parsed.identification.category)
                .enrich(parsed.identification, parsed.nutrition, options)

            DataResult.Success(
                Scan(
                    id = draft.id,
                    imagePath = draft.imagePath,
                    identification = parsed.identification,
                    nutrition = enrichment.nutrition,
                    price = enrichment.price,
                    createdAt = System.currentTimeMillis(),
                    isFavorite = false,
                ),
            )
        } catch (e: Exception) {
            DataResult.Error(e)
        }
    }

    override suspend fun save(scan: Scan) = withContext(ioDispatcher) {
        scanDao.upsert(mapper.toEntity(scan))
    }

    override suspend fun refreshPrice(
        scan: Scan,
        options: ScanOptions,
    ): DataResult<Scan> = withContext(ioDispatcher) {
        try {
            val enrichment = registry.handlerFor(scan.category)
                .enrich(scan.identification, scan.nutrition, options.copy(pricingEnabled = true))
            val updated = scan.copy(
                price = enrichment.price ?: scan.price,
                previousLowPrice = scan.price?.lowPrice ?: scan.previousLowPrice,
            )
            // Persist the refresh only if this scan is already in the library.
            if (scanDao.getById(scan.id) != null) {
                scanDao.upsert(mapper.toEntity(updated))
            }
            DataResult.Success(updated)
        } catch (e: Exception) {
            DataResult.Error(e)
        }
    }
}
