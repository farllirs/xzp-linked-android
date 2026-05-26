# Arquitectura de XZP Linked

## Descripción General

XZP Linked es una aplicación Android nativa construida con **Kotlin** y **MVVM** que permite descargar y reproducir medios desde múltiples plataformas.

```
┌─────────────────────────────────────────────────────────────┐
│                    XZP Linked Architecture                   │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              UI Layer (Fragments & Activities)       │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │   │
│  │  │  Downloads   │  │   Player     │  │   Settings   │ │   │
│  │  │  Fragment    │  │   Fragment   │  │   Fragment   │ │   │
│  │  └──────────────┘  └──────────────┘  └──────────────┘ │   │
│  └──────────────────────────────────────────────────────┘   │
│                           ▲                                   │
│                           │ observes                          │
│  ┌────────────────────────┴────────────────────────────┐    │
│  │         ViewModel Layer (MainViewModel)             │    │
│  │  ┌──────────────────────────────────────────────┐   │    │
│  │  │  LiveData<Downloads>                        │   │    │
│  │  │  LiveData<Tracks>                           │   │    │
│  │  │  LiveData<CurrentTrack>                     │   │    │
│  │  │  LiveData<Theme>                            │   │    │
│  │  │  LiveData<AccentColor>                      │   │    │
│  │  └──────────────────────────────────────────────┘   │    │
│  └────────────────────────────────────────────────────┘    │
│                           ▲                                   │
│                           │ manages                           │
│  ┌────────────────────────┴────────────────────────────┐    │
│  │      Repository & Service Layer                     │    │
│  │  ┌──────────────────────────────────────────────┐   │    │
│  │  │  AudioPlaybackService (Foreground Service)  │   │    │
│  │  │  DownloadService (Foreground Service)       │   │    │
│  │  │  MediaExtractor (Metadata Extraction)       │   │    │
│  │  │  NotificationReceiver (Broadcast Receiver)  │   │    │
│  │  └──────────────────────────────────────────────┘   │    │
│  └────────────────────────────────────────────────────┘    │
│                           ▲                                   │
│                           │ uses                              │
│  ┌────────────────────────┴────────────────────────────┐    │
│  │         Data Layer (Models & Storage)               │    │
│  │  ┌──────────────────────────────────────────────┐   │    │
│  │  │  Track (Data Class)                         │   │    │
│  │  │  DownloadItem (Data Class)                  │   │    │
│  │  │  SharedPreferences (Theme & Settings)       │   │    │
│  │  │  File System (Downloaded Media)             │   │    │
│  │  └──────────────────────────────────────────────┘   │    │
│  └────────────────────────────────────────────────────┘    │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

## Capas de Arquitectura

### 1. UI Layer (Presentation)

**Responsabilidades**:
- Mostrar información al usuario
- Capturar entrada del usuario
- Observar cambios de estado

**Componentes**:
- `MainActivity.kt`: Actividad principal con navegación
- `DownloadsFragment.kt`: Interfaz de descargas
- `PlayerFragment.kt`: Interfaz del reproductor
- `SettingsFragment.kt`: Interfaz de configuración

**Flujo**:
```
Usuario interactúa → Fragment captura evento → ViewModel actualiza estado
```

### 2. ViewModel Layer (Presentation Logic)

**Responsabilidades**:
- Gestionar estado de la aplicación
- Coordinar operaciones
- Perseguir datos en cambios de configuración

**Componentes**:
- `MainViewModel.kt`: ViewModel centralizado con LiveData

**Datos Expuestos**:
```kotlin
val currentTheme: LiveData<String>
val accentColor: LiveData<String>
val downloads: LiveData<List<DownloadItem>>
val tracks: LiveData<List<Track>>
val currentTrack: LiveData<Track?>
val isPlaying: LiveData<Boolean>
val downloadProgress: LiveData<Int>
```

### 3. Repository & Service Layer

**Responsabilidades**:
- Ejecutar operaciones de larga duración
- Manejar servicios del sistema
- Gestionar notificaciones

**Componentes**:

#### AudioPlaybackService
```
Responsabilidades:
- Reproducir archivos de audio
- Manejar audio focus
- Mostrar notificaciones
- Procesar controles de notificación

Ciclo de vida:
onCreate() → setupMediaPlayer() → setupBroadcastReceiver()
onStartCommand() → playAudio() → updateNotification()
onDestroy() → release() → unregisterReceiver()
```

#### DownloadService
```
Responsabilidades:
- Descargar archivos desde URLs
- Reportar progreso
- Guardar archivos en almacenamiento

