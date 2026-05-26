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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xzplinked.app.R
import com.xzplinked.app.databinding.FragmentDownloadsBinding
import com.xzplinked.app.model.DownloadItem
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
        setupSpinners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        downloadAdapter = DownloadAdapter()
        binding.downloadsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = downloadAdapter
        }
    }

    private fun pasteFromClipboard() {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val pasteData = clip.getItemAt(0).text.toString()
            binding.urlInput.setText(pasteData)
        } else {
            showToast("Portapapeles vacío")
        }
    }

    private fun setupSpinners() {
        val qualities = listOf("1080p", "720p", "480p", "360p", "320kbps", "192kbps", "128kbps")
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, qualities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.qualitySpinner.adapter = adapter
    }

    private fun setupListeners() {
        binding.pasteBtn.setOnClickListener {
            pasteFromClipboard()
        }

        binding.downloadBtn.setOnClickListener {
            startDownload()
        }

        binding.fmtVideo.setOnClickListener {
            binding.fmtVideo.isSelected = true
            binding.fmtAudio.isSelected = false
            // Actualizar spinner para video si es necesario
        }

        binding.fmtAudio.setOnClickListener {
            binding.fmtAudio.isSelected = true
            binding.fmtVideo.isSelected = false
            // Actualizar spinner para audio
        }

        binding.urlInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                detectPlatform(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        
        // Seleccionar video por defecto
        binding.fmtVideo.isSelected = true
    }

    private fun detectPlatform(url: String) {
        // Reset alphas
        binding.platformYoutube.alpha = 0.3f
        binding.platformTiktok.alpha = 0.3f
        binding.platformInstagram.alpha = 0.3f
        binding.platformX.alpha = 0.3f

        when {
            url.contains("youtube.com", true) || url.contains("youtu.be", true) -> {
                binding.platformYoutube.alpha = 1.0f
            }
            url.contains("tiktok.com", true) -> {
                binding.platformTiktok.alpha = 1.0f
            }
            url.contains("instagram.com", true) -> {
                binding.platformInstagram.alpha = 1.0f
            }
            url.contains("twitter.com", true) || url.contains("x.com", true) -> {
                binding.platformX.alpha = 1.0f
            }
        }
    }

    private fun startDownload() {
        val url = binding.urlInput.text.toString()
        if (url.isEmpty()) {
            showToast("Ingresa una URL")
            return
        }

        if (!mediaExtractor.validateUrl(url)) {
            showToast("URL no válida")
            return
        }

        val format = if (binding.fmtVideo.isSelected) "video" else "audio"

        lifecycleScope.launch(Dispatchers.Main) {
            binding.downloadBtn.isEnabled = false
            binding.downloadBtn.text = "Procesando..."

            val downloadItem = mediaExtractor.extractMediaInfo(url, format)

            if (downloadItem != null) {
                viewModel.addDownload(downloadItem)

                // Iniciar servicio de descarga con la URL REAL devuelta por la API
                val downloadIntent = Intent(requireContext(), DownloadService::class.java).apply {
                    putExtra(DownloadService.EXTRA_URL, downloadItem.url)
                    putExtra(DownloadService.EXTRA_FILE_NAME, "${downloadItem.title}.${if (format == "video") "mp4" else "mp3"}")
                    putExtra(DownloadService.EXTRA_DOWNLOAD_PATH, viewModel.downloadPath.value)
                    putExtra(DownloadService.EXTRA_DOWNLOAD_ID, downloadItem.id)
                }

                requireContext().startService(downloadIntent)
                showToast("Descarga iniciada")
                binding.urlInput.setText("")
            } else {
                showToast("Error al procesar la URL (API no disponible)")
            }

            binding.downloadBtn.isEnabled = true
            binding.downloadBtn.text = "Descargar"
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
