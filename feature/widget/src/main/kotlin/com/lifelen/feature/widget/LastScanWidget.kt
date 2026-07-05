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
import com.lifelen.core.model.Scan

/**
 * Widget 2 — Last Scan. A wide (4x1) card showing the most recent scan: its title, a value line
 * (calories for food, price for a priced item, else the category) and a "Last scan" caption.
 *
 * Home-screen widget (see [QuickScanWidget] for the lock-screen caveat); the compact single-row
 * layout keeps it glanceable.
 */
class LastScanWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val mostRecent = loadScans(context).maxByOrNull { it.createdAt }
        provideContent { LastScanContent(mostRecent) }
    }
}

class LastScanWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LastScanWidget()
}

@Composable
internal fun LastScanContent(scan: Scan?) {
    Box(modifier = rootModifier(), contentAlignment = Alignment.CenterStart) {
        if (scan == null) {
            Text(
                text = "No scans yet",
                style = TextStyle(color = provider(WidgetColors.TextSecondary), fontSize = 15.sp),
            )
        } else {
            Column(modifier = GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = scan.title,
                    maxLines = 1,
                    style = TextStyle(
                        color = provider(WidgetColors.TextPrimary),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = scan.valueLine(),
                    maxLines = 1,
                    style = TextStyle(color = provider(WidgetColors.Amber), fontSize = 15.sp, fontWeight = FontWeight.Medium),
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = "Last scan",
                    style = TextStyle(color = provider(WidgetColors.TextSecondary), fontSize = 12.sp),
                )
            }
        }
    }
}
