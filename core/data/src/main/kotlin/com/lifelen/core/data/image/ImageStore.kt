package com.lifelen.core.data.image

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Persists captured JPEGs to app-private storage and returns their file paths. */
@Singleton
class ImageStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dir: File
        get() = File(context.filesDir, "scans").apply { if (!exists()) mkdirs() }

    fun save(id: String, bytes: ByteArray): String {
        val file = File(dir, "$id.jpg")
        file.writeBytes(bytes)
        return file.absolutePath
    }

    fun delete(path: String) {
        runCatching { File(path).delete() }
    }
}
