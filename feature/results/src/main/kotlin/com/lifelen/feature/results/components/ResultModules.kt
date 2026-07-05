package com.lifelen.feature.results.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lifelen.core.designsystem.LifeLensIcons
import com.lifelen.core.designsystem.component.BracketThumb
import com.lifelen.core.designsystem.component.ButtonType
import com.lifelen.core.designsystem.component.CategoryChip
import com.lifelen.core.designsystem.component.ConfidenceBadge
import com.lifelen.core.designsystem.component.LifeLensButton
import com.lifelen.core.designsystem.component.RaisedIconButton
import com.lifelen.core.designsystem.component.Skeleton
import com.lifelen.core.designsystem.component.SourceFootnote
import com.lifelen.core.designsystem.component.StatRow
import com.lifelen.core.designsystem.component.Stepper
import com.lifelen.core.designsystem.component.clickableEnabled
import com.lifelen.core.designsystem.component.visual
import com.lifelen.core.designsystem.theme.Amber
import com.lifelen.core.designsystem.theme.AmberTint
import com.lifelen.core.designsystem.theme.BodyStyle
import com.lifelen.core.designsystem.theme.CaptionStyle
import com.lifelen.core.designsystem.theme.CatPlant
import com.lifelen.core.designsystem.theme.DataLg
import com.lifelen.core.designsystem.theme.DataSm
import com.lifelen.core.designsystem.theme.DataXl
import com.lifelen.core.designsystem.theme.Hairline
import com.lifelen.core.designsystem.theme.LabelStyle
import com.lifelen.core.designsystem.theme.LifeLensShapes
import com.lifelen.core.designsystem.theme.MacroCarbs
import com.lifelen.core.designsystem.theme.MacroFat
import com.lifelen.core.designsystem.theme.MacroProtein
import com.lifelen.core.designsystem.theme.Raised
import com.lifelen.core.designsystem.theme.SubtleBorder
import com.lifelen.core.designsystem.theme.TextFaint
import com.lifelen.core.designsystem.theme.TextPrimary
import com.lifelen.core.designsystem.theme.TextSecondary
import com.lifelen.core.model.NutritionInfo
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.model.Scan
import com.lifelen.core.model.ScanCategory
import java.io.File
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

// ---------------------------------------------------------------------------
// Identity header — shared by every category (Design Spec §3.3 IdentityHeader).
// ---------------------------------------------------------------------------

