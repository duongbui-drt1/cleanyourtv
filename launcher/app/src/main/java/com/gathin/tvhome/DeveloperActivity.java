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

        findViewById(R.id.btnSysDevOptions).setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (Exception e) {
                Toast.makeText(this, R.string.not_available, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnReloadTheme).setOnClickListener(v -> {
            // Signal MainActivity to reload theme on next foreground
            setResult(RESULT_OK);
            Toast.makeText(this, "JUONG TV Theme reloaded", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private String buildDiagnosticsInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== JUONG TV HARDWARE DIAGNOSTICS ===\n\n");

        // Device & Model
        sb.append("• Model: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL)
                .append(" (").append(Build.PRODUCT).append(" / ").append(Build.DEVICE).append(")\n");

        // Android OS & SDK
        sb.append("• Android OS: Version ").append(Build.VERSION.RELEASE)
                .append(" (API Level ").append(Build.VERSION.SDK_INT).append(")\n");
        sb.append("• Build Fingerprint: ").append(Build.FINGERPRINT).append("\n");

        // CPU & ABI
        sb.append("• CPU ABIs: ").append(Arrays.toString(Build.SUPPORTED_ABIS)).append("\n");
        boolean is32BitOnly = true;
        for (String abi : Build.SUPPORTED_ABIS) {
            if (abi.contains("arm64") || abi.contains("x86_64")) {
                is32BitOnly = false;
                break;
            }
        }
        sb.append("• Architecture Status: ").append(is32BitOnly ? "32-bit (armeabi-v7a)" : "64-bit capable").append("\n\n");

        // Display
        DisplayMetrics dm = getResources().getDisplayMetrics();
        sb.append("• Display Resolution: ").append(dm.widthPixels).append(" x ").append(dm.heightPixels)
                .append(" (Density: ").append(dm.densityDpi).append(" dpi)\n");

        // Memory (RAM)
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        if (am != null) {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            long availMb = mi.availMem / (1024 * 1024);
            long totalMb = mi.totalMem / (1024 * 1024);
            sb.append("• RAM: ").append(availMb).append(" MB free / ").append(totalMb).append(" MB total")
                    .append(mi.lowMemory ? " (LOW MEMORY ALERT!)" : " (Optimal)").append("\n");
        }

        // Storage
        try {
            File dataDir = Environment.getDataDirectory();
            StatFs stat = new StatFs(dataDir.getPath());
            long freeGb = stat.getAvailableBytes() / (1024 * 1024 * 1024);
            long totalGb = stat.getTotalBytes() / (1024 * 1024 * 1024);
            sb.append("• Internal Storage: ").append(freeGb).append(" GB free / ").append(totalGb).append(" GB total\n\n");
        } catch (Throwable ignored) {
        }

        // Home activity
        Intent homeIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo home = getPackageManager().resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY);
        sb.append("• Current HOME Resolver: ").append(home != null ? home.activityInfo.packageName : "Unknown").append("\n");

        // ADB Status
        try {
            int adb = Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED, 0);
            sb.append("• Network ADB Port 5555: ").append(adb == 1 ? "Active / Enabled" : "Disabled / Closed").append("\n");
        } catch (Throwable ignored) {
        }

        return sb.toString();
    }
}
