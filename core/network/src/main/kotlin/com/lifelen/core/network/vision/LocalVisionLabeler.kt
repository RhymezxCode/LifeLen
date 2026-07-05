package com.lifelen.core.network.vision

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** A single on-device label with its confidence in 0..1. */
data class LocalLabel(val text: String, val confidence: Float)

/**
 * On-device image labelling (ML Kit). Used as the keyless fallback for identification and to drive
 * the live viewfinder label — no network or API key required.
 */
interface LocalVisionLabeler {
    suspend fun label(jpeg: ByteArray): List<LocalLabel>
}

@Singleton
class MlKitVisionLabeler @Inject constructor() : LocalVisionLabeler {
    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder().setConfidenceThreshold(0.5f).build(),
    )

    override suspend fun label(jpeg: ByteArray): List<LocalLabel> {
        val bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size) ?: return emptyList()
        val image = InputImage.fromBitmap(bitmap, 0)
        return suspendCancellableCoroutine { cont ->
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    cont.resume(labels.map { LocalLabel(it.text, it.confidence) }.sortedByDescending { it.confidence })
                }
                .addOnFailureListener { cont.resume(emptyList()) }
        }
    }
}
