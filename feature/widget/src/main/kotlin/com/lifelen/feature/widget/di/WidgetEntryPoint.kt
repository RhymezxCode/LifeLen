package com.lifelen.feature.widget.di

import com.lifelen.core.data.repository.HistoryRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Glance [androidx.glance.appwidget.GlanceAppWidgetReceiver]s are plain
 * [android.appwidget.AppWidgetProvider] broadcast receivers — they are instantiated by the
 * framework and are NOT part of the Hilt component graph, so they cannot use `@Inject`.
 *
 * This entry point lets a widget pull the singleton [HistoryRepository] out of the application
 * Hilt component at runtime via
 * `EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)`.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun historyRepository(): HistoryRepository
}
