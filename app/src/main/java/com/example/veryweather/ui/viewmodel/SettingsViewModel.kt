package com.example.veryweather.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _heatwaveThreshold = MutableStateFlow(
        sharedPreferences.getFloat("heatwave_threshold", 35f)
    )
    val heatwaveThreshold: StateFlow<Float> = _heatwaveThreshold.asStateFlow()

    private val _coldThreshold = MutableStateFlow(
        sharedPreferences.getFloat("cold_threshold", 0f)
    )
    val coldThreshold: StateFlow<Float> = _coldThreshold.asStateFlow()

    private val _fallbackCity = MutableStateFlow(
        sharedPreferences.getString("fallback_city", "Paris") ?: "Paris"
    )
    val fallbackCity: StateFlow<String> = _fallbackCity.asStateFlow()

    private val _useFahrenheit = MutableStateFlow(
        sharedPreferences.getBoolean("use_fahrenheit", false)
    )
    val useFahrenheit: StateFlow<Boolean> = _useFahrenheit.asStateFlow()

    fun updateHeatwaveThreshold(newThreshold: Float) {
        _heatwaveThreshold.value = newThreshold
    }

    fun updateColdThreshold(newThreshold: Float) {
        _coldThreshold.value = newThreshold
    }

    fun updateFallbackCity(city: String) {
        _fallbackCity.value = city
    }

    fun updateUseFahrenheit(useFahrenheit: Boolean) {
        _useFahrenheit.value = useFahrenheit
    }

    fun saveThresholds() {
        sharedPreferences.edit()
            .putFloat("heatwave_threshold", _heatwaveThreshold.value)
            .putFloat("cold_threshold", _coldThreshold.value)
            .putString("fallback_city", _fallbackCity.value.ifBlank { "Paris" })
            .putBoolean("use_fahrenheit", _useFahrenheit.value)
            .apply()
    }
}
