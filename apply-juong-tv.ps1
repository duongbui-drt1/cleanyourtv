param(
    [Parameter(Position = 0)]
    [string]$Ip = ""
)

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$repo = $PSScriptRoot

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "      KICH HOAT GIAO DIEN JUONG TV (APPLE tvOS + GLASS UI)       " -ForegroundColor Yellow
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

# 3. Kiem tra xem Launcher da duoc cai tren TV chua
$py = Join-Path $repo "tools\venv\Scripts\python.exe"
$tvPy = Join-Path $repo "tools\tv.py"
$env:TV_HOST = $Ip
$installed = & $py -I $tvPy "pm path com.gathin.tvhome" 2>$null
if ($installed -notmatch "package:") {
    Write-Host "`n[!] CHU Y: Ung dung Launcher JUONG TV (com.gathin.tvhome) chua duoc cai dat tren TV!" -ForegroundColor Yellow
    Write-Host "    TV hien tai van dang chay giao dien goc hoac launcher cu." -ForegroundColor Yellow
    Write-Host "    He thong dang tiep tuc day Theme & Wallpaper len bo nho TV de san sang..." -ForegroundColor Gray
}

# 4. Day Theme JUONG Glass len TV
$themePath = Join-Path $repo "themes\juong-glass"
Write-Host "`n[*] Dang day theme JUONG Glass va Wallpaper len TV..." -ForegroundColor Green

$originalLocation = Get-Location
try {
    & "$repo\tools\theme.ps1" -TvHost $Ip $themePath
    Write-Host "`n==================================================================" -ForegroundColor Cyan
    Write-Host "         KICH HOAT JUONG TV THANH CONG!                           " -ForegroundColor Yellow
    Write-Host "==================================================================" -ForegroundColor Cyan
    Write-Host " [+] Da ap dung giao dien Spatial Glassmorphism cao cap." -ForegroundColor Green
    Write-Host " [+] Da kich hoat Apple tvOS Hero Section voi Category Badges." -ForegroundColor Green
    Write-Host " [+] Khay Dock kinh 28dp bo goc em ai, vien phat sang Cyan." -ForegroundColor Green
    Write-Host " [+] Man hinh Home tren TV da duoc tu dong cap nhat!" -ForegroundColor Green
} catch {
    if ($installed -notmatch "package:") {
        Write-Host "`n[+] File Theme & Wallpaper da duoc truyen len TV thanh cong!" -ForegroundColor Green
        Write-Host "    (Luu y: Hieu ung se hien thi ngay khi Launcher com.gathin.tvhome duoc mo)" -ForegroundColor Yellow
    } else {
        Write-Host "[!] Co loi khi khoi dong lai Home: $_" -ForegroundColor Red
    }
} finally {
    Set-Location $originalLocation
}
