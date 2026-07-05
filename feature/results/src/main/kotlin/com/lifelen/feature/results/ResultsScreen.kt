package com.lifelen.feature.results

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.lifelen.core.designsystem.LifeLensIcons
import com.lifelen.core.designsystem.component.LifeLensButton
import com.lifelen.core.designsystem.component.MediaIconButton
import com.lifelen.core.designsystem.component.SheetGrabber
import com.lifelen.core.designsystem.theme.Body
import com.lifelen.core.designsystem.theme.BodyStyle
import com.lifelen.core.designsystem.theme.Chamber
import com.lifelen.core.designsystem.theme.LabelStyle
import com.lifelen.core.designsystem.theme.LifeLensShapes
import com.lifelen.core.designsystem.theme.Raised
import com.lifelen.core.designsystem.theme.SubtleBorder
import com.lifelen.core.designsystem.theme.TextPrimary
import com.lifelen.core.designsystem.theme.TextSecondary
import com.lifelen.core.designsystem.theme.TitleStyle
import com.lifelen.feature.results.components.DocumentResultBody
import com.lifelen.feature.results.components.FoodResultBody
import com.lifelen.core.model.ScanCategory
import com.lifelen.feature.results.components.IdentityHeader
import com.lifelen.feature.results.components.PlantResultBody
import com.lifelen.feature.results.components.ProductResultBody
import com.lifelen.feature.results.components.ResultSkeleton
import kotlinx.coroutines.delay
import java.io.File

/** Height of the frozen capture the result sheet sits over (Design Spec §3.3). */
private val CaptureHeight = 338.dp

@Composable
fun ResultRoute(
    onBack: () -> Unit,
    onOpenPrices: (String) -> Unit,
    viewModel: ResultsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val capturedImagePath by viewModel.capturedImagePath.collectAsStateWithLifecycle()
    var savedPillVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ResultEvent.Saved -> savedPillVisible = true
                ResultEvent.Retake -> onBack()
                ResultEvent.Deleted -> onBack()
            }
        }
    }
    LaunchedEffect(savedPillVisible) {
        if (savedPillVisible) {
            delay(2200)
            savedPillVisible = false
        }
    }

    ResultsScreen(
        uiState = uiState,
        capturedImagePath = capturedImagePath,
        savedPillVisible = savedPillVisible,
        onBack = onBack,
        onRetake = viewModel::retake,
        onRefresh = viewModel::refresh,
        onSave = viewModel::save,
        onSetPortion = viewModel::setPortion,
        onOpenPrices = onOpenPrices,
        onToggleFavorite = viewModel::toggleFavorite,
        onDelete = viewModel::delete,
    )
}

/**
 * Backwards-compatible entry point used by the app NavHost, which does not (yet) route to the
 * standalone prices screen. Delegates to [ResultRoute].
 */
@Composable
fun ResultsRoute(
    onBack: () -> Unit,
    onOpenPrices: (String) -> Unit = {},
    viewModel: ResultsViewModel = hiltViewModel(),
) = ResultRoute(onBack = onBack, onOpenPrices = onOpenPrices, viewModel = viewModel)

@Composable
internal fun ResultsScreen(
    uiState: ResultsUiState,
    capturedImagePath: String?,
    savedPillVisible: Boolean,
    onBack: () -> Unit,
    onRetake: () -> Unit,
    onRefresh: () -> Unit,
    onSave: () -> Unit,
    onSetPortion: (Float) -> Unit,
    onOpenPrices: (String) -> Unit,
    onToggleFavorite: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    val ready = uiState as? ResultsUiState.Ready
    val isSavedDetail = ready?.saved == true
    val isFavorite = ready?.scan?.isFavorite == true

    Box(
        Modifier
            .fillMaxSize()
            .background(Chamber),
    ) {
        // Frozen capture + dimming scrim.
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(CaptureHeight),
        ) {
            if (capturedImagePath != null) {
                AsyncImage(
                    model = File(capturedImagePath),
                    contentDescription = "Captured frame",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(Modifier.fillMaxSize().background(Body))
            }
            Box(Modifier.matchParentSize().background(Color(0x73000000)))
        }

        // Capture controls over the frame.
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MediaIconButton(
                icon = LifeLensIcons.Close,
                contentDescription = "Close",
                onClick = onBack,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isSavedDetail) {
                    MediaIconButton(
                        icon = if (isFavorite) LifeLensIcons.Favorite else LifeLensIcons.FavoriteOutline,
                        contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                        onClick = onToggleFavorite,
                    )
                    MediaIconButton(
                        icon = LifeLensIcons.Trash,
                        contentDescription = "Delete",
                        onClick = onDelete,
                    )
                }
                MediaIconButton(
                    icon = LifeLensIcons.Refresh,
                    contentDescription = if (isSavedDetail) "Refresh price" else "Retake",
                    onClick = if (isSavedDetail) onRefresh else onRetake,
                )
            }
        }

        // Result sheet.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.62f)
                .clip(LifeLensShapes.sheet)
                .background(Body)
                .border(1.dp, SubtleBorder, LifeLensShapes.sheet)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 20.dp),
        ) {
            SheetGrabber(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp),
            )
            when (uiState) {
                ResultsUiState.Processing -> ResultSkeleton()

                is ResultsUiState.Failed -> FailedContent(uiState.message, onBack)

                ResultsUiState.NotFound -> NotFoundContent(onBack)

                is ResultsUiState.Ready -> {
                    IdentityHeader(uiState.scan)
                    val nutrition = uiState.scan.nutrition
                    when {
                        nutrition != null -> FoodResultBody(
                            scan = uiState.scan,
                            nutrition = nutrition,
                            portionFactor = uiState.portionFactor,
                            saved = uiState.saved,
                            onSave = onSave,
                            onSetPortion = onSetPortion,
                        )

                        uiState.scan.category == ScanCategory.PLANT -> PlantResultBody(
                            scan = uiState.scan,
                            saved = uiState.saved,
                            onSave = onSave,
                        )

                        uiState.scan.category == ScanCategory.DOCUMENT -> DocumentResultBody(
                            scan = uiState.scan,
                            saved = uiState.saved,
                            onSave = onSave,
                        )

                        else -> ProductResultBody(
                            scan = uiState.scan,
                            saved = uiState.saved,
                            onSave = onSave,
                            onOpenPrices = onOpenPrices,
                        )
                    }
                }
            }
        }

        // "Saved to library" confirmation pill.
        AnimatedVisibility(
            visible = savedPillVisible,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Row(
                Modifier
                    .clip(LifeLensShapes.chip)
                    .background(Raised)
                    .border(1.dp, SubtleBorder, LifeLensShapes.chip)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text("Saved to library", style = LabelStyle, color = TextPrimary)
            }
        }
    }
}

@Composable
private fun FailedContent(message: String, onBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Couldn't identify this", style = TitleStyle, color = TextPrimary)
        Text(message, style = BodyStyle, color = TextSecondary)
        Spacer(Modifier.height(6.dp))
        LifeLensButton("Retake", onBack, Modifier.fillMaxWidth())
    }
}

@Composable
private fun NotFoundContent(onBack: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Scan not found", style = TitleStyle, color = TextPrimary)
        Text("This scan may have been deleted.", style = BodyStyle, color = TextSecondary)
        Spacer(Modifier.height(6.dp))
        LifeLensButton("Go back", onBack, Modifier.fillMaxWidth())
    }
}
