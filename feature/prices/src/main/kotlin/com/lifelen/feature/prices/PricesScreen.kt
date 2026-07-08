package com.lifelen.feature.prices

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifelen.core.designsystem.LifeLensIcons
import com.lifelen.core.designsystem.component.ButtonType
import com.lifelen.core.designsystem.component.EmptyState
import com.lifelen.core.designsystem.component.LifeLensButton
import com.lifelen.core.designsystem.component.ModeChip
import com.lifelen.core.designsystem.component.RaisedCircleButton
import com.lifelen.core.designsystem.component.SourceFootnote
import com.lifelen.core.designsystem.component.clickableEnabled
import com.lifelen.core.designsystem.theme.Body
import com.lifelen.core.designsystem.theme.BodyStyle
import com.lifelen.core.designsystem.theme.CaptionStyle
import com.lifelen.core.designsystem.theme.Chamber
import com.lifelen.core.designsystem.theme.DataMd
import com.lifelen.core.designsystem.theme.DataSm
import com.lifelen.core.designsystem.theme.Hairline
import com.lifelen.core.designsystem.theme.LifeLensShapes
import com.lifelen.core.designsystem.theme.Positive
import com.lifelen.core.designsystem.theme.PositiveTint
import com.lifelen.core.designsystem.theme.Raised
import com.lifelen.core.designsystem.theme.Raised2
import com.lifelen.core.designsystem.theme.LabelStyle
import com.lifelen.core.designsystem.theme.SubtleBorder
import com.lifelen.core.designsystem.theme.TextFaint
import com.lifelen.core.designsystem.theme.TextPrimary
import com.lifelen.core.designsystem.theme.TextSecondary
import com.lifelen.core.designsystem.theme.TitleStyle
import com.lifelen.core.designsystem.theme.NavTitle
import com.lifelen.core.model.BuyOption
import com.lifelen.core.model.PriceCondition
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.model.ShoppingLink
import java.util.Locale

/** A resolved external destination (a retailer/engine name + its URL) pending an "open" confirm. */
private data class OpenTarget(val name: String, val url: String)

@Composable
fun PricesRoute(
    onBack: () -> Unit,
    viewModel: PricesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PricesScreen(
        uiState = uiState,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onSelectCondition = viewModel::selectCondition,
    )
}

