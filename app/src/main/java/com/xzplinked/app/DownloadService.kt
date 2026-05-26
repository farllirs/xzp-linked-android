package com.xzplinked.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.xzplinked.app.R
import com.xzplinked.app.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLConnection

class DownloadService : Service() {

    private val notificationId = 2
    private val channelId = "xzp_download_channel"
    private var currentDownloadId: String? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra("url") ?: return START_NOT_STICKY
        val fileName = intent.getStringExtra("file_name") ?: "download_${System.currentTimeMillis()}"
        val downloadPath = intent.getStringExtra("download_path") ?: "/storage/emulated/0/Download/XZPLinked"
        val downloadId = intent.getStringExtra("download_id") ?: System.currentTimeMillis().toString()

        currentDownloadId = downloadId

        GlobalScope.launch(Dispatchers.IO) {
            downloadFile(url, fileName, downloadPath, downloadId)
        }

        return START_STICKY
    }

    private fun downloadFile(url: String, fileName: String, downloadPath: String, downloadId: String) {
        try {
            val downloadDir = File(downloadPath)
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }

            val file = File(downloadDir, fileName)
            val connection = URL(url).openConnection() as URLConnection
            connection.connect()

            val fileLength = connection.contentLength
            val inputStream = connection.getInputStream()
            val outputStream = FileOutputStream(file)

            val buffer = ByteArray(4096)
            var bytesRead: Int
            var totalBytesRead = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                val progress = if (fileLength > 0) {
                    ((totalBytesRead * 100) / fileLength).toInt()
                } else {
                    0
                }

                updateNotification(progress, fileName)

                // Enviar broadcast con el progreso
                val progressIntent = Intent("com.xzplinked.app.DOWNLOAD_PROGRESS").apply {
                    putExtra("download_id", downloadId)
                    putExtra("progress", progress)
                }
                sendBroadcast(progressIntent)
            }

            inputStream.close()
            outputStream.close()

            // Notificar descarga completada
            val completeIntent = Intent("com.xzplinked.app.DOWNLOAD_COMPLETE").apply {
                putExtra("download_id", downloadId)
                putExtra("file_path", file.absolutePath)
                putExtra("file_name", fileName)
            }
            sendBroadcast(completeIntent)

            updateNotification(100, fileName)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()

        } catch (e: Exception) {
            android.util.Log.e("DownloadService", "Error downloading file", e)

            val errorIntent = Intent("com.xzplinked.app.DOWNLOAD_ERROR").apply {
                putExtra("download_id", downloadId)
                putExtra("error", e.message)
            }
            sendBroadcast(errorIntent)

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Descargas",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones de descarga de XZP Linked"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(progress: Int, fileName: String) {
        val mainIntent = Intent(this, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Descargando...")
            .setContentText(fileName)
            .setSmallIcon(R.drawable.ic_notification)
            .setProgress(100, progress, false)
            .setContentIntent(mainPendingIntent)
            .setOngoing(true)
            .build()

        startForeground(notificationId, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_URL = "url"
        const val EXTRA_FILE_NAME = "file_name"
        const val EXTRA_DOWNLOAD_PATH = "download_path"
        const val EXTRA_DOWNLOAD_ID = "download_id"
    }
}
