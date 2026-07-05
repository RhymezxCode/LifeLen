package com.lifelen.feature.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifelen.core.data.repository.AppSettings
import com.lifelen.core.designsystem.LifeLensIcons
import com.lifelen.core.designsystem.component.ButtonType
import com.lifelen.core.designsystem.component.LifeLensButton
import com.lifelen.core.designsystem.component.MediaIconButton
import com.lifelen.core.designsystem.component.ModeChip
import com.lifelen.core.designsystem.theme.Amber
import com.lifelen.core.designsystem.theme.BodyStyle
import com.lifelen.core.designsystem.theme.CaptionStyle
import com.lifelen.core.designsystem.theme.Chamber
import com.lifelen.core.designsystem.theme.Hairline
import com.lifelen.core.designsystem.theme.LabelStyle
import com.lifelen.core.designsystem.theme.LifeLensShapes
import com.lifelen.core.designsystem.theme.OnAmber
import com.lifelen.core.designsystem.theme.Raised
import com.lifelen.core.designsystem.theme.Raised2
import com.lifelen.core.designsystem.theme.TextFaint
import com.lifelen.core.designsystem.theme.TextPrimary
import com.lifelen.core.designsystem.theme.TextSecondary
import com.lifelen.core.designsystem.theme.TitleStyle
import com.lifelen.core.model.ThemeMode

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        settings = settings,
        onSaveKeys = viewModel::saveKeys,
        onThemeMode = viewModel::setThemeMode,
        onPricingChange = viewModel::setPricingEnabled,
        onHapticsChange = viewModel::setHapticsEnabled,
        onAutoSaveChange = viewModel::setAutoSaveScans,
        onRememberKeysChange = viewModel::setRememberKeys,
        onClearLibrary = viewModel::clearLibrary,
        onBack = onBack,
    )
}

@Composable
internal fun SettingsScreen(
    settings: AppSettings,
    onSaveKeys: (String, String) -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    onPricingChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onAutoSaveChange: (Boolean) -> Unit,
    onRememberKeysChange: (Boolean) -> Unit,
    onClearLibrary: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Chamber),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MediaIconButton(LifeLensIcons.ChevronLeft, "Back", onBack)
            Text(
                "Settings",
                style = TitleStyle,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.size(38.dp))
        }

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // --- Appearance ---
            SectionTitle("Appearance")
            Text("Theme", style = BodyStyle, color = TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    ModeChip(
                        text = mode.label(),
                        selected = settings.themeMode == mode,
                        onClick = { onThemeMode(mode) },
                    )
                }
            }

            HorizontalDivider(color = Hairline)

            // --- API keys ---
            SectionTitle("API keys")
            Text(
                "Stored on this device only. See docs/API-KEYS.md to obtain them.",
                style = CaptionStyle,
                color = TextSecondary,
            )
            ApiKeyEditor(settings = settings, onSaveKeys = onSaveKeys)
            PreferenceSwitchRow(
                title = "Remember API keys",
                subtitle = "Keep keys on this device. Turning off wipes stored keys.",
                checked = settings.rememberKeys,
                onCheckedChange = onRememberKeysChange,
            )

            HorizontalDivider(color = Hairline)

            // --- Scanning ---
            SectionTitle("Scanning")
            PreferenceSwitchRow(
                title = "Live pricing",
                subtitle = "Search the web for current prices and buy links.",
                checked = settings.pricingEnabled,
                onCheckedChange = onPricingChange,
            )
            PreferenceSwitchRow(
                title = "Auto-save scans",
                subtitle = "Add every identified item to your library automatically.",
                checked = settings.autoSaveScans,
                onCheckedChange = onAutoSaveChange,
            )
            PreferenceSwitchRow(
                title = "Haptics",
                subtitle = "Vibrate on detection lock, capture and save.",
                checked = settings.hapticsEnabled,
                onCheckedChange = onHapticsChange,
            )

            HorizontalDivider(color = Hairline)

            // --- Library ---
            SectionTitle("Library")
            ClearLibraryButton(onClearLibrary)

            Spacer(Modifier.height(8.dp))
            Text("LifeLens · local storage only", style = CaptionStyle, color = TextFaint)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = TitleStyle, color = TextPrimary)
}

@Composable
private fun ApiKeyEditor(settings: AppSettings, onSaveKeys: (String, String) -> Unit) {
    var seeded by rememberSaveable { mutableStateOf(false) }
    var dashKey by rememberSaveable { mutableStateOf("") }
    var searchKey by rememberSaveable { mutableStateOf("") }
    var reveal by remember { mutableStateOf(false) }

    // Seed the editable fields from storage the first time real values arrive.
    if (!seeded && (settings.dashScopeApiKey.isNotEmpty() || settings.searchApiKey.isNotEmpty())) {
        dashKey = settings.dashScopeApiKey
        searchKey = settings.searchApiKey
        seeded = true
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        KeyField("DashScope (Qwen) key", dashKey, reveal) { dashKey = it }
        KeyField("Search (Serper) key", searchKey, reveal) { searchKey = it }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LifeLensButton(
                text = "Save keys",
                onClick = { onSaveKeys(dashKey, searchKey); seeded = true },
                modifier = Modifier.weight(1f),
            )
            LifeLensButton(
                text = if (reveal) "Hide" else "Show",
                onClick = { reveal = !reveal },
                type = ButtonType.Secondary,
            )
        }
    }
}

@Composable
private fun KeyField(label: String, value: String, reveal: Boolean, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = LabelStyle, color = TextSecondary)
        Box(
            Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(LifeLensShapes.control)
                .background(Raised)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                Text("Paste key…", style = BodyStyle, color = TextFaint)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = BodyStyle.copy(color = TextPrimary),
                cursorBrush = SolidColor(Amber),
                visualTransformation = if (reveal) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PreferenceSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = BodyStyle, color = TextPrimary)
            Text(subtitle, style = CaptionStyle, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = OnAmber,
                checkedTrackColor = Amber,
                uncheckedTrackColor = Raised2,
            ),
        )
    }
}

@Composable
private fun ClearLibraryButton(onClearLibrary: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    LifeLensButton(
        text = "Clear scan history",
        onClick = { confirm = true },
        type = ButtonType.Destructive,
        modifier = Modifier.fillMaxWidth(),
    )
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Clear scan history?", color = TextPrimary) },
            text = { Text("This permanently deletes all saved scans.", color = TextSecondary) },
            containerColor = Raised,
            confirmButton = {
                TextButton(onClick = { onClearLibrary(); confirm = false }) {
                    Text("Delete all", color = com.lifelen.core.designsystem.theme.Negative)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) { Text("Cancel", color = TextSecondary) }
            },
        )
    }
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> "System"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}
