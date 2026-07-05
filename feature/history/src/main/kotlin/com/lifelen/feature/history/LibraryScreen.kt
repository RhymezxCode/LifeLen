package com.lifelen.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.lifelen.core.designsystem.LifeLensIcons
import com.lifelen.core.designsystem.component.BracketThumb
import com.lifelen.core.designsystem.component.EmptyState
import com.lifelen.core.designsystem.component.MediaIconButton
import com.lifelen.core.designsystem.component.LifeLensSearchBar
import com.lifelen.core.designsystem.component.ModeChip
import com.lifelen.core.designsystem.component.TrendPill
import com.lifelen.core.designsystem.component.clickableEnabled
import com.lifelen.core.designsystem.component.visual
import com.lifelen.core.designsystem.theme.Amber
import com.lifelen.core.designsystem.theme.BodyStyle
import com.lifelen.core.designsystem.theme.CaptionStyle
import com.lifelen.core.designsystem.theme.CatPlant
import com.lifelen.core.designsystem.theme.Chamber
import com.lifelen.core.designsystem.theme.DataMd
import com.lifelen.core.designsystem.theme.DataSm
import com.lifelen.core.designsystem.theme.Hairline
import com.lifelen.core.designsystem.theme.LifeLensShapes
import com.lifelen.core.designsystem.theme.LifeLensTheme
import com.lifelen.core.designsystem.theme.OnAmber
import com.lifelen.core.designsystem.theme.Positive
import com.lifelen.core.designsystem.theme.TextFaint
import com.lifelen.core.designsystem.theme.TextPrimary
import com.lifelen.core.designsystem.theme.TextSecondary
import com.lifelen.core.designsystem.theme.NavTitle
import com.lifelen.core.designsystem.theme.TitleStyle
import com.lifelen.core.model.BuyOption
import com.lifelen.core.model.Identification
import com.lifelen.core.model.NutritionInfo
import com.lifelen.core.model.PriceCondition
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.model.Scan
import com.lifelen.core.model.ScanCategory
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/** Library (S08) with grouped scan history and its empty state (S10). */
@Composable
fun LibraryRoute(
    onBack: () -> Unit,
    onOpenScan: (String) -> Unit,
    onNewScan: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LibraryScreen(
        uiState = uiState,
        onQueryChange = viewModel::onQueryChange,
        onFilter = viewModel::onFilter,
        onOpenScan = onOpenScan,
        onNewScan = onNewScan,
        onBack = onBack,
    )
}

/** Stateless library screen — rendered by [LibraryRoute] and by the previews below. */
@Composable
internal fun LibraryScreen(
    uiState: LibraryUiState,
    onQueryChange: (String) -> Unit,
    onFilter: (ScanCategory?) -> Unit,
    onOpenScan: (String) -> Unit,
    onNewScan: () -> Unit,
    onBack: () -> Unit,
) {
    val showEmpty = !uiState.isLoading && uiState.groups.isEmpty()

    Box(Modifier.fillMaxSize().background(Chamber)) {
        Column(Modifier.fillMaxSize()) {
            // NavBar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MediaIconButton(
                    icon = LifeLensIcons.ChevronLeft,
                    contentDescription = "Back",
                    onClick = onBack,
                )
                Text(
                    text = "Library",
                    style = NavTitle,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = uiState.totalCount.toString(),
                    style = DataSm,
                    color = TextSecondary,
                )
            }

            // Search
            LifeLensSearchBar(
                query = uiState.query,
                onQueryChange = onQueryChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = "Search scans, specs, prices",
            )

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ModeChip("All", uiState.filter == null, onClick = { onFilter(null) })
                ModeChip(
                    "Electronics",
                    uiState.filter == ScanCategory.ELECTRONICS,
                    onClick = { onFilter(ScanCategory.ELECTRONICS) },
                )
                ModeChip("Food", uiState.filter == ScanCategory.FOOD, onClick = { onFilter(ScanCategory.FOOD) })
                ModeChip("Plants", uiState.filter == ScanCategory.PLANT, onClick = { onFilter(ScanCategory.PLANT) })
            }

            if (showEmpty) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(
                        headline = "Nothing scanned yet",
                        body = "Your identified items will live here.",
                        ctaText = "Start scanning",
                        onCta = onNewScan,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                ) {
                    uiState.groups.forEach { group ->
                        item(key = "header_${group.header}") { GroupHeader(group.header) }
                        items(group.scans, key = { it.id }) { scan ->
                            LibraryRow(scan = scan, onClick = { onOpenScan(scan.id) })
                        }
                    }
                }
            }
        }

        if (!showEmpty) {
            FloatingScanButton(
                onClick = onNewScan,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 26.dp),
            )
        }
    }
}

