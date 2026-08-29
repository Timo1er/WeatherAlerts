package com.example.veryweather.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.veryweather.domain.model.WeatherInfo
import com.example.veryweather.domain.usecase.AlertResult
import com.example.veryweather.domain.usecase.CheckAlertsUseCase
import com.example.veryweather.domain.usecase.GetWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(
        val weatherInfo: WeatherInfo,
        val alertResult: AlertResult,
        val useFahrenheit: Boolean
    ) : WeatherUiState()

    data class Error(val message: String) : WeatherUiState()
}

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getWeatherUseCase: GetWeatherUseCase,
    private val checkAlertsUseCase: CheckAlertsUseCase,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        loadWeather()
    }

    fun loadWeather() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            val result = getWeatherUseCase.execute()
            if (result.isFailure) {
                _uiState.value = WeatherUiState.Error(
                    result.exceptionOrNull()?.message ?: "Unable to load weather"
                )
                return@launch
            }

            val weatherInfo = result.getOrNull() ?: run {
                _uiState.value = WeatherUiState.Error("Unable to load weather")
                return@launch
            }

            val heatwaveThreshold = sharedPreferences.getFloat("heatwave_threshold", 35f).toDouble()
            val coldThreshold = sharedPreferences.getFloat("cold_threshold", 0f).toDouble()
            val alertResult = checkAlertsUseCase.execute(
                weatherInfo = weatherInfo,
                heatwaveThreshold = heatwaveThreshold,
                coldThreshold = coldThreshold
            )

            val currentTemp = weatherInfo.currentTemp
            val tempIn6Hours = weatherInfo.hourlyForecast.getOrNull(6)?.temperature
            
            val alertTypeCurrent = when {
                currentTemp <= coldThreshold -> "FROST"
                currentTemp >= heatwaveThreshold -> "HEATWAVE"
                else -> "NONE"
            }
            
            val alertType6h = when {
                tempIn6Hours == null -> "NONE"
                tempIn6Hours <= coldThreshold -> "FROST"
                tempIn6Hours >= heatwaveThreshold -> "HEATWAVE"
                else -> "NONE"
            }
            
            sharedPreferences.edit()
                .putFloat("widget_temp", currentTemp.toFloat())
                .putFloat("widget_temp_6h", tempIn6Hours?.toFloat() ?: -999f)
                .putString("widget_alert_type_current", alertTypeCurrent)
                .putString("widget_alert_type_6h", alertType6h)
                .putString("widget_city_name", weatherInfo.cityName.ifBlank { "Unknown" })
                .apply()

            val useFahrenheit = sharedPreferences.getBoolean("use_fahrenheit", false)

            _uiState.value = WeatherUiState.Success(
                weatherInfo = weatherInfo,
                alertResult = alertResult,
                useFahrenheit = useFahrenheit
            )
        }
    }
}
