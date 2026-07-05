package com.lifelen.feature.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Widget previews.
 *
 * These are STANDARD Compose `@Preview`s, not Glance previews. Glance's own preview annotation
 * (`androidx.glance.preview.Preview` + `ExperimentalGlancePreviewApi`) ships in the separate
 * `androidx.glance:glance-preview` / `glance-appwidget-preview` artifacts, which this module does
 * not depend on; and a Glance `@Composable` cannot be rendered by the ordinary Compose preview host
 * because it runs on the Glance `Applier`. So each function below re-creates its widget's look with
 * regular Compose + sample data — a faithful approximation for design iteration, while the real
 * appearance is produced by the `XxxContent` Glance composables at runtime.
 */

@Preview(name = "1 · Quick Scan (1x1)", widthDp = 72, heightDp = 72)
@Composable
private fun QuickScanWidgetPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(WidgetColors.Amber),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("◎", color = WidgetColors.OnAmber, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Scan", color = WidgetColors.OnAmber, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Preview(name = "2 · Last Scan (4x1)", widthDp = 320, heightDp = 80)
@Composable
private fun LastScanWidgetPreview() {
    WidgetCard {
        Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
            Text("Apple MacBook Air M3", color = WidgetColors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("$1099", color = WidgetColors.Amber, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text("Last scan", color = WidgetColors.TextSecondary, fontSize = 12.sp)
        }
    }
}

@Preview(name = "3 · Library Stats (4x1)", widthDp = 320, heightDp = 80)
@Composable
private fun LibraryStatsWidgetPreview() {
    WidgetCard {
        Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
            Text("128", color = WidgetColors.TextPrimary, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Text("in your library", color = WidgetColors.TextSecondary, fontSize = 13.sp)
            Text("+4 today", color = WidgetColors.Positive, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Preview(name = "4 · Daily Calories (2x2)", widthDp = 160, heightDp = 160)
@Composable
private fun DailyCaloriesWidgetPreview() {
    WidgetCard {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("1420", color = WidgetColors.TextPrimary, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Text("kcal today", color = WidgetColors.TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            listOf("Oatmeal bowl", "Grilled chicken salad", "Latte").forEach {
                Text("• $it", color = WidgetColors.TextPrimary, fontSize = 13.sp, maxLines = 1)
            }
        }
    }
}

@Preview(name = "5 · Price Watch (2x2)", widthDp = 160, heightDp = 160)
@Composable
private fun PriceWatchWidgetPreview() {
    WidgetCard {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Price watch", color = WidgetColors.TextSecondary, fontSize = 12.sp)
            Text("Sony WH-1000XM5", color = WidgetColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text("$328", color = WidgetColors.Amber, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("▼ 21", color = WidgetColors.Positive, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/** Shared chamber card chrome for the approximation previews. */
@Composable
private fun WidgetCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(WidgetColors.Chamber)
            .padding(12.dp),
    ) {
        content()
    }
}
