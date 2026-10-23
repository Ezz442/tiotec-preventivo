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

if (Test-Path $Destino) { Remove-Item $Destino -Recurse -Force }
Write-Host "1/4 Clonando $repo ($Rama) en $Destino"
git clone --depth 1 --branch $Rama $repo $Destino 2>&1 | Out-Host
if ($LASTEXITCODE -ne 0) { Write-Host "No se pudo clonar el repositorio." -ForegroundColor Red; exit 1 }
Set-Location $Destino

$log = Join-Path $Destino "verificacion-TT21.txt"
"TT-21 - Verificacion desde copia limpia" | Out-File $log -Encoding utf8
"Fecha: $(Get-Date -Format 'yyyy-MM-dd HH:mm')" | Out-File $log -Append -Encoding utf8
"Commit: $(git rev-parse --short HEAD) ($Rama)" | Out-File $log -Append -Encoding utf8

function Paso($titulo, $tareas) {
    Write-Host $titulo
    & .\gradlew.bat @tareas --console=plain 2>&1 | Tee-Object -FilePath $log -Append
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
