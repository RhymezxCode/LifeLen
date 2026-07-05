package com.lifelen.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lifelen.core.designsystem.theme.Amber
import com.lifelen.core.designsystem.theme.BodyStyle
import com.lifelen.core.designsystem.theme.LifeLensShapes
import com.lifelen.core.designsystem.theme.MediaControlFill
import com.lifelen.core.designsystem.theme.Negative
import com.lifelen.core.designsystem.theme.OnAmber
import com.lifelen.core.designsystem.theme.Raised
import com.lifelen.core.designsystem.theme.SubtleBorder
import com.lifelen.core.designsystem.theme.TextPrimary
import com.lifelen.core.designsystem.theme.TextSecondary

/** Button type — Design Spec §3.1. Max one primary per screen. */
enum class ButtonType { Primary, Secondary, Ghost, Destructive }

@Composable
fun LifeLensButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    val container = when (type) {
        ButtonType.Primary -> Amber
        ButtonType.Secondary -> Raised
        ButtonType.Ghost -> Color.Transparent
        ButtonType.Destructive -> Raised
    }
    val content = when (type) {
        ButtonType.Primary -> OnAmber
        ButtonType.Destructive -> Negative
        ButtonType.Ghost -> TextSecondary
        ButtonType.Secondary -> TextPrimary
    }
    val borderMod = if (type == ButtonType.Secondary || type == ButtonType.Destructive) {
        Modifier.border(1.dp, SubtleBorder, LifeLensShapes.control)
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(LifeLensShapes.control)
            .background(if (enabled) container else container.copy(alpha = 0.4f))
            .then(borderMod)
            .semantics { contentDescription = text }
            .clickableEnabled(enabled && !loading, onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            LoadingDots(color = content)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (leadingIcon != null) {
                    Icon(leadingIcon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
                }
                Text(text, style = BodyStyle, color = content)
            }
        }
    }
}

/** Circular control that sits over the live viewfinder — Design Spec §3.1 IconButton `circle-on-media`. */
@Composable
fun MediaIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 38,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MediaControlFill)
            .clickableEnabled(true, onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
    }
}

/** Square raised icon button for surfaces — Design Spec §3.1 IconButton `square-raised`. */
@Composable
fun RaisedIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(LifeLensShapes.control)
            .background(Raised)
            .border(1.dp, SubtleBorder, LifeLensShapes.control)
            .clickableEnabled(true, onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(19.dp))
    }
}
