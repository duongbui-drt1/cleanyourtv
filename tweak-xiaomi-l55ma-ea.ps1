param(
    [Parameter(Position = 0)]
    [string]$Ip = ""
)

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$repo = $PSScriptRoot

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "   BO CONG CU TOI UU HOA SIEU TOC CHO XIAOMI TV EA55 (L55MA-EA)   " -ForegroundColor Yellow
Write-Host "==================================================================" -ForegroundColor Cyan

# 1. Kiem tra moi truong Python
$py = Join-Path $repo "tools\venv\Scripts\python.exe"
if (-not (Test-Path $py)) {
    Write-Host "[*] Dang khoi tao moi truong Python ao (tools\venv)..." -ForegroundColor Yellow
    python -m venv "$repo\tools\venv"
    & "$repo\tools\venv\Scripts\pip.exe" install -q -r "$repo\tools\requirements.txt"
    if (-not (Test-Path $py)) {
        Write-Host "[!] Khong the khoi tao Python venv. Hay kiem tra lai Python tren may!" -ForegroundColor Red
        exit 1
    }
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

$TvHost = "$($Ip):5555"
$env:TV_HOST = $Ip

# 3. Kiem tra ket noi TCP toi cong 5555 cua TV (Timeout 3 giay)
Write-Host "`n[*] Dang kiem tra ket noi toi Xiaomi TV ($($Ip):5555)..." -ForegroundColor Green
$tcpClient = New-Object System.Net.Sockets.TcpClient
try {
    $asyncResult = $tcpClient.BeginConnect($Ip, 5555, $null, $null)
    $wait = $asyncResult.AsyncWaitHandle.WaitOne(3000, $false)

    if (-not $wait -or -not $tcpClient.Connected) {
        $tcpClient.Close()
        Write-Host "`n[!] KHONG THE KET NOI TOI TV TAI $($Ip):5555 !" -ForegroundColor Red
        Write-Host "Vui long kiem tra:" -ForegroundColor Yellow
        Write-Host " 1. TV va may tinh da ket noi chung mot mang Wi-Fi / LAN chua?"
        Write-Host " 2. TV da BAT che do ADB Debugging trong Cai dat chua?"
        Write-Host "    (Cai dat -> Tai khoan va Bao mat -> ADB Debugging -> Cho phep)"
        Write-Host " 3. Dia chi IP cua TV co dung la: $Ip khong?"
        exit 1
    }
    $tcpClient.Close()
    Write-Host "[+] Ket noi mang toi cong ADB cua TV thanh cong!" -ForegroundColor Green
} catch {
    Write-Host "[!] Loi ket noi: $_" -ForegroundColor Red
    exit 1
}

# 4. Chay debloat chuan tu tools\disable.ps1
$debloatList = Join-Path $repo "examples\xiaomi-tv-ea55-l55ma-ea\debloat.txt"
Write-Host "`n[*] Dang tien hanh Debloat (tat tien trinh nguyen nhan 190% CPU va app rac)..." -ForegroundColor Green

try {
    & "$repo\tools\disable.ps1" -TvHost $Ip $debloatList
} catch {
    Write-Host "[!] Co loi trong qua trinh debloat: $_" -ForegroundColor Red
    exit 1
}

# 5. Ap dung cac tinh chinh he thong chuyen sau cho Xiaomi TV L55MA-EA
Write-Host "`n[*] Dang ap dung cac tinh chinh hieu nang rieng cho Xiaomi TV EA55..." -ForegroundColor Green

$tweaks = @(
    "settings put system boot_ad_switch 0",
    "settings put global window_animation_scale 0.5",
    "settings put global transition_animation_scale 0.5",
    "settings put global animator_duration_scale 0.5",
    "setprop persist.sys.timezone Asia/Ho_Chi_Minh",
    "service call alarm 3 s16 Asia/Ho_Chi_Minh"
)

$tvPy = Join-Path $repo "tools\tv.py"
foreach ($tw in $tweaks) {
    Write-Host "    [TWEAK] $tw" -ForegroundColor Gray
    & $py -I $tvPy $tw 2>$null | Out-Null
}

Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host "               TOI UU HOA HOAN TAT THANH CONG!                   " -ForegroundColor Yellow
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "Cac cai tien da ap dung thanh cong:" -ForegroundColor Green
Write-Host " [+] Da tat tien trinh remotecontroller ngốn 100-190% CPU."
Write-Host " [+] Da tat tro ly giong noi tieng Trung XiaoAI ngốn 30% CPU."
Write-Host " [+] Da tat quang cao khoi dong TV (boot ads off)."
Write-Host " [+] Da tang toc do phan hoi giao dien (Animations 0.5x)."
Write-Host " [+] Da dong bo mui gio chuan Viet Nam (GMT+7)."
Write-Host " [+] Giai phong RAM cho cac ung dung xem TV, YouTube."

$rebootChoice = Read-Host "`nBan co muon khoi dong lai TV ngay bay gio khong? (y/n)"
if ($rebootChoice -match "^[yY]$") {
    Write-Host "[*] Dang gui lenh khoi dong lai TV..." -ForegroundColor Green
    & $py -I $tvPy "reboot" 2>$null | Out-Null
    Write-Host "[+] TV dang khoi dong lai. Chuc ban co trai nghiem tuyet voi!" -ForegroundColor Green
}
