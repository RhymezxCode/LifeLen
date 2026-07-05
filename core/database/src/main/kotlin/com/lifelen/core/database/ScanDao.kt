package com.lifelen.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.lifelen.core.database.entity.ScanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {

    @Query("SELECT * FROM scans ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<ScanEntity>>

    @Query(
        "SELECT * FROM scans WHERE title LIKE '%' || :query || '%' " +
            "OR category LIKE '%' || :query || '%' ORDER BY createdAt DESC",
    )
    fun search(query: String): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE id = :id")
    suspend fun getById(id: String): ScanEntity?

    @Upsert
    suspend fun upsert(entity: ScanEntity)

    @Query("UPDATE scans SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM scans WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM scans")
    suspend fun deleteAll()
}
