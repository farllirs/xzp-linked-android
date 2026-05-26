package com.xzplinked.app.model

import java.io.Serializable

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val filePath: String,
    val format: String, // "mp3", "m4a", "wav", etc.
    val size: Long,
    val folder: String,
    val isFavorite: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis(),
    val artworkUrl: String? = null
) : Serializable

data class DownloadItem(
    val id: String,
    val title: String,
    val artist: String? = null,
    val platform: String, // "youtube", "tiktok", "instagram", "x"
    val url: String,
    val format: String, // "video", "audio"
    val quality: String, // "1080p", "720p", "320kbps", etc.
    val size: Long,
    val folder: String,
    val status: String, // "pending", "downloading", "completed", "failed"
    val progress: Int = 0,
    val dateDownloaded: Long = System.currentTimeMillis(),
    val filePath: String? = null,
    val thumbnailUrl: String? = null
) : Serializable

data class Folder(
    val id: String,
    val name: String,
    val path: String,
    val trackCount: Int,
    val totalDuration: String,
    val dateCreated: Long
) : Serializable
