package com.xzplinked.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.xzplinked.app.R
import com.xzplinked.app.ui.MainActivity
import com.xzplinked.app.receiver.NotificationReceiver
import java.io.File

class AudioPlaybackService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var isPlaying = false
    private var currentFilePath: String? = null

    private val notificationId = 1
    private val channelId = "xzp_playback_channel"

    private lateinit var notificationReceiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        createNotificationChannel()
        setupMediaPlayer()
        setupBroadcastReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> playAudio(intent.getStringExtra("file_path"))
            ACTION_PAUSE -> pauseAudio()
            ACTION_RESUME -> resumeAudio()
            ACTION_STOP -> stopAudio()
            ACTION_NEXT -> nextTrack()
            ACTION_PREV -> previousTrack()
        }

        return START_STICKY
    }

    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnCompletionListener {
                nextTrack()
            }
            setOnErrorListener { _, what, extra ->
                android.util.Log.e("AudioPlayback", "Error: $what, $extra")
                false
            }
        }
    }

    private fun setupBroadcastReceiver() {
        notificationReceiver = NotificationReceiver()
        val filter = IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
            addAction(ACTION_RESUME)
            addAction(ACTION_STOP)
            addAction(ACTION_NEXT)
            addAction(ACTION_PREV)
        }
        registerReceiver(notificationReceiver, filter)
    }

    private fun playAudio(filePath: String?) {
        if (filePath == null || !File(filePath).exists()) {
            return
        }

        try {
            requestAudioFocus()

            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                    reset()
                }
                setDataSource(filePath)
                prepare()
                start()
            }

            currentFilePath = filePath
            isPlaying = true
            updateNotification(true)
        } catch (e: Exception) {
            android.util.Log.e("AudioPlayback", "Error playing audio", e)
        }
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()
        isPlaying = false
        updateNotification(false)
    }

    private fun resumeAudio() {
        mediaPlayer?.start()
        isPlaying = true
        updateNotification(true)
    }

    private fun stopAudio() {
        mediaPlayer?.apply {
            stop()
            reset()
        }
        isPlaying = false
        currentFilePath = null
        abandonAudioFocus()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun nextTrack() {
        // Implementar lógica para ir al siguiente track
        // Esto será conectado con el ViewModel
    }

    private fun previousTrack() {
        // Implementar lógica para ir al track anterior
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_GAIN -> resumeAudio()
                        AudioManager.AUDIOFOCUS_LOSS -> stopAudio()
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pauseAudio()
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                            mediaPlayer?.setVolume(0.3f, 0.3f)
                        }
                    }
                }
                .build()

            audioManager?.requestAudioFocus(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_GAIN -> resumeAudio()
                        AudioManager.AUDIOFOCUS_LOSS -> stopAudio()
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pauseAudio()
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                            mediaPlayer?.setVolume(0.3f, 0.3f)
                        }
                    }
                },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
            audioManager?.abandonAudioFocusRequest(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus(null)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Reproducción de Música",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones de reproducción de XZP Linked"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(isPlaying: Boolean) {
        val notification = buildNotification(isPlaying)
        startForeground(notificationId, notification)
    }

    private fun buildNotification(isPlaying: Boolean): Notification {
        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action(
                R.drawable.ic_pause,
                "Pausar",
                getPendingIntent(ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_play,
                "Reproducir",
                getPendingIntent(ACTION_RESUME)
            )
        }

        val mainIntent = Intent(this, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("XZP Linked")
            .setContentText(currentFilePath?.let { File(it).name } ?: "Sin reproducción")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(mainPendingIntent)
            .addAction(
                R.drawable.ic_prev,
                "Anterior",
                getPendingIntent(ACTION_PREV)
            )
            .addAction(playPauseAction)
            .addAction(
                R.drawable.ic_next,
                "Siguiente",
                getPendingIntent(ACTION_NEXT)
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setOngoing(true)
            .build()
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        unregisterReceiver(notificationReceiver)
        abandonAudioFocus()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_PLAY = "com.xzplinked.app.PLAY"
        const val ACTION_PAUSE = "com.xzplinked.app.PAUSE"
        const val ACTION_RESUME = "com.xzplinked.app.RESUME"
        const val ACTION_STOP = "com.xzplinked.app.STOP"
        const val ACTION_NEXT = "com.xzplinked.app.NEXT"
        const val ACTION_PREV = "com.xzplinked.app.PREV"
    }
}
