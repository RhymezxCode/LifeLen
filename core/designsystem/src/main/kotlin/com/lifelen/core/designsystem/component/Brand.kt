package com.lifelen.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lifelen.core.designsystem.theme.Amber
import com.lifelen.core.designsystem.theme.Raised
import com.lifelen.core.designsystem.theme.TextPrimary

/** Detection state for the viewfinder brackets — Design Spec §3.2. */
enum class DetectionState { Searching, Locked }

/**
 * Four amber corner L-strokes framing a detection. `Searching` = white @ 40% with a slow pulse,
 * `Locked` = static amber. The signature LifeLens motif.
 */
@Composable
fun DetectionBrackets(
    state: DetectionState,
    modifier: Modifier = Modifier,
    cornerLength: Dp = 26.dp,
    strokeWidth: Dp = 3.dp,
) {
    val pulse by rememberInfiniteTransition(label = "brackets").animateFloat(
        initialValue = 0.35f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "pulse",
    )
    val color = when (state) {
        DetectionState.Locked -> Amber
        DetectionState.Searching -> TextPrimary.copy(alpha = pulse)
    }
    Canvas(modifier) {
        val len = cornerLength.toPx()
        val sw = strokeWidth.toPx()
        val i = sw / 2f
        val w = size.width
        val h = size.height
        fun l(a: Offset, b: Offset) = drawLine(color, a, b, sw, StrokeCap.Round)
        // top-left
        l(Offset(i, len), Offset(i, i)); l(Offset(i, i), Offset(len, i))
        // top-right
        l(Offset(w - i, len), Offset(w - i, i)); l(Offset(w - i, i), Offset(w - len, i))
        // bottom-left
        l(Offset(i, h - len), Offset(i, h - i)); l(Offset(i, h - i), Offset(len, h - i))
        // bottom-right
        l(Offset(w - i, h - len), Offset(w - i, h - i)); l(Offset(w - i, h - i), Offset(w - len, h - i))
    }
}

/** Shutter states — Design Spec §3.2. */
enum class ShutterState { Idle, Processing }

/** 68px ring + 52px inner fill; pressed scales inner to 0.9; processing swaps to an amber spinner. */
@Composable
fun ShutterButton(
    state: ShutterState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier = modifier
            .size(68.dp)
            .clip(CircleShape)
            .border(3.dp, TextPrimary, CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = state == ShutterState.Idle,
                onClick = onClick,
            )
            .semantics { contentDescription = "Identify"; role = Role.Button },
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            ShutterState.Processing -> CircularProgressIndicator(
                color = Amber,
                strokeWidth = 3.dp,
                modifier = Modifier.size(46.dp),
            )
            ShutterState.Idle -> Box(
                Modifier
                    .size(52.dp)
                    .scale(if (pressed) 0.9f else 1f)
                    .clip(CircleShape)
                    .background(TextPrimary),
            )
        }
    }
}

/**
 * Thumbnail with the corner-bracket motif (top-left + bottom-right, 2px amber) — used on identity
 * headers, library rows and the camera library button. [content] draws the image/placeholder.
 */
@Composable
fun BracketThumb(
    size: Dp,
    modifier: Modifier = Modifier,
    bracketColor: Color = Amber,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(com.lifelen.core.designsystem.theme.LifeLensShapes.thumb)
            .background(Raised),
        contentAlignment = Alignment.Center,
    ) {
        content()
        Canvas(Modifier.fillMaxSize()) {
            val len = size.toPx() * 0.22f
            val sw = 2.dp.toPx()
            val i = sw / 2f
            val w = this.size.width
            val h = this.size.height
            fun l(a: Offset, b: Offset) = drawLine(bracketColor, a, b, sw, StrokeCap.Square)
            // top-left corner
            l(Offset(i, len), Offset(i, i)); l(Offset(i, i), Offset(len, i))
            // bottom-right corner
            l(Offset(w - i, h - len), Offset(w - i, h - i)); l(Offset(w - i, h - i), Offset(w - len, h - i))
        }
    }
}
