package com.xzplinked.app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.xzplinked.app.databinding.FragmentPlayerBinding
import com.xzplinked.app.service.AudioPlaybackService
import com.xzplinked.app.ui.adapter.TrackAdapter
import com.xzplinked.app.viewmodel.MainViewModel

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel
    private lateinit var trackAdapter: TrackAdapter
    private var isPlaying = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        trackAdapter = TrackAdapter { track ->
            viewModel.setCurrentTrack(track)
            playTrack(track.filePath)
        }

        binding.tracksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trackAdapter
        }
    }

    private fun setupListeners() {
        binding.filterAllButton.setOnClickListener {
            binding.filterAllButton.isSelected = true
            binding.filterFoldersButton.isSelected = false
            loadAllTracks()
        }

        binding.filterFoldersButton.setOnClickListener {
            binding.filterFoldersButton.isSelected = true
            binding.filterAllButton.isSelected = false
            // Mostrar selector de carpetas
        }

        binding.miniPlay.setOnClickListener {
            togglePlayPause()
        }

        binding.miniNext.setOnClickListener {
            playNextTrack()
        }

        binding.miniPrev.setOnClickListener {
            playPreviousTrack()
        }

    }

    private fun loadAllTracks() {
        val tracks = viewModel.getAllTracks()
        trackAdapter.submitList(tracks)
    }

    private fun playTrack(filePath: String) {
        val intent = Intent(requireContext(), AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_PLAY
            putExtra("file_path", filePath)
        }
        requireContext().startService(intent)
        isPlaying = true
        updatePlayButton()
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            pausePlayback()
        } else {
            resumePlayback()
        }
    }

    private fun pausePlayback() {
        val intent = Intent(requireContext(), AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_PAUSE
        }
        requireContext().startService(intent)
        isPlaying = false
        updatePlayButton()
    }

    private fun resumePlayback() {
        val intent = Intent(requireContext(), AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_RESUME
        }
        requireContext().startService(intent)
        isPlaying = true
        updatePlayButton()
    }

    private fun playNextTrack() {
        val intent = Intent(requireContext(), AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_NEXT
        }
        requireContext().startService(intent)
    }

    private fun playPreviousTrack() {
        val intent = Intent(requireContext(), AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_PREV
        }
        requireContext().startService(intent)
    }

    private fun updatePlayButton() {
        binding.miniPlay.isSelected = isPlaying
    }

    private fun observeViewModel() {
        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            trackAdapter.submitList(tracks)
        }

        viewModel.currentTrack.observe(viewLifecycleOwner) { track ->
            if (track != null) {
                binding.miniTitle.text = track.title
                binding.miniArtist.text = track.artist
            }
        }

        viewModel.isPlaying.observe(viewLifecycleOwner) { playing ->
            isPlaying = playing
            updatePlayButton()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
