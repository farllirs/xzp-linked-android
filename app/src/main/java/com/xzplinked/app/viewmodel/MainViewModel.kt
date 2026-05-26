package com.xzplinked.app.viewmodel

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.xzplinked.app.model.Track
import com.xzplinked.app.model.DownloadItem

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(application)

    // Tema
    private val _currentTheme = MutableLiveData<String>()
    val currentTheme: LiveData<String> = _currentTheme

    // Color de acento
    private val _accentColor = MutableLiveData<String>()
    val accentColor: LiveData<String> = _accentColor

    // Descargas
    private val _downloads = MutableLiveData<List<DownloadItem>>()
    val downloads: LiveData<List<DownloadItem>> = _downloads

    // Tracks en reproducción
    private val _tracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>> = _tracks

    // Track actual
    private val _currentTrack = MutableLiveData<Track?>()
    val currentTrack: LiveData<Track?> = _currentTrack

    // Estado de reproducción
    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    // Progreso de descarga
    private val _downloadProgress = MutableLiveData<Int>(0)
    val downloadProgress: LiveData<Int> = _downloadProgress

    // Progreso de reproducción
    private val _playerProgress = MutableLiveData<Int>(0)
    val playerProgress: LiveData<Int> = _playerProgress

    // Ruta de descarga
    private val _downloadPath = MutableLiveData<String>()
    val downloadPath: LiveData<String> = _downloadPath

    init {
        loadPreferences()
        loadDownloads()
        loadTracks()
    }

    fun updatePlayerProgress(progress: Int) {
        _playerProgress.postValue(progress)
    }

    private fun loadPreferences() {
        val theme = prefs.getString("theme", "system") ?: "system"
        _currentTheme.value = theme

        val accentColor = prefs.getString("accent_color", "mint") ?: "mint"
        _accentColor.value = accentColor

        val downloadPath = prefs.getString("download_path", "/storage/emulated/0/Download/XZPLinked") ?: "/storage/emulated/0/Download/XZPLinked"
        _downloadPath.value = downloadPath
    }

    fun setTheme(theme: String) {
        _currentTheme.value = theme
        prefs.edit().putString("theme", theme).apply()
    }

    fun getCurrentTheme(): String {
        return prefs.getString("theme", "system") ?: "system"
    }

    fun setAccentColor(color: String) {
        _accentColor.value = color
        prefs.edit().putString("accent_color", color).apply()
    }

    fun setDownloadPath(path: String) {
        _downloadPath.value = path
        prefs.edit().putString("download_path", path).apply()
    }

    fun updateDownloadProgress(progress: Int) {
        _downloadProgress.value = progress
    }

    fun addDownload(item: DownloadItem) {
        val currentList = _downloads.value?.toMutableList() ?: mutableListOf()
        currentList.add(0, item)
        _downloads.value = currentList
    }

    fun setCurrentTrack(track: Track) {
        _currentTrack.value = track
    }

    fun setIsPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    private fun loadDownloads() {
        // Cargar descargas desde base de datos o almacenamiento
        _downloads.value = emptyList()
    }

    private fun loadTracks() {
        // Cargar tracks desde almacenamiento local
        _tracks.value = emptyList()
    }

    fun getTrackById(id: String): Track? {
        return _tracks.value?.find { it.id == id }
    }

    fun getTrackByPath(path: String): Track? {
        return _tracks.value?.find { it.filePath == path }
    }

    fun getAllTracks(): List<Track> {
        return _tracks.value ?: emptyList()
    }

    fun getDownloadsByFolder(folder: String): List<DownloadItem> {
        return _downloads.value?.filter { it.folder == folder } ?: emptyList()
    }
}
