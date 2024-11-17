package com.example.geolocation

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding

class MainActivity : ComponentActivity() {

    private lateinit var locationManager: LocationManager
    private lateinit var locationText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаем TextView программно
        locationText = TextView(this).apply {
            text = "Waiting for location..."
            textSize = 25f
            setPadding(20)
        }

        // Устанавливаем TextView в качестве контента
        setContentView(locationText)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        // Проверка разрешений
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocationUpdates()
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getLocationUpdates()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun getLocationUpdates() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS отключен. Включите GPS для работы приложения.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (lastKnownLocation != null) {
                updateLocation(lastKnownLocation)
            } else {
                locationText.text = "Нет данных о предыдущем местоположении."
            }

            // Запрос обновлений GPS
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2000L, // Интервал обновления
                0f,    // Минимальное расстояние
                locationListener
            )

            // Резервный запрос для сети
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                2000L,
                0f,
                locationListener
            )
        } catch (e: SecurityException) {
            Toast.makeText(this, "Нет разрешений на доступ к геолокации.", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, "Провайдер недоступен.", Toast.LENGTH_SHORT).show()
        }
    }


    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            updateLocation(location)
        }

        override fun onProviderEnabled(provider: String) {
            Toast.makeText(this@MainActivity, "GPS Enabled", Toast.LENGTH_SHORT).show()
        }

        override fun onProviderDisabled(provider: String) {
            Toast.makeText(this@MainActivity, "GPS Disabled", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLocation(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        locationText.text = "Ваше местоположение:\nШирота: $latitude\nДолгота: $longitude"
    }

    override fun onDestroy() {
        super.onDestroy()
        // Остановка получения обновлений для предотвращения утечек памяти
        locationManager.removeUpdates(locationListener)
    }
}
