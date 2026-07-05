package com.lifelen.core.data.handler

import com.lifelen.core.model.ScanCategory
import javax.inject.Inject
import javax.inject.Singleton

/** Routes a [ScanCategory] to its [CategoryHandler], falling back to the generic handler. */
@Singleton
class CategoryHandlerRegistry @Inject constructor(
    handlers: Set<@JvmSuppressWildcards CategoryHandler>,
) {
    private val byCategory = handlers.associateBy { it.category }
    private val fallback = byCategory[ScanCategory.GENERIC]
        ?: handlers.firstOrNull()
        ?: error("No CategoryHandler registered")

    fun handlerFor(category: ScanCategory): CategoryHandler = byCategory[category] ?: fallback
}
