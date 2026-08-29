package com.example.veryweather.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.veryweather.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val heatwaveThreshold by viewModel.heatwaveThreshold.collectAsState()
    val coldThreshold by viewModel.coldThreshold.collectAsState()
    val fallbackCity by viewModel.fallbackCity.collectAsState()
    val useFahrenheit by viewModel.useFahrenheit.collectAsState()

    val formatTemp: (Float) -> String = { tempCelsius ->
        if (useFahrenheit) {
            "${((tempCelsius * 9f / 5f) + 32f).toInt()}°F"
        } else {
            "${tempCelsius.toInt()}°C"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use Fahrenheit", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Switch(
                    checked = useFahrenheit,
                    onCheckedChange = { viewModel.updateUseFahrenheit(it) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Text("Heatwave Threshold", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = formatTemp(heatwaveThreshold))
            Slider(
                value = heatwaveThreshold,
                onValueChange = { viewModel.updateHeatwaveThreshold(it) },
                valueRange = 20f..50f,
                steps = 29
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Cold Threshold", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = formatTemp(coldThreshold))
            Slider(
                value = coldThreshold,
                onValueChange = { viewModel.updateColdThreshold(it) },
                valueRange = -10f..20f,
                steps = 29
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Fallback City", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = fallbackCity,
                onValueChange = { viewModel.updateFallbackCity(it) },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = {
                viewModel.saveThresholds()
                onNavigateBack()
            }) {
                Text("Save and Back")
            }
        }
    }
}
