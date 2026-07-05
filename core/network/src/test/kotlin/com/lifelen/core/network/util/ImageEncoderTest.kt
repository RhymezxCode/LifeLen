package com.lifelen.core.network.util

import android.util.Base64
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Robolectric-backed: exercises the real android.util.Base64 encoder. */
@RunWith(RobolectricTestRunner::class)
class ImageEncoderTest {

    private val encoder = ImageEncoder()

    @Test
    fun `toDataUrl emits a jpeg data url whose payload round-trips`() {
        val bytes = byteArrayOf(1, 2, 3)

        val url = encoder.toDataUrl(bytes)

        val prefix = "data:image/jpeg;base64,"
        assertTrue(url, url.startsWith(prefix))

        val payload = url.removePrefix(prefix)
        assertArrayEquals(bytes, Base64.decode(payload, Base64.NO_WRAP))
    }
}
