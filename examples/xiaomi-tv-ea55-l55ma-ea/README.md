# Hướng Dẫn Tối Ưu Hóa Riêng Cho Xiaomi TV EA55 (Mã hiệu: L55MA-EA)

Dòng TV **Xiaomi EA55 (L55MA-EA)** chạy vi xử lý Amlogic, RAM khiêm tốn 1.5GB - 2GB, và chạy giao diện MIUI for TV / HyperOS nội địa Trung Quốc. Nếu để mặc định, TV sẽ:
1. Bị nghẽn CPU nặng: Tiến trình `com.xiaomi.mitv.remotecontroller.service` bị vòng lặp lỗi kết nối chiếm 100-190% CPU.
2. Trợ lý ảo XiaoAI (`com.xiaomi.wakeupservice`) ngốn liên tục ~30% CPU.
3. Chứa hàng chục ứng dụng rác, quảng cáo khởi động (boot ads), và sai múi giờ tại Việt Nam.

---

## ⚡ Các Tối Ưu Sâu Được Áp Dụng:
- **Giải phóng 150-200% CPU**: Vô hiệu hóa triệt để tiến trình lỗi `remotecontroller.service` và dịch vụ lắng nghe giọng nói tiếng Trung.
- **Tắt quảng cáo khởi động**: `boot_ad_switch 0`.
- **Tăng gấp đôi tốc độ hiệu ứng (Animations 0.5x)**: Giúp TV phản hồi nhanh, không bị giật lag khi bấm điều khiển.
- **Sửa chuẩn múi giờ Việt Nam**: Chuyển từ `Asia/Shanghai` (+8) sang `Asia/Ho_Chi_Minh` (+7).
- **Giải phóng ~400MB - 600MB RAM**: Tắt hơn 35 app rác nội địa Trung Quốc, giải phóng RAM cho YouTube, SmartTube, Netflix và xem TV mượt mà.
- **An toàn tuyệt đối**: Bảo vệ nguyên vẹn driver âm thanh/hình ảnh phần cứng (`mitv.service`), remote Bluetooth, và cổng chuyển HDMI 1/2/3.

---

## 🚀 Cách Thực Hiện Trong 5 Phút (1 Click)

### Bước 1: Bật ADB Debugging trên Xiaomi TV
1. Mở TV, vào mục **Settings (Cài đặt / 设置)** -> **About (Về thiết bị / 关于)**.
2. Bấm liên tục 5-7 lần vào dòng **MIUI TV version (hoặc Build number / 软件版本号)** cho đến khi hiện thông báo đã bật chế độ nhà phát triển.
3. Quay lại **Settings** -> **Account & Security (Tài khoản & Bảo mật / 账号与安全)** -> Bật **ADB Debugging (ADB 调试)** thành **Allow (允许)**.
4. Xem địa chỉ IP của TV trong mục **Network (Mạng / 网络)** (Ví dụ: `192.168.1.15`).

### Bước 2: Chạy Tự Động Trên Máy Tính

#### Cách 1: Trên Windows (Khuyên dùng - Nhanh nhất)
Nhấp đúp chuột vào file `tweak-xiaomi-l55ma-ea.bat` (hoặc mở Terminal/PowerShell gõ):
```powershell
.\tweak-xiaomi-l55ma-ea.ps1 -Ip 192.168.1.xxx
```
*(Thay `192.168.1.xxx` bằng địa chỉ IP TV của bạn)*

#### Cách 2: Trên macOS / Linux
Mở terminal và gõ:
```bash
chmod +x tweak-xiaomi-l55ma-ea.sh
./tweak-xiaomi-l55ma-ea.sh 192.168.1.xxx
```

---

## 📺 Chuyển Cổng HDMI Nhanh Trên Xiaomi TV
Nếu cần chuyển nhanh cổng qua lệnh hoặc launcher bên thứ ba:
- **HDMI 1**: `am start -a com.xiaomi.mitv.tvplayer.EXTSRC_PLAY -n com.xiaomi.mitv.tvplayer/.ExternalSourceActivity --ei input 23`
- **HDMI 2**: `am start -a com.xiaomi.mitv.tvplayer.EXTSRC_PLAY -n com.xiaomi.mitv.tvplayer/.ExternalSourceActivity --ei input 24`
- **HDMI 3**: `am start -a com.xiaomi.mitv.tvplayer.EXTSRC_PLAY -n com.xiaomi.mitv.tvplayer/.ExternalSourceActivity --ei input 25`
- **Cổng AV**: `am start -a com.xiaomi.mitv.tvplayer.EXTSRC_PLAY -n com.xiaomi.mitv.tvplayer/.ExternalSourceActivity --ei input 2`

---

## 🎬 Hướng Dẫn Mở Khóa Xem Video 4K UHD Trên YouTube
Xiaomi TV EA55 sở hữu tấm nền vật lý 4K UHD (3840 x 2160 @ 60Hz) và vi xử lý Amlogic hỗ trợ phần cứng HEVC/AVC 4K.
YouTube thông thường bị giới hạn 1080p do thiếu chứng chỉ Google. Để xem chuẩn 4K siêu nét:
1. Mở ứng dụng **SmartTube** (đã cài sẵn trên TV).
2. Vào **Cài đặt** (Bánh răng) -> **Trình phát video** -> **Định dạng video**.
3. Chọn **Độ phân giải mặc định** -> Chọn **Tốt nhất (4K 2160p)**.
4. Bật tùy chọn **Mở khóa tất cả định dạng** và ưu tiên bộ giải mã **HEVC** hoặc **AVC**.
5. Mọi video 4K trên YouTube giờ đây sẽ hiển thị độ phân giải **2160p** và chạy mượt mà trên TV!


