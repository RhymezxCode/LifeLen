package com.lifelen.core.data.repository

import com.lifelen.core.database.ScanDao
import com.lifelen.core.database.entity.ScanEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [ScanDao] backed by a [MutableStateFlow], reproducing the ordering/filtering
 * semantics of the real Room queries so repository tests can run as plain JVM/Robolectric
 * tests without a database.
 */
class FakeScanDao(initial: List<ScanEntity> = emptyList()) : ScanDao {

    val rows = MutableStateFlow(initial)

    private fun List<ScanEntity>.newestFirst() = sortedByDescending { it.createdAt }

    override fun observeAll(): Flow<List<ScanEntity>> =
        rows.map { it.newestFirst() }

    override fun observeFavorites(): Flow<List<ScanEntity>> =
        rows.map { list -> list.filter { it.isFavorite }.newestFirst() }

    override fun search(query: String): Flow<List<ScanEntity>> =
        rows.map { list ->
            list.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
            }.newestFirst()
        }

    override suspend fun getById(id: String): ScanEntity? =
        rows.value.firstOrNull { it.id == id }

    override suspend fun upsert(entity: ScanEntity) {
        rows.value = rows.value.filterNot { it.id == entity.id } + entity
    }

    override suspend fun setFavorite(id: String, isFavorite: Boolean) {
        rows.value = rows.value.map {
            if (it.id == id) it.copy(isFavorite = isFavorite) else it
        }
    }

    override suspend fun deleteById(id: String) {
        rows.value = rows.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll() {
        rows.value = emptyList()
    }
}
