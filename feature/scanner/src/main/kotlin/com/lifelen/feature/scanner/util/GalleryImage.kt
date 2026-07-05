package com.lifelen.feature.scanner.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * Loads a picked gallery image and re-encodes it as a downscaled JPEG, matching the camera capture
 * path so the same identify pipeline handles both sources. Returns null if the image can't be read.
 */
fun uriToDownscaledJpeg(
    context: Context,
    uri: Uri,
    maxDimension: Int = 1024,
    quality: Int = 85,
): ByteArray? = try {
    val raw = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
    if (raw == null) {
        null
    } else {
        var bitmap = BitmapFactory.decodeByteArray(raw, 0, raw.size)
        if (bitmap == null) {
            null
        } else {
            val longestSide = max(bitmap.width, bitmap.height)
            if (longestSide > maxDimension) {
                val scale = maxDimension.toFloat() / longestSide
                bitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true,
                )
            }
            ByteArrayOutputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                out.toByteArray()
            }
        }
    }
} catch (e: Exception) {
    null
}
