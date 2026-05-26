package com.xzplinked.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xzplinked.app.databinding.ItemDownloadBinding
import com.xzplinked.app.model.DownloadItem

class DownloadAdapter : ListAdapter<DownloadItem, DownloadAdapter.DownloadViewHolder>(DownloadDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
        val binding = ItemDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DownloadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DownloadViewHolder(private val binding: ItemDownloadBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DownloadItem) {
            binding.apply {
                downloadTitle.text = item.title
                downloadArtist.text = item.artist ?: "Desconocido"
                downloadFormat.text = item.format.uppercase()
                downloadQuality.text = item.quality
                downloadSize.text = formatFileSize(item.size)

                when (item.status) {
                    "downloading" -> {
                        downloadProgress.visibility = android.view.View.VISIBLE
                        downloadProgress.progress = item.progress
                    }
                    "completed" -> {
                        downloadProgress.visibility = android.view.View.GONE
                        downloadStatus.text = "✓ Completado"
                    }
                    "failed" -> {
                        downloadProgress.visibility = android.view.View.GONE
                        downloadStatus.text = "✗ Error"
                    }
                    else -> {
                        downloadProgress.visibility = android.view.View.GONE
                    }
                }
            }
        }

        private fun formatFileSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
                else -> "${bytes / (1024 * 1024 * 1024)} GB"
            }
        }
    }

    private class DownloadDiffCallback : DiffUtil.ItemCallback<DownloadItem>() {
        override fun areItemsTheSame(oldItem: DownloadItem, newItem: DownloadItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: DownloadItem, newItem: DownloadItem) =
            oldItem == newItem
    }
}
