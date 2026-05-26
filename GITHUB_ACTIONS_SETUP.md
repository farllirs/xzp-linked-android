# Configurar GitHub Actions - XZP Linked

## Problema: Permisos de Workflow

GitHub está bloqueando la creación automática del workflow por permisos. Aquí está la solución:

---

## Solución: Habilitar Permisos Manualmente

### Paso 1: Ir a Configuración del Repositorio

1. Ve a: https://github.com/farllirs/xzp-linked-android/settings
2. O en tu repositorio: **Settings** (Configuración)

### Paso 2: Habilitar Permisos de Workflow

1. En el menú lateral, ve a: **Actions** → **General**
2. Bajo **"Workflow permissions"**, selecciona:
   - ✅ **"Read and write permissions"**
   - ✅ **"Allow GitHub Actions to create and approve pull requests"**
3. Haz clic en **"Save"**

### Paso 3: Crear el Workflow Manualmente

1. Ve a: **Actions** en tu repositorio
2. Haz clic en **"New workflow"**
3. Selecciona **"set up a workflow yourself"**
4. Copia y pega este código:

```yaml
name: Build XZP Linked APK

on:
  push:
    branches: [ master, main, develop ]
  pull_request:
    branches: [ master, main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    timeout-minutes: 60

    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Setup Android SDK
        uses: android-actions/setup-android@v2
        with:
          api-level: 34
          build-tools-version: 34.0.0
          ndk-version: 25.1.8937393

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build Debug APK
        run: ./gradlew assembleDebug --stacktrace --info

      - name: Build Release APK
        run: ./gradlew assembleRelease --stacktrace --info

      - name: Upload Debug APK
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: xzp-linked-debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk
          retention-days: 90

      - name: Upload Release APK
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: xzp-linked-release-apk
          path: app/build/outputs/apk/release/app-release.apk
          retention-days: 90

      - name: Create Release (on Tag)
        if: startsWith(github.ref, 'refs/tags/')
        uses: softprops/action-gh-release@v1
        with:
          files: |
            app/build/outputs/apk/debug/app-debug.apk
            app/build/outputs/apk/release/app-release.apk
          draft: false
          prerelease: false
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

5. Haz clic en **"Commit changes"**
6. GitHub compilará automáticamente

---

## Descargar el APK Compilado

### Opción 1: Desde GitHub Actions (Recomendado)

1. Ve a **Actions** en tu repositorio
2. Haz clic en el workflow **"Build XZP Linked APK"**
3. Desplázate hacia abajo hasta **"Artifacts"**
4. Descarga:
   - `xzp-linked-debug-apk` (para testing)
   - `xzp-linked-release-apk` (para producción)

### Opción 2: Desde Releases (si creaste un tag)

1. Ve a **Releases** en tu repositorio
2. Los APKs estarán adjuntos automáticamente

---

## Instalar en tu Teléfono

### Opción A: Desde tu PC (Requiere ADB)

```bash
# Conecta tu teléfono por USB
adb install app-debug.apk
```

### Opción B: Directamente en tu Teléfono

1. Descarga el APK en tu teléfono
2. Abre el archivo (generalmente en Descargas)
3. Toca **"Instalar"**
4. Si pide permiso, habilita **"Instalar desde fuentes desconocidas"**

---

## Compilación Automática

Cada vez que hagas:

```bash
git push origin master
```

GitHub Actions compilará automáticamente el APK y lo guardará en **Artifacts** por 90 días.

---

## Troubleshooting

### "Build failed"

1. Ve a **Actions** → **Build XZP Linked APK**
2. Haz clic en el workflow fallido
3. Revisa los logs en **Build Debug APK** o **Build Release APK**
4. Busca el error específico

### "No artifacts found"

El build falló. Revisa los logs en Actions.

### "Permission denied" al instalar

En tu teléfono:
1. Ve a **Configuración** → **Seguridad** → **Fuentes desconocidas**
2. Habilita la opción
3. Intenta instalar de nuevo

---

## Crear un Release

```bash
# Crear un tag
git tag -a v1.0.0 -m "Release version 1.0.0"

# Hacer push del tag
git push origin v1.0.0
```

Los APKs se publicarán automáticamente en **Releases**.

---

**¡Tu aplicación XZP Linked se compilará automáticamente en GitHub!**
