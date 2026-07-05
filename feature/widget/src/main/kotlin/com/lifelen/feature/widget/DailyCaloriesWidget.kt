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
import com.lifelen.core.model.ScanCategory

/**
 * Widget 4 — Daily Calories. A medium (2x2) card summing the calories of today's FOOD scans, with
 * up to three of today's meal titles below. Empty state: "No meals logged today".
 *
 * Home-screen widget (see [QuickScanWidget] for the lock-screen caveat).
 */
class DailyCaloriesWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val todayFood = loadScans(context)
            .filter { it.category == ScanCategory.FOOD && isToday(it.createdAt) }
        val calories = todayFood.sumOf { it.nutrition?.calories ?: 0 }
        val titles = todayFood.take(3).map { it.title }
        provideContent { DailyCaloriesContent(calories = calories, titles = titles, hasMeals = todayFood.isNotEmpty()) }
    }
}

class DailyCaloriesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyCaloriesWidget()
}

@Composable
internal fun DailyCaloriesContent(calories: Int, titles: List<String>, hasMeals: Boolean) {
    Box(modifier = rootModifier(), contentAlignment = Alignment.TopStart) {
        if (!hasMeals) {
            Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No meals logged today",
                    style = TextStyle(color = provider(WidgetColors.TextSecondary), fontSize = 14.sp),
                )
            }
        } else {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Text(
                    text = calories.toString(),
                    style = TextStyle(
                        color = provider(WidgetColors.TextPrimary),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Text(
                    text = "kcal today",
                    style = TextStyle(color = provider(WidgetColors.TextSecondary), fontSize = 13.sp),
                )
                Spacer(GlanceModifier.height(8.dp))
                titles.forEach { title ->
                    Text(
                        text = "• $title",
                        maxLines = 1,
                        style = TextStyle(color = provider(WidgetColors.TextPrimary), fontSize = 13.sp),
                    )
                }
            }
        }
    }
}
