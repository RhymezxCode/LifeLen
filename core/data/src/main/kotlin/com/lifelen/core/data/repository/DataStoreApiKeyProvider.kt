package com.lifelen.core.data.repository

import com.lifelen.core.common.network.ApiKeyProvider
import com.lifelen.core.datastore.UserPreferencesDataSource
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Supplies the network/search layers with the keys the user stored via Settings. */
class DataStoreApiKeyProvider @Inject constructor(
    private val dataSource: UserPreferencesDataSource,
) : ApiKeyProvider {
    override suspend fun dashScopeApiKey(): String = dataSource.preferences.first().dashScopeApiKey
    override suspend fun searchApiKey(): String = dataSource.preferences.first().searchApiKey
}
