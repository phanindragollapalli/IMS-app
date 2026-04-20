package com.example.ims_app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ims_app.data.AppCurrency
import com.example.ims_app.data.AppLanguage
import com.example.ims_app.data.AppTimeZone
import com.example.ims_app.data.DemoRepository
import com.example.ims_app.data.SessionManager
import com.example.ims_app.data.TermType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(repository: DemoRepository) {
    val context = LocalContext.current
    val sessionManager = remember(context) { SessionManager(context.applicationContext) }

    val localization = repository.localizationSettings
    val general = repository.generalSettings
    val canEditAdminOnly = repository.canEditAdminOnlyGeneralSettings()

    // Local copies for the text field (attendance threshold) to avoid recompose loops
    var thresholdText by remember(general.defaultAttendanceThreshold) {
        mutableStateOf(general.defaultAttendanceThreshold.toString())
    }

    fun saveLocalization(updated: com.example.ims_app.data.UserLocalizationSettings) {
        repository.updateLocalizationSettings(updated)
        repository.currentUser?.username?.let { username ->
            sessionManager.saveUserLocalizationSettings(username, updated)
        }
    }

    fun saveGeneral(updated: com.example.ims_app.data.GeneralSettings) {
        repository.updateGeneralSettings(updated)
        sessionManager.saveGeneralSettings(updated)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Localization ─────────────────────────────────────────────────
        item {
            SettingsSectionCard(title = "Language and region") {
                SettingsDropdownField(
                    label = "Language",
                    selectedOption = localization.language,
                    options = AppLanguage.values().toList(),
                    optionLabel = { it.label },
                    onOptionSelected = { saveLocalization(localization.copy(language = it)) }
                )

                OutlinedTextField(
                    value = localization.country,
                    onValueChange = { saveLocalization(localization.copy(country = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Country") },
                    singleLine = true
                )

                SettingsDropdownField(
                    label = "Currency",
                    selectedOption = localization.currency,
                    options = AppCurrency.values().toList(),
                    optionLabel = { it.label },
                    onOptionSelected = { saveLocalization(localization.copy(currency = it)) }
                )

                SettingsDropdownField(
                    label = "Time zone",
                    selectedOption = localization.timeZone,
                    options = AppTimeZone.values().toList(),
                    optionLabel = { it.label },
                    onOptionSelected = { saveLocalization(localization.copy(timeZone = it)) }
                )
            }
        }

        // ── Academic ─────────────────────────────────────────────────────
        item {
            SettingsSectionCard(title = "Academic") {
                SettingsDropdownField(
                    label = "Term type",
                    selectedOption = general.termType,
                    options = TermType.values().toList(),
                    optionLabel = { it.label },
                    onOptionSelected = { saveGeneral(general.copy(termType = it)) },
                    enabled = canEditAdminOnly
                )

                OutlinedTextField(
                    value = thresholdText,
                    onValueChange = { typed ->
                        thresholdText = typed
                        typed.toIntOrNull()?.let { value ->
                            saveGeneral(general.copy(defaultAttendanceThreshold = value))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Default attendance threshold (%)") },
                    singleLine = true,
                    enabled = canEditAdminOnly
                )

                if (!canEditAdminOnly) {
                    Text(
                        "Only admin can edit term type and attendance threshold.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionCard(title: String, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SettingsDropdownField(
    label: String,
    selectedOption: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }

    androidx.compose.material3.ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = optionLabel(selectedOption),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            label = { Text(label) },
            trailingIcon = {
                androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            singleLine = true
        )
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
