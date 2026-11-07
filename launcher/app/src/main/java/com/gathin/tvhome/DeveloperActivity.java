package com.gathin.tvhome;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;

/**
 * JUONG TV - Developer Mode & Diagnostics Screen.
 * Provides real-time hardware diagnostics, architecture details, RAM, storage, and safe developer tools.
 */
public final class DeveloperActivity extends Activity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LauncherLocale.wrap(base));
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_developer);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        TextView specs = findViewById(R.id.tvDeviceSpecs);
        specs.setText(buildDiagnosticsInfo());

        findViewById(R.id.btnCleanRam).setOnClickListener(v -> {
            System.gc();
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            if (am != null) {
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                am.getMemoryInfo(mi);
                long availMb = mi.availMem / (1024 * 1024);
                Toast.makeText(this, getString(R.string.jtv_clean_ram_done, availMb), Toast.LENGTH_SHORT).show();
            }
            specs.setText(buildDiagnosticsInfo());
        });

        findViewById(R.id.btnLaunchSmartTube4K).setOnClickListener(v -> {
            Intent intent = getPackageManager().getLaunchIntentForPackage("org.smarttube.stable");
            if (intent != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "SmartTube chưa được cài đặt", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnDisplaySettings).setOnClickListener(v -> {
            Intent disp = SystemSettings.displayIntent(this);
            try {
                startActivity(disp);
            } catch (Exception e) {
                try {
                    startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS));
                } catch (Exception e2) {
                    Toast.makeText(this, R.string.not_available, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btnSysDevOptions).setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (Exception e) {
                Toast.makeText(this, R.string.not_available, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String buildDiagnosticsInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("⚡ JUONG TV - GIÁM SÁT & HIỆU NĂNG HỆ THỐNG\n\n");

        // Device & Model
        sb.append("• Thiết bị: Xiaomi ").append(Build.MODEL)
                .append(" (").append(Build.PRODUCT).append(" / ").append(Build.DEVICE).append(")\n");
        sb.append("• Hệ điều hành: Android ").append(Build.VERSION.RELEASE)
                .append(" (API Level ").append(Build.VERSION.SDK_INT).append(")\n");
        sb.append("• Chip xử lý: Amlogic 64-bit SoC (Chạy nhân 32-bit armeabi-v7a tối ưu RAM)\n\n");

        // Memory (RAM)
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (am != null) {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            long availMb = mi.availMem / (1024 * 1024);
            long totalMb = mi.totalMem / (1024 * 1024);
            long usedMb = totalMb - availMb;
            int pct = totalMb > 0 ? (int) ((usedMb * 100) / totalMb) : 0;
            sb.append("• Bộ nhớ RAM: ").append(availMb).append(" MB trống / ").append(totalMb).append(" MB tổng (Đang dùng: ").append(pct).append("%)\n");
            sb.append("  Trạng thái: ").append(mi.lowMemory ? "⚠️ Cảnh báo thiếu RAM - Hãy dọn RAM!" : "✅ Ổn định (Mượt mà)").append("\n\n");
        }

        // Storage
        try {
            File dataDir = Environment.getDataDirectory();
            StatFs stat = new StatFs(dataDir.getPath());
            long freeGb = stat.getAvailableBytes() / (1024 * 1024 * 1024);
            long totalGb = stat.getTotalBytes() / (1024 * 1024 * 1024);
            sb.append("• Bộ nhớ trong (ROM): ").append(freeGb).append(" GB trống / ").append(totalGb).append(" GB tổng\n\n");
        } catch (Throwable ignored) {
        }

        // Display & 4K UHD Capabilities
        sb.append("• MÀN HÌNH & KHẢ NĂNG PHÁT VIDEO 4K UHD:\n");
        sb.append("  - Tấm nền vật lý: 55 inch 4K Ultra HD (3840 x 2160 @ 60Hz) HKC Display\n");
        sb.append("  - Giao diện Android UI: 1920 x 1080 (Chuẩn nhẹ máy 60fps)\n");
        sb.append("  - Bộ giải mã phần cứng: OMX.amlogic.hevc (H.265 4K) & OMX.amlogic.avc (H.264 4K)\n");
        sb.append("  - Hướng dẫn xem YouTube 4K: Mở SmartTube -> Cài đặt -> Trình phát video -> Định dạng video -> Bật 'Mở khóa tất cả độ phân giải' và chọn định dạng HEVC/AVC. Video sẽ phát chuẩn 2160p siêu nét!\n\n");

        // ADB Status
        try {
            int adb = Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED, 0);
            sb.append("• Cổng gỡ lỗi ADB (5555): ").append(adb == 1 ? "Đang bật" : "Đã tắt").append("\n");
        } catch (Throwable ignored) {
        }

        return sb.toString();
    }
}
