param(
    [Parameter(Position = 0)]
    [string]$Ip = ""
)

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$repo = $PSScriptRoot

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "     BO CONG CU KHOI PHUC (RESTORE) CHO XIAOMI TV L55MA-EA        " -ForegroundColor Yellow
Write-Host "==================================================================" -ForegroundColor Cyan

# 1. Kiem tra moi truong Python
$py = Join-Path $repo "tools\venv\Scripts\python.exe"
if (-not (Test-Path $py)) {
    Write-Host "[*] Dang khoi tao moi truong Python ao..." -ForegroundColor Yellow
    python -m venv "$repo\tools\venv"
    & "$repo\tools\venv\Scripts\pip.exe" install -q -r "$repo\tools\requirements.txt"
}

# 2. Nhap IP TV
if (-not $Ip) {
    $Ip = Read-Host "Nhap dia chi IP cua Xiaomi TV (Vi du: 192.168.1.50)"
}
$Ip = $Ip.Trim()

if (-not $Ip) {
    Write-Host "[!] Ban chua nhap IP cua TV. Huy bo." -ForegroundColor Red
    exit 1
}

# 3. Kiem tra ket noi TCP toi cong 5555 cua TV (Timeout 3s)
Write-Host "`n[*] Dang kiem tra ket noi toi Xiaomi TV ($($Ip):5555)..." -ForegroundColor Green
$tcpClient = New-Object System.Net.Sockets.TcpClient
try {
    $asyncResult = $tcpClient.BeginConnect($Ip, 5555, $null, $null)
    $wait = $asyncResult.AsyncWaitHandle.WaitOne(3000, $false)

    if (-not $wait -or -not $tcpClient.Connected) {
        $tcpClient.Close()
        Write-Host "`n[!] KHONG THE KET NOI TOI TV TAI $($Ip):5555 !" -ForegroundColor Red
        Write-Host "Vui long kiem tra ket noi mang va bat ADB Debugging tren TV." -ForegroundColor Yellow
        exit 1
    }
    $tcpClient.Close()
} catch {
    Write-Host "[!] Loi ket noi: $_" -ForegroundColor Red
    exit 1
}

$debloatList = Join-Path $repo "examples\xiaomi-tv-ea55-l55ma-ea\debloat.txt"
Write-Host "`n[*] Dang khoi phuc lai cac ung dung / dich vu goc cho Xiaomi TV..." -ForegroundColor Green

try {
    & "$repo\tools\restore.ps1" -TvHost $Ip -Stock "com.mitv.tvhome" $debloatList
    Write-Host "`n[+] Khoi phuc hoan tat! TV da tro lai trang thai ban dau." -ForegroundColor Green
} catch {
    Write-Host "[!] Co loi trong qua trinh khoi phuc: $_" -ForegroundColor Red
}
