package com.xzplinked.app.viewmodel

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.xzplinked.app.model.Track
import com.xzplinked.app.model.DownloadItem

import android.app.Application
import android.content.ContentUris
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

    // ... (LiveData declarations remain the same)
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
        refreshTracks()
    }

    fun refreshTracks() {
        val trackList = mutableListOf<Track>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DISPLAY_NAME
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

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
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
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
        _tracks.postValue(trackList)
    }

    private fun loadTracks() {
        refreshTracks()
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
