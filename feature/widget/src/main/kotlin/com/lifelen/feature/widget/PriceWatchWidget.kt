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
import kotlin.math.abs

/**
 * Widget 5 — Price Watch. A medium (2x2) card tracking the most recent priced scan: its title, the
 * low price, and a signed movement line (▼ drop in positive, ▲ rise in negative) when a previous
 * price is on record. Empty state: "Scan a product to watch its price".
 *
 * Home-screen widget (see [QuickScanWidget] for the lock-screen caveat).
 */
class PriceWatchWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val priced = loadScans(context).filter { it.price != null }.maxByOrNull { it.createdAt }
        val title = priced?.title
        val priceText = priced?.let { "${it.price!!.currency}${money(it.price!!.lowPrice)}" }
        val delta = priced?.priceDelta
        provideContent { PriceWatchContent(title = title, priceText = priceText, delta = delta) }
    }
}

class PriceWatchWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PriceWatchWidget()
}

@Composable
internal fun PriceWatchContent(title: String?, priceText: String?, delta: Double?) {
    Box(modifier = rootModifier(), contentAlignment = Alignment.TopStart) {
        if (title == null || priceText == null) {
            Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Scan a product to watch its price",
                    style = TextStyle(color = provider(WidgetColors.TextSecondary), fontSize = 14.sp),
                )
            }
        } else {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Text(
                    text = "Price watch",
                    style = TextStyle(color = provider(WidgetColors.TextSecondary), fontSize = 12.sp),
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = title,
                    maxLines = 1,
                    style = TextStyle(color = provider(WidgetColors.TextPrimary), fontSize = 15.sp, fontWeight = FontWeight.Medium),
                )
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = priceText,
                    style = TextStyle(
                        color = provider(WidgetColors.Amber),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                if (delta != null) {
                    Spacer(GlanceModifier.height(4.dp))
                    val down = delta < 0
                    Text(
                        text = if (down) "▼ ${money(abs(delta))}" else "▲ ${money(delta)}",
                        style = TextStyle(
                            color = provider(if (down) WidgetColors.Positive else WidgetColors.Negative),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
            }
        }
    }
}
