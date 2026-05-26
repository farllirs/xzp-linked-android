package com.xzplinked.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.documentfile.provider.DocumentFile
import com.xzplinked.app.R
import com.xzplinked.app.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
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
        val url = intent?.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
        val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: "download_${System.currentTimeMillis()}"
        val downloadPath = intent.getStringExtra(EXTRA_DOWNLOAD_PATH) ?: "/storage/emulated/0/Download/XZPLinked"
        val downloadId = intent.getStringExtra(EXTRA_DOWNLOAD_ID) ?: System.currentTimeMillis().toString()

        currentDownloadId = downloadId

        GlobalScope.launch(Dispatchers.IO) {
            downloadFile(url, fileName, downloadPath, downloadId)
        }

        return START_STICKY
    }

    private fun downloadFile(url: String, fileName: String, downloadPath: String, downloadId: String) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            val connection = URL(url).openConnection() as URLConnection
            connection.connect()
            val fileLength = connection.contentLength
            inputStream = connection.getInputStream()

            if (downloadPath.startsWith("content://")) {
                val treeUri = Uri.parse(downloadPath)
                val pickedDir = DocumentFile.fromTreeUri(this, treeUri)
                val file = pickedDir?.createFile(if (fileName.endsWith(".mp4")) "video/mp4" else "audio/mpeg", fileName)
                outputStream = file?.let { contentResolver.openOutputStream(it.uri) }
            } else {
                val downloadDir = File(downloadPath)
                if (!downloadDir.exists()) {
                    downloadDir.mkdirs()
                }
                val file = File(downloadDir, fileName)
                outputStream = FileOutputStream(file)
            }

            if (outputStream == null) throw Exception("No se pudo crear el archivo de salida")

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

                val progressIntent = Intent("com.xzplinked.app.DOWNLOAD_PROGRESS").apply {
                    putExtra("download_id", downloadId)
                    putExtra("progress", progress)
                }
                sendBroadcast(progressIntent)
            }

            // Notificar descarga completada
            val completeIntent = Intent("com.xzplinked.app.DOWNLOAD_COMPLETE").apply {
                putExtra("download_id", downloadId)
                putExtra("file_name", fileName)
            }
            sendBroadcast(completeIntent)

            // Escanear archivo para que aparezca en la galería/reproductor
            android.media.MediaScannerConnection.scanFile(
                this@DownloadService,
                arrayOf(if (downloadPath.startsWith("content://")) fileName else File(downloadPath, fileName).absolutePath),
                null
            ) { path, uri ->
                android.util.Log.d("DownloadService", "File scanned: $path")
            }

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
        } finally {
            inputStream?.close()
            outputStream?.close()
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
