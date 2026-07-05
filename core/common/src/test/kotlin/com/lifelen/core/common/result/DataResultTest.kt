package com.lifelen.core.common.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DataResultTest {

    @Test
    fun `asResult prefixes Loading and wraps each value in Success`() = runTest {
        val results = flowOf(1, 2).asResult().toList()

        assertEquals(
            listOf(
                DataResult.Loading,
                DataResult.Success(1),
                DataResult.Success(2),
            ),
            results,
        )
    }

    @Test
    fun `asResult emits Loading first then Error when the flow throws`() = runTest {
        val boom = IllegalStateException("boom")
        val source: Flow<Int> = flow {
            emit(1)
            throw boom
        }

        val results = source.asResult().toList()

        // Loading, Success(1), then Error as the terminal item.
        assertEquals(DataResult.Loading, results.first())
        assertEquals(DataResult.Success(1), results[1])
        val last = results.last()
        assertTrue("expected terminal Error but was $last", last is DataResult.Error)
        assertEquals(boom, (last as DataResult.Error).throwable)
        assertEquals(3, results.size)
    }
}
