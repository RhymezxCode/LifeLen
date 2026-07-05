package com.lifelen.core.common.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/** A minimal loading/success/error wrapper used across repositories and ViewModels. */
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Error(val throwable: Throwable) : DataResult<Nothing>
    data object Loading : DataResult<Nothing>
}

/** Wraps a cold [Flow] so downstream collectors see Loading → Success | Error. */
fun <T> Flow<T>.asResult(): Flow<DataResult<T>> =
    map<T, DataResult<T>> { DataResult.Success(it) }
        .onStart { emit(DataResult.Loading) }
        .catch { emit(DataResult.Error(it)) }
