@echo off
chcp 65001 >nul
title Kích Hoạt JUONG TV (Apple tvOS + Glass UI)
cls
echo ==================================================================
echo       KÍCH HOẠT GIAO DIỆN JUONG TV (APPLE tvOS + GLASS UI)
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
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0apply-juong-tv.ps1" -Ip "%TV_IP%"

echo.
echo ==================================================================
echo Bấm phím bất kỳ để đóng cửa sổ...
pause >nul
