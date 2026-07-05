package com.lifelen.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifelen.core.data.repository.HistoryRepository
import com.lifelen.core.model.Scan
import com.lifelen.core.model.ScanCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/** A day's worth of scans under a human header ("Today", "Yesterday", "Mar 4"). */
data class LibraryGroup(
    val header: String,
    val scans: List<Scan>,
)

/** Immutable state for the Library screen (S08 / empty S10). */
data class LibraryUiState(
    val query: String = "",
    val filter: ScanCategory? = null,
    val groups: List<LibraryGroup> = emptyList(),
    val totalCount: Int = 0,
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow<ScanCategory?>(null)

    /** Query drives the source: full history when blank, search results otherwise. */
    private val displayed: Flow<List<Scan>> = query.flatMapLatest { q ->
        if (q.isBlank()) historyRepository.observeHistory() else historyRepository.search(q)
    }

    val uiState: StateFlow<LibraryUiState> = combine(
        query,
        filter,
        displayed,
        historyRepository.observeHistory(), // unfiltered — powers the nav-bar total count
    ) { q, category, scans, all ->
        val filtered = category?.let { c -> scans.filter { it.category == c } } ?: scans
        LibraryUiState(
            query = q,
            filter = category,
            groups = groupByDay(filtered),
            totalCount = all.size,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(),
    )

    fun onQueryChange(q: String) {
        query.value = q
    }

    fun onFilter(category: ScanCategory?) {
        filter.value = category
    }

    /** Buckets scans by local calendar day, newest first, and labels each bucket. */
    private fun groupByDay(scans: List<Scan>): List<LibraryGroup> {
        if (scans.isEmpty()) return emptyList()

        val todayBucket = dayBucket(System.currentTimeMillis())
        val yesterdayBucket = Calendar.getInstance().apply {
            timeInMillis = todayBucket
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis

        // groupBy preserves first-seen order, so sorting desc keeps Today -> Yesterday -> older.
        return scans
            .sortedByDescending { it.createdAt }
            .groupBy { dayBucket(it.createdAt) }
            .map { (bucket, dayScans) ->
                LibraryGroup(
                    header = headerFor(bucket, todayBucket, yesterdayBucket),
                    scans = dayScans,
                )
            }
    }

    private fun headerFor(bucket: Long, today: Long, yesterday: Long): String = when (bucket) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> monthDayFormat.format(Date(bucket))
    }

    /** Truncates an epoch millis to the start of its local calendar day. */
    private fun dayBucket(millis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private companion object {
        val monthDayFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    }
}