@Composable
private fun GroupHeader(text: String) {
    Text(
        text = text,
        style = CaptionStyle.copy(letterSpacing = 0.7.sp),
        color = TextFaint,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun LibraryRow(scan: Scan, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .clickableEnabled(true, onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BracketThumb(size = 42.dp) {
                AsyncImage(
                    model = File(scan.imagePath),
                    contentDescription = scan.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Column(Modifier.weight(1f)) {
                Text(
                    text = scan.title,
                    style = TitleStyle.copy(fontSize = 13.sp),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${scan.category.visual().label} · ${timeOf(scan.createdAt)}",
                    style = CaptionStyle,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                when (scan.category) {
                    ScanCategory.ELECTRONICS, ScanCategory.BOOK, ScanCategory.CLOTHING ->
                        scan.price?.let { price ->
                            Text(
                                text = "${price.currency}${price.lowPrice.money()}",
                                style = DataMd,
                                color = TextPrimary,
                            )
                        }

                    ScanCategory.FOOD ->
                        scan.nutrition?.let { nutrition ->
                            Text(
                                text = "${nutrition.calories} kcal",
                                style = DataMd,
                                color = TextPrimary,
                            )
                        }

                    ScanCategory.PLANT ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = LifeLensIcons.Droplet,
                                contentDescription = null,
                                tint = CatPlant,
                                modifier = Modifier.size(14.dp),
                            )
                            Text("Water in 3 d", style = DataSm, color = Positive)
                        }

                    else -> Unit
                }

                scan.priceDelta?.let { delta ->
                    val cur = scan.price?.currency ?: ""
                    TrendPill(
                        deltaLabel = "$cur${abs(delta).money()}",
                        isDown = delta < 0,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Hairline),
        )
    }
}

@Composable
private fun FloatingScanButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(LifeLensShapes.chip)
            .background(Amber)
            .clickableEnabled(true, onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = LifeLensIcons.Scan,
            contentDescription = null,
            tint = OnAmber,
            modifier = Modifier.size(18.dp),
        )
        Text("New scan", style = BodyStyle, color = OnAmber)
    }
}

private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

private fun timeOf(millis: Long): String = timeFormat.format(Date(millis))

/** Formats a price magnitude with thousands separators, dropping ".00" on whole values. */
private fun Double.money(): String {
    val nf = NumberFormat.getNumberInstance(Locale.US)
    nf.minimumFractionDigits = 0
    nf.maximumFractionDigits = if (this % 1.0 == 0.0) 0 else 2
    return nf.format(this)
}

// ---------------------------------------------------------------------------
// Previews — render the real stateless LibraryScreen with sample data.
// ---------------------------------------------------------------------------

private fun previewLaptopScan() = Scan(
    id = "1",
    imagePath = "",
    createdAt = 0L,
    identification = Identification(
        title = "MacBook Air 13\" (M2, 2022)",
        category = ScanCategory.ELECTRONICS,
        summary = "A thin-and-light laptop.",
        confidence = 0.94f,
        attributes = linkedMapOf(
            "Chip" to "M2",
            "Memory" to "8 GB",
            "Storage" to "256 GB",
            "Display" to "13.6\"",
        ),
    ),
    price = PriceInfo(
        currency = "$",
        lowPrice = 849.0,
        highPrice = 999.0,
        average = 967.0,
        source = "Google Shopping",
        options = listOf(
            BuyOption("Amazon", 849.0, "$", "http://a", true, PriceCondition.NEW, "Free shipping · in stock"),
            BuyOption("Best Buy", 899.0, "$", "http://b", true, PriceCondition.NEW, "Free pickup today"),
            BuyOption("eBay", 699.0, "$", "http://e", true, PriceCondition.RENEWED, "1 yr warranty"),
        ),
    ),
)

private fun previewFoodScan() = Scan(
    id = "2",
    imagePath = "",
    createdAt = 0L,
    identification = Identification(
        title = "Jollof rice with grilled chicken",
        category = ScanCategory.FOOD,
        summary = "",
        confidence = 0.8f,
    ),
    nutrition = NutritionInfo(
        servingSize = "1 plate · ~350 g",
        calories = 540,
        protein = 28.0,
        carbs = 62.0,
        fat = 19.0,
        fiber = 4.0,
        sugars = 6.0,
        sodium = 890,
        ingredients = listOf("rice", "chicken", "sauce"),
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFF0D0F13, widthDp = 390, heightDp = 844)
@Composable
private fun LibraryPopulatedPreview() {
    LifeLensTheme {
        LibraryScreen(
            uiState = LibraryUiState(
                isLoading = false,
                totalCount = 2,
                groups = listOf(
                    LibraryGroup(header = "Today", scans = listOf(previewLaptopScan())),
                    LibraryGroup(header = "Yesterday", scans = listOf(previewFoodScan())),
                ),
            ),
            onQueryChange = {},
            onFilter = {},
            onOpenScan = {},
            onNewScan = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0F13, widthDp = 390, heightDp = 844)
@Composable
private fun LibraryEmptyPreview() {
    LifeLensTheme {
        LibraryScreen(
            uiState = LibraryUiState(isLoading = false, groups = emptyList(), totalCount = 0),
            onQueryChange = {},
            onFilter = {},
            onOpenScan = {},
            onNewScan = {},
            onBack = {},
        )
    }
}
