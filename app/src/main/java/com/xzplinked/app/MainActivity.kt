package com.xzplinked.app.ui

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.content.pm.PackageManager
import com.xzplinked.app.R
import com.xzplinked.app.databinding.ActivityMainBinding
import com.xzplinked.app.ui.fragments.DownloadsFragment
import com.xzplinked.app.ui.fragments.PlayerFragment
import com.xzplinked.app.ui.fragments.SettingsFragment
import com.xzplinked.app.viewmodel.MainViewModel
import com.xzplinked.app.service.AudioPlaybackService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewModel
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // Aplicar tema guardado
        applyTheme()

        // Configurar ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Solicitar permisos necesarios
        requestRequiredPermissions()

        // Configurar Bottom Navigation
        setupBottomNavigation()

        // Iniciar servicio de reproducción de audio
        startAudioService()

        // Observar cambios de tema
        viewModel.currentTheme.observe(this) { theme ->
            applyTheme()
        }
    }

    private fun applyTheme() {
        val theme = viewModel.getCurrentTheme()
        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_downloads -> DownloadsFragment()
                R.id.nav_player -> PlayerFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> DownloadsFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()

            true
        }

        // Cargar fragmento inicial
        if (supportFragmentManager.fragments.isEmpty()) {
            binding.bottomNavigation.selectedItemId = R.id.nav_downloads
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.POST_NOTIFICATIONS
        )

        // Agregar permisos específicos de Android 11+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        }

        // Agregar permisos de Foreground Service (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE)
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (!allGranted) {
                // Mostrar mensaje de error
                showPermissionError()
            }
        }
    }

    private fun showPermissionError() {
        // Implementar diálogo de error de permisos
        android.app.AlertDialog.Builder(this)
            .setTitle("Permisos Requeridos")
            .setMessage("XZP Linked necesita permisos para funcionar correctamente")
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    private fun startAudioService() {
        val intent = Intent(this, AudioPlaybackService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Manejar cambios de configuración (rotación, cambio de tema del sistema)
        applyTheme()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detener servicio de audio si es necesario
        stopService(Intent(this, AudioPlaybackService::class.java))
    }
}
