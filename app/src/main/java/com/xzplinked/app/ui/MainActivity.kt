package com.xzplinked.app.ui

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.content.pm.PackageManager
import com.xzplinked.app.R
import com.xzplinked.app.databinding.ActivityMainBinding
import com.xzplinked.app.model.Track
import com.xzplinked.app.ui.fragments.DownloadsFragment
import com.xzplinked.app.ui.fragments.PlayerFragment
import com.xzplinked.app.ui.fragments.SettingsFragment
import com.xzplinked.app.viewmodel.MainViewModel
import com.xzplinked.app.service.AudioPlaybackService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private val PERMISSION_REQUEST_CODE = 100

    private val progressFilter = android.content.IntentFilter("com.xzplinked.app.PLAYER_PROGRESS")
    private val progressReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            val progress = intent?.getIntExtra("progress", 0) ?: 0
            viewModel.updatePlayerProgress(progress)
        }
    }

    private var currentAppliedTheme = -1
    private var currentAppliedNightMode = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        // Inicializar ViewModel
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // Aplicar tema y color de acento
        applyTheme()

        super.onCreate(savedInstanceState)

        // Configurar ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Solicitar permisos necesarios
        requestRequiredPermissions()

        // Configurar Bottom Navigation
        setupBottomNavigation()

        // Configurar Mini Player
        setupMiniPlayer()

        // Observar cambios de tema y color
        viewModel.currentTheme.observe(this) { theme ->
            if (getNightMode(theme) != currentAppliedNightMode) {
                recreate()
            }
        }
        viewModel.accentColor.observe(this) { color ->
            if (getThemeId(color) != currentAppliedTheme) {
                recreate()
            }
        }
    }

    private fun getNightMode(themeMode: String): Int {
        return when (themeMode) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    private fun getThemeId(accentColor: String?): Int {
        return when (accentColor) {
            "lilac" -> R.style.Theme_XZPLinked_Lilac
            "blue" -> R.style.Theme_XZPLinked_Blue
            "cream" -> R.style.Theme_XZPLinked_Cream
            "peach" -> R.style.Theme_XZPLinked_Peach
            "yellow" -> R.style.Theme_XZPLinked_Yellow
            "rose" -> R.style.Theme_XZPLinked_Rose
            "sage" -> R.style.Theme_XZPLinked_Sage
            "sky" -> R.style.Theme_XZPLinked_Sky
            "lavender" -> R.style.Theme_XZPLinked_Lavender
            else -> R.style.Theme_XZPLinked
        }
    }

    private fun applyTheme() {
        val themeMode = viewModel.getCurrentTheme()
        currentAppliedNightMode = getNightMode(themeMode)
        AppCompatDelegate.setDefaultNightMode(currentAppliedNightMode)

        val accentColor = viewModel.accentColor.value ?: "mint"
        currentAppliedTheme = getThemeId(accentColor)
        setTheme(currentAppliedTheme)
    }

    private fun setupMiniPlayer() {
        viewModel.currentTrack.observe(this) { track ->
            if (track != null) {
                binding.miniPlayer.visibility = View.VISIBLE
                binding.miniTitle.text = track.title
                binding.miniArtist.text = track.artist
                // Aquí podrías cargar la carátula si existiera
            } else {
                binding.miniPlayer.visibility = View.GONE
            }
        }

        viewModel.isPlaying.observe(this) { isPlaying ->
            binding.miniPlay.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        }
        
        viewModel.playerProgress.observe(this) { progress ->
            binding.miniProgress.progress = progress
        }

        binding.miniPlay.setOnClickListener {
            togglePlayPause()
        }

        binding.miniNext.setOnClickListener {
            playNext()
        }

        binding.miniPrev.setOnClickListener {
            playPrev()
        }

        // Registrar receptor de estado
        val stateFilter = android.content.IntentFilter("com.xzplinked.app.PLAYER_STATE")
        val stateReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
                val isPlaying = intent?.getBooleanExtra("is_playing", false) ?: false
                val filePath = intent?.getStringExtra("file_path")
                
                viewModel.setIsPlaying(isPlaying)
                filePath?.let { path ->
                    viewModel.getTrackByPath(path)?.let { track ->
                        viewModel.setCurrentTrack(track)
                    }
                }
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(progressReceiver, progressFilter, Context.RECEIVER_NOT_EXPORTED)
            registerReceiver(stateReceiver, stateFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(progressReceiver, progressFilter)
            registerReceiver(stateReceiver, stateFilter)
        }
    }

    private fun togglePlayPause() {
        val intent = Intent(this, AudioPlaybackService::class.java)
        intent.action = if (viewModel.isPlaying.value == true) {
            AudioPlaybackService.ACTION_PAUSE
        } else {
            AudioPlaybackService.ACTION_RESUME
        }
        startService(intent)
    }

    private fun playNext() {
        val intent = Intent(this, AudioPlaybackService::class.java)
        intent.action = AudioPlaybackService.ACTION_NEXT
        startService(intent)
    }

    private fun playPrev() {
        val intent = Intent(this, AudioPlaybackService::class.java)
        intent.action = AudioPlaybackService.ACTION_PREV
        startService(intent)
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_downloads -> DownloadsFragment()
                R.id.nav_player -> PlayerFragment()
                R.id.nav_settings -> {
                    binding.miniPlayer.visibility = View.GONE
                    SettingsFragment()
                }
                else -> DownloadsFragment()
            }

            if (item.itemId != R.id.nav_settings && viewModel.currentTrack.value != null) {
                binding.miniPlayer.visibility = View.VISIBLE
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()

            true
        }

        // Cargar fragmento inicial
        if (supportFragmentManager.fragments.isEmpty()) {
            binding.bottomNavigation.selectedItemId = R.id.nav_downloads
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf<String>()
        
        permissions.add(Manifest.permission.INTERNET)
        permissions.add(Manifest.permission.MODIFY_AUDIO_SETTINGS)

        // Permisos de Notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Permisos de Almacenamiento
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        // Permisos de Foreground Service (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE)
        }
        
        // Android 14+ requiere tipos específicos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK)
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
            if (allGranted) {
                viewModel.refreshTracks()
            } else {
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
