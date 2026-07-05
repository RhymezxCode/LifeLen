package com.lifelen.feature.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

/**
 * Widget 3 — Library Stats. A wide (4x1) card with the total scan count, an "in your library"
 * caption and today's new-scan count highlighted in the positive accent.
 *
 * Home-screen widget (see [QuickScanWidget] for the lock-screen caveat).
 */
class LibraryStatsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val scans = loadScans(context)
        val total = scans.size
        val today = scans.count { isToday(it.createdAt) }
        provideContent { LibraryStatsContent(total = total, today = today) }
    }
}

class LibraryStatsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LibraryStatsWidget()
}

@Composable
internal fun LibraryStatsContent(total: Int, today: Int) {
    Box(modifier = rootModifier(), contentAlignment = Alignment.CenterStart) {
        Column(modifier = GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = total.toString(),
                style = TextStyle(
                    color = provider(WidgetColors.TextPrimary),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                text = "in your library",
                style = TextStyle(color = provider(WidgetColors.TextSecondary), fontSize = 13.sp),
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = if (today == 1) "+1 today" else "+$today today",
                style = TextStyle(
                    color = provider(WidgetColors.Positive),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}
