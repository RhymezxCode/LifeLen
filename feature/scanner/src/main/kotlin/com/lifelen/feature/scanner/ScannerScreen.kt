package com.lifelen.feature.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.lifelen.core.designsystem.LifeLensIcons
import com.lifelen.core.designsystem.component.ButtonType
import com.lifelen.core.designsystem.component.BracketThumb
import com.lifelen.core.designsystem.component.DetectionBrackets
import com.lifelen.core.designsystem.component.DetectionState
import com.lifelen.core.designsystem.component.LifeLensButton
import com.lifelen.core.designsystem.component.MediaIconButton
import com.lifelen.core.designsystem.component.ModeChip
import com.lifelen.core.designsystem.component.RaisedIconButton
import com.lifelen.core.designsystem.component.ShutterButton
import com.lifelen.core.designsystem.component.ShutterState
import com.lifelen.core.designsystem.component.clickableEnabled
import com.lifelen.core.designsystem.theme.Amber
import com.lifelen.core.designsystem.theme.BodyStyle
import com.lifelen.core.designsystem.theme.Chamber
import com.lifelen.core.designsystem.theme.DataSm
import com.lifelen.core.designsystem.theme.Display
import com.lifelen.core.designsystem.theme.Hairline
import com.lifelen.core.designsystem.theme.OnAmber
import com.lifelen.core.designsystem.theme.Raised
import com.lifelen.core.designsystem.theme.TextPrimary
import com.lifelen.core.designsystem.theme.TextSecondary
import com.lifelen.feature.scanner.util.toDownscaledJpeg
import com.lifelen.feature.scanner.util.uriToDownscaledJpeg
import java.io.File

/** Selectable capture modes shown in the [ModeStrip]. Visual-only for v1. */
private val ScanModes = listOf("Auto", "Electronics", "Food", "Text")

@Composable
fun ScannerRoute(
    onNavigateToResult: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { onNavigateToResult() }
    }

    ScannerScreen(
        uiState = uiState,
        onCaptured = viewModel::onCaptured,
        onCaptureError = viewModel::onCaptureError,
        onDismissError = viewModel::dismissError,
        onSelectMode = viewModel::selectMode,
        onOpenLibrary = onOpenLibrary,
        onOpenSettings = onOpenSettings,
    )
}

@Composable
internal fun ScannerScreen(
    uiState: ScannerUiState,
    onCaptured: (ByteArray) -> Unit,
    onCaptureError: (String) -> Unit,
    onDismissError: () -> Unit,
    onSelectMode: (String) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasPermission = granted }

    // Gallery import — reuses the same identify pipeline as a camera capture (Spec S01/S02).
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            val bytes = uriToDownscaledJpeg(context, uri)
            if (bytes != null) onCaptured(bytes) else onCaptureError("Couldn't read that image.")
        }
    }
    val onPickGallery = {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    // Auto-prompt the first time the screen appears without permission.
    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            onDismissError()
        }
    }

    Scaffold(
        containerColor = Chamber,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { _ ->
        // Camera home is intentionally full-bleed; Scaffold insets are ignored.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Chamber),
        ) {
            if (hasPermission) {
                CameraHome(
                    uiState = uiState,
                    onCaptured = onCaptured,
                    onCaptureError = onCaptureError,
                    onSelectMode = onSelectMode,
                    onPickGallery = onPickGallery,
                    onOpenLibrary = onOpenLibrary,
                    onOpenSettings = onOpenSettings,
                )
            } else {
                PermissionPrime(
                    onEnable = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onImportGallery = onPickGallery,
                )
            }
        }
    }
}

