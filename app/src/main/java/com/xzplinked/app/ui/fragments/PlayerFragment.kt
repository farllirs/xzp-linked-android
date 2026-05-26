package com.xzplinked.app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.xzplinked.app.R
import com.xzplinked.app.databinding.FragmentPlayerBinding
import com.xzplinked.app.model.Track
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
    }

    private fun observeViewModel() {
        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            trackAdapter.submitList(tracks)
        }

        viewModel.isPlaying.observe(viewLifecycleOwner) { playing ->
            isPlaying = playing
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
