# Configuración en GitHub - XZP Linked

## Pasos para Subir el Proyecto a GitHub

### 1. Crear Repositorio en GitHub

1. Ve a https://github.com/new
2. Nombre del repositorio: `xzp-linked-android`
3. Descripción: `Media Downloader & Player for Android`
4. Selecciona **"Public"** (para que GitHub Actions funcione gratis)
5. **NO** inicialices con README, .gitignore ni licencia (ya los tenemos)
6. Haz clic en **"Create repository"**

### 2. Clonar y Configurar Localmente

```bash
# Clonar el repositorio vacío
git clone https://github.com/TU_USUARIO/xzp-linked-android.git
cd xzp-linked-android

# Copiar todos los archivos del proyecto
# (Asume que tienes los archivos en /home/ubuntu/xzp-linked-android)
cp -r /home/ubuntu/xzp-linked-android/* .
cp -r /home/ubuntu/xzp-linked-android/.github .
cp /home/ubuntu/xzp-linked-android/.gitignore .

# Verificar que todo está
ls -la
```

### 3. Hacer Commit Inicial

```bash
git add .
git commit -m "Initial commit: XZP Linked Android app"
git push origin main
```

### 4. Verificar GitHub Actions

1. Ve a tu repositorio en GitHub
2. Haz clic en la pestaña **"Actions"**
3. Deberías ver un workflow llamado **"Build APK"** ejecutándose
4. Espera a que termine (5-10 minutos)

### 5. Descargar los APKs Compilados

#### Opción A: Desde GitHub Actions (Recomendado)

1. Ve a **Actions** → **Build APK** (el último workflow)
2. Desplázate hacia abajo hasta **"Artifacts"**
3. Descarga:
   - `xzp-linked-debug` (para testing)
   - `xzp-linked-release` (para producción)

#### Opción B: Desde Releases

1. Ve a **Releases** (en la barra lateral)
2. Si creaste un tag, los APKs estarán aquí automáticamente

### 6. Instalar en tu Teléfono

#### Desde tu PC (Windows/Mac/Linux)

```bash
# Conecta tu teléfono por USB
adb install app-debug.apk

# O para release
adb install app-release.apk
```

#### Directamente en tu Teléfono

1. Descarga el APK en tu teléfono
2. Abre el archivo (generalmente en Descargas)
3. Toca **"Instalar"**
4. Permite permisos si es necesario

---

## Workflow Automático

Cada vez que hagas `git push`:

1. GitHub Actions se activa automáticamente
2. Compila el APK Debug y Release
3. Los guarda como **Artifacts** (disponibles por 90 días)
4. Si creas un **Release/Tag**, los publica automáticamente

### Crear un Release

```bash
# Crear tag
git tag -a v1.0.0 -m "Release version 1.0.0"

# Hacer push del tag
git push origin v1.0.0
```

Los APKs aparecerán automáticamente en **Releases**.

---

## Estructura del Proyecto en GitHub

```
xzp-linked-android/
├── .github/
│   └── workflows/
│       └── build-apk.yml          # GitHub Actions workflow
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/xzplinked/app/
│   │       │   ├── MainActivity.kt
│   │       │   ├── MainViewModel.kt
│   │       │   ├── *Fragment.kt
│   │       │   ├── *Service.kt
│   │       │   └── ...
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   ├── menu/
│   │       │   ├── values/
│   │       │   └── drawable/
│   │       └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── .gitignore
├── README.md
├── INSTALLATION_GUIDE.md
├── ARCHITECTURE.md
└── GITHUB_SETUP.md
```

---

## Troubleshooting

### "Build failed" en GitHub Actions

**Solución**:
1. Verifica que `settings.gradle.kts` incluye `include(":app")`
2. Asegúrate de que `build.gradle.kts` está en la raíz
3. Revisa los logs en **Actions** → **Build APK** → **Build Debug APK**

### "No artifacts found"

**Solución**:
1. El build falló. Revisa los logs
2. Asegúrate de que el proyecto compila localmente primero

### "Permission denied" al instalar APK

**Solución**:
1. Habilita **"Instalar desde fuentes desconocidas"** en tu teléfono
2. Ve a **Configuración** → **Seguridad** → **Fuentes desconocidas**

---

## Configuración Avanzada

### Firma Automática de APK

Para firmar automáticamente los APKs en GitHub Actions:

1. Genera un keystore:
```bash
keytool -genkey -v -keystore xzp-linked.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 -alias xzp
```

2. Codifica en Base64:
```bash
base64 -i xzp-linked.keystore | tr -d '\n' | xclip -selection clipboard
```

3. En GitHub:
   - Ve a **Settings** → **Secrets and variables** → **Actions**
   - Crea nuevo secreto: `KEYSTORE_FILE` (pega el contenido)
   - Crea: `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`

4. Actualiza `.github/workflows/build-apk.yml` para usar los secretos

### Publicar en Google Play Store

1. Crea una cuenta en [Google Play Console](https://play.google.com/console)
2. Sube el APK Release firmado
3. Completa la información de la app
4. Envía para revisión

---

## Próximos Pasos

1. ✅ Subir a GitHub
2. ✅ Compilar automáticamente con GitHub Actions
3. ✅ Descargar APK
4. ✅ Instalar en teléfono
5. 📝 Integrar yt-dlp para descargas reales
6. 📝 Conectar con backend
7. 📝 Publicar en Google Play Store

---

**¡Tu aplicación XZP Linked está lista para GitHub!**
