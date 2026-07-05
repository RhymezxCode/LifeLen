package com.lifelen.core.network.util

import android.util.Base64
import javax.inject.Inject

/** Turns raw JPEG bytes into an OpenAI-style base64 data URL for the vision request. */
class ImageEncoder @Inject constructor() {

    fun toDataUrl(jpegBytes: ByteArray, mimeType: String = "image/jpeg"): String {
        val base64 = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
        return "data:$mimeType;base64,$base64"
    }
}
