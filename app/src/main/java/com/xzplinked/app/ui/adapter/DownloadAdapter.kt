package com.xzplinked.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xzplinked.app.R
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
                downloadFormatBadge.text = item.format.uppercase()
                downloadQuality.text = item.quality
                downloadSize.text = if (item.size > 0) formatFileSize(item.size) else "-- MB"

                // Color según plataforma
                val colorRes = when (item.platform) {
                    "youtube" -> R.color.accent_rose
                    "tiktok" -> R.color.accent_blue
                    "instagram" -> R.color.accent_lilac
                    "x" -> R.color.accent_mint
                    else -> R.color.accent_cream
                }
                downloadThumb.setBackgroundResource(colorRes)

                when (item.status) {
                    "downloading" -> {
                        downloadStatusIcon.setImageResource(R.drawable.ic_download)
                        downloadStatusIcon.alpha = 0.5f
                    }
                    "completed" -> {
                        downloadStatusIcon.setImageResource(R.drawable.ic_check)
                        downloadStatusIcon.alpha = 1.0f
                    }
                    "failed" -> {
                        // Podrías usar un ic_error
                        downloadStatusIcon.alpha = 1.0f
                    }
                    else -> {
                        downloadStatusIcon.alpha = 0.3f
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
