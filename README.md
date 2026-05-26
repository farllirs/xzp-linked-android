# XZP Linked - Aplicación Android

Una aplicación móvil completa para descargar y reproducir medios desde YouTube, TikTok, Instagram y X, con soporte para reproducción en segundo plano, notificaciones personalizadas y un sistema de temas dinámico.

## Características

✅ **Descarga de Medios**
- Soporte para YouTube, TikTok, Instagram y X
- Descarga de video y audio
- Selección de calidad
- Descarga en segundo plano
- Progreso en tiempo real

✅ **Reproductor de Audio**
- Reproducción en segundo plano
- Controles en la barra de notificaciones
- Gestión de audio focus
- Lista de reproducción
- Favoritos

✅ **Sistema de Temas**
- Modo Claro/Oscuro/Sistema
- 10 colores pasteles (Material You)
- Contraste optimizado para iconos
- Persistencia de preferencias

✅ **Interfaz Neobrutalista**
- Bordes sólidos de 2px
- Sombras planas
- Geometría Squircle
- Tipografía Bungee y DM Sans
- Diseño 100% Mobile-First

## Estructura del Proyecto

```
xzp-linked-android/
├── build.gradle.kts              # Configuración Gradle
├── AndroidManifest.xml           # Permisos y componentes
├── themes.xml                    # Temas (Claro/Oscuro)
├── colors.xml                    # Paleta de colores
├── strings.xml                   # Cadenas de texto
│
├── MainActivity.kt               # Actividad principal
├── MainViewModel.kt              # ViewModel centralizado
│
├── service/
│   ├── AudioPlaybackService.kt   # Reproducción en segundo plano
│   ├── DownloadService.kt        # Descargas en segundo plano
│   └── NotificationReceiver.kt   # Controles de notificación
│
├── ui/fragments/
│   ├── DownloadsFragment.kt      # Interfaz de descargas
│   ├── PlayerFragment.kt         # Interfaz del reproductor
│   └── SettingsFragment.kt       # Interfaz de configuración
│
├── ui/adapter/
│   ├── DownloadAdapter.kt        # Adaptador de descargas
│   └── TrackAdapter.kt           # Adaptador de tracks
│
├── model/
│   └── Track.kt                  # Modelos de datos
│
├── util/
│   └── MediaExtractor.kt         # Extracción de metadatos
│
└── res/
    ├── layout/
    │   ├── activity_main.xml
    │   ├── fragment_downloads.xml
    │   ├── fragment_player.xml
    │   ├── fragment_settings.xml
    │   ├── item_download.xml
    │   └── item_track.xml
    └── menu/
        └── bottom_nav_menu.xml
```

## Requisitos

