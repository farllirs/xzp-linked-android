package com.xzplinked.app.util

import android.util.Log
import com.xzplinked.app.model.DownloadItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class MediaExtractor {

    suspend fun extractMediaInfo(url: String): DownloadItem? = withContext(Dispatchers.IO) {
        try {
            val platform = detectPlatform(url)
            if (platform == null) {
                Log.e("MediaExtractor", "Plataforma no soportada: $url")
                return@withContext null
            }

            // Aquí se integraría yt-dlp o similar
            // Por ahora, retornamos un objeto mock
            val title = extractTitle(url)
            val artist = extractArtist(url)

            DownloadItem(
                id = System.currentTimeMillis().toString(),
                title = title,
                artist = artist,
                platform = platform,
                url = url,
                format = "audio",
                quality = "320kbps",
                size = 0L,
                folder = "Music",
                status = "pending",
                progress = 0
            )
        } catch (e: Exception) {
            Log.e("MediaExtractor", "Error extracting media info", e)
            null
        }
    }

    private fun detectPlatform(url: String): String? {
        return when {
            url.contains("youtube.com") || url.contains("youtu.be") -> "youtube"
            url.contains("tiktok.com") -> "tiktok"
            url.contains("instagram.com") -> "instagram"
            url.contains("twitter.com") || url.contains("x.com") -> "x"
            else -> null
        }
    }

    private fun extractTitle(url: String): String {
        return try {
            val connection = URL(url).openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
            val content = reader.readText()
            reader.close()

            // Extraer título del HTML (implementación simplificada)
            val titleRegex = """<title>(.*?)</title>""".toRegex()
            val match = titleRegex.find(content)
            match?.groupValues?.get(1)?.trim() ?: "Unknown Title"
        } catch (e: Exception) {
            "Unknown Title"
        }
    }

    private fun extractArtist(url: String): String? {
        return try {
            val connection = URL(url).openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
            val content = reader.readText()
            reader.close()

            // Extraer artista del HTML (implementación simplificada)
            val artistRegex = """<meta property="og:description" content="(.*?)">""".toRegex()
            val match = artistRegex.find(content)
            match?.groupValues?.get(1)?.trim()
        } catch (e: Exception) {
            null
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
