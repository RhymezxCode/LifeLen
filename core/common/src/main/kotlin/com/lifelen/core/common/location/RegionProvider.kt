package com.lifelen.core.common.location

/**
 * The user's shopping region, resolved from device location when permitted. Used to localize price
 * search and currency.
 */
data class Region(
    /** ISO 3166-1 alpha-2 country code, e.g. "NG". */
    val countryCode: String,
    /** ISO 4217 currency code, e.g. "NGN". */
    val currencyCode: String,
    /** Human-readable country name, e.g. "Nigeria". */
    val countryName: String,
)

/**
 * Resolves the user's [Region] for localized pricing. Returns `null` when location permission is
 * not granted (or no fix is available) — callers then fall back to a generic, non-localized price.
 */
interface RegionProvider {
    suspend fun currentRegion(): Region?
}
