package com.lifelen.core.database.di

import android.content.Context
import androidx.room.Room
import com.lifelen.core.database.LifeLensDatabase
import com.lifelen.core.database.ScanDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LifeLensDatabase =
        Room.databaseBuilder(context, LifeLensDatabase::class.java, "lifelens.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideScanDao(database: LifeLensDatabase): ScanDao = database.scanDao()
}
