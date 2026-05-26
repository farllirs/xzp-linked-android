package com.xzplinked.app.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.xzplinked.app.model.Track
import com.xzplinked.app.model.DownloadItem
import java.util.concurrent.TimeUnit

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

    // Tracks en biblioteca
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
        refreshTracks()
    }

    private fun loadPreferences() {
        val theme = prefs.getString("theme", "system") ?: "system"
        _currentTheme.value = theme

        val accentColor = prefs.getString("accent_color", "mint") ?: "mint"
        _accentColor.value = accentColor

        val path = prefs.getString("download_path", "/storage/emulated/0/Download/XZPLinked") ?: "/storage/emulated/0/Download/XZPLinked"
        _downloadPath.value = path
    }

    private fun loadDownloads() {
        // En una app real, esto cargaría de una DB. Por ahora lista vacía persistente en sesión.
        _downloads.value = emptyList()
    }

    fun refreshTracks() {
        val trackList = mutableListOf<Track>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        try {
            getApplication<Application>().contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Título Desconocido"
                    val artist = cursor.getString(artistColumn) ?: "Artista Desconocido"
                    val durationMs = cursor.getLong(durationColumn)
                    val data = cursor.getString(dataColumn)
                    val size = cursor.getLong(sizeColumn)

                    val duration = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(durationMs),
                        TimeUnit.MILLISECONDS.toSeconds(durationMs) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationMs))
                    )

                    trackList.add(Track(
                        id = id.toString(),
                        title = title,
                        artist = artist,
                        duration = duration,
                        filePath = data,
                        format = data.substringAfterLast(".", "mp3"),
                        size = size,
                        folder = data.substringBeforeLast("/", "Desconocido")
                    ))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "Error scanning tracks", e)
        }
        _tracks.postValue(trackList)
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

    fun addDownload(item: DownloadItem) {
        val currentList = _downloads.value?.toMutableList() ?: mutableListOf()
        currentList.add(0, item)
        _downloads.postValue(currentList)
    }

    fun updateDownloadProgress(progress: Int) {
        _downloadProgress.postValue(progress)
    }

    fun updatePlayerProgress(progress: Int) {
        _playerProgress.postValue(progress)
    }

    fun setCurrentTrack(track: Track) {
        _currentTrack.postValue(track)
    }

    fun setIsPlaying(playing: Boolean) {
        _isPlaying.postValue(playing)
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
