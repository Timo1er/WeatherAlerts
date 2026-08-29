package com.example.veryweather.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.example.veryweather.data.network.OpenMeteoApi
import com.example.veryweather.domain.model.HourlyData
import com.example.veryweather.domain.model.LocationModel
import com.example.veryweather.domain.model.WeatherInfo
import com.example.veryweather.domain.repository.WeatherRepository
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject

import android.content.SharedPreferences

class WeatherRepositoryImpl @Inject constructor(
    private val api: OpenMeteoApi,
    private val fusedLocationClient: FusedLocationProviderClient,
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences
) : WeatherRepository {

    override suspend fun getWeather(lat: Double, lon: Double): Result<WeatherInfo> {
        return try {
            val response = api.getWeatherForecast(lat = lat, lon = lon)
            
            // Map to Domain model
            val hourlyList = mutableListOf<HourlyData>()
            // We usually get 48 hours back or 24 hours depending on the API. 
            // We only need the next 24 hours.
            val size = minOf(24, response.hourly.time.size)
            for (i in 0 until size) {
                hourlyList.add(
                    HourlyData(
                        time = response.hourly.time[i],
                        temperature = response.hourly.temperature_2m[i],
                        weatherCode = response.hourly.weather_code[i]
                    )
                )
            }

            val weatherInfo = WeatherInfo(
                currentTemp = response.current.temperature_2m,
                apparentTemp = response.current.apparent_temperature,
                weatherCode = response.current.weather_code,
                isDay = response.current.is_day == 1,
                hourlyForecast = hourlyList
            )
            Result.success(weatherInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentLocation(): Result<LocationModel> {
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCoarseLocationPermission && !hasFineLocationPermission) {
            return Result.success(getDefaultLocation())
        }

        return try {
            val location = fusedLocationClient.lastLocation.await()
            if (location != null) {
                Result.success(
                    LocationModel(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        name = getCityName(location.latitude, location.longitude)
                    )
                )
            } else {
                Result.success(getDefaultLocation())
            }
        } catch (e: Exception) {
            Result.success(getDefaultLocation())
        }
    }

    @Suppress("DEPRECATION")
    private fun getDefaultLocation(): LocationModel {
        val fallbackCity = sharedPreferences.getString("fallback_city", "Paris") ?: "Paris"
        
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(fallbackCity, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                LocationModel(
                    latitude = address.latitude,
                    longitude = address.longitude,
                    name = address.locality ?: fallbackCity
                )
            } else {
                LocationModel(48.8566, 2.3522, "Paris")
            }
        } catch (e: Exception) {
            LocationModel(48.8566, 2.3522, fallbackCity)
        }
    }

    @Suppress("DEPRECATION")
    private fun getCityName(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (addresses?.isNotEmpty() == true) {
                addresses[0].locality ?: "Unknown"
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
