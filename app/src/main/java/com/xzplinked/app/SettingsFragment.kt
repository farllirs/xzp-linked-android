package com.xzplinked.app.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.xzplinked.app.databinding.FragmentSettingsBinding
import com.xzplinked.app.viewmodel.MainViewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        setupThemeSelector()
        setupAccentColorPalette()
        setupDownloadPath()
        setupToggles()
    }

    private fun setupThemeSelector() {
        val currentTheme = viewModel.getCurrentTheme()

        when (currentTheme) {
            "light" -> binding.themeLightButton.isSelected = true
            "dark" -> binding.themeDarkButton.isSelected = true
            "system" -> binding.themeSystemButton.isSelected = true
        }

        binding.themeLightButton.setOnClickListener {
            selectTheme("light")
        }

        binding.themeDarkButton.setOnClickListener {
            selectTheme("dark")
        }

        binding.themeSystemButton.setOnClickListener {
            selectTheme("system")
        }
    }

    private fun selectTheme(theme: String) {
        viewModel.setTheme(theme)

        binding.themeLightButton.isSelected = theme == "light"
        binding.themeDarkButton.isSelected = theme == "dark"
        binding.themeSystemButton.isSelected = theme == "system"

        showToast("Tema cambiado a: $theme")
    }

    private fun setupAccentColorPalette() {
        val colors = listOf(
            "mint" to binding.colorMint,
            "lilac" to binding.colorLilac,
            "blue" to binding.colorBlue,
            "cream" to binding.colorCream,
            "peach" to binding.colorPeach,
            "yellow" to binding.colorYellow,
            "rose" to binding.colorRose,
            "sage" to binding.colorSage,
            "sky" to binding.colorSky,
            "lavender" to binding.colorLavender
        )

        colors.forEach { (colorName, button) ->
            button.setOnClickListener {
                selectAccentColor(colorName)
            }
        }
    }

    private fun selectAccentColor(color: String) {
        viewModel.setAccentColor(color)

        // Actualizar estado visual de los botones
        binding.colorMint.isSelected = color == "mint"
        binding.colorLilac.isSelected = color == "lilac"
        binding.colorBlue.isSelected = color == "blue"
        binding.colorCream.isSelected = color == "cream"
        binding.colorPeach.isSelected = color == "peach"
        binding.colorYellow.isSelected = color == "yellow"
        binding.colorRose.isSelected = color == "rose"
        binding.colorSage.isSelected = color == "sage"
        binding.colorSky.isSelected = color == "sky"
        binding.colorLavender.isSelected = color == "lavender"

        showToast("Color de acento: $color")
    }

    private fun setupDownloadPath() {
        binding.browseBtn.setOnClickListener {
            // Abrir selector de carpetas
            showToast("Selector de carpetas (próximamente)")
        }

        viewModel.downloadPath.observe(viewLifecycleOwner) { path ->
            binding.downloadPathInput.setText(path)
        }
    }

    private fun setupToggles() {
        binding.toggleWifi.setOnClickListener {
            showToast("WiFi solo: ${binding.toggleWifi.isChecked}")
        }

        binding.toggleBg.setOnClickListener {
            showToast("Reproducción en segundo plano: ${binding.toggleBg.isChecked}")
        }

        binding.toggleLoop.setOnClickListener {
            showToast("Reproducción en bucle: ${binding.toggleLoop.isChecked}")
        }

        binding.toggleShuffle.setOnClickListener {
            showToast("Aleatorio por defecto: ${binding.toggleShuffle.isChecked}")
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
