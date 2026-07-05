package com.lifelen.core.data.image

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/** Robolectric-backed: exercises real file IO against a Robolectric-provided app filesDir. */
@RunWith(RobolectricTestRunner::class)
class ImageStoreTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val store = ImageStore(context)

    @Test
    fun `save writes the bytes and returns a readable path`() {
        val bytes = byteArrayOf(1, 2, 3, 4)
        val path = store.save("abc", bytes)
        val file = File(path)
        assertTrue(file.exists())
        assertArrayEquals(bytes, file.readBytes())
    }

    @Test
    fun `delete removes the file`() {
        val path = store.save("del", byteArrayOf(9))
        assertTrue(File(path).exists())
        store.delete(path)
        assertFalse(File(path).exists())
    }

    @Test
    fun `delete of a missing path is a no-op`() {
        store.delete("/does/not/exist.jpg") // should not throw
    }
}
