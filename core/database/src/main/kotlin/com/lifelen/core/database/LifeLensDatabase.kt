package com.lifelen.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lifelen.core.database.entity.ScanEntity

@Database(
    entities = [ScanEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class LifeLensDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
}
