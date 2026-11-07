param(
    [Parameter(Position = 0)]
    [string]$Ip = ""
)

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$repo = $PSScriptRoot

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "    CAI DAT UNG DUNG THIET YEU CHO XIAOMI TV (SMARTTUBE, TV BRO)   " -ForegroundColor Yellow
Write-Host "==================================================================" -ForegroundColor Cyan

# 1. Kiem tra IP TV
if (-not $Ip) {
    $Ip = Read-Host "Nhap dia chi IP cua Xiaomi TV (Vi du: 192.168.1.50)"
}
$Ip = $Ip.Trim()

if (-not $Ip) {
    Write-Host "[!] Ban chua nhap IP cua TV. Huy bo." -ForegroundColor Red
    exit 1
}

# 2. Kiem tra ket noi TCP toi cong 5555 cua TV
Write-Host "`n[*] Dang kiem tra ket noi toi Xiaomi TV ($($Ip):5555)..." -ForegroundColor Green
$tcpClient = New-Object System.Net.Sockets.TcpClient
try {
    $asyncResult = $tcpClient.BeginConnect($Ip, 5555, $null, $null)
    $wait = $asyncResult.AsyncWaitHandle.WaitOne(3000, $false)

    if (-not $wait -or -not $tcpClient.Connected) {
        $tcpClient.Close()
        Write-Host "`n[!] KHONG THE KET NOI TOI TV TAI $($Ip):5555 !" -ForegroundColor Red
        Write-Host "Vui long kiem tra TV da bat va da mo ADB Debugging trong Cai dat." -ForegroundColor Yellow
        exit 1
    }
    $tcpClient.Close()
} catch {
    Write-Host "[!] Loi ket noi: $_" -ForegroundColor Red
    exit 1
}

$py = Join-Path $repo "tools\venv\Scripts\python.exe"
$tvPy = Join-Path $repo "tools\tv.py"
$env:TV_HOST = $Ip

$downloadDir = Join-Path $repo "downloads"
if (-not (Test-Path $downloadDir)) {
    New-Item -ItemType Directory -Path $downloadDir | Out-Null
}

$apps = @(
    @{
        Name = "SmartTube (YouTube TV Khong Quang Cao)";
        Pkg = "com.teamsmart.videomanager.tv";
        Url = "https://github.com/yuliskov/SmartTube/releases/download/latest/smarttube_beta.apk";
        File = "smarttube.apk"
    },
    @{
        Name = "TV Bro (Trinh Duyet TV tot nhat)";
        Pkg = "io.github.truefedex.tvbro";
        Url = "https://github.com/truefedex/tv-bro/releases/download/v2.0.4/tvbro_2.0.4.apk";
        File = "tvbro.apk"
    }
)

foreach ($app in $apps) {
    Write-Host "`n[*] Kiem tra ung dung: $($app.Name)..." -ForegroundColor Cyan
    $installed = & $py -I $tvPy "pm path $($app.Pkg)" 2>$null
    if ($installed -match "package:") {
        Write-Host "    [OK] Ung dung da duoc cai dat tren TV!" -ForegroundColor Green
        continue
    }

    $apkPath = Join-Path $downloadDir $app.File
    if (-not (Test-Path $apkPath)) {
        Write-Host "    [*] Dang tai goi cai dat tu GitHub..." -ForegroundColor Yellow
        try {
            [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
            Invoke-WebRequest -Uri $app.Url -OutFile $apkPath -TimeoutSec 60
            Write-Host "    [+] Tai thanh cong: $($app.File)" -ForegroundColor Green
        } catch {
            Write-Host "    [!] Khong the tai tu link tu dong: $_" -ForegroundColor Red
            continue
        }
    }

    Write-Host "    [*] Dang cai dat qua ADB len TV..." -ForegroundColor Yellow
    $adbCmd = Get-Command "adb" -ErrorAction SilentlyContinue
    if ($adbCmd) {
        adb -s "$($Ip):5555" install -r "$apkPath"
    } else {
        Write-Host "    [!] File APK da duoc tai ve tai: $apkPath" -ForegroundColor Yellow
        Write-Host "    [i] Ban co the copy vao USB de cai dat hoac dung lenh adb install." -ForegroundColor Gray
    }
}

Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host "                 HOAN TAT QUY TRINH HO TRO!                      " -ForegroundColor Yellow
Write-Host "==================================================================" -ForegroundColor Cyan
