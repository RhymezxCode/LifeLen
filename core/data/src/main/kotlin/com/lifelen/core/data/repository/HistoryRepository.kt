package com.lifelen.core.data.repository

import com.lifelen.core.database.ScanDao
import com.lifelen.core.model.Scan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Read/update access to the saved scan history. */
interface HistoryRepository {
    fun observeHistory(): Flow<List<Scan>>
    fun observeFavorites(): Flow<List<Scan>>
    fun search(query: String): Flow<List<Scan>>
    suspend fun getScan(id: String): Scan?
    suspend fun toggleFavorite(id: String, isFavorite: Boolean)
    suspend fun delete(id: String)
}

class DefaultHistoryRepository @Inject constructor(
    private val scanDao: ScanDao,
    private val mapper: ScanMapper,
    private val imageStore: com.lifelen.core.data.image.ImageStore,
) : HistoryRepository {

    override fun observeHistory(): Flow<List<Scan>> =
        scanDao.observeAll().map { it.map(mapper::toDomain) }

    override fun observeFavorites(): Flow<List<Scan>> =
        scanDao.observeFavorites().map { it.map(mapper::toDomain) }

    override fun search(query: String): Flow<List<Scan>> =
        scanDao.search(query).map { it.map(mapper::toDomain) }

    override suspend fun getScan(id: String): Scan? =
        scanDao.getById(id)?.let(mapper::toDomain)

    override suspend fun toggleFavorite(id: String, isFavorite: Boolean) =
        scanDao.setFavorite(id, isFavorite)

    override suspend fun delete(id: String) {
        scanDao.getById(id)?.let { imageStore.delete(it.imagePath) }
        scanDao.deleteById(id)
    }
}
