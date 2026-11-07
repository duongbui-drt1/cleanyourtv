#!/usr/bin/env bash
# ==============================================================================
# Script Khôi Phục (Restore) Cho Xiaomi TV EA55 (Mã hiệu: L55MA-EA)
# ==============================================================================
set -euo pipefail

echo "=================================================================="
echo "     BỘ CÔNG CỤ KHÔI PHỤC (RESTORE) CHO XIAOMI TV L55MA-EA        "
echo "=================================================================="

TV_IP="${1:-}"
if [ -z "$TV_IP" ]; then
    read -rp "Nhập địa chỉ IP của Xiaomi TV: " TV_IP
fi

if [ -z "$TV_IP" ]; then
    echo "[!] Chưa nhập địa chỉ IP. Thoát."
    exit 1
fi

TV_HOST="${TV_IP}:5555"
echo "[*] Đang kết nối tới Xiaomi TV tại $TV_HOST..."
adb disconnect "$TV_HOST" 2>/dev/null || true
adb connect "$TV_HOST"

DEBLOAT_FILE="$(dirname "$0")/examples/xiaomi-tv-ea55-l55ma-ea/debloat.txt"

echo "[*] Đang bật lại các ứng dụng đã tắt..."
grep -v '^[[:space:]]*#' "$DEBLOAT_FILE" | grep -v '^[[:space:]]*$' | while read -r pkg; do
    adb -s "$TV_HOST" shell pm enable "$pkg" 2>&1 | grep -q 'enabled' && echo "    [RESTORED] Đã bật lại: $pkg" || true
done

echo "=================================================================="
echo "               KHÔI PHỤC HOÀN TẤT THÀNH CÔNG!                     "
echo "=================================================================="
