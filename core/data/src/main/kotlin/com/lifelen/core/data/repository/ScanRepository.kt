package com.lifelen.core.data.repository

import com.lifelen.core.common.di.Dispatcher
import com.lifelen.core.common.di.LifelenDispatcher
import com.lifelen.core.common.result.DataResult
import com.lifelen.core.data.handler.CategoryHandlerRegistry
import com.lifelen.core.data.image.ImageStore
import com.lifelen.core.data.model.ScanOptions
import com.lifelen.core.data.qwen.AnalysisParser
import com.lifelen.core.data.qwen.QwenPrompts
import com.lifelen.core.database.ScanDao
import com.lifelen.core.model.Scan
import com.lifelen.core.network.QwenClient
import com.lifelen.core.network.util.ImageEncoder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

/** Runs a full capture → identify → enrich → persist pipeline for one photo. */
interface ScanRepository {
    suspend fun identify(imageBytes: ByteArray, options: ScanOptions): DataResult<Scan>
}

class DefaultScanRepository @Inject constructor(
    private val qwenClient: QwenClient,
    private val imageEncoder: ImageEncoder,
    private val parser: AnalysisParser,
    private val registry: CategoryHandlerRegistry,
    private val imageStore: ImageStore,
    private val mapper: ScanMapper,
    private val scanDao: ScanDao,
    @Dispatcher(LifelenDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) : ScanRepository {

    override suspend fun identify(
        imageBytes: ByteArray,
        options: ScanOptions,
    ): DataResult<Scan> = withContext(ioDispatcher) {
        try {
            val dataUrl = imageEncoder.toDataUrl(imageBytes)
            val raw = qwenClient.analyzeImage(
                imageDataUrl = dataUrl,
                systemPrompt = QwenPrompts.IDENTIFY_SYSTEM,
                userPrompt = QwenPrompts.IDENTIFY_USER,
            )
            val parsed = parser.parseAnalysis(raw)
            val enrichment = registry.handlerFor(parsed.identification.category)
                .enrich(parsed.identification, parsed.nutrition, options)

            val id = UUID.randomUUID().toString()
            val imagePath = imageStore.save(id, imageBytes)
            val scan = Scan(
                id = id,
                imagePath = imagePath,
                identification = parsed.identification,
                nutrition = enrichment.nutrition,
                price = enrichment.price,
                createdAt = System.currentTimeMillis(),
                isFavorite = false,
            )
            scanDao.upsert(mapper.toEntity(scan))
            DataResult.Success(scan)
        } catch (e: Exception) {
            DataResult.Error(e)
        }
    }
}
