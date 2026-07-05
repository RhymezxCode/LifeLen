package com.lifelen.feature.widget

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.unit.ColorProvider
import com.lifelen.core.model.Scan
import com.lifelen.core.model.ScanCategory
import com.lifelen.feature.widget.di.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Locale

/**
 * LifeLens widget palette (Design Spec §2.1). Kept as plain [Color]s here so both the Glance
 * content and the fallback Compose previews can share them; wrap with [ColorProvider] where a
 * Glance `TextStyle`/`background` needs one.
 */
internal object WidgetColors {
    val Chamber = Color(0xFF0D0F13)
    val Raised = Color(0xFF1F242C)
    val Amber = Color(0xFFF2A33C)
    val OnAmber = Color(0xFF2A1C05)
    val TextPrimary = Color(0xFFF2F4F8)
    val TextSecondary = Color(0xFF9AA3B0)
    val Positive = Color(0xFF4CC38A)
    val Negative = Color(0xFFE5675F)
}

/** [ColorProvider] shortcut for use in Glance `TextStyle`. */
internal fun provider(color: Color): ColorProvider = ColorProvider(color)

/**
 * The shared root modifier: fill the cell, mark it as the widget background (so the system applies
 * rounded corners on Android 12+), paint the chamber surface, round the corners, and open the app
 * on tap.
 *
 * The widget module compiles independently of `:app`, so it cannot reference `MainActivity` by type.
 * We target it by component name via an explicit [Intent] instead — `:app` depends on
 * `:feature:widget` at runtime, so the class is present in the merged APK.
 */
private val openAppIntent: Intent
    get() = Intent().setComponent(ComponentName("com.lifelen", "com.lifelen.MainActivity"))

internal fun rootModifier(background: Color = WidgetColors.Chamber): GlanceModifier =
    GlanceModifier
        .fillMaxSize()
        .appWidgetBackground()
        .background(background)
        .cornerRadius(16.dp)
        .padding(12.dp)
        .clickable(actionStartActivity(openAppIntent))

/**
 * Loads the saved scan history for a widget render.
 *
 * Receivers are not Hilt components, so the [com.lifelen.core.data.repository.HistoryRepository]
 * is resolved via [WidgetEntryPoint]. Any failure (DI not ready, empty DB, IO) degrades to an
 * empty list so the widget shows its placeholder instead of crashing.
 */
internal suspend fun loadScans(context: Context): List<Scan> = runCatching {
    EntryPointAccessors
        .fromApplication(context, WidgetEntryPoint::class.java)
        .historyRepository()
        .observeHistory()
        .first()
}.getOrDefault(emptyList())

/** True when [epochMillis] falls on the current calendar day. */
internal fun isToday(epochMillis: Long): Boolean {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = epochMillis }
    return now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
}

/** Formats a price so whole values read "12" and fractional values read "12.99". */
internal fun money(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString()
    else String.format(Locale.US, "%.2f", value)

/** The value line shown for a scan: calories for food, price for a priced item, else the category. */
internal fun Scan.valueLine(): String = when {
    category == ScanCategory.FOOD && nutrition != null -> "${nutrition!!.calories} kcal"
    price != null -> "${price!!.currency}${money(price!!.lowPrice)}"
    else -> category.name.lowercase(Locale.US).replaceFirstChar { it.uppercase(Locale.US) }
}
