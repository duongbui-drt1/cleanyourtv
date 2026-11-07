@echo off
chcp 65001 >nul
title Tối Ưu Hóa Xiaomi TV EA55 (L55MA-EA) - CleanYourTV
cls
echo ==================================================================
echo    BỘ CÔNG CỤ TỐI ƯU HÓA SIÊU TỐC CHO XIAOMI TV EA55 (L55MA-EA)
echo ==================================================================
echo.

set /p TV_IP="Nhập địa chỉ IP của Xiaomi TV (Ví dụ: 192.168.1.50): "

if "%TV_IP%"=="" (
    echo [!] Bạn chưa nhập IP của TV.
    echo.
    pause
    exit /b
)

echo.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0tweak-xiaomi-l55ma-ea.ps1" -Ip "%TV_IP%"

echo.
echo ==================================================================
echo Bấm phím bất kỳ để đóng cửa sổ...
pause >nul
