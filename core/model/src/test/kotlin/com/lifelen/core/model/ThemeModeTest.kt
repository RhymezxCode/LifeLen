package com.lifelen.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `lowercase dark maps to DARK`() {
        assertEquals(ThemeMode.DARK, ThemeMode.fromName("dark"))
    }

    @Test
    fun `uppercase LIGHT maps to LIGHT`() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromName("LIGHT"))
    }

    @Test
    fun `system maps to SYSTEM`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName("system"))
    }

    @Test
    fun `null maps to SYSTEM`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName(null))
    }

    @Test
    fun `unrecognised value falls back to SYSTEM`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName("garbage"))
    }
}
