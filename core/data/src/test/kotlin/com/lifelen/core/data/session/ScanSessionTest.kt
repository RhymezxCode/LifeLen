package com.lifelen.core.data.session

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.lifelen.core.data.image.ImageStore
import com.lifelen.core.model.Identification
import com.lifelen.core.model.Scan
import com.lifelen.core.model.ScanCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class ScanSessionTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val session = ScanSession(ImageStore(context))

    private val scan = Scan(
        id = "s1",
        imagePath = "/tmp/s1.jpg",
        identification = Identification("Apple", ScanCategory.FOOD, "A fruit.", 0.9f),
        createdAt = 1L,
    )

    @Test
    fun `beginCapture persists the frame and exposes the draft`() {
        val draft = session.beginCapture(byteArrayOf(1, 2, 3))

        assertTrue(draft.imagePath.isNotBlank())
        assertTrue(File(draft.imagePath).exists())
        assertEquals(draft, session.currentDraft())
        assertNull(session.result.value)
        assertNull(session.currentResult())
    }

    @Test
    fun `setResult publishes the scan to both accessors`() {
        session.beginCapture(byteArrayOf(9))
        session.setResult(scan)

        assertEquals(scan, session.currentResult())
        assertEquals(scan, session.result.value)
    }

    @Test
    fun `clear resets the draft and result`() {
        session.beginCapture(byteArrayOf(4, 5))
        session.setResult(scan)

        session.clear()

        assertNull(session.currentDraft())
        assertNull(session.currentResult())
        assertNull(session.result.value)
    }
}
