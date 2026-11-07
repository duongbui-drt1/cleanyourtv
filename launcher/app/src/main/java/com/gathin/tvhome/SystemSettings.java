package com.gathin.tvhome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;

/** The standard settings intent, or, when nothing usable answers it, the first vendor settings entry
 *  from settings_fallback_components that is installed (Xiaomi China, TCL / FFALCON, Fire OS), else the
 *  AOSP TV settings app's main screen where it is installed. */
final class SystemSettings {
    private SystemSettings() {}

    static Intent intent(Context context, String action) {
        PackageManager pm = context.getPackageManager();
        // Priority for Xiaomi TV network/connectivity actions:
        if (Settings.ACTION_WIFI_SETTINGS.equals(action) || "android.settings.NETWORK_OPERATOR_SETTINGS".equals(action)) {
            Intent xiaomiWifi = new Intent().setComponent(new ComponentName("com.xiaomi.mitv.settings", "com.xiaomi.mitv.settings.wifi.MitvWifiActivity"));
            if (pm.resolveActivity(xiaomiWifi, 0) != null) return xiaomiWifi;
            Intent aospNet = new Intent().setComponent(new ComponentName("com.android.tv.settings", "com.android.tv.settings.connectivity.NetworkActivity"));
            if (pm.resolveActivity(aospNet, 0) != null) return aospNet;
        } else if (Settings.ACTION_BLUETOOTH_SETTINGS.equals(action)) {
            Intent xiaomiBt = new Intent().setComponent(new ComponentName("com.xiaomi.mitv.settings", "com.xiaomi.mitv.settings.bluetooth.MitvBluetoothActivity"));
            if (pm.resolveActivity(xiaomiBt, 0) != null) return xiaomiBt;
        }

        Intent standard = new Intent(action);
        ResolveInfo resolved = pm.resolveActivity(standard, PackageManager.MATCH_DEFAULT_ONLY);
        // Fire OS resolves the standard actions to a CTS compliance stub that opens nothing;
        // treat it as missing so the vendor fallback runs.
        if (resolved != null && !resolved.activityInfo.name.contains("CTSDummy")) return standard;
        Intent vendor = vendorIntent(context);
        if (vendor != null) return vendor;
        // Some firmware ships the AOSP TV settings app with no filter for the standard actions (HY300 Pro+
        // projector, rk3566, Android 14): open its main screen directly. Not a vendor entry, so the Settings
        // menu's "Native settings" row stays as it was.
        Intent aosp = new Intent().setComponent(AOSP_TV_SETTINGS);
        try {
            pm.getPackageInfo(AOSP_TV_SETTINGS.getPackageName(), 0);
            return aosp;
        } catch (PackageManager.NameNotFoundException e) {
            return standard;
        }
    }

    private static final ComponentName AOSP_TV_SETTINGS =
            new ComponentName("com.android.tv.settings", "com.android.tv.settings.MainSettings");

    /** The first vendor settings entry from settings_fallback_components that is installed, or null
     *  when none is. China-market firmware keeps pages the AOSP TV settings has no row for -
     *  Xiaomi's Account & Security, where the ADB switch lives - in the vendor's own settings app;
     *  MainActivity's Settings menu offers it as "Native settings" while one is installed. */
    static Intent vendorIntent(Context context) {
        PackageManager pm = context.getPackageManager();
        for (String value : context.getResources().getStringArray(R.array.settings_fallback_components)) {
            ComponentName component = ComponentName.unflattenFromString(value);
            if (component == null) continue;
            // Explicit resolveActivity() always succeeds, so check the package exists instead.
            try {
                pm.getPackageInfo(component.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                continue;
            }
            return new Intent().setComponent(component);
        }
        return null;
    }

    /** The TV's App info page for pkg, or null where the firmware has none (CVTE's settings app, #1202). */
    static Intent appInfo(Context context, String pkg) {
        Intent info = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + pkg));
        return context.getPackageManager().resolveActivity(info, PackageManager.MATCH_DEFAULT_ONLY) != null ? info : null;
    }

    /**
     * The TV's display or picture settings. On Xiaomi sets, the AOSP DISPLAY_SETTINGS action resolves to
     * an empty stub in DroidTvSettings; prefer Xiaomi's own PictureActivity where installed.
     */
    static Intent displayIntent(Context context) {
        ComponentName xiaomiDisplay = new ComponentName("com.xiaomi.mitv.settings",
                "com.xiaomi.mitv.settings.entry.DisplaySoundActivity");
        if (context.getPackageManager().resolveActivity(new Intent().setComponent(xiaomiDisplay), 0) != null) {
            return new Intent("com.xiaomi.mitv.settings.display_settings").setComponent(xiaomiDisplay);
        }
        return intent(context, Settings.ACTION_DISPLAY_SETTINGS);
    }

    /** Bluetooth settings intent. */
    static Intent bluetoothIntent(Context context) {
        return intent(context, Settings.ACTION_BLUETOOTH_SETTINGS);
    }

    /** Sound settings intent. */
    static Intent soundIntent(Context context) {
        ComponentName xiaomiSound = new ComponentName("com.xiaomi.mitv.settings",
                "com.xiaomi.mitv.settings.entry.DisplaySoundActivity");
        if (context.getPackageManager().resolveActivity(new Intent().setComponent(xiaomiSound), 0) != null) {
            return new Intent("com.xiaomi.mitv.settings.sound_settings").setComponent(xiaomiSound);
        }
        return intent(context, Settings.ACTION_SOUND_SETTINGS);
    }

    /** Xiaomi TV HDMI / External source switch intent (HDMI 1=23, HDMI 2=24, HDMI 3=25, AV=2). */
    static Intent xiaomiInputIntent(int inputId) {
        Intent intent = new Intent("com.xiaomi.mitv.tvplayer.EXTSRC_PLAY");
        intent.setComponent(new ComponentName("com.xiaomi.mitv.tvplayer", "com.xiaomi.mitv.tvplayer.ExternalSourceActivity"));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("input", inputId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /** Xiaomi TV general input source chooser overlay. */
    static Intent xiaomiSourcePickerIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName("com.xiaomi.mitv.tvplayer", "com.xiaomi.mitv.tvplayer.ExternalSourceActivity"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