- **Android SDK**: 26+ (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Java/Kotlin**: 17+
- **Gradle**: 8.1.0+

## Dependencias Principales

```gradle
// Android Core
androidx.core:core:1.12.0
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.10.0

// Media
androidx.media3:media3-exoplayer:1.1.1
androidx.media3:media3-session:1.1.1

// Networking
com.squareup.retrofit2:retrofit:2.10.0
com.squareup.okhttp3:okhttp:4.11.0

// Coroutines
org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3

// Lifecycle
androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2
```

## Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/tuusuario/xzp-linked-android.git
cd xzp-linked-android
```

### 2. Abrir en Android Studio

1. Abre Android Studio
2. Selecciona "Open an existing Android Studio project"
3. Navega a la carpeta del proyecto
4. Espera a que Gradle sincronice

### 3. Configurar el emulador o dispositivo

```bash
# Listar dispositivos disponibles
adb devices

# Crear un emulador (opcional)
avdmanager create avd -n XZPLinked -k "system-images;android-34;google_apis;x86_64"

# Iniciar emulador
emulator -avd XZPLinked
```

### 4. Compilar y ejecutar

```bash
# Compilar
./gradlew build

# Ejecutar en dispositivo/emulador
./gradlew installDebug
```

## Configuración de Permisos

La aplicación requiere los siguientes permisos (solicitados en tiempo de ejecución):

- `INTERNET` - Descargar medios
- `READ_EXTERNAL_STORAGE` - Leer archivos
- `WRITE_EXTERNAL_STORAGE` - Guardar descargas
- `MANAGE_EXTERNAL_STORAGE` - Acceso a almacenamiento (Android 11+)
- `MODIFY_AUDIO_SETTINGS` - Control de volumen
- `POST_NOTIFICATIONS` - Notificaciones
- `FOREGROUND_SERVICE` - Servicios en primer plano

## Uso

### Descargar Medios

1. Abre la pestaña "Descargas"
2. Pega una URL válida (YouTube, TikTok, Instagram, X)
3. Selecciona formato (Video/Audio)
4. Elige la calidad
5. Toca "Descargar"

### Reproducir Música

1. Abre la pestaña "Reproductor"
2. Selecciona una canción de la lista
3. Usa los controles para reproducir/pausar
4. La reproducción continúa en segundo plano

### Configurar Temas

1. Abre la pestaña "Ajustes"
2. Selecciona modo (Claro/Oscuro/Sistema)
3. Elige un color de acento
4. Los cambios se aplican inmediatamente

## Integración con Backend

### Descargas Reales

Para integrar descargas reales, reemplaza `MediaExtractor.kt` con una llamada a `yt-dlp`:

```kotlin
// Ejemplo con yt-dlp
val command = arrayOf(
    "yt-dlp",
    "-f", "best[ext=mp4]",
    "-o", "%(title)s.%(ext)s",
    url
)
Runtime.getRuntime().exec(command)
```

### API Backend

Crea un backend que exponga endpoints:

```
POST /api/download
- url: String
- format: "video" | "audio"
- quality: String

GET /api/tracks
- folder: String (opcional)

GET /api/download-progress/:id
```

## Compilación del APK

### Debug APK

```bash
./gradlew assembleDebug
# Ubicación: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK

```bash
# Generar keystore
keytool -genkey -v -keystore xzp-linked.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias xzp

# Compilar release
./gradlew assembleRelease \
  -Pandroid.injected.signing.store.file=xzp-linked.keystore \
  -Pandroid.injected.signing.store.password=PASSWORD \
  -Pandroid.injected.signing.key.alias=xzp \
  -Pandroid.injected.signing.key.password=PASSWORD

# Ubicación: app/build/outputs/apk/release/app-release.apk
```

## Troubleshooting

### Problema: "Gradle sync failed"
**Solución**: Asegúrate de tener Java 17+ instalado
```bash
java -version
```

### Problema: "Permission denied" en descargas
**Solución**: Verifica que los permisos están concedidos en Configuración > Aplicaciones > XZP Linked

### Problema: "No audio playing"
**Solución**: Asegúrate de que el archivo de audio existe en la ruta especificada

### Problema: "Notificación no aparece"
**Solución**: Verifica que las notificaciones están habilitadas en Configuración > Aplicaciones > XZP Linked

## Arquitectura

### MVVM (Model-View-ViewModel)

- **Model**: `Track.kt`, `DownloadItem.kt` - Modelos de datos
- **View**: Fragments y Activities - Interfaz de usuario
- **ViewModel**: `MainViewModel.kt` - Lógica de negocio

### Servicios

- **AudioPlaybackService**: Reproducción de audio en segundo plano
- **DownloadService**: Descargas en segundo plano
- **NotificationReceiver**: Manejo de controles de notificación

### Comunicación

- **LiveData**: Observables para cambios de estado
- **Coroutines**: Operaciones asincrónicas
- **BroadcastReceiver**: Eventos del sistema

## Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está bajo la licencia MIT. Ver `LICENSE` para más detalles.

## Soporte

Para reportar bugs o sugerir features, abre un issue en el repositorio.

## Roadmap

- [ ] Integración real con yt-dlp
- [ ] Soporte para descargas en batch
- [ ] Sincronización con cloud
- [ ] Búsqueda y filtrado avanzado
- [ ] Playlists personalizadas
- [ ] Estadísticas de uso
- [ ] Integración con Spotify
- [ ] Versión para iOS (React Native)

---

**Desarrollado con ❤️ por el equipo de XZP Linked**