Ciclo de vida:
onCreate() → createNotificationChannel()
onStartCommand() → downloadFile()
onDestroy() → stopForeground()
```

#### MediaExtractor
```
Responsabilidades:
- Validar URLs
- Detectar plataforma
- Extraer metadatos

Métodos:
- validateUrl(url: String): Boolean
- detectPlatform(url: String): String?
- extractMediaInfo(url: String): DownloadItem?
```

### 4. Data Layer

**Responsabilidades**:
- Almacenar y recuperar datos
- Perseguir preferencias

**Componentes**:

#### Models
```kotlin
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val filePath: String,
    val format: String,
    val isFavorite: Boolean
)

data class DownloadItem(
    val id: String,
    val title: String,
    val platform: String,
    val url: String,
    val format: String,
    val quality: String,
    val status: String,
    val progress: Int
)
```

#### Storage
- **SharedPreferences**: Tema, color de acento, ruta de descarga
- **File System**: Archivos de audio descargados
- **MediaStore**: Integración con galería del sistema

## Flujos de Datos

### Flujo 1: Descarga de Media

```
1. Usuario pega URL en DownloadsFragment
2. MediaExtractor valida y extrae metadatos
3. DownloadsFragment llama a MainViewModel.addDownload()
4. MainViewModel inicia DownloadService
5. DownloadService descarga el archivo
6. DownloadService envía BroadcastIntent con progreso
7. MainViewModel actualiza LiveData<downloads>
8. DownloadsFragment observa cambios y actualiza UI
```

**Código**:
```kotlin
// DownloadsFragment.kt
private fun startDownload() {
    val url = binding.urlInput.text.toString()
    val downloadItem = mediaExtractor.extractMediaInfo(url)
    
    viewModel.addDownload(downloadItem)
    
    val intent = Intent(requireContext(), DownloadService::class.java).apply {
        putExtra(DownloadService.EXTRA_URL, url)
        putExtra(DownloadService.EXTRA_DOWNLOAD_ID, downloadItem.id)
    }
    requireContext().startService(intent)
}

// MainViewModel.kt
fun addDownload(item: DownloadItem) {
    val currentList = _downloads.value?.toMutableList() ?: mutableListOf()
    currentList.add(0, item)
    _downloads.value = currentList
}
```

### Flujo 2: Reproducción de Audio

```
1. Usuario selecciona track en PlayerFragment
2. PlayerFragment llama a MainViewModel.setCurrentTrack()
3. MainViewModel inicia AudioPlaybackService
4. AudioPlaybackService reproduce el archivo
5. AudioPlaybackService muestra notificación
6. Usuario interactúa con controles de notificación
7. NotificationReceiver captura el evento
8. NotificationReceiver inicia AudioPlaybackService con acción
9. AudioPlaybackService procesa la acción
10. MainViewModel observa cambios y actualiza UI
```

**Código**:
```kotlin
// PlayerFragment.kt
private fun playTrack(filePath: String) {
    val intent = Intent(requireContext(), AudioPlaybackService::class.java).apply {
        action = AudioPlaybackService.ACTION_PLAY
        putExtra("file_path", filePath)
    }
    requireContext().startService(intent)
}

// AudioPlaybackService.kt
override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
        ACTION_PLAY -> playAudio(intent.getStringExtra("file_path"))
        ACTION_PAUSE -> pauseAudio()
        ACTION_NEXT -> nextTrack()
    }
    return START_STICKY
}
```

### Flujo 3: Cambio de Tema

```
1. Usuario selecciona tema en SettingsFragment
2. SettingsFragment llama a MainViewModel.setTheme()
3. MainViewModel guarda en SharedPreferences
4. MainViewModel actualiza LiveData<currentTheme>
5. MainActivity observa cambios
6. MainActivity aplica AppCompatDelegate.setDefaultNightMode()
7. Sistema recrea la actividad
8. Todos los Fragments se reconstruyen con nuevo tema
```

**Código**:
```kotlin
// SettingsFragment.kt
private fun selectTheme(theme: String) {
    viewModel.setTheme(theme)
}

// MainViewModel.kt
fun setTheme(theme: String) {
    _currentTheme.value = theme
    prefs.edit().putString("theme", theme).apply()
}

// MainActivity.kt
viewModel.currentTheme.observe(this) { theme ->
    applyTheme()
}

