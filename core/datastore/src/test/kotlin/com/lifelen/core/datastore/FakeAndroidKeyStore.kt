package com.lifelen.core.datastore

import java.io.InputStream
import java.io.OutputStream
import java.security.Key
import java.security.KeyStore
import java.security.KeyStoreSpi
import java.security.Provider
import java.security.Security
import java.security.cert.Certificate
import java.util.Collections
import java.util.Date
import java.util.Enumeration

/**
 * Test-only "AndroidKeyStore" security provider.
 *
 * SimpleStore's `DatastorePreference.kt` file facade eagerly constructs a `CryptoManager` in its
 * static initializer — even when `encryption(false)` is used — and that constructor calls
 * `KeyStore.getInstance("AndroidKeyStore").load(null)`. The AndroidKeyStore provider does not exist
 * on the plain JVM Robolectric runs on, so the class init fails with `NoSuchAlgorithmException`.
 *
 * Registering this empty in-memory KeyStore under the "AndroidKeyStore" name lets that constructor
 * complete. The [CryptoManager] instance is never actually used (encryption is off), so a no-op
 * store is sufficient. This touches nothing in production code.
 */
internal object FakeAndroidKeyStore {
    fun installIfMissing() {
        if (Security.getProvider(PROVIDER_NAME) == null) {
            Security.addProvider(FakeProvider())
        }
    }

    private const val PROVIDER_NAME = "AndroidKeyStore"

    @Suppress("DEPRECATION")
    private class FakeProvider : Provider(PROVIDER_NAME, 1.0, "Test-only AndroidKeyStore") {
        init {
            put("KeyStore.$PROVIDER_NAME", FakeKeyStoreSpi::class.java.name)
        }
    }

    /** Minimal in-memory [KeyStoreSpi]; only load() is exercised by SimpleStore's CryptoManager. */
    class FakeKeyStoreSpi : KeyStoreSpi() {
        override fun engineGetKey(alias: String?, password: CharArray?): Key? = null
        override fun engineGetCertificateChain(alias: String?): Array<Certificate>? = null
        override fun engineGetCertificate(alias: String?): Certificate? = null
        override fun engineGetCreationDate(alias: String?): Date? = null
        override fun engineSetKeyEntry(alias: String?, key: Key?, password: CharArray?, chain: Array<out Certificate>?) = Unit
        override fun engineSetKeyEntry(alias: String?, key: ByteArray?, chain: Array<out Certificate>?) = Unit
        override fun engineSetCertificateEntry(alias: String?, cert: Certificate?) = Unit
        override fun engineDeleteEntry(alias: String?) = Unit
        override fun engineAliases(): Enumeration<String> = Collections.emptyEnumeration()
        override fun engineContainsAlias(alias: String?): Boolean = false
        override fun engineSize(): Int = 0
        override fun engineIsKeyEntry(alias: String?): Boolean = false
        override fun engineIsCertificateEntry(alias: String?): Boolean = false
        override fun engineGetCertificateAlias(cert: Certificate?): String? = null
        override fun engineStore(stream: OutputStream?, password: CharArray?) = Unit
        override fun engineLoad(stream: InputStream?, password: CharArray?) = Unit
        override fun engineLoad(param: KeyStore.LoadStoreParameter?) = Unit
    }
}
