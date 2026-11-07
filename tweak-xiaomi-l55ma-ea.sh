#!/usr/bin/env bash
# ==============================================================================
# Script Tối Ưu Hóa Chuyên Sâu Cho Xiaomi TV EA55 (Mã hiệu: L55MA-EA)
# ==============================================================================
set -euo pipefail

echo "=================================================================="
echo "   BỘ CÔNG CỤ TỐI ƯU HÓA SIÊU TỐC CHO XIAOMI TV EA55 (L55MA-EA)   "
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

echo "[*] Đang tiến hành Debloat..."
grep -v '^[[:space:]]*#' "$DEBLOAT_FILE" | grep -v '^[[:space:]]*$' | while read -r pkg; do
    # Bảo vệ an toàn
    case "$pkg" in
        *mitv.service*|*bluetooth*|*com.mitv.tvhome*)
            echo "    [BẢO VỆ] Bỏ qua: $pkg"
            continue
            ;;
    esac
    adb -s "$TV_HOST" shell pm disable-user --user 0 "$pkg" 2>&1 | grep -q 'disabled' && echo "    [OK] Đã tắt: $pkg" || true
done

echo "[*] Áp dụng các tinh chỉnh hệ thống..."
adb -s "$TV_HOST" shell settings put system boot_ad_switch 0
adb -s "$TV_HOST" shell settings put global window_animation_scale 0.5
adb -s "$TV_HOST" shell settings put global transition_animation_scale 0.5
adb -s "$TV_HOST" shell settings put global animator_duration_scale 0.5
adb -s "$TV_HOST" shell setprop persist.sys.timezone Asia/Ho_Chi_Minh
adb -s "$TV_HOST" shell service call alarm 3 s16 Asia/Ho_Chi_Minh

echo "=================================================================="
echo "               TỐI ƯU HÓA HOÀN TẤT THÀNH CÔNG!                   "
echo "=================================================================="
read -rp "Bạn có muốn khởi động lại TV ngay bây giờ không? (y/n) " choice
if [[ "$choice" =~ ^[yY]$ ]]; then
    adb -s "$TV_HOST" reboot
fi

