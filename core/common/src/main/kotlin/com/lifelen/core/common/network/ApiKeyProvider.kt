package com.lifelen.core.common.network

/**
 * Supplies API keys to the network/search layers without those layers depending on
 * storage. The real implementation (backed by DataStore/BuildConfig) is bound in `:core:data`.
 */
interface ApiKeyProvider {
    suspend fun dashScopeApiKey(): String
    suspend fun searchApiKey(): String
}
