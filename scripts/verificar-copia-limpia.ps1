<#
  Archivo solo con caracteres ASCII a proposito: Windows PowerShell 5.1 lee los .ps1
  sin BOM como ANSI y las tildes rompen el script.

  TT-21: compila y ejecuta las pruebas desde una copia limpia del repositorio.

  Uso (PowerShell, desde cualquier carpeta):
      powershell -ExecutionPolicy Bypass -File scripts\verificar-copia-limpia.ps1
  Con emulador encendido, agrega -ConEmulador para correr tambien las pruebas de Room:
      powershell -ExecutionPolicy Bypass -File scripts\verificar-copia-limpia.ps1 -ConEmulador

  Deja el resultado en verificacion-TT21.txt dentro de la copia limpia y abre los
  reportes HTML de Gradle.
#>
param(
    [string]$Rama = "main",
    [string]$Destino = "$env:TEMP\tiotec-copia-limpia",
    [switch]$ConEmulador
)

# "Continue": git y Gradle escriben avisos normales en stderr; con "Stop" PowerShell 5
# los trataria como errores. El resultado se revisa con $LASTEXITCODE.
$ErrorActionPreference = "Continue"
$repo = "https://github.com/Ezz442/tiotec-preventivo.git"
# Proyecto desde el que se ejecuta el script (carpeta padre de scripts\).
$origen = Split-Path -Parent $PSScriptRoot

if (Test-Path $Destino) { Remove-Item $Destino -Recurse -Force }
Write-Host "1/4 Clonando $repo ($Rama) en $Destino"
# cmd /c: asi PowerShell 5.1 no muestra en rojo los avisos normales que git escribe en stderr.
cmd /c "git clone --depth 1 --branch $Rama $repo `"$Destino`" 2>&1" | Out-Host
if ($LASTEXITCODE -ne 0) { Write-Host "No se pudo clonar el repositorio." -ForegroundColor Red; exit 1 }
Set-Location $Destino

# local.properties (ruta del Android SDK) no se sube a GitHub: la copia limpia no lo trae.
# Se copia el del proyecto o, si no existe, se usa la ruta por defecto de Android Studio.
if (-not $env:ANDROID_HOME) {
    $propiedades = Join-Path $origen "local.properties"
    $sdkPorDefecto = Join-Path $env:LOCALAPPDATA "Android\Sdk"
    if (Test-Path $propiedades) {
        Copy-Item $propiedades (Join-Path $Destino "local.properties")
        Write-Host "    SDK: se copio local.properties del proyecto"
    } elseif (Test-Path $sdkPorDefecto) {
        "sdk.dir=" + ($sdkPorDefecto -replace '\\', '\\' -replace ':', '\:') |
            Out-File (Join-Path $Destino "local.properties") -Encoding ascii
        Write-Host "    SDK: $sdkPorDefecto"
    } else {
        Write-Host "No se encontro el Android SDK. Abre el proyecto una vez en Android Studio o define ANDROID_HOME." -ForegroundColor Red
        exit 1
    }
}

$log = Join-Path $Destino "verificacion-TT21.txt"
"TT-21 - Verificacion desde copia limpia" | Out-File $log -Encoding utf8
"Fecha: $(Get-Date -Format 'yyyy-MM-dd HH:mm')" | Out-File $log -Append -Encoding utf8
"Commit: $(git rev-parse --short HEAD) ($Rama)" | Out-File $log -Append -Encoding utf8

function Paso($titulo, $tareas) {
    Write-Host $titulo
    cmd /c ".\gradlew.bat $($tareas -join ' ') --console=plain 2>&1" | Tee-Object -FilePath $log -Append
    if ($LASTEXITCODE -ne 0) {
        "RESULTADO: FALLO en '$titulo'" | Out-File $log -Append -Encoding utf8
        Write-Host "Fallo: $titulo. Revisa $log" -ForegroundColor Red
        exit $LASTEXITCODE
    }
}

Paso "2/4 Compilando la app (assembleDebug)" @("clean", "assembleDebug")
Paso "3/4 Pruebas JVM (testDebugUnitTest)" @("testDebugUnitTest")
if ($ConEmulador) {
    Paso "4/4 Pruebas en emulador (connectedDebugAndroidTest)" @("connectedDebugAndroidTest")
} else {
    Write-Host "4/4 Pruebas en emulador omitidas (usa -ConEmulador)"
}

"RESULTADO: BUILD SUCCESSFUL" | Out-File $log -Append -Encoding utf8
Write-Host "Listo. Resumen en $log" -ForegroundColor Green
foreach ($reporte in @("app\build\reports\tests\testDebugUnitTest\index.html",
                       "app\build\reports\androidTests\connected\debug\index.html")) {
    if (Test-Path $reporte) { Invoke-Item $reporte }
}
