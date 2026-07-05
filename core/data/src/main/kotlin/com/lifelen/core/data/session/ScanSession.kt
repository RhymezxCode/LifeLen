package com.lifelen.core.data.session

import com.lifelen.core.data.image.ImageStore
import com.lifelen.core.model.Scan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** The captured frame awaiting/holding identification, shared across camera → result → prices. */
data class CaptureDraft(
    val id: String,
    val imagePath: String,
    val bytes: ByteArray,
) {
    // Identity is the id; bytes excluded from equals/hashCode to avoid array-content surprises.
    override fun equals(other: Any?) = other is CaptureDraft && other.id == id
    override fun hashCode() = id.hashCode()
}

/**
 * In-memory holder for the current scan flow. The result sheet renders from here (so it can show a
 * skeleton the instant the shutter fires), and the Prices screen reads the freshly-identified scan
 * before it is ever saved to the library.
 */
@Singleton
class ScanSession @Inject constructor(
    private val imageStore: ImageStore,
) {
    private var draft: CaptureDraft? = null

    private val _result = MutableStateFlow<Scan?>(null)
    val result: StateFlow<Scan?> = _result.asStateFlow()

    /** Persists the captured JPEG to disk and starts a fresh flow. Returns the draft to identify. */
    fun beginCapture(bytes: ByteArray): CaptureDraft {
        val id = UUID.randomUUID().toString()
        val path = imageStore.save(id, bytes)
        val newDraft = CaptureDraft(id, path, bytes)
        draft = newDraft
        _result.value = null
        return newDraft
    }

    fun currentDraft(): CaptureDraft? = draft

    fun setResult(scan: Scan) {
        _result.value = scan
    }

    fun currentResult(): Scan? = _result.value

    fun clear() {
        draft = null
        _result.value = null
    }
}
