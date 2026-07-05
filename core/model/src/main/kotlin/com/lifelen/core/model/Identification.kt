package com.lifelen.core.model

import kotlinx.serialization.Serializable

/**
 * The structured result of asking Qwen-VL "what is this?".
 *
 * @param title short human-readable name, e.g. "Apple MacBook Air M3".
 * @param category coarse type used to route enrichment.
 * @param summary one-to-two sentence description.
 * @param confidence model confidence in 0f..1f (best-effort).
 * @param attributes free-form key/value facts (specs, author, material, ...).
 * @param tags searchable keywords.
 * @param searchQuery a ready-to-use query the grounding step can send to a shopping/search API.
 */
@Serializable
data class Identification(
    val title: String,
    val category: ScanCategory,
    val summary: String,
    val confidence: Float = 0f,
    val attributes: Map<String, String> = emptyMap(),
    val tags: List<String> = emptyList(),
    val searchQuery: String? = null,
)
