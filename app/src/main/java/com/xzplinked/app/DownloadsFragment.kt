package com.xzplinked.app.ui.fragments

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.xzplinked.app.databinding.FragmentDownloadsBinding
import com.xzplinked.app.service.DownloadService
import com.xzplinked.app.ui.adapter.DownloadAdapter
import com.xzplinked.app.util.MediaExtractor
import com.xzplinked.app.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DownloadsFragment : Fragment() {

    private var _binding: FragmentDownloadsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel
    private lateinit var downloadAdapter: DownloadAdapter
    private val mediaExtractor = MediaExtractor.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDownloadsBinding.inflate(inflater, container, false)
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
        downloadAdapter = DownloadAdapter()
        binding.downloadsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = downloadAdapter
        }
    }

    private fun setupListeners() {
        binding.pasteButton.setOnClickListener {
            pasteFromClipboard()
        }

        binding.downloadButton.setOnClickListener {
            startDownload()
        }

        binding.formatVideo.setOnClickListener {
            binding.formatVideo.isSelected = true
            binding.formatAudio.isSelected = false
        }

        binding.formatAudio.setOnClickListener {
            binding.formatAudio.isSelected = true
            binding.formatVideo.isSelected = false
        }
    }

    private fun pasteFromClipboard() {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip

        if (clip != null && clip.itemCount > 0) {
            val url = clip.getItemAt(0).text.toString()

            if (mediaExtractor.validateUrl(url)) {
                binding.urlInput.setText(url)
                detectPlatform(url)
                showToast("URL pegada correctamente")
            } else {
                showToast("URL no válida o plataforma no soportada")
            }
        } else {
            showToast("Portapapeles vacío")
        }
    }

    private fun detectPlatform(url: String) {
        when {
            url.contains("youtube.com") || url.contains("youtu.be") -> {
                binding.platformYoutube.isSelected = true
            }
            url.contains("tiktok.com") -> {
                binding.platformTiktok.isSelected = true
            }
            url.contains("instagram.com") -> {
                binding.platformInstagram.isSelected = true
            }
            url.contains("twitter.com") || url.contains("x.com") -> {
                binding.platformX.isSelected = true
            }
        }
    }

    private fun startDownload() {
        val url = binding.urlInput.text.toString().trim()

        if (url.isEmpty()) {
            showToast("Ingresa una URL")
            return
        }

        if (!mediaExtractor.validateUrl(url)) {
            showToast("URL no válida")
            return
        }

        GlobalScope.launch(Dispatchers.Main) {
            val downloadItem = mediaExtractor.extractMediaInfo(url)

            if (downloadItem != null) {
                viewModel.addDownload(downloadItem)

                // Iniciar servicio de descarga
                val downloadIntent = Intent(requireContext(), DownloadService::class.java).apply {
                    putExtra(DownloadService.EXTRA_URL, url)
                    putExtra(DownloadService.EXTRA_FILE_NAME, "${downloadItem.title}.mp3")
                    putExtra(DownloadService.EXTRA_DOWNLOAD_PATH, viewModel.downloadPath.value)
                    putExtra(DownloadService.EXTRA_DOWNLOAD_ID, downloadItem.id)
                }

                requireContext().startService(downloadIntent)
                showToast("Descarga iniciada")
                binding.urlInput.setText("")
            } else {
                showToast("Error al procesar la URL")
            }
        }
    }

    private fun observeViewModel() {
        viewModel.downloads.observe(viewLifecycleOwner) { downloads ->
            downloadAdapter.submitList(downloads)
        }

        viewModel.downloadProgress.observe(viewLifecycleOwner) { progress ->
            // Actualizar progreso en tiempo real
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
