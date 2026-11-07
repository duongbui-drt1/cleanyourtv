@echo off
title DIEU KHIEN MAN HINH XIAOMI TV (SCRCPY)
cd /d "C:\Users\Juong\AppData\Local\Microsoft\WinGet\Packages\Genymobile.scrcpy_Microsoft.Winget.Source_8wekyb3d8bbwe\scrcpy-win64-v5.0.1"

set "TV_IP=192.168.1.77"
if not "%~1"=="" set "TV_IP=%~1"

echo ==================================================================
echo         MO DIEU KHIEN MAN HINH XIAOMI TV TREN MAY TINH            
echo ==================================================================
echo [*] Dang ket noi toi Xiaomi TV (%TV_IP%:5555)...
adb connect %TV_IP%:5555

echo [*] Dang khoi dong cua so dieu khien TV bang chuot va ban phim...
echo.
echo [!] Phim tat dieu khien:
echo     - Chuot trai      : Click / Chon
echo     - Chuot phai      : Nut BACK (Quay lai)
echo     - Con lan giua    : Nut HOME (Ve man hinh chinh)
echo     - Phim mui ten    : D-pad Len / Xuong / Trai / Phai
echo     - Phim Enter      : Nut OK
echo     - Go ban phim     : Nhap text truc tiep vao TV rat nhanh!
echo.
scrcpy.exe -s %TV_IP%:5555 --no-audio --max-size 1920 --video-bit-rate 12M --window-title="Xiaomi TV Remote - JUONG TV"
pause
