package com.lifelen.feature.results

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.lifelen.core.designsystem.component.MessageState
import com.lifelen.core.model.NutritionInfo
import com.lifelen.core.model.PriceInfo
import com.lifelen.core.model.Scan
import androidx.compose.material.icons.filled.SearchOff
import java.io.File

@Composable
fun ResultsRoute(
    onBack: () -> Unit,
    viewModel: ResultsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ResultsScreen(uiState = uiState, onBack = onBack, onToggleFavorite = viewModel::toggleFavorite)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ResultsScreen(
    uiState: ResultsUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val scan = (uiState as? ResultsUiState.Success)?.scan
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(scan?.title ?: "Result") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (scan != null) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (scan.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Toggle favorite",
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        when (uiState) {
            ResultsUiState.Loading -> com.lifelen.core.designsystem.component.LoadingState(
                modifier = Modifier.padding(padding),
                message = "Loading…",
            )

            ResultsUiState.NotFound -> MessageState(
                modifier = Modifier.padding(padding),
                icon = Icons.Filled.SearchOff,
                title = "Scan not found",
                description = "This scan may have been deleted.",
                actionLabel = "Go back",
                onAction = onBack,
            )

            is ResultsUiState.Success -> ResultsContent(
                scan = uiState.scan,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun ResultsContent(scan: Scan, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            AsyncImage(
                model = File(scan.imagePath),
                contentDescription = scan.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.4f),
            )
        }
        item {
            Column {
                Text(scan.title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = scan.category.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (scan.identification.summary.isNotBlank()) {
                    Text(
                        text = scan.identification.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }

        if (scan.identification.attributes.isNotEmpty()) {
            item { SectionCard(title = "Details") { AttributeList(scan.identification.attributes) } }
        }
        scan.nutrition?.let { nutrition ->
            item { SectionCard(title = "Nutrition") { NutritionBlock(nutrition) } }
        }
        scan.price?.let { price ->
            item { SectionCard(title = "Pricing & where to buy") { PriceBlock(price) } }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            content()
        }
    }
}

@Composable
private fun AttributeList(attributes: Map<String, String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        attributes.forEach { (key, value) ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(key, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun NutritionBlock(nutrition: NutritionInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("${nutrition.calories} kcal · ${nutrition.servingSize}", style = MaterialTheme.typography.titleLarge)
        Text(
            "Protein ${nutrition.protein}g · Carbs ${nutrition.carbs}g · Fat ${nutrition.fat}g",
            style = MaterialTheme.typography.bodyMedium,
        )
        nutrition.healthNotes?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PriceBlock(price: PriceInfo) {
    val uriHandler = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (price.highPrice > 0) {
            Text(
                "${price.currency} ${price.lowPrice} – ${price.highPrice}",
                style = MaterialTheme.typography.titleLarge,
            )
        }
        price.options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { runCatching { uriHandler.openUri(option.url) } }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(option.retailer, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${option.currency} ${option.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Text(
            price.disclaimer,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