@Composable
internal fun PricesScreen(
    uiState: PricesUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onSelectCondition: (PriceCondition) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    var hasConfirmedExternal by rememberSaveable { mutableStateOf(false) }
    var pending by remember { mutableStateOf<OpenTarget?>(null) }

    val onOpenTarget: (OpenTarget) -> Unit = { target ->
        if (hasConfirmedExternal) runCatching { uriHandler.openUri(target.url) } else pending = target
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Chamber)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        NavBar(title = uiState.title, onBack = onBack, onRefresh = onRefresh)

        val price = uiState.price
        when {
            // No scan resolved at all.
            uiState.notFound -> EmptyState(
                headline = "Nothing to price",
                body = "We couldn't find this scan.",
                modifier = Modifier.fillMaxWidth(),
                ctaText = "Go back",
                onCta = onBack,
            )

            // A synthesised price → the full breakdown, with where-to-buy links underneath.
            price != null -> {
                PriceSummaryStrip(
                    price = price,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp),
                )
                ConditionChips(
                    selected = uiState.selectedCondition,
                    onSelectCondition = onSelectCondition,
                )
                PriceList(
                    price = price,
                    selected = uiState.selectedCondition,
                    whereToBuy = uiState.whereToBuy,
                    onOpen = onOpenTarget,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }

            // No extracted price — the where-to-buy links are the guaranteed path to buy.
            else -> WhereToBuyList(
                links = uiState.whereToBuy,
                searching = uiState.isRefreshing,
                onOpen = onOpenTarget,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
    }

    val target = pending
    if (target != null) {
        Dialog(onDismissRequest = { pending = null }) {
            Column(
                Modifier
                    .clip(LifeLensShapes.card)
                    .background(Body)
                    .border(1.dp, SubtleBorder, LifeLensShapes.card)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Open ${target.name}?", style = TitleStyle, color = TextPrimary)
                Text("This opens an external site.", style = BodyStyle, color = TextSecondary)
                Row(
                    Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    LifeLensButton(
                        "Cancel",
                        { pending = null },
                        Modifier.weight(1f),
                        type = ButtonType.Secondary,
                    )
                    LifeLensButton(
                        "Open",
                        {
                            hasConfirmedExternal = true
                            runCatching { uriHandler.openUri(target.url) }
                            pending = null
                        },
                        Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/** Standalone "where to buy" list shown when no live price could be synthesised. */
@Composable
private fun WhereToBuyList(
    links: List<ShoppingLink>,
    searching: Boolean,
    onOpen: (OpenTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        item(key = "wtb-header") {
            Column(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                Text(
                    if (searching) "Finding live prices…" else "Open a store to see prices",
                    style = TitleStyle,
                    color = TextPrimary,
                )
                Text(
                    "Live prices weren't available, so pick a search engine or retailer to check the " +
                        "current price and buy.",
                    style = CaptionStyle,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        whereToBuySection(links, onOpen)
    }
}

/** Grouped retailer/search-engine rows appended to a price or where-to-buy list. */
private fun LazyListScope.whereToBuySection(
    links: List<ShoppingLink>,
    onOpen: (OpenTarget) -> Unit,
) {
    if (links.isEmpty()) return
    val engines = links.filter { it.kind == ShoppingLink.Kind.SEARCH_ENGINE }
    val retailers = links.filter { it.kind == ShoppingLink.Kind.RETAILER }
    if (engines.isNotEmpty()) {
        item(key = "wtb-engines") { GroupHeader("Shopping search") }
        items(engines, key = { "wtb-e-${it.name}" }) { ShopLinkRow(it, onOpen) }
    }
    if (retailers.isNotEmpty()) {
        item(key = "wtb-retailers") { GroupHeader("Where to buy") }
        items(retailers, key = { "wtb-r-${it.name}" }) { ShopLinkRow(it, onOpen) }
    }
}

@Composable
private fun ShopLinkRow(link: ShoppingLink, onOpen: (OpenTarget) -> Unit) {
    Column(Modifier.clickableEnabled(true) { onOpen(OpenTarget(link.name, link.url)) }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Raised2),
                contentAlignment = Alignment.Center,
            ) {
                Text(link.name.initials(), style = LabelStyle, color = TextPrimary)
            }
            Text(
                link.name,
                style = BodyStyle.copy(fontWeight = FontWeight.Medium),
                color = TextPrimary,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            Icon(
                LifeLensIcons.ChevronRight,
                contentDescription = "Open ${link.name}",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp),
            )
        }
        HorizontalDivider(color = Hairline, thickness = 1.dp)
    }
}

@Composable
private fun NavBar(
    title: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RaisedCircleButton(
            icon = LifeLensIcons.ChevronLeft,
            contentDescription = "Back",
            onClick = onBack,
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Prices", style = NavTitle, color = TextPrimary, maxLines = 1)
            if (title.isNotBlank()) {
                Text(title, style = CaptionStyle, color = TextSecondary, maxLines = 1)
            }
        }
        RaisedCircleButton(
            icon = LifeLensIcons.Refresh,
            contentDescription = "Refresh",
            onClick = onRefresh,
        )
    }
}

@Composable
private fun PriceSummaryStrip(price: PriceInfo, modifier: Modifier = Modifier) {
    val cur = price.currency
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(LifeLensShapes.card)
            .background(Raised)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SummaryCell(
            label = "Lowest",
            value = "$cur${price.lowPrice.money()}",
            valueColor = Positive,
            modifier = Modifier.weight(1f),
        )
        VerticalHairline()
        SummaryCell(
            label = "Average",
            value = "$cur${price.average.money()}",
            valueColor = TextPrimary,
            modifier = Modifier.weight(1f),
        )
        VerticalHairline()
        SummaryCell(
            label = "Sellers",
            value = "${price.sellerCount}",
            valueColor = TextPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryCell(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = CaptionStyle, color = TextSecondary, maxLines = 1)
        Text(
            value,
            style = DataMd,
            color = valueColor,
            maxLines = 1,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun VerticalHairline() {
    Box(
        Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(Hairline),
    )
}

@Composable
private fun ConditionChips(
    selected: PriceCondition,
    onSelectCondition: (PriceCondition) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ModeChip(
            text = "New",
            selected = selected == PriceCondition.NEW,
            onClick = { onSelectCondition(PriceCondition.NEW) },
        )
        ModeChip(
            text = "Renewed",
            selected = selected == PriceCondition.RENEWED,
            onClick = { onSelectCondition(PriceCondition.RENEWED) },
        )
        ModeChip(
            text = "Used",
            selected = selected == PriceCondition.USED,
            onClick = { onSelectCondition(PriceCondition.USED) },
        )
    }
}

@Composable
private fun PriceList(
    price: PriceInfo,
    selected: PriceCondition,
    whereToBuy: List<ShoppingLink>,
    onOpen: (OpenTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryOptions = price.options(selected)
    val cheapestNew = price.cheapestNew
    // On the New tab the design shows Renewed listings grouped underneath.
    val renewedOptions =
        if (selected == PriceCondition.NEW) price.options(PriceCondition.RENEWED) else emptyList()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        if (primaryOptions.isEmpty()) {
            item(key = "empty") {
                Text(
                    "No ${selected.displayName()} listings found — check other conditions or a store below.",
                    style = CaptionStyle,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                )
            }
        } else {
            itemsIndexed(
                items = primaryOptions,
                key = { index, option -> "sel-$index-${option.url}" },
            ) { index, option ->
                PriceRow(
                    option = option,
                    isBest = cheapestNew != null && option == cheapestNew,
                    onTap = { onOpen(OpenTarget(option.retailer, option.url)) },
                    showDivider = !(renewedOptions.isEmpty() && index == primaryOptions.lastIndex),
                )
            }
        }

        if (renewedOptions.isNotEmpty()) {
            item(key = "renewed-header") { GroupHeader("Renewed") }
            itemsIndexed(
                items = renewedOptions,
                key = { index, option -> "renewed-$index-${option.url}" },
            ) { index, option ->
                PriceRow(
                    option = option,
                    isBest = false,
                    onTap = { onOpen(OpenTarget(option.retailer, option.url)) },
                    showDivider = index < renewedOptions.lastIndex,
                )
            }
        }

        item(key = "footnote") {
            SourceFootnote(
                text = price.footnote(),
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        // Always offer stores/engines to open, even alongside synthesised prices.
        whereToBuySection(whereToBuy, onOpen)
    }
}

@Composable
private fun PriceRow(
    option: BuyOption,
    isBest: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    val priceColor = if (option.condition == PriceCondition.NEW) TextPrimary else TextSecondary
    Column(modifier = modifier.clickableEnabled(true, onTap)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Raised2),
                contentAlignment = Alignment.Center,
            ) {
                Text(option.retailer.initials(), style = LabelStyle, color = TextPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    option.retailer,
                    style = BodyStyle.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary,
                    maxLines = 1,
                )
                Text(
                    option.meta ?: if (option.inStock) "In stock" else "Out of stock",
                    style = CaptionStyle,
                    color = TextSecondary,
                    maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${option.currency}${option.price.money()}",
                    style = DataMd,
                    color = priceColor,
                    maxLines = 1,
                )
                if (isBest) {
                    Text(
                        "Best price",
                        style = CaptionStyle,
                        color = Positive,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(LifeLensShapes.chip)
                            .background(PositiveTint)
                            .padding(horizontal = 9.dp, vertical = 2.dp),
                    )
                }
            }
        }
        if (showDivider) HorizontalDivider(color = Hairline, thickness = 1.dp)
    }
}

@Composable
private fun GroupHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = CaptionStyle.copy(letterSpacing = 0.7.sp),
        color = TextFaint,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 4.dp),
    )
}

private val updatedTimeFormat = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())

/** "Via {source} · updated HH:mm" when the price carries a fetch time, else a plain estimate note. */
private fun PriceInfo.footnote(): String {
    val updated = fetchedAt?.let { " · updated ${updatedTimeFormat.format(java.util.Date(it))}" } ?: " · estimate"
    return "Via $source$updated"
}

/** Whole number → no decimals; otherwise two decimal places. */
private fun Double.money(): String =
    if (this % 1.0 == 0.0) this.toLong().toString()
    else String.format(Locale.US, "%.2f", this)

private fun String.initials(): String {
    val words = trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words[0].take(1).uppercase()
        else -> (words[0].take(1) + words[1].take(1)).uppercase()
    }
}

private fun PriceCondition.displayName(): String = when (this) {
    PriceCondition.NEW -> "new"
    PriceCondition.RENEWED -> "renewed"
    PriceCondition.USED -> "used"
}
