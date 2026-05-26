# Guía de Instalación - XZP Linked

## Requisitos Previos

### 1. Java Development Kit (JDK)

Necesitas **Java 17 o superior**:

```bash
# Verificar versión instalada
java -version

# Si no está instalado (Ubuntu/Debian)
sudo apt-get update
sudo apt-get install openjdk-17-jdk

# Si no está instalado (macOS)
brew install openjdk@17
```

### 2. Android Studio

Descarga desde: https://developer.android.com/studio

**Versión recomendada**: Flamingo (2023.2.1) o superior

### 3. Android SDK

Se instala automáticamente con Android Studio, pero verifica:

```bash
# Listar SDKs instalados
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --list
```

**Componentes necesarios**:
- SDK Platform 34 (Android 14)
- Build Tools 34.0.0
- Android Emulator (opcional)
- Google APIs (opcional)

## Instalación Paso a Paso

### Paso 1: Clonar el Repositorio

```bash
# HTTPS
git clone https://github.com/tuusuario/xzp-linked-android.git
cd xzp-linked-android

# O SSH
git clone git@github.com:tuusuario/xzp-linked-android.git
cd xzp-linked-android
```

### Paso 2: Abrir en Android Studio

1. Abre Android Studio
2. Selecciona **"File" → "Open"**
3. Navega a la carpeta `xzp-linked-android`
4. Haz clic en **"Open"**

**Gradle sincronizará automáticamente**. Espera a que termine (puede tomar 2-5 minutos).

### Paso 3: Configurar el Dispositivo

#### Opción A: Usar un Dispositivo Real

1. Conecta tu teléfono Android por USB
2. Habilita el **"Modo de Desarrollador"**:
   - Abre **Configuración → Acerca de**
   - Toca **"Número de compilación"** 7 veces
3. Habilita **"Depuración por USB"**
4. Autoriza la conexión en tu teléfono

Verifica la conexión:
```bash
adb devices
```

#### Opción B: Usar un Emulador

```bash
# Crear un emulador (si no existe)
$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager create avd \
  -n XZPLinked \
  -k "system-images;android-34;google_apis;x86_64"

# Iniciar el emulador
$ANDROID_HOME/emulator/emulator -avd XZPLinked

# O desde Android Studio:
# 1. Tools → Device Manager
# 2. Create Device
# 3. Selecciona "Pixel 6"
# 4. Selecciona "Android 14 (API 34)"
# 5. Haz clic en "Finish"
```

### Paso 4: Compilar y Ejecutar

#### Desde Android Studio

1. Abre el archivo `MainActivity.kt`
2. Haz clic en el botón **"Run"** (▶️) en la barra de herramientas
3. Selecciona tu dispositivo
4. Haz clic en **"OK"**

#### Desde Terminal

```bash
# Compilar debug APK
./gradlew assembleDebug

# Instalar en dispositivo
./gradlew installDebug

# Compilar y ejecutar en un paso
./gradlew installDebug
adb shell am start -n com.xzplinked.app/.ui.MainActivity
```

### Paso 5: Verificar la Instalación

Si todo funcionó correctamente, deberías ver:

1. **Splash Screen** con el logo de XZP Linked
2. **Pestaña Descargas** con el input de URL
3. **Bottom Navigation** con 3 pestañas

## Troubleshooting

### Error: "JAVA_HOME not set"

```bash
# Encuentra la ruta de Java
which java
# Ejemplo: /usr/lib/jvm/java-17-openjdk-amd64/bin/java

# Configura JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Verifica
echo $JAVA_HOME
```

### Error: "Gradle sync failed"

```bash
# Limpiar caché de Gradle
./gradlew clean

# Sincronizar nuevamente
./gradlew sync

# O desde Android Studio:
# File → Invalidate Caches → Invalidate and Restart
```

### Error: "Build failed: SDK not found"

```bash
# Instalar SDK Platform 34
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platforms;android-34"

# Instalar Build Tools
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "build-tools;34.0.0"
```

### Error: "Device not found"

```bash
# Verificar conexión
adb devices

# Reiniciar adb
adb kill-server
adb start-server

# Verificar permisos (Linux)
sudo usermod -a -G plugdev $USER
newgrp plugdev
```

### Error: "Permission denied" en descargas

1. Abre **Configuración del teléfono**
2. Navega a **Aplicaciones → XZP Linked**
3. Toca **"Permisos"**
4. Habilita:
   - Almacenamiento
   - Cámara (si es necesario)
   - Micrófono (si es necesario)

### Error: "No se muestra la notificación"

1. Abre **Configuración del teléfono**
2. Navega a **Aplicaciones → XZP Linked**
3. Toca **"Notificaciones"**
4. Habilita **"Permitir notificaciones"**

## Configuración Adicional

### Cambiar el Nombre de la Aplicación

Edita `strings.xml`:

```xml
<string name="app_name">Tu Nombre de App</string>
```

### Cambiar el Package Name

1. En Android Studio, haz clic derecho en `com.xzplinked.app`
2. Selecciona **"Refactor" → "Rename"**
3. Ingresa el nuevo nombre (ej: `com.tuempresa.mediaapp`)
4. Haz clic en **"Refactor"**

### Cambiar el Icono de la Aplicación

1. Descarga tu icono (512x512 PNG)
2. En Android Studio, abre **"File" → "New" → "Image Asset"**
3. Selecciona tu icono
4. Haz clic en **"Next" → "Finish"**

### Configurar Firma para Release

```bash
# Generar keystore
keytool -genkey -v -keystore xzp-linked.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 -alias xzp

# Compilar APK firmado
./gradlew assembleRelease \
  -Pandroid.injected.signing.store.file=xzp-linked.keystore \
  -Pandroid.injected.signing.store.password=TU_PASSWORD \
  -Pandroid.injected.signing.key.alias=xzp \
  -Pandroid.injected.signing.key.password=TU_PASSWORD
```

## Distribución

### Google Play Store

1. Crea una cuenta en [Google Play Console](https://play.google.com/console)
2. Crea una nueva aplicación
3. Completa la información de la app
4. Sube el APK firmado
5. Configura precios y distribución
6. Envía para revisión

### Distribución Directa (APK)

```bash
# El APK compilado estará en:
app/build/outputs/apk/release/app-release.apk

# Puedes compartirlo directamente o subirlo a:
# - GitHub Releases
# - APKMirror
# - F-Droid
```

## Próximos Pasos

1. **Integrar yt-dlp**: Reemplaza `MediaExtractor.kt` con llamadas reales
2. **Conectar Backend**: Implementa endpoints para descargas
3. **Testing**: Prueba en múltiples dispositivos
4. **Publicar**: Sube a Google Play Store

## Soporte

Si encuentras problemas:

1. Revisa el archivo `build.gradle.kts` para dependencias
2. Consulta la documentación de [Android Developers](https://developer.android.com/)
3. Abre un issue en GitHub

---

**¡Listo! Tu aplicación XZP Linked está instalada y funcionando.**
