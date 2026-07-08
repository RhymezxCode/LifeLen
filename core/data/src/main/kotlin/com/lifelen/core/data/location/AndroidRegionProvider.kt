package com.lifelen.core.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import com.lifelen.core.common.di.Dispatcher
import com.lifelen.core.common.di.LifelenDispatcher
import com.lifelen.core.common.location.Region
import com.lifelen.core.common.location.RegionProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Currency
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [RegionProvider] backed by the platform [LocationManager] + [Geocoder]. It reads the last known
 * coarse location and reverse-geocodes it to a country → currency.
 *
 * Returns `null` — meaning "price generically, not in a country currency" — whenever:
 *  - coarse-location permission has not been granted,
 *  - no last-known fix is available, or
 *  - geocoding / currency lookup fails.
 */
@Singleton
class AndroidRegionProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(LifelenDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) : RegionProvider {

    override suspend fun currentRegion(): Region? = withContext(ioDispatcher) {
        if (!hasLocationPermission()) return@withContext null
        val location = lastKnownLocation() ?: return@withContext null
        val countryCode = reverseGeocodeCountry(location) ?: return@withContext null
        runCatching {
            val locale = Locale("", countryCode)
            val currency = Currency.getInstance(locale).currencyCode
            Region(
                countryCode = countryCode.uppercase(),
                currencyCode = currency,
                countryName = locale.displayCountry.ifBlank { countryCode },
            )
        }.getOrNull()
    }

    private fun hasLocationPermission(): Boolean {
        val coarse = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        val fine = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        return coarse == PackageManager.PERMISSION_GRANTED || fine == PackageManager.PERMISSION_GRANTED
    }

    private fun lastKnownLocation(): Location? {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        return try {
            manager.getProviders(true)
                .mapNotNull { provider -> manager.getLastKnownLocation(provider) }
                .maxByOrNull { it.time }
        } catch (_: SecurityException) {
            null
        }
    }

    @Suppress("DEPRECATION") // Blocking getFromLocation is fine on the IO dispatcher and supports minSdk 24.
    private fun reverseGeocodeCountry(location: Location): String? {
        if (!Geocoder.isPresent()) return null
        return runCatching {
            Geocoder(context, Locale.getDefault())
                .getFromLocation(location.latitude, location.longitude, 1)
                ?.firstOrNull()
                ?.countryCode
                ?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
