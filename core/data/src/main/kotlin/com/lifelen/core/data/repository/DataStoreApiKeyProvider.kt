package com.lifelen.core.data.repository

import com.lifelen.core.common.network.ApiKeyProvider
import com.lifelen.core.common.network.DefaultApiKeys
import com.lifelen.core.datastore.UserPreferencesDataSource
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Supplies the network/search layers with keys. Uses the key the user stored via Settings, falling
 * back to the build-time [DefaultApiKeys] when none is set — so the app works out of the box while
 * the default key stays out of storage and out of the Settings UI.
 */
class DataStoreApiKeyProvider @Inject constructor(
    private val dataSource: UserPreferencesDataSource,
    private val defaults: DefaultApiKeys,
) : ApiKeyProvider {
    override suspend fun dashScopeApiKey(): String =
        dataSource.preferences.first().dashScopeApiKey.ifBlank { defaults.dashScopeKey }

    override suspend fun searchApiKey(): String =
        dataSource.preferences.first().searchApiKey.ifBlank { defaults.searchKey }
}
