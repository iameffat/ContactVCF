package com.contacts.vcf.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.ModeNight
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.contacts.vcf.data.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onNavigateBack: () -> Unit) {
    val themeState by viewModel.themeState.collectAsState()
    val countryCode by viewModel.countryCodeState.collectAsState()
    val alwaysAsk by viewModel.alwaysAskState.collectAsState()

    var tempCountryCode by remember(countryCode) { mutableStateOf(countryCode) }
    var tempAlwaysAsk by remember(alwaysAsk) { mutableStateOf(alwaysAsk) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Settings
            Text("Theme", style = MaterialTheme.typography.titleMedium)
            Column(Modifier.selectableGroup()) {
                ThemeRadioButton("System Default", themeState == SettingsManager.THEME_SYSTEM) { viewModel.setTheme(SettingsManager.THEME_SYSTEM) }
                ThemeRadioButton("Light", themeState == SettingsManager.THEME_LIGHT) { viewModel.setTheme(SettingsManager.THEME_LIGHT) }
                ThemeRadioButton("Dark", themeState == SettingsManager.THEME_DARK) { viewModel.setTheme(SettingsManager.THEME_DARK) }
            }

            Divider()

            // WhatsApp Settings
            Text("WhatsApp Country Code", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = tempCountryCode,
                onValueChange = { tempCountryCode = it },
                label = { Text("Default Country Code") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("+880") }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ask every time if code is missing", modifier = Modifier.weight(1f))
                Switch(
                    checked = tempAlwaysAsk,
                    onCheckedChange = { tempAlwaysAsk = it }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.saveCountryCodeSettings(tempCountryCode, tempAlwaysAsk)
                    onNavigateBack() // সেভ করে আগের পেজে ফিরে যাওয়া
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings")
            }
        }
    }
}

@Composable
fun ThemeRadioButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}