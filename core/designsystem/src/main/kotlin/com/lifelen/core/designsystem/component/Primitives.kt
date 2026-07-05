package com.lifelen.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lifelen.core.designsystem.LifeLensIcons
import com.lifelen.core.designsystem.theme.Amber
import com.lifelen.core.designsystem.theme.BodyStyle
import com.lifelen.core.designsystem.theme.CaptionStyle
import com.lifelen.core.designsystem.theme.DataMd
import com.lifelen.core.designsystem.theme.DataSm
import com.lifelen.core.designsystem.theme.Display
import com.lifelen.core.designsystem.theme.LifeLensShapes
import com.lifelen.core.designsystem.theme.Negative
import com.lifelen.core.designsystem.theme.NegativeTint
import com.lifelen.core.designsystem.theme.Positive
import com.lifelen.core.designsystem.theme.PositiveTint
import com.lifelen.core.designsystem.theme.Raised
import com.lifelen.core.designsystem.theme.Raised2
import com.lifelen.core.designsystem.theme.TextFaint
import com.lifelen.core.designsystem.theme.TextPrimary
import com.lifelen.core.designsystem.theme.TextSecondary

/** A single stat tile — label (caption/faint) over value (data-md). Design Spec §3.3 StatTile. */
@Composable
fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(LifeLensShapes.tile)
            .background(Raised)
            .padding(vertical = 11.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = CaptionStyle, color = TextFaint, maxLines = 1)
        Text(value, style = DataMd, color = TextPrimary, maxLines = 1, modifier = Modifier.padding(top = 3.dp))
    }
}

/** 4-up stat grid (§3.3 StatRow). Pass up to 4 label→value pairs. */
@Composable
fun StatRow(stats: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        stats.take(4).forEach { (label, value) ->
            StatTile(label, value, Modifier.weight(1f))
        }
    }
}

/** Minus / value / plus stepper — Design Spec §3.1. */
@Composable
fun Stepper(
    valueText: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StepButton(icon = LifeLensIcons.Minus, description = "Decrease", onClick = onMinus)
        Text(valueText, style = DataSm, color = TextPrimary)
        StepButton(icon = LifeLensIcons.Plus, description = "Increase", onClick = onPlus)
    }
}

@Composable
private fun StepButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(Raised2)
            .clickableEnabled(true, onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = TextPrimary, modifier = Modifier.size(16.dp))
    }
}

/** Raised search field, 38px, radius 11, leading icon, faint placeholder (§3.1 SearchBar). */
@Composable
fun LifeLensSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Raised)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(LifeLensIcons.Search, contentDescription = null, tint = TextFaint, modifier = Modifier.size(18.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(placeholder, style = BodyStyle, color = TextFaint)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = BodyStyle.copy(color = TextPrimary),
                cursorBrush = SolidColor(Amber),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Δ trend pill — icon + value; down=positive, up=negative. Never color-only (§7). */
@Composable
fun TrendPill(deltaLabel: String, isDown: Boolean, modifier: Modifier = Modifier) {
    val color = if (isDown) Positive else Negative
    val tint = if (isDown) PositiveTint else NegativeTint
    val icon = if (isDown) LifeLensIcons.TrendingDown else LifeLensIcons.TrendingUp
    Row(
        modifier = modifier
            .clip(LifeLensShapes.chip)
            .background(tint)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = if (isDown) "down" else "up", tint = color, modifier = Modifier.size(13.dp))
        Text(deltaLabel, style = DataSm, color = color)
    }
}

/** Mandatory source/freshness line on any fetched-data surface (§3.3 SourceFootnote). */
@Composable
fun SourceFootnote(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = CaptionStyle,
        color = TextFaint,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

/** Sheet grabber pill — 36×4 (§3.1). */
@Composable
fun SheetGrabber(modifier: Modifier = Modifier) {
    Box(
        modifier
            .width(36.dp)
            .height(4.dp)
            .clip(CircleShape)
            .background(com.lifelen.core.designsystem.theme.SheetGrabber),
    )
}

/** Skeleton block with a sweeping shimmer highlight (§3.5 Skeleton). */
@Composable
fun Skeleton(modifier: Modifier = Modifier, shape: androidx.compose.ui.graphics.Shape = LifeLensShapes.tile) {
    Box(modifier.clip(shape).shimmer())
}

/** Full-screen empty/error state — bracket illustration + headline + body + primary CTA (§3.5, §6). */
@Composable
fun EmptyState(
    headline: String,
    body: String,
    modifier: Modifier = Modifier,
    ctaText: String? = null,
    onCta: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DetectionBrackets(
            state = DetectionState.Locked,
            modifier = Modifier.size(96.dp),
        )
        Text(headline, style = Display, color = TextPrimary, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
        Text(body, style = BodyStyle, color = TextSecondary, textAlign = TextAlign.Center)
        if (ctaText != null && onCta != null) {
            LifeLensButton(ctaText, onCta, Modifier.padding(top = 8.dp).fillMaxWidth(0.7f))
        }
    }
}
