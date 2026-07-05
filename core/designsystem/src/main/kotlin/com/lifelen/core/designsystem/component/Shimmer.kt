package com.lifelen.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.composed
import com.lifelen.core.designsystem.theme.TextPrimary

/**
 * A diagonally-sweeping highlight over a faint base — the LifeLens loading shimmer (Design Spec §3.5).
 * Draws behind any content, so it can fill a [Skeleton] block or sit under an image while it loads.
 */
fun Modifier.shimmer(): Modifier = composed {
    val base = TextPrimary.copy(alpha = 0.07f)
    val highlight = TextPrimary.copy(alpha = 0.17f)
    val progress by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "sweep",
    )
    drawBehind {
        val sweep = size.width * 1.4f
        val startX = -sweep + (size.width + sweep) * progress
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(base, highlight, base),
                start = Offset(startX, 0f),
                end = Offset(startX + sweep, size.height),
            ),
        )
    }
}

/** A shimmering placeholder block for images/thumbnails that are still loading. */
@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(modifier.shimmer())
}
