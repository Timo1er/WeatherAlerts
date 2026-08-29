package com.example.veryweather.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.veryweather.domain.model.HourlyData
import com.example.veryweather.domain.model.WeatherInfo
import com.example.veryweather.domain.usecase.AlertResult
import com.example.veryweather.theme.FrostBlue
import com.example.veryweather.theme.HeatwaveRed
import com.example.veryweather.ui.viewmodel.WeatherUiState
import com.example.veryweather.ui.viewmodel.WeatherViewModel

@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            is WeatherUiState.Loading -> {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is WeatherUiState.Success -> {
                WeatherContent(
                    weatherInfo = state.weatherInfo,
                    alertResult = state.alertResult,
                    useFahrenheit = state.useFahrenheit,
                    onRefresh = { viewModel.loadWeather() }
                )
            }
            is WeatherUiState.Error -> {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadWeather() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

fun formatTemp(celsius: Double, useFahrenheit: Boolean): String {
    return if (useFahrenheit) {
        "${((celsius * 9 / 5) + 32).toInt()}°F"
    } else {
        "${celsius.toInt()}°C"
    }
}

@Composable
fun WeatherContent(
    weatherInfo: WeatherInfo,
    alertResult: AlertResult,
    useFahrenheit: Boolean,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AlertBanner(alertResult, useFahrenheit)
        Spacer(modifier = Modifier.height(16.dp))
        
        CurrentWeatherCard(weatherInfo, useFahrenheit)
        
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "24-Hour Forecast",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        HourlyForecastRow(weatherInfo.hourlyForecast, useFahrenheit)
        
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onRefresh) {
            Text("Refresh")
        }
    }
}

@Composable
fun AlertBanner(alertResult: AlertResult, useFahrenheit: Boolean) {
    if (alertResult is AlertResult.None) return

    val (backgroundColor, text, icon) = when (alertResult) {
        is AlertResult.FrostAlert -> Triple(FrostBlue, "Frost Alert: ${formatTemp(alertResult.minTemp, useFahrenheit)}", "❄️")
        is AlertResult.HeatwaveAlert -> Triple(HeatwaveRed, "Heatwave Alert: ${formatTemp(alertResult.maxTemp, useFahrenheit)}", "☀️")
        else -> Triple(MaterialTheme.colorScheme.surface, "", "")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun CurrentWeatherCard(weatherInfo: WeatherInfo, useFahrenheit: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val icon = if (weatherInfo.isDay) "☀️" else "🌙"
            Text(text = icon, fontSize = 64.sp)
            Spacer(modifier = Modifier.height(8.dp))
            if (weatherInfo.cityName.isNotEmpty()) {
                Text(
                    text = weatherInfo.cityName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = formatTemp(weatherInfo.currentTemp, useFahrenheit),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Feels like ${formatTemp(weatherInfo.apparentTemp, useFahrenheit)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun HourlyForecastRow(forecasts: List<HourlyData>, useFahrenheit: Boolean) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(forecasts.take(24)) { data ->
            HourlyForecastItem(data, useFahrenheit)
        }
    }
}

@Composable
fun HourlyForecastItem(data: HourlyData, useFahrenheit: Boolean) {
    // time format is "yyyy-mm-ddThh:mm" from OpenMeteo
    val timeString = data.time.substringAfter("T")
    
    Card(
        modifier = Modifier.width(80.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = timeString, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            // Simplified icon logic
            val icon = if (data.weatherCode < 3) "🌤" else if (data.weatherCode < 60) "☁️" else "🌧"
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatTemp(data.temperature, useFahrenheit),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
