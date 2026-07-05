package com.lifelen.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lifelen.core.designsystem.theme.Amber
import com.lifelen.core.designsystem.theme.AmberTint
import com.lifelen.core.designsystem.theme.DataSm
import com.lifelen.core.designsystem.theme.LabelStyle
import com.lifelen.core.designsystem.theme.LifeLensShapes
import com.lifelen.core.designsystem.theme.Raised
import com.lifelen.core.designsystem.theme.TextPrimary
import com.lifelen.core.designsystem.theme.TextSecondary

/** Mode / filter chip — selected = amber-tint fill + amber border + amber text (§3.1). */
@Composable
fun ModeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fill = if (selected) AmberTint else Color.White.copy(alpha = 0.07f)
    val border = if (selected) Amber else Color.Transparent
    // Unselected label ≈ HTML #C6CCD6 in dark (a lightly-dimmed primary), theme-aware in light.
    val textColor = if (selected) Amber else TextPrimary.copy(alpha = 0.82f)
    Text(
        text = text,
        style = LabelStyle,
        color = textColor,
        modifier = modifier
            .clip(LifeLensShapes.chip)
            .background(fill)
            .border(1.dp, border, LifeLensShapes.chip)
            .clickableEnabled(true, onClick)
            .padding(horizontal = 15.dp, vertical = 7.dp),
    )
}

/** Category chip — category tint fill + category-colored text; no selected state (§3.1). */
@Composable
fun CategoryChip(
    label: String,
    color: Color,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        style = LabelStyle,
        color = color,
        modifier = modifier
            .clip(LifeLensShapes.chip)
            .background(tint)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}

/**
 * Confidence badge — `data-sm` in an amber-tint pill; below 70% it goes neutral (§3.3 IdentityHeader).
 * Renders e.g. "94% match" or "~52%".
 */
@Composable
fun ConfidenceBadge(
    confidence: Float,
    modifier: Modifier = Modifier,
) {
    val percent = (confidence * 100).toInt()
    val low = confidence < 0.70f
    val fill = if (low) Raised else AmberTint
    val textColor = if (low) TextSecondary else Amber
    val label = if (low) "~$percent%" else "$percent% match"
    Text(
        text = label,
        style = DataSm,
        color = textColor,
        modifier = modifier
            .clip(LifeLensShapes.chip)
            .background(fill)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}
