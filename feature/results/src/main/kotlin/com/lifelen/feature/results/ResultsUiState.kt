package com.lifelen.feature.results

import com.lifelen.core.model.Scan

/**
 * Result-sheet state — Design Spec S03/S04/S06/S07/S09.
 *
 * The sheet renders a skeleton the instant the shutter fires ([Processing]), then either the
 * identified [Ready] scan or a recoverable [Failed]/[NotFound] message.
 */
sealed interface ResultsUiState {
    /** S03 — vision + enrichment in flight for a freshly captured frame. */
    data object Processing : ResultsUiState

    /**
     * S04/S06/S07/S09 — an identified (or saved) scan is ready.
     *
     * @param saved whether this scan is already in the library (drives "Saved" vs "Save to library").
     * @param portionFactor food portion multiplier (0.5f..4f in 0.5 steps); nutrition scales by this.
     */
    data class Ready(
        val scan: Scan,
        val saved: Boolean,
        val portionFactor: Float = 1f,
    ) : ResultsUiState

    /** Identification failed but the user can retake. */
    data class Failed(val message: String) : ResultsUiState

    /** A saved-detail scanId that no longer exists in the library. */
    data object NotFound : ResultsUiState

    /**
     * A fresh capture couldn't reach the network. Offers a retry and, if the library isn't empty,
     * the most recent saved scan as a last-result fallback (Design Spec — offline handling).
     */
    data class Offline(val lastScan: Scan?) : ResultsUiState
}

/** One turn of the follow-up "Ask about this" thread. [answer] is null while the reply is pending. */
data class AskMessage(val question: String, val answer: String?)

/** One-shot effects the route reacts to. */
sealed interface ResultEvent {
    data object Saved : ResultEvent
    data object Retake : ResultEvent

    /** The saved scan was deleted from the library; the route should pop back. */
    data object Deleted : ResultEvent
}