private fun applyTheme() {
    val theme = viewModel.getCurrentTheme()
    when (theme) {
        "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}
```

## Patrones Utilizados

### 1. MVVM (Model-View-ViewModel)

```
View (Fragment) ←→ ViewModel ←→ Repository ←→ Data
     ↓                ↓
  observa          expone
  LiveData         LiveData
```

### 2. LiveData & Observers

```kotlin
// En ViewModel
private val _downloads = MutableLiveData<List<DownloadItem>>()
val downloads: LiveData<List<DownloadItem>> = _downloads

// En Fragment
viewModel.downloads.observe(viewLifecycleOwner) { downloads ->
    adapter.submitList(downloads)
}
```

### 3. Coroutines para Operaciones Asincrónicas

```kotlin
GlobalScope.launch(Dispatchers.IO) {
    val mediaInfo = mediaExtractor.extractMediaInfo(url)
    // Operación de larga duración
}
```

### 4. Foreground Services para Operaciones en Segundo Plano

```kotlin
// En Android 8.0+, servicios deben ser foreground
startForeground(notificationId, notification)
```

### 5. BroadcastReceiver para Eventos del Sistema

```kotlin
// Registrar receptor
registerReceiver(notificationReceiver, IntentFilter(ACTION_PLAY))

// Enviar evento
sendBroadcast(Intent(ACTION_PLAY))
```

## Gestión de Estado

### SharedPreferences

```kotlin
// Guardar
prefs.edit().putString("theme", "dark").apply()

// Recuperar
val theme = prefs.getString("theme", "system")
```

### LiveData

```kotlin
// Actualizar
_currentTrack.value = track

// Observar
currentTrack.observe(viewLifecycleOwner) { track ->
    updateUI(track)
}
```

## Manejo de Permisos

```kotlin
// Solicitar en tiempo de ejecución
val permissions = arrayOf(
    Manifest.permission.INTERNET,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.POST_NOTIFICATIONS
)

ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)

// Manejar respuesta
override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
) {
    val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
}
```

## Optimizaciones

### 1. ViewBinding

```kotlin
// En lugar de findViewById
val binding = FragmentDownloadsBinding.inflate(layoutInflater, container, false)
binding.downloadButton.setOnClickListener { /* ... */ }
```

### 2. RecyclerView con DiffUtil

```kotlin
class DownloadAdapter : ListAdapter<DownloadItem, DownloadAdapter.ViewHolder>(
    DownloadDiffCallback()
) {
    // Solo actualiza items que cambiaron
}
```

### 3. Lazy Loading

```kotlin
// Las imágenes se cargan bajo demanda
trackAdapter.submitList(tracks)
```

## Testing

### Unit Tests

```kotlin
@Test
fun testThemeChange() {
    viewModel.setTheme("dark")
    assertEquals("dark", viewModel.getCurrentTheme())
}
```

### Instrumented Tests

```kotlin
@Test
fun testDownloadFragment() {
    val scenario = launchFragmentInContainer<DownloadsFragment>()
    onView(withId(R.id.download_btn)).perform(click())
}
```

## Seguridad

### 1. Permisos Mínimos

Solo se solicitan permisos necesarios:
- INTERNET (descargas)
- WRITE_EXTERNAL_STORAGE (guardar archivos)
- POST_NOTIFICATIONS (notificaciones)

### 2. Validación de URLs

```kotlin
fun validateUrl(url: String): Boolean {
    return try {
        URL(url)
        detectPlatform(url) != null
    } catch (e: Exception) {
        false
    }
}
```

### 3. ProGuard/R8

Ofusca código en release:
```gradle
buildTypes {
    release {
        isMinifyEnabled = true
        proguardFiles(...)
    }
}
```

## Performance

### 1. Lazy Initialization

```kotlin
private val mediaExtractor by lazy { MediaExtractor.getInstance() }
```

### 2. Coroutines para No Bloquear UI

```kotlin
GlobalScope.launch(Dispatchers.IO) {
    // Operación pesada
    withContext(Dispatchers.Main) {
        // Actualizar UI
    }
}
```

### 3. Caché de Metadatos

```kotlin
private val metadataCache = mutableMapOf<String, DownloadItem>()
```

## Escalabilidad

### Próximas Mejoras

1. **Room Database**: Reemplazar SharedPreferences con SQLite
2. **Dependency Injection**: Usar Hilt para inyección de dependencias
3. **API Backend**: Conectar con servidor para descargas reales
4. **Sync Manager**: Sincronizar descargas entre dispositivos
5. **Analytics**: Rastrear uso de la app

---

**Documentación actualizada**: Mayo 2026
