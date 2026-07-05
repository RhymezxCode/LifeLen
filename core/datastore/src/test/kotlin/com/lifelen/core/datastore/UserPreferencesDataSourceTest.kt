package com.lifelen.core.datastore

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.github.rhymezxcode.simplestore.DatastorePreference
import io.github.rhymezxcode.simplestore.SimpleStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserPreferencesDataSourceTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    // Registered before the first SimpleStore call so its CryptoManager static init can complete.
    @Before
    fun installKeyStoreProvider() = FakeAndroidKeyStore.installIfMissing()

    // SimpleStore's DatastorePreference is backed by a single top-level `preferencesDataStore`
    // delegate whose file name is fixed (the builder's storeName is ignored for the datastore file),
    // so every instance shares one on-disk file. We wipe it up front so each test starts pristine.
    private suspend fun newDataSource(): UserPreferencesDataSource {
        val store = SimpleStore.Builder()
            .context(context)
            .storeName("test_prefs")
            .encryption(false)
            .build()
            .getType<DatastorePreference>()
        store.clearAllTheStore()
        return UserPreferencesDataSource(store)
    }

    @org.junit.Test
    fun `defaults are on-by-default with system theme`() = runTest {
        val prefs = newDataSource().preferences.first()
        assertTrue(prefs.pricingEnabled)
        assertTrue(prefs.hapticsEnabled)
        assertTrue(prefs.rememberKeys)
        assertFalse(prefs.autoSaveScans)
        assertEquals("system", prefs.themeMode)
        assertEquals("", prefs.dashScopeApiKey)
    }

    @org.junit.Test
    fun `setPricingEnabled false is reflected`() = runTest {
        val ds = newDataSource()
        ds.setPricingEnabled(false)
        assertFalse(ds.preferences.first().pricingEnabled)
    }

    @org.junit.Test
    fun `setHapticsEnabled false is reflected`() = runTest {
        val ds = newDataSource()
        ds.setHapticsEnabled(false)
        assertFalse(ds.preferences.first().hapticsEnabled)
    }

    @org.junit.Test
    fun `setAutoSaveScans true is reflected`() = runTest {
        val ds = newDataSource()
        ds.setAutoSaveScans(true)
        assertTrue(ds.preferences.first().autoSaveScans)
    }

    @org.junit.Test
    fun `dashScope key round-trips`() = runTest {
        val ds = newDataSource()
        ds.setDashScopeApiKey("K")
        assertEquals("K", ds.preferences.first().dashScopeApiKey)
    }

    @org.junit.Test
    fun `disabling rememberKeys wipes the stored keys`() = runTest {
        val ds = newDataSource()
        ds.setDashScopeApiKey("K")
        ds.setSearchApiKey("S")
        ds.setRememberKeys(false)
        val prefs = ds.preferences.first()
        assertFalse(prefs.rememberKeys)
        assertTrue(prefs.dashScopeApiKey.isBlank())
        assertTrue(prefs.searchApiKey.isBlank())
    }

    @org.junit.Test
    fun `setThemeMode dark is reflected`() = runTest {
        val ds = newDataSource()
        ds.setThemeMode("dark")
        assertEquals("dark", ds.preferences.first().themeMode)
    }
}
