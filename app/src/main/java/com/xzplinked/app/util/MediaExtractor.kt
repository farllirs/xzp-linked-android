package com.xzplinked.app.util

import android.util.Log
import com.xzplinked.app.model.DownloadItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class MediaExtractor {

    suspend fun extractMediaInfo(url: String, format: String = "audio"): DownloadItem? = withContext(Dispatchers.IO) {
        try {
            val platform = detectPlatform(url)
            if (platform == null) return@withContext null

            // Usando una API de ejemplo (Cobalt o similar que soporte yt-dlp)
            // En producción, el usuario debería configurar su propia URL de API
            val apiUrl = "https://api.cobalt.tools/api/json"
            val requestBody = """
                {
                    "url": "$url",
                    "downloadMode": "${if (format == "video") "video" else "audio"}",
                    "audioFormat": "mp3",
                    "videoQuality": "720",
                    "isNoTTWatermark": true
                }
            """.trimIndent()

            val connection = URL(apiUrl).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true

            connection.outputStream.use { it.write(requestBody.toByteArray()) }

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = org.json.JSONObject(response)
                
                // El formato de respuesta depende de la API usada (aquí usamos el estándar de Cobalt)
                val downloadUrl = jsonResponse.optString("url")
                val title = jsonResponse.optString("filename", extractTitle(url))

                if (downloadUrl.isNotEmpty()) {
                    return@withContext DownloadItem(
                        id = System.currentTimeMillis().toString(),
                        title = title,
                        artist = extractArtist(url),
                        platform = platform,
                        url = downloadUrl, // URL real de descarga
                        format = format,
                        quality = if (format == "video") "720p" else "320kbps",
                        size = 0L,
                        folder = if (format == "video") "Videos" else "Music",
                        status = "pending",
                        progress = 0
                    )
                }
            }
            null
        } catch (e: Exception) {
            Log.e("MediaExtractor", "Error extracting real media info", e)
            null
        }
    }

    private fun detectPlatform(url: String): String? {
        val patterns = mapOf(
            "youtube" to listOf("youtube.com/watch", "youtu.be/", "youtube.com/shorts", "youtube.com/playlist"),
            "tiktok" to listOf("tiktok.com/@", "tiktok.com/t/", "vm.tiktok.com"),
            "instagram" to listOf("instagram.com/p/", "instagram.com/reel", "instagram.com/tv"),
            "x" to listOf("twitter.com/", "x.com/")
        )

        for ((platform, keywords) in patterns) {
            if (keywords.any { url.contains(it, ignoreCase = true) }) {
                return platform
            }
        }
        return null
    }

    private fun extractTitle(url: String): String {
        val platform = detectPlatform(url)
        return when (platform) {
            "youtube" -> "YouTube Media " + url.takeLast(5)
            "tiktok" -> "TikTok Video " + System.currentTimeMillis().toString().takeLast(4)
            "instagram" -> "Instagram Reel " + System.currentTimeMillis().toString().takeLast(4)
            "x" -> "X Post " + System.currentTimeMillis().toString().takeLast(4)
            else -> "Media " + System.currentTimeMillis().toString().takeLast(4)
        }
    }

    private fun extractArtist(url: String): String? {
        val platform = detectPlatform(url)
        return when (platform) {
            "youtube" -> "YouTube Creator"
            "tiktok" -> "TikTok User"
            "instagram" -> "Instagram User"
            "x" -> "X User"
            else -> "Unknown Artist"
        }
    }

    fun validateUrl(url: String): Boolean {
        return try {
            URL(url)
            detectPlatform(url) != null
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        private const val TAG = "MediaExtractor"

        fun getInstance(): MediaExtractor = MediaExtractor()
    }
}
