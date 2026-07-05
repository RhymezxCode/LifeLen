package com.lifelen.feature.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle

/**
 * Widget 1 — Quick Scan. A small (1x1) amber launcher tile: a scan glyph over a "Scan" label that
 * opens LifeLens straight into the camera flow. Needs no data.
 *
 * Note on lock-screen use: standard Android has not supported third-party lock-screen widgets on
 * phones since API 21. This is a home-screen [GlanceAppWidget]; it only reaches the lock screen on
 * OEMs/launchers that opt widgets into the keyguard. It is kept compact so it reads well there too.
 */
class QuickScanWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { QuickScanContent() }
    }
}

class QuickScanWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickScanWidget()
}

@Composable
internal fun QuickScanContent() {
    Box(
        modifier = rootModifier(background = WidgetColors.Amber),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = GlanceModifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "◎", // ◎
                style = TextStyle(
                    color = provider(WidgetColors.OnAmber),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
            )
            Text(
                text = "Scan",
                style = TextStyle(
                    color = provider(WidgetColors.OnAmber),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}
