@echo off
chcp 65001 >nul
title Cài Đặt Ứng Dụng Thiết Yếu Cho Xiaomi TV
cls
echo ==================================================================
echo    CÀI ĐẶT ỨNG DỤNG THIẾT YẾU CHO XIAOMI TV (SMARTTUBE, TV BRO)
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
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0install-essential-apps.ps1" -Ip "%TV_IP%"

echo.
echo ==================================================================
echo Bấm phím bất kỳ để đóng cửa sổ...
pause >nul
