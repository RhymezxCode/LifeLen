package com.lifelen.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** LifeLens radii — Design Spec §2.1 `radius` tokens. */
object LifeLensShapes {
    val chip = RoundedCornerShape(999.dp) // full-round pills
    val control = RoundedCornerShape(12.dp) // buttons, inputs
    val card = RoundedCornerShape(12.dp)
    val tile = RoundedCornerShape(10.dp) // stat tiles, thumbnails
    val thumb = RoundedCornerShape(10.dp)
    val sheet = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp) // bottom sheet top corners
}

/** 4pt spacing scale — Design Spec §2.1 `space`. Screen gutter is [gutter] (16). */
object Space {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val gutter = 16.dp
}