@Composable
internal fun IdentityHeader(scan: Scan, modifier: Modifier = Modifier) {
    val lowConfidence = scan.identification.confidence < 0.70f
    val title = if (lowConfidence) "Looks like ${scan.title}" else scan.title
    Column(modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BracketThumb(size = 46.dp) {
                AsyncImage(
                    model = File(scan.imagePath),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = title,
                    style = com.lifelen.core.designsystem.theme.TitleStyle,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                MetaChips(scan)
                Text(
                    text = "Not this?",
                    style = CaptionStyle.copy(textDecoration = TextDecoration.Underline),
                    color = TextSecondary,
                    modifier = Modifier.clickableEnabled(true) { /* no-op */ },
                )
            }
        }
        if (lowConfidence) {
            Text(
                text = "Retake closer or pick a category",
                style = CaptionStyle,
                color = TextFaint,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun MetaChips(scan: Scan) {
    val visual = scan.category.visual()
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CategoryChip(label = visual.label, color = visual.color, tint = visual.tint)
        ConfidenceBadge(confidence = scan.identification.confidence)
        if (scan.category == ScanCategory.FOOD) {
            NeutralChip("Photo estimate")
        }
    }
}

@Composable
private fun NeutralChip(text: String) {
    Text(
        text = text,
        style = CaptionStyle,
        color = TextSecondary,
        modifier = Modifier
            .clip(LifeLensShapes.chip)
            .background(Raised)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

// ---------------------------------------------------------------------------
// Electronics / product modules (Design Spec §3.3 S04).
// ---------------------------------------------------------------------------

@Composable
internal fun ProductResultBody(
    scan: Scan,
    saved: Boolean,
    onSave: () -> Unit,
    onOpenPrices: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(modifier.fillMaxWidth()) {
        val stats = scan.identification.attributes.entries.take(4).map { it.key to it.value }
        if (stats.isNotEmpty()) {
            StatRow(stats = stats, modifier = Modifier.padding(top = 18.dp))
        }
        scan.price?.let { price ->
            Spacer(Modifier.height(16.dp))
            PriceBlock(
                price = price,
                onSellers = { onOpenPrices(scan.id) },
            )
        }
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LifeLensButton(
                text = if (saved) "Saved" else "Save to library",
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = !saved,
            )
            RaisedIconButton(
                icon = LifeLensIcons.Share,
                contentDescription = "Share",
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, scan.title)
                    }
                    context.startActivity(Intent.createChooser(intent, null))
                },
            )
        }
        Spacer(Modifier.height(14.dp))
        SourceFootnote(
            text = scan.price?.let { "Prices via ${it.source}" } ?: "Identified moments ago",
        )
        Spacer(Modifier.height(14.dp))
    }
}

@Composable
internal fun PriceBlock(
    price: PriceInfo,
    onSellers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(LifeLensShapes.card)
            .background(Raised)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Lowest price · new", style = CaptionStyle, color = TextSecondary)
            Text(
                text = "${price.currency}${price.lowPrice.formatMoney()}",
                style = DataLg,
                color = Amber,
            )
            val cheapest = price.cheapestNew
            if (cheapest != null) {
                val meta = cheapest.meta?.takeIf { it.isNotBlank() }
                Text(
                    text = if (meta != null) "${cheapest.retailer} · $meta" else cheapest.retailer,
                    style = CaptionStyle,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(
            modifier = Modifier
                .clip(LifeLensShapes.chip)
                .background(Color.White.copy(alpha = 0.08f))
                .clickableEnabled(true, onSellers)
                .padding(horizontal = 13.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text("${price.sellerCount} sellers", style = LabelStyle, color = TextPrimary)
            Icon(
                LifeLensIcons.ChevronRight,
                contentDescription = "View sellers",
                tint = TextPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Plant modules (Design Spec §4 — StatRow + CareCard).
// ---------------------------------------------------------------------------

@Composable
internal fun PlantResultBody(
    scan: Scan,
    saved: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val attributes = scan.identification.attributes
    Column(modifier.fillMaxWidth()) {
        val stats = attributes.entries.take(4).map { it.key to it.value }
        if (stats.isNotEmpty()) {
            StatRow(stats = stats, modifier = Modifier.padding(top = 18.dp))
        }
        Spacer(Modifier.height(16.dp))
        CareCard(
            watering = attributes["Water"] ?: attributes["Watering"],
            petSafe = attributes["Pet-safe"] ?: attributes["Pet safe"],
            placement = scan.identification.summary,
        )
        Spacer(Modifier.height(24.dp))
        LifeLensButton(
            text = if (saved) "Saved" else "Save to library",
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !saved,
        )
        Spacer(Modifier.height(14.dp))
        SourceFootnote(text = "Care tips are general guidance")
        Spacer(Modifier.height(14.dp))
    }
}

/** Half-depth plant module: next watering + placement/pet-safety care guidance. */
@Composable
private fun CareCard(watering: String?, petSafe: String?, placement: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LifeLensShapes.card)
            .background(Raised)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Care", style = com.lifelen.core.designsystem.theme.TitleStyle, color = TextPrimary)
        Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
        if (!watering.isNullOrBlank()) {
            CareLine(icon = LifeLensIcons.Droplet, label = "Watering", value = watering)
        }
        if (!petSafe.isNullOrBlank()) {
            CareLine(icon = LifeLensIcons.Pin, label = "Pet-safe", value = petSafe)
        }
        if (placement.isNotBlank()) {
            Text(placement, style = BodyStyle, color = TextSecondary)
        }
    }
}

@Composable
private fun CareLine(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, contentDescription = null, tint = CatPlant, modifier = Modifier.size(16.dp))
            Text(label, style = BodyStyle, color = TextSecondary)
        }
        Text(value, style = DataSm, color = TextPrimary)
    }
}

// ---------------------------------------------------------------------------
// Food modules (Design Spec §3.3 S06).
// ---------------------------------------------------------------------------

@Composable
internal fun FoodResultBody(
    scan: Scan,
    nutrition: NutritionInfo,
    portionFactor: Float,
    saved: Boolean,
    onSave: () -> Unit,
    onSetPortion: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val f = portionFactor
    val caloriesScaled = (nutrition.calories * f).roundToInt()
    val proteinScaled = nutrition.protein * f
    val carbsScaled = nutrition.carbs * f
    val fatScaled = nutrition.fat * f
    val fiberScaled = (nutrition.fiber * f).roundToInt()
    val sugarsScaled = (nutrition.sugars * f).roundToInt()
    val sodiumScaled = (nutrition.sodium * f).roundToInt()

    Column(modifier.fillMaxWidth()) {
        Spacer(Modifier.height(18.dp))
        PortionRow(
            factor = f,
            onMinus = { onSetPortion(f - 0.5f) },
            onPlus = { onSetPortion(f + 0.5f) },
        )

        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$caloriesScaled", style = DataXl, color = TextPrimary)
                Text(
                    " kcal",
                    style = LabelStyle,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            Text(
                text = "${caloriesScaled * 100 / 2000}% of a 2,000 kcal day",
                style = CaptionStyle,
                color = TextSecondary,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        Spacer(Modifier.height(16.dp))
        MacroBar(protein = proteinScaled, carbs = carbsScaled, fat = fatScaled)

        Spacer(Modifier.height(18.dp))
        NutritionRow(label = "Fiber", value = "${fiberScaled}g")
        NutritionRow(label = "Sugars", value = "${sugarsScaled}g")
        NutritionRow(label = "Sodium", value = "$sodiumScaled mg", trailingChevron = true)

        Spacer(Modifier.height(24.dp))
        var adjustOpen by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LifeLensButton(
                text = if (saved) "Saved" else "Save to library",
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = !saved,
            )
            LifeLensButton(
                text = "Adjust items",
                onClick = { adjustOpen = !adjustOpen },
                type = ButtonType.Secondary,
            )
        }
        if (adjustOpen && nutrition.ingredients.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            IngredientChecklist(nutrition.ingredients)
        }

        Spacer(Modifier.height(14.dp))
        SourceFootnote(text = "Estimates vary by recipe")
        Spacer(Modifier.height(14.dp))
    }
}

@Composable
internal fun PortionRow(
    factor: Float,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(LifeLensShapes.card)
            .background(Raised)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Portion", style = BodyStyle, color = TextPrimary)
        Stepper(
            valueText = "${factor.stepLabel()} · ~${(350 * factor).roundToInt()} g",
            onMinus = onMinus,
            onPlus = onPlus,
        )
    }
}

@Composable
internal fun MacroBar(
    protein: Double,
    carbs: Double,
    fat: Double,
    modifier: Modifier = Modifier,
) {
    val segments = listOf(
        Triple("Protein", protein, MacroProtein),
        Triple("Carbs", carbs, MacroCarbs),
        Triple("Fat", fat, MacroFat),
    )
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(LifeLensShapes.chip),
        ) {
            segments.forEach { (_, grams, color) ->
                if (grams > 0.0) {
                    Box(
                        Modifier
                            .weight(grams.toFloat())
                            .fillMaxHeight()
                            .background(color),
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            segments.forEach { (label, grams, color) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(color))
                    Text(label, style = CaptionStyle, color = TextSecondary)
                    Text("${grams.roundToInt()} g", style = DataSm, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
internal fun NutritionRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    trailingChevron: Boolean = false,
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = BodyStyle, color = TextSecondary)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(value, style = DataSm, color = TextPrimary)
                if (trailingChevron) {
                    Icon(
                        LifeLensIcons.ChevronRight,
                        contentDescription = null,
                        tint = TextFaint,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Hairline),
        )
    }
}

@Composable
private fun IngredientChecklist(ingredients: List<String>) {
    val checked = remember(ingredients) {
        mutableStateMapOf<String, Boolean>().apply { ingredients.forEach { put(it, true) } }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LifeLensShapes.card)
            .background(Raised)
            .padding(vertical = 4.dp),
    ) {
        ingredients.forEach { ingredient ->
            val isChecked = checked[ingredient] ?: true
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableEnabled(true) { checked[ingredient] = !isChecked }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(LifeLensShapes.tile)
                        .background(if (isChecked) AmberTint else Color.Transparent)
                        .border(1.dp, if (isChecked) Amber else SubtleBorder, LifeLensShapes.tile),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isChecked) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Amber,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
                Text(
                    text = ingredient,
                    style = BodyStyle,
                    color = if (isChecked) TextPrimary else TextFaint,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Processing skeleton (Design Spec §3.5 / S03).
// ---------------------------------------------------------------------------

@Composable
internal fun ResultSkeleton(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Skeleton(Modifier.size(46.dp), shape = LifeLensShapes.thumb)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Skeleton(Modifier.fillMaxWidth(0.72f).height(16.dp), shape = LifeLensShapes.chip)
                Skeleton(Modifier.fillMaxWidth(0.42f).height(12.dp), shape = LifeLensShapes.chip)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            repeat(4) {
                Skeleton(Modifier.weight(1f).height(56.dp))
            }
        }
        Skeleton(Modifier.fillMaxWidth().height(84.dp), shape = LifeLensShapes.card)
    }
}

// ---------------------------------------------------------------------------
// Helpers.
// ---------------------------------------------------------------------------

/** Money with grouping — no decimals when whole, otherwise two. e.g. 1299.0 -> "1,299", 12.5 -> "12.50". */
internal fun Double.formatMoney(): String {
    val whole = this % 1.0 == 0.0
    val nf = NumberFormat.getNumberInstance(Locale.US)
    nf.minimumFractionDigits = if (whole) 0 else 2
    nf.maximumFractionDigits = if (whole) 0 else 2
    return nf.format(this)
}

/** Portion label: "1 plate", "1.5 plates", "0.5 plates". */
internal fun Float.stepLabel(): String {
    val number = if (this % 1f == 0f) this.toInt().toString() else this.toString()
    val unit = if (this == 1f) "plate" else "plates"
    return "$number $unit"
}