/** S02 — the live viewfinder with camera chrome. */
@Composable
private fun CameraHome(
    uiState: ScannerUiState,
    onCaptured: (ByteArray) -> Unit,
    onCaptureError: (String) -> Unit,
    onSelectMode: (String) -> Unit,
    onPickGallery: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }
    LaunchedEffect(controller, lifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
    }

    fun capture() {
        if (uiState.isCapturing) return
        controller.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bytes = runCatching { image.toDownscaledJpeg() }
                        .also { image.close() }
                        .getOrNull()
                    if (bytes != null) onCaptured(bytes) else onCaptureError("Couldn't read the photo.")
                }

                override fun onError(exc: ImageCaptureException) {
                    onCaptureError(exc.message ?: "Couldn't take that photo. Try again.")
                }
            },
        )
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx -> PreviewView(ctx).apply { this.controller = controller } },
            modifier = Modifier.fillMaxSize(),
        )

        // Top controls (y ≈ 54, 16dp gutters).
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 54.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MediaIconButton(LifeLensIcons.Flash, "Flash", {})
            MediaIconButton(LifeLensIcons.Settings, "Settings", onOpenSettings)
        }

        // Idle hint, centered in the lower third.
        if (!uiState.isCapturing) {
            Text(
                text = "Point at anything to identify",
                style = BodyStyle,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(BiasAlignment(0f, 0.35f))
                    .padding(horizontal = 32.dp),
            )
        }

        // Mode strip + bottom chrome pinned to the bottom.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            ModeStrip(
                selected = uiState.selectedMode,
                onSelect = onSelectMode,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            BottomChrome(
                uiState = uiState,
                onCapture = { capture() },
                onPickGallery = onPickGallery,
                onOpenLibrary = onOpenLibrary,
            )
        }
    }
}

/** Horizontal, centered row of capture-mode chips just above the bottom chrome. */
@Composable
private fun ModeStrip(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScanModes.forEach { mode ->
            ModeChip(
                text = mode,
                selected = mode == selected,
                onClick = { onSelect(mode) },
            )
        }
    }
}

/** 118dp Chamber bar with gallery, shutter and library controls. */
@Composable
private fun BottomChrome(
    uiState: ScannerUiState,
    onCapture: () -> Unit,
    onPickGallery: () -> Unit,
    onOpenLibrary: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .background(Chamber)
            .drawBehind {
                drawLine(
                    color = Hairline,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(start = 38.dp, end = 38.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RaisedIconButton(
            icon = LifeLensIcons.Gallery,
            contentDescription = "Import from gallery",
            onClick = onPickGallery,
            modifier = Modifier.size(42.dp),
        )
        ShutterButton(
            state = if (uiState.isCapturing) ShutterState.Processing else ShutterState.Idle,
            onClick = onCapture,
        )
        LibraryThumb(
            thumbPath = uiState.lastThumbPath,
            count = uiState.libraryCount,
            onOpenLibrary = onOpenLibrary,
        )
    }
}

/** 42dp bracket thumbnail of the latest scan with an amber count badge; opens the library. */
@Composable
private fun LibraryThumb(
    thumbPath: String?,
    count: Int,
    onOpenLibrary: () -> Unit,
) {
    Box(modifier = Modifier.clickableEnabled(true, onOpenLibrary)) {
        BracketThumb(size = 42.dp) {
            if (thumbPath != null) {
                AsyncImage(
                    model = File(thumbPath),
                    contentDescription = "Open library",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(Modifier.fillMaxSize().background(Raised))
            }
        }
        if (count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .sizeIn(minWidth = 17.dp, minHeight = 17.dp)
                    .clip(CircleShape)
                    .background(Amber)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = count.toString(),
                    style = DataSm,
                    color = OnAmber,
                )
            }
        }
    }
}

/** S01 — the permission prime shown until camera access is granted. */
@Composable
private fun PermissionPrime(
    onEnable: () -> Unit,
    onImportGallery: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Chamber)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        DetectionBrackets(
            state = DetectionState.Locked,
            modifier = Modifier.size(120.dp),
        )
        Spacer(Modifier.height(32.dp))
        Text(
            text = "Point LifeLens at anything",
            style = Display,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Identify anything you see — get specs, prices, and nutrition in a tap.",
            style = BodyStyle,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        LifeLensButton(
            text = "Enable camera",
            onClick = onEnable,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        LifeLensButton(
            text = "Import a photo instead",
            onClick = onImportGallery,
            modifier = Modifier.fillMaxWidth(),
            type = ButtonType.Ghost,
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "S01 · Permission prime",
    showBackground = true,
    backgroundColor = 0xFF0D0F13,
    widthDp = 390,
    heightDp = 844,
)
@Composable
private fun PermissionPrimePreview() {
    com.lifelen.core.designsystem.theme.LifeLensTheme {
        PermissionPrime(onEnable = {}, onImportGallery = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "S02 · Camera bottom chrome",
    showBackground = true,
    backgroundColor = 0xFF0D0F13,
    widthDp = 390,
)
@Composable
private fun BottomChromePreview() {
    com.lifelen.core.designsystem.theme.LifeLensTheme {
        BottomChrome(
            uiState = ScannerUiState(libraryCount = 3),
            onCapture = {},
            onPickGallery = {},
            onOpenLibrary = {},
        )
    }
}
