@echo off
title MO KHOA XEM VIDEO 4K UHD TREN XIAOMI TV
chcp 65001 >nul

set "TV_IP=192.168.1.86"
if not "%~1"=="" set "TV_IP=%~1"

echo ==================================================================
echo         HUONG DAN MO KHOA XEM 4K UHD TREN XIAOMI TV 55 INCH        
echo ==================================================================
echo.
echo [1] THONG SO PHAN CUNG XIAOMI TV L55MA-EA:
echo     - Tam nen vat ly: 55 inch 4K Ultra HD (3840 x 2160 @ 60Hz) HKC.
echo     - Chip Amlogic ho tro phan cung HEVC / AVC 4K 60fps.
echo.
echo [2] NGUYEN NHAN YOUTUBE GOC BI KHOA 1080P:
echo     - Ung dung YouTube goc cua Google kiem tra chung chi Widevine va
echo       chi mo 4K cho codec VP9 tren cac thiet bi duoc Google cap phep.
echo.
echo [3] CACH MO KHOA 4K BANG SMARTTUBE (DA CAI SAN TREN TV):
echo     1. Mo SmartTube tren TV.
echo     2. Vao Cai dat (Bieu tuong banh rang ben trai) ^> Trinh phat video.
echo     3. Chon 'Dinh dang video' (Video formats).
echo     4. Chon 'Do phan giai mac dinh' ^> Chon 'Tot nhat (4K 2160p)'.
echo     5. Bat tuy chon 'Mo khoa tat ca dinh dang / Unlock all formats'.
echo.
echo Dang mo SmartTube tren TV (%TV_IP%)...
adb -s %TV_IP%:5555 shell "am start -n org.smarttube.stable/.MainActivity"
echo.
echo [OK] Da mo SmartTube! Ban co the mo bat ky video 4K nao de thuong thuc.
pause
