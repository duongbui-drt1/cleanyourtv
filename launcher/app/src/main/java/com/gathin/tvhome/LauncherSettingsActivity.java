package com.gathin.tvhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Remote-friendly settings owned by the launcher; system settings stay one row away. */
public final class LauncherSettingsActivity extends Activity {
    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LauncherLocale.wrap(base));   // locale
    }

    static final String EXTRA_REORDER = "com.gathin.tvhome.extra.REORDER_FROM_SETTINGS";
    private SharedPreferences choices;
    private final TextView[] columnChoices = new TextView[4];
    private final TextView[] rowChoices = new TextView[4];
    private static final int[] DOCK_OPACITIES = {100, 70, 50, 30, 15, 5};
    private final TextView[] dockChoices = new TextView[DOCK_OPACITIES.length];
    private static final String[] CLOCK_FORMATS = {"system", "12", "24"};
    private final TextView[] clockChoices = new TextView[CLOCK_FORMATS.length];
    private TextView wallpaperChoice;
    private TextView wallpaperIntervalChoice;
    private TextView wallpaperEffectChoice;
    private TextView wallpaperDurationChoice;
    private TextView ambientIntensityChoice;
    private TextView bootLogoChoice;
    private TextView bootLogoDurationChoice;
    private TextView topLogoChoice;
    private TextView customGreetingChoice;
    private TextView wifiPillChoice;
    private TextView powerPillChoice;
    private TextView lunarChoice;
    private TextView calendarWidgetChoice;
    private TextView weatherWidgetChoice;
    private TextView clockFontChoice;
    private TextView dateFontChoice;
    private TextView animationsChoice;
    private TextView advancedChoice;
    private TextView followThemeChoice;
    private TextView openOnStartChoice;
    private int columns;
    private int rowsBelowDock;
    private int dockOpacity;
    private String clockFormat;
    private boolean animations;
    private boolean advancedEffects;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_launcher_settings);
        choices = getSharedPreferences(Config.SETTINGS_NAME, MODE_PRIVATE);
        Config config = Config.load(this);
        View reorderChoice = findViewById(R.id.reorderChoice);
        reorderChoice.setVisibility(config.launcherSettingsButton ? View.VISIBLE : View.GONE);
        reorderChoice.setOnClickListener(v -> {
            setResult(RESULT_OK, new Intent().putExtra(EXTRA_REORDER, true));
            finish();
        });
        columns = config.columns;
        rowsBelowDock = config.rowsBelowDock;
        dockOpacity = config.dockOpacity;
        clockFormat = config.clockFormat;
        animations = config.animations;
        advancedEffects = config.animations && config.advancedEffects;

        int[] ids = {R.id.columns4, R.id.columns5, R.id.columns6, R.id.columns7};
        for (int i = 0; i < ids.length; i++) {
            final int value = i + 4;
            columnChoices[i] = findViewById(ids[i]);
            columnChoices[i].setOnClickListener(v -> {
                if (columns == value) return;
                columns = value;
                choices.edit().putInt("columns", value).apply();
                changed();
                refresh();
            });
        }
        int[] rowIds = {R.id.rows0, R.id.rows1, R.id.rows2, R.id.rows3};
        for (int i = 0; i < rowIds.length; i++) {
            final int value = i;
            rowChoices[i] = findViewById(rowIds[i]);
            rowChoices[i].setOnClickListener(v -> {
                if (rowsBelowDock == value) return;
                rowsBelowDock = value;
                choices.edit().putInt(Config.ROWS_BELOW_DOCK_KEY, value).apply();
                changed();
                refresh();
            });
        }
        int[] dockIds = {R.id.dock100, R.id.dock70, R.id.dock50, R.id.dock30, R.id.dock15, R.id.dock5};
        for (int i = 0; i < dockIds.length; i++) {
            final int value = DOCK_OPACITIES[i];
            dockChoices[i] = findViewById(dockIds[i]);
            dockChoices[i].setOnClickListener(v -> {
                if (dockOpacity == value) return;
                dockOpacity = value;
                choices.edit().putInt(Config.DOCK_OPACITY_KEY, value).apply();
                changed();
                refresh();
            });
        }
        int[] clockIds = {R.id.clockSystem, R.id.clock12, R.id.clock24};
        for (int i = 0; i < clockIds.length; i++) {
            final String value = CLOCK_FORMATS[i];
            clockChoices[i] = findViewById(clockIds[i]);
            clockChoices[i].setOnClickListener(v -> {
                if (value.equals(clockFormat)) return;
                clockFormat = value;
                choices.edit().putString(Config.CLOCK_FORMAT_KEY, value).apply();
                changed();
                refresh();
            });
        }
        // Mirror text alignment without globally opting the launcher into RTL. Numeric rows
        // still need explicit ordering in Android's no-supportsRtl compatibility mode.
        if (getResources().getBoolean(R.bool.rtl_menu_bar))
            findViewById(android.R.id.content).setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        if (getResources().getBoolean(R.bool.rtl_app_grid)) {
            reverseRow(columnChoices);
            reverseRow(rowChoices);
            reverseRow(dockChoices);
            reverseRow(clockChoices);
        }
        wallpaperChoice = findViewById(R.id.wallpaperChoice);
        wallpaperChoice.setOnClickListener(v -> showWallpaperPicker());

        wallpaperIntervalChoice = findViewById(R.id.wallpaperIntervalChoice);
        wallpaperIntervalChoice.setOnClickListener(v -> showWallpaperIntervalPicker());

        wallpaperEffectChoice = findViewById(R.id.wallpaperEffectChoice);
        wallpaperEffectChoice.setOnClickListener(v -> showWallpaperEffectPicker());

        wallpaperDurationChoice = findViewById(R.id.wallpaperDurationChoice);
        wallpaperDurationChoice.setOnClickListener(v -> showWallpaperDurationPicker());

        ambientIntensityChoice = findViewById(R.id.ambientIntensityChoice);
        ambientIntensityChoice.setOnClickListener(v -> showAmbientIntensityPicker());

        bootLogoChoice = findViewById(R.id.bootLogoChoice);
        bootLogoChoice.setOnClickListener(v -> toggleBootLogo());

        bootLogoDurationChoice = findViewById(R.id.bootLogoDurationChoice);
        bootLogoDurationChoice.setOnClickListener(v -> showBootLogoDurationPicker());

        topLogoChoice = findViewById(R.id.topLogoChoice);
        topLogoChoice.setOnClickListener(v -> toggleTopLogo());

        customGreetingChoice = findViewById(R.id.customGreetingChoice);
        customGreetingChoice.setOnClickListener(v -> showCustomGreetingDialog());

        wifiPillChoice = findViewById(R.id.wifiPillChoice);
        wifiPillChoice.setOnClickListener(v -> toggleWifiPill());

        powerPillChoice = findViewById(R.id.powerPillChoice);
        powerPillChoice.setOnClickListener(v -> togglePowerPill());

        lunarChoice = findViewById(R.id.lunarChoice);
        lunarChoice.setOnClickListener(v -> toggleLunarDate());

        calendarWidgetChoice = findViewById(R.id.calendarWidgetChoice);
        calendarWidgetChoice.setOnClickListener(v -> toggleCalendarWidget());

        weatherWidgetChoice = findViewById(R.id.weatherWidgetChoice);
        weatherWidgetChoice.setOnClickListener(v -> toggleWeatherWidget());

        clockFontChoice = findViewById(R.id.clockFontChoice);
        clockFontChoice.setOnClickListener(v -> showClockFontPicker());

        dateFontChoice = findViewById(R.id.dateFontChoice);
        dateFontChoice.setOnClickListener(v -> showDateFontPicker());

        findViewById(R.id.performanceManagerChoice).setOnClickListener(v ->
                open(new Intent(this, DeveloperActivity.class)));
        animationsChoice = findViewById(R.id.animationsChoice);
        animationsChoice.setOnClickListener(v -> {
            animations = !animations;
            if (!animations) advancedEffects = false;
            choices.edit().putBoolean("animations", animations)
                    .putBoolean("advanced_effects", advancedEffects).apply();
            changed();
            refresh();
        });
        advancedChoice = findViewById(R.id.advancedChoice);
        advancedChoice.setOnClickListener(v -> {
            if (advancedEffects) {
                setAdvanced(false);
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.advanced_effects)
                        .setMessage(R.string.advanced_effects_warning)
                        .setPositiveButton(R.string.enable, (dialog, which) -> setAdvanced(true))
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });
        openOnStartChoice = findViewById(R.id.openOnStartChoice);
        openOnStartChoice.setVisibility(View.VISIBLE);
        openOnStartChoice.setOnClickListener(v -> showOpenOnStartPicker());
        followThemeChoice = findViewById(R.id.followThemeChoice);
        followThemeChoice.setOnClickListener(v -> {
            if (!hasOnTvChoices()) return;
            choices.edit().remove("columns").remove(Config.ROWS_BELOW_DOCK_KEY).remove("animations")
                    .remove("advanced_effects").remove(Config.WALLPAPER_CHOICE_KEY)
                    .remove(Config.DOCK_OPACITY_KEY).remove(Config.CLOCK_FORMAT_KEY).remove(OpenOnStart.KEY).commit();
            Config current = Config.load(this);
            columns = current.columns;
            rowsBelowDock = current.rowsBelowDock;
            dockOpacity = current.dockOpacity;
            clockFormat = current.clockFormat;
            animations = current.animations;
            advancedEffects = current.animations && current.advancedEffects;
            changed();
            refresh();
        });
        findViewById(R.id.cleanRamChoice).setOnClickListener(v -> {
            try {
                System.gc();
                android.app.ActivityManager am = (android.app.ActivityManager) getSystemService(ACTIVITY_SERVICE);
                if (am != null) {
                    android.app.ActivityManager.MemoryInfo mi = new android.app.ActivityManager.MemoryInfo();
                    am.getMemoryInfo(mi);
                    long freeMb = mi.availMem / (1024 * 1024);
                    Toast.makeText(this, getString(R.string.jtv_clean_ram_done, freeMb), Toast.LENGTH_LONG).show();
                }
            } catch (Exception ignored) {}
        });
        findViewById(R.id.screenSaverChoice).setOnClickListener(v -> {
            try {
                Intent dreamSettings = new Intent("android.settings.DREAM_SETTINGS");
                if (getPackageManager().resolveActivity(dreamSettings, 0) != null) {
                    startActivity(dreamSettings);
                } else {
                    Intent aospSettings = SystemSettings.intent(this, Settings.ACTION_DISPLAY_SETTINGS);
                    startActivity(aospSettings);
                }
            } catch (Exception ignored) {
                Toast.makeText(this, R.string.not_available, Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.languageChoice).setOnClickListener(v -> {
            String[] langs = {"Tiếng Việt (vi)", "English (en)", "Mặc định hệ thống / System default"};
            String[] tags = {"vi", "en", ""};
            new AlertDialog.Builder(this)
                    .setTitle(R.string.jtv_language)
                    .setItems(langs, (dialog, which) -> {
                        choices.edit().putString("locale", tags[which]).apply();
                        changed();
                        Toast.makeText(this, "Đã lưu cài đặt ngôn ngữ / Language saved.", Toast.LENGTH_SHORT).show();
                        recreate();
                    })
                    .show();
        });
        findViewById(R.id.appHubChoice).setOnClickListener(v ->
                open(new Intent(this, AppHubActivity.class)));
        findViewById(R.id.developerChoice).setOnClickListener(v ->
                open(new Intent(this, DeveloperActivity.class)));
        findViewById(R.id.defaultHomeChoice).setOnClickListener(v ->
                open(new Intent(this, HomeSettingsActivity.class)));
        findViewById(R.id.tvSettingsChoice).setOnClickListener(v ->
                open(SystemSettings.intent(this, Settings.ACTION_SETTINGS)));
        refresh();
        if (config.launcherSettingsButton) reorderChoice.requestFocus();
        else columnChoices[Math.max(0, Math.min(columns - 4, 3))].requestFocus();
    }

    private void reverseRow(TextView[] views) {
        LinearLayout row = (LinearLayout) views[0].getParent();
        row.removeAllViews();
        for (int i = views.length - 1; i >= 0; i--) row.addView(views[i]);
    }

    private void showCustomGreetingDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setTextColor(0xFFFFFFFF);
        input.setHint("Ví dụ: Dương, Gia đình...");
        input.setText(choices.getString("custom_greeting_name", ""));
        new AlertDialog.Builder(this)
                .setTitle(R.string.jtv_custom_greeting)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    choices.edit().putString("custom_greeting_name", name).apply();
                    changed();
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static final int[] ROTATION_INTERVALS = {0, 1, 5, 15, 30, 60, 360, 1440};
    private static final String[] ROTATION_LABELS = {
            "Giữ 1 ảnh (Tắt đổi tự động)", "1 phút", "5 phút", "15 phút",
            "30 phút (Mặc định)", "1 giờ", "6 giờ", "24 giờ (1 ngày)"
    };

    private void showWallpaperIntervalPicker() {
        int current = choices.getInt("wallpaper_rotation_minutes", 30);
        int checked = 4;
        for (int i = 0; i < ROTATION_INTERVALS.length; i++) {
            if (ROTATION_INTERVALS[i] == current) {
                checked = i;
                break;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.jtv_wallpaper_interval)
                .setSingleChoiceItems(ROTATION_LABELS, checked, (dialog, which) -> {
                    dialog.dismiss();
                    choices.edit().putInt("wallpaper_rotation_minutes", ROTATION_INTERVALS[which]).apply();
                    changed();
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static final String[] WALLPAPER_EFFECT_KEYS = {
            "crossfade", "blur", "zoom", "slide", "glass", "sweep", "ken_burns"
    };
    private static final String[] WALLPAPER_EFFECT_LABELS = {
            "Crossfade (Chuyển mờ dần kinh điển)",
            "Blur Transition (Làm mờ chuyển cảnh sâu)",
            "Zoom Transition (Thu phóng không gian 3D)",
            "Slide Transition (Trượt ngang Cinematic)",
            "Glass Layers (Lớp kính mờ nghệ thuật)",
            "Light Sweep (Quét ánh sáng phản chiếu)",
            "Ken Burns (Lướt lia ảnh điện ảnh)"
    };

    private void showWallpaperEffectPicker() {
        String current = choices.getString("wallpaper_effect", "crossfade");
        int checked = 0;
        for (int i = 0; i < WALLPAPER_EFFECT_KEYS.length; i++) {
            if (WALLPAPER_EFFECT_KEYS[i].equalsIgnoreCase(current)) {
                checked = i;
                break;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.jtv_wallpaper_effect)
                .setSingleChoiceItems(WALLPAPER_EFFECT_LABELS, checked, (dialog, which) -> {
                    dialog.dismiss();
                    choices.edit().putString("wallpaper_effect", WALLPAPER_EFFECT_KEYS[which]).apply();
                    changed();
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static final int[] WALLPAPER_DURATIONS = {400, 800, 1200, 1800, 2500, 3500};
    private static final String[] WALLPAPER_DURATION_LABELS = {
            "0.4 giây (Rất nhanh)", "0.8 giây (Nhanh)", "1.2 giây (Mặc định)",
            "1.8 giây (Mượt mà)", "2.5 giây (Điện ảnh)", "3.5 giây (Siêu êm ái)"
    };

    private void showWallpaperDurationPicker() {
        int current = choices.getInt("wallpaper_transition_duration", 1200);
        int checked = 2;
        for (int i = 0; i < WALLPAPER_DURATIONS.length; i++) {
            if (WALLPAPER_DURATIONS[i] == current) {
                checked = i;
                break;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.jtv_wallpaper_duration)
                .setSingleChoiceItems(WALLPAPER_DURATION_LABELS, checked, (dialog, which) -> {
                    dialog.dismiss();
                    choices.edit().putInt("wallpaper_transition_duration", WALLPAPER_DURATIONS[which]).apply();
                    changed();
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static final int[] AMBIENT_INTENSITIES = {10, 25, 45, 70, 100};
    private static final String[] AMBIENT_INTENSITY_LABELS = {
            "10% (Siêu nhẹ / Tinh tế)", "25% (Nhẹ dịu)", "45% (Vừa phải)",
            "70% (Đậm rõ)", "100% (Mặc định / Rực rỡ)"
    };

    private void showAmbientIntensityPicker() {
        int current = choices.getInt("ambient_intensity", 100);
        int checked = 4;
        for (int i = 0; i < AMBIENT_INTENSITIES.length; i++) {
            if (AMBIENT_INTENSITIES[i] == current) {
                checked = i;
                break;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.jtv_ambient_intensity)
                .setSingleChoiceItems(AMBIENT_INTENSITY_LABELS, checked, (dialog, which) -> {
                    dialog.dismiss();
                    choices.edit().putInt("ambient_intensity", AMBIENT_INTENSITIES[which]).apply();
                    changed();
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void toggleBootLogo() {
        boolean show = choices.getBoolean("show_boot_logo", true);
        choices.edit().putBoolean("show_boot_logo", !show).apply();
        changed();
        refresh();
    }

    private static final int[] BOOT_LOGO_DURATIONS = {0, 1, 2, 3, 5, 7, 10};
    private static final String[] BOOT_LOGO_DURATION_LABELS = {
            "0s (Tắt / Ngay lập tức)", "1 giây", "2 giây (Mặc định)", "3 giây", "5 giây", "7 giây", "10 giây"
    };

    private void showBootLogoDurationPicker() {
        int current = choices.getInt("boot_logo_duration", 2);
        int checked = 2;
        for (int i = 0; i < BOOT_LOGO_DURATIONS.length; i++) {
            if (BOOT_LOGO_DURATIONS[i] == current) {
                checked = i;
                break;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.jtv_boot_logo_duration)
                .setSingleChoiceItems(BOOT_LOGO_DURATION_LABELS, checked, (dialog, which) -> {
                    dialog.dismiss();
                    choices.edit().putInt("boot_logo_duration", BOOT_LOGO_DURATIONS[which]).apply();
                    changed();
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void toggleTopLogo() {
        boolean show = choices.getBoolean("show_top_logo", false);
        choices.edit().putBoolean("show_top_logo", !show).apply();
        changed();
        refresh();
    }

    private void toggleWifiPill() {
        boolean show = choices.getBoolean("show_network_pill", true);
        choices.edit().putBoolean("show_network_pill", !show).apply();
        changed();
        refresh();
    }

    private void togglePowerPill() {
        boolean show = choices.getBoolean("show_power_pill", true);
        choices.edit().putBoolean("show_power_pill", !show).apply();
        changed();
        refresh();
    }

    private void toggleLunarDate() {
        boolean lunar = choices.getBoolean("lunar_date", false);
        choices.edit().putBoolean("lunar_date", !lunar).apply();
        changed();
        refresh();
    }

    private void toggleCalendarWidget() {
        boolean cal = choices.getBoolean("show_calendar_widget", false);
        choices.edit().putBoolean("show_calendar_widget", !cal).apply();
        changed();
        refresh();
    }

    private void toggleWeatherWidget() {
        boolean weather = choices.getBoolean("show_weather_widget", true);
        choices.edit().putBoolean("show_weather_widget", !weather).apply();
        changed();
        refresh();
    }

    private static final String[] CLOCK_FONT_KEYS = {
            "default", "inter", "space_grotesk", "roboto", "din", "bebas_neue",
            "righteous", "audiowide", "orbitron", "comfortaa", "press_start_2p"
    };
    private static final String[] CLOCK_FONT_LABELS = {
            "Mặc định (Outfit / System)", "Inter", "SF Pro / Space Grotesk", "Roboto", "DIN / DIN Next", "Bebas Neue (Số điện ảnh)",
            "Righteous (Hiện đại / Tươi mới)", "Audiowide (Cyberpunk / Công nghệ)", "Orbitron (Sci-Fi / Vũ trụ)", "Comfortaa (Bo tròn / Tinh tế)", "Press Start 2P (Pixel Arcade 8-bit)"
    };

    private void showClockFontPicker() {
        String current = choices.getString("clock_font", "default");
        int checked = 0;
        for (int i = 0; i < CLOCK_FONT_KEYS.length; i++) {
            if (CLOCK_FONT_KEYS[i].equalsIgnoreCase(current)) {
                checked = i;
                break;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.jtv_clock_font)
                .setSingleChoiceItems(CLOCK_FONT_LABELS, checked, (dialog, which) -> {
                    dialog.dismiss();
                    choices.edit().putString("clock_font", CLOCK_FONT_KEYS[which]).apply();
                    changed();
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static final String[] DATE_FONT_KEYS = {
            "default", "inter", "manrope", "ibm_plex_sans", "nunito_sans",
            "cabin", "quicksand", "outfit_custom", "montserrat", "playfair_display"
    };
    private static final String[] DATE_FONT_LABELS = {
            "Mặc định (Default)", "Inter", "Manrope", "IBM Plex Sans", "Nunito Sans",
            "Cabin (Hiện đại, bo tròn)", "Quicksand (Nhẹ nhàng, thanh lịch)", "Outfit (Công nghệ, tối giản)",
            "Montserrat (Hiện đại, sang trọng)", "Playfair Display (Cổ điển, quý phái)"
    };

    private void showDateFontPicker() {
        String current = choices.getString("date_font", "default");
        int checked = 0;
        for (int i = 0; i < DATE_FONT_KEYS.length; i++) {
            if (DATE_FONT_KEYS[i].equalsIgnoreCase(current)) {
                checked = i;
                break;
            }
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.jtv_date_font)
                .setSingleChoiceItems(DATE_FONT_LABELS, checked, (dialog, which) -> {
                    dialog.dismiss();
                    choices.edit().putString("date_font", DATE_FONT_KEYS[which]).apply();
                    changed();
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showWallpaperPicker() {
        File dir = Config.dir(this);
        List<File> images = new java.util.ArrayList<>();
        if (dir != null) {
            images.addAll(WallpaperRotation.images(new File(dir, "wallpapers")));
            File single = new File(dir, Config.WALLPAPER_NAME);
            if (single.isFile()) images.add(0, single);
        }
        // Also scan external shared Pictures/Wallpapers if available
        File extWallpapers = new File("/sdcard/Pictures");
        if (extWallpapers.isDirectory()) {
            images.addAll(WallpaperRotation.images(extWallpapers));
        }

        String[] labels = new String[images.size() + 1];
        for (int i = 0; i < images.size(); i++) labels[i] = images.get(i).getName();
        labels[images.size()] = getString(R.string.wallpaper_none);
        String current = choices.getString(Config.WALLPAPER_CHOICE_KEY, null);
        int checked = "none".equals(current) ? images.size()
                : indexOf(images, current);
        new AlertDialog.Builder(this)
                .setTitle(R.string.wallpaper_label)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    dialog.dismiss();
                    if (which == images.size()) {
                        choices.edit().putString(Config.WALLPAPER_CHOICE_KEY, "none").apply();
                    } else {
                        File picked = images.get(which);
                        // If picked from external pictures, copy to theme dir so it survives and displays
                        if (dir != null && !picked.getParentFile().equals(dir) && !picked.getParentFile().equals(new File(dir, "wallpapers"))) {
                            try {
                                File target = new File(new File(dir, "wallpapers"), picked.getName());
                                target.getParentFile().mkdirs();
                                java.nio.file.Files.copy(picked.toPath(), target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                            } catch (Exception ignored) {}
                        }
                        choices.edit().putString(Config.WALLPAPER_CHOICE_KEY, picked.getName()).apply();
                    }
                    changed();
                    Toast.makeText(this, R.string.wallpaper_applied, Toast.LENGTH_SHORT).show();
                    refresh();
                })
                .setNeutralButton(R.string.wallpaper_theme_default, (dialog, which) -> {
                    choices.edit().remove(Config.WALLPAPER_CHOICE_KEY).apply();
                    changed();
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** Off, then every app with a TV entry point by name. Saved at once; it is read on the next boot. */
    private void showOpenOnStartPicker() {
        PackageManager pm = getPackageManager();
        List<String[]> apps = new ArrayList<>();
        Intent tv = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        for (ResolveInfo r : pm.queryIntentActivities(tv, 0)) {
            String pkg = r.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            boolean seen = false;
            for (String[] a : apps) seen |= a[1].equals(pkg);
            if (!seen) apps.add(new String[]{r.loadLabel(pm).toString(), pkg});
        }
        Collections.sort(apps, (a, b) -> a[0].compareToIgnoreCase(b[0]));
        String[] labels = new String[apps.size() + 1];
        labels[0] = getString(R.string.off);
        String current = choices.getString(OpenOnStart.KEY, "");
        int checked = 0;
        for (int i = 0; i < apps.size(); i++) {
            labels[i + 1] = apps.get(i)[0];
            if (apps.get(i)[1].equals(current)) checked = i + 1;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.open_on_start_label)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    dialog.dismiss();
                    if (which == 0) choices.edit().remove(OpenOnStart.KEY).apply();
                    else {
                        OpenOnStart.skipThisBoot(this);
                        choices.edit().putString(OpenOnStart.KEY, apps.get(which - 1)[1]).apply();
                    }
                    refresh();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** The picked app's name, its package when it is gone, or Off. */
    private String openOnStartLabel() {
        String pkg = choices.getString(OpenOnStart.KEY, "");
        if (pkg.isEmpty()) return getString(R.string.off);
        try {
            PackageManager pm = getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return pkg;
        }
    }

    private static int indexOf(List<File> images, String name) {
        if (name == null) return -1;
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getName().equals(name)) return i;
        }
        return -1;
    }

    /**
     * A choice was saved. Home rebuilds when it next resumes because the change count moved, whether these
     * settings are left with Back or with the remote's Home button (which cancels the activity result).
     */
    private void changed() {
        setResult(RESULT_OK);
        choices.edit().putLong(Config.SETTINGS_REVISION_KEY,
                choices.getLong(Config.SETTINGS_REVISION_KEY, 0) + 1).apply();
    }

    private void setAdvanced(boolean enabled) {
        advancedEffects = enabled;
        choices.edit().putBoolean("advanced_effects", enabled).apply();
        changed();
        refresh();
    }

    private void refresh() {
        for (int i = 0; i < columnChoices.length; i++) {
            columnChoices[i].setSelected(columns == i + 4);
        }
        for (int i = 0; i < rowChoices.length; i++) {
            rowChoices[i].setSelected(rowsBelowDock == i);
        }
        for (int i = 0; i < dockChoices.length; i++) {
            dockChoices[i].setSelected(dockOpacity == DOCK_OPACITIES[i]);
        }
        for (int i = 0; i < clockChoices.length; i++) {
            clockChoices[i].setSelected(CLOCK_FORMATS[i].equals(clockFormat));
        }
        animationsChoice.setText(getString(R.string.animations_state,
                getString(animations ? R.string.on : R.string.off)));
        advancedChoice.setText(getString(R.string.advanced_effects_state,
                getString(advancedEffects ? R.string.on : R.string.off)));
        advancedChoice.setEnabled(animations);
        advancedChoice.setAlpha(animations ? 1f : 0.5f);
        String wallpaperCurrent = choices.getString(Config.WALLPAPER_CHOICE_KEY, null);
        wallpaperChoice.setText(getString(R.string.wallpaper_state,
                "none".equals(wallpaperCurrent) ? getString(R.string.wallpaper_none)
                        : wallpaperCurrent != null ? wallpaperCurrent
                        : getString(R.string.wallpaper_theme_default)));

        int interval = choices.getInt("wallpaper_rotation_minutes", 30);
        String intervalText = interval == 0 ? "Tắt tự động đổi" : interval + " phút";
        wallpaperIntervalChoice.setText(getString(R.string.jtv_wallpaper_interval) + ": " + intervalText);

        String effect = choices.getString("wallpaper_effect", "crossfade");
        String effectLabel = "Crossfade";
        for (int i = 0; i < WALLPAPER_EFFECT_KEYS.length; i++) {
            if (WALLPAPER_EFFECT_KEYS[i].equalsIgnoreCase(effect)) {
                effectLabel = WALLPAPER_EFFECT_LABELS[i];
                break;
            }
        }
        wallpaperEffectChoice.setText(getString(R.string.jtv_wallpaper_effect) + ": " + effectLabel);

        int duration = choices.getInt("wallpaper_transition_duration", 1200);
        wallpaperDurationChoice.setText(getString(R.string.jtv_wallpaper_duration) + ": " + (duration / 1000f) + "s");

        int ambientInt = choices.getInt("ambient_intensity", 100);
        ambientIntensityChoice.setText(getString(R.string.jtv_ambient_intensity) + ": " + ambientInt + "%");

        boolean showBoot = choices.getBoolean("show_boot_logo", true);
        bootLogoChoice.setText(getString(R.string.jtv_boot_logo) + ": " + (showBoot ? getString(R.string.on) : getString(R.string.off)));

        int bootDur = choices.getInt("boot_logo_duration", 2);
        String bootDurLabel = bootDur == 0 ? "Ngay lập tức (Tắt)" : bootDur + " giây";
        bootLogoDurationChoice.setText(getString(R.string.jtv_boot_logo_duration) + ": " + bootDurLabel);

        boolean showTop = choices.getBoolean("show_top_logo", false);
        topLogoChoice.setText(getString(R.string.jtv_top_logo) + ": " + (showTop ? getString(R.string.on) : getString(R.string.off)));

        String greetingName = choices.getString("custom_greeting_name", "");
        customGreetingChoice.setText(getString(R.string.jtv_custom_greeting) + ": "
                + (greetingName.isEmpty() ? "Mặc định (Xin chào)" : "Xin chào, " + greetingName));

        boolean showWifi = choices.getBoolean("show_network_pill", true);
        wifiPillChoice.setText(getString(R.string.jtv_wifi_pill) + ": " + (showWifi ? getString(R.string.on) : getString(R.string.off)));

        boolean showPower = choices.getBoolean("show_power_pill", true);
        powerPillChoice.setText(getString(R.string.jtv_power_pill) + ": " + (showPower ? getString(R.string.on) : getString(R.string.off)));

        boolean showLunar = choices.getBoolean("lunar_date", false);
        lunarChoice.setText(getString(R.string.jtv_lunar_calendar) + ": " + (showLunar ? getString(R.string.on) : getString(R.string.off)));

        boolean showCal = choices.getBoolean("show_calendar_widget", false);
        calendarWidgetChoice.setText(getString(R.string.jtv_calendar_widget) + ": " + (showCal ? getString(R.string.on) : getString(R.string.off)));

        boolean showWeather = choices.getBoolean("show_weather_widget", true);
        weatherWidgetChoice.setText(getString(R.string.jtv_weather_widget) + ": " + (showWeather ? getString(R.string.on) : getString(R.string.off)));

        String clockFont = choices.getString("clock_font", "default");
        clockFontChoice.setText(getString(R.string.jtv_clock_font) + ": " + clockFont);

        String dateFont = choices.getString("date_font", "default");
        dateFontChoice.setText(getString(R.string.jtv_date_font) + ": " + dateFont);

        openOnStartChoice.setText(getString(R.string.open_on_start_state, openOnStartLabel()));
        followThemeChoice.setSelected(!hasOnTvChoices());
    }

    private boolean hasOnTvChoices() {
        return choices.contains("columns") || choices.contains(Config.ROWS_BELOW_DOCK_KEY)
                || choices.contains("animations") || choices.contains("advanced_effects")
                || choices.contains(Config.WALLPAPER_CHOICE_KEY) || choices.contains(Config.DOCK_OPACITY_KEY)
                || choices.contains(Config.CLOCK_FORMAT_KEY) || choices.contains(OpenOnStart.KEY);
    }

    private void open(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(this, R.string.not_available, Toast.LENGTH_LONG).show();
        }
    }
}
