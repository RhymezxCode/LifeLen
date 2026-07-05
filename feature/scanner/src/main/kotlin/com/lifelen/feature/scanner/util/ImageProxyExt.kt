package com.lifelen.feature.scanner.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * Converts a captured [ImageProxy] (JPEG) into rotation-corrected, downscaled JPEG bytes.
 * Downscaling keeps the base64 payload — and therefore Qwen latency/cost — reasonable.
 */
fun ImageProxy.toDownscaledJpeg(maxDimension: Int = 1024, quality: Int = 85): ByteArray {
    val buffer = planes[0].buffer
    val raw = ByteArray(buffer.remaining()).also { buffer.get(it) }

    var bitmap = BitmapFactory.decodeByteArray(raw, 0, raw.size)
        ?: return raw // fall back to the original bytes if decoding fails

    val rotation = imageInfo.rotationDegrees
    if (rotation != 0) {
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

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

    return ByteArrayOutputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        out.toByteArray()
    }
}
