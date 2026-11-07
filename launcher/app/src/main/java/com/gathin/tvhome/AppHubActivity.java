package com.gathin.tvhome;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * JUONG TV - App Hub & TV Browser Ecosystem.
 * A dedicated application marketplace and browser discovery interface designed for TV Remote & D-pad.
 */
public final class AppHubActivity extends Activity {

    static final class HubApp {
        final String name;
        final String category;
        final String pkg;
        final String desc;
        final String downloadUrl;

        HubApp(String name, String category, String pkg, String desc, String downloadUrl) {
            this.name = name;
            this.category = category;
            this.pkg = pkg;
            this.desc = desc;
            this.downloadUrl = downloadUrl;
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LauncherLocale.wrap(base));
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_app_hub);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        LinearLayout container = findViewById(R.id.hubContainer);
        List<HubApp> catalog = getCuratedApps();

        PackageManager pm = getPackageManager();
        for (HubApp app : catalog) {
            boolean installed = isPackageInstalled(pm, app.pkg);
            addAppCard(container, app, installed);
        }

        if (container.getChildCount() > 0) {
            container.getChildAt(0).requestFocus();
        }
    }

    private List<HubApp> getCuratedApps() {
        List<HubApp> list = new ArrayList<>();
        list.add(new HubApp(
                "TV Bro (Trình Duyệt TV)",
                "INTERNET \u00B7 BROWSER",
                "io.github.truefedex.tvbro",
                "Trình duyệt tốt nhất cho Android TV: Chuột ảo remote, chặn quảng cáo, quản lý tải file.",
                "https://github.com/truefedex/tv-bro/releases"
        ));
        list.add(new HubApp(
                "SmartTube",
                "CINEMA \u00B7 STREAMING",
                "com.teamsmart.videomanager.tv",
                "YouTube 4K không quảng cáo, tương thích hoàn hảo Remote và tối ưu RAM trên Xiaomi TV.",
                "https://github.com/yuliskov/SmartTube/releases"
        ));
        list.add(new HubApp(
                "VLC for Android",
                "MEDIA \u00B7 PLAYER",
                "org.videolan.vlc",
                "Trình phát video đỉnh cao, hỗ trợ đọc ổ cứng USB, phụ đề tiếng Việt và phát qua mạng LAN.",
                "https://get.videolan.org/vlc-android/"
        ));
        list.add(new HubApp(
                "LeanKey Keyboard Pro",
                "SYSTEM \u00B7 KEYBOARD",
                "com.liskovsoft.leankeykeyboard",
                "Bàn phím ảo tiếng Việt chuyên dụng cho điều khiển TV, gọn nhẹ và phản hồi nhanh.",
                "https://github.com/yuliskov/LeanKeyKeyboard/releases"
        ));
        list.add(new HubApp(
                "Monster TV & OTT",
                "LIVE BROADCAST \u00B7 TV",
                "com.monstertv.live",
                "Kênh truyền hình trực tuyến Việt Nam độ nét cao, tốc độ tải nhanh trên mạng nội địa.",
                "https://monstertv.vn/"
        ));
        return list;
    }

    private boolean isPackageInstalled(PackageManager pm, String pkg) {
        try {
            pm.getPackageInfo(pkg, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void addAppCard(LinearLayout container, HubApp app, boolean installed) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.tile_bg);
        card.setPadding(30, 24, 30, 24);
        card.setFocusable(true);
        card.setClickable(true);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 20);
        card.setLayoutParams(lp);

        TextView cat = new TextView(this);
        cat.setText(app.category + (installed ? " \u00B7 [ĐÃ CÀI ĐẶT]" : " \u00B7 [CHƯA CÀI]"));
        cat.setTextColor(installed ? 0xFF00D2FF : 0xFF8E9BAE);
        cat.setTextSize(12);
        cat.setLetterSpacing(0.12f);
        card.addView(cat);

        TextView title = new TextView(this);
        title.setText(app.name);
        title.setTextColor(0xFFF5F7FA);
        title.setTextSize(20);
        title.setPadding(0, 4, 0, 6);
        card.addView(title);

        TextView desc = new TextView(this);
        desc.setText(app.desc);
        desc.setTextColor(0xFF8E9BAE);
        desc.setTextSize(14);
        card.addView(desc);

        card.setOnClickListener(v -> {
            if (installed) {
                Intent launch = getPackageManager().getLeanbackLaunchIntentForPackage(app.pkg);
                if (launch == null) launch = getPackageManager().getLaunchIntentForPackage(app.pkg);
                if (launch != null) {
                    startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } else {
                    Toast.makeText(this, R.string.cant_open, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Mở trình tải cho " + app.name, Toast.LENGTH_SHORT).show();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(app.downloadUrl))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (Exception e) {
                    Toast.makeText(this, "Vui lòng cài đặt TV Bro để tải file", Toast.LENGTH_SHORT).show();
                }
            }
        });

        container.addView(card);
    }
}
