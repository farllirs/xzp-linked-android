package com.xzplinked.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xzplinked.app.databinding.ItemTrackBinding
import com.xzplinked.app.model.Track

class TrackAdapter(
    private val onTrackClick: (Track) -> Unit
) : ListAdapter<Track, TrackAdapter.TrackViewHolder>(TrackDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = ItemTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TrackViewHolder(private val binding: ItemTrackBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Track) {
            binding.apply {
                trackTitle.text = item.title
                trackArtist.text = item.artist
                trackDuration.text = item.duration
                trackFormat.text = item.format.uppercase()

                // Mostrar icono según el formato
                val iconRes = when (item.format.lowercase()) {
                    "mp3" -> android.R.drawable.ic_media_play
                    "m4a" -> android.R.drawable.ic_media_play
                    "wav" -> android.R.drawable.ic_media_play
                    else -> android.R.drawable.ic_media_play
                }
                trackIcon.setImageResource(iconRes)

                // Marcar como favorito si lo es
                if (item.isFavorite) {
                    favoriteIcon.visibility = android.view.View.VISIBLE
                } else {
                    favoriteIcon.visibility = android.view.View.GONE
                }

                root.setOnClickListener {
                    onTrackClick(item)
                }
            }
        }
    }

    private class TrackDiffCallback : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Track, newItem: Track) =
            oldItem == newItem
    }
}
