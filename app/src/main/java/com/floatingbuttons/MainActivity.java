package com.floatingbuttons;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest;
import android.content.pm.PackageManager;

public class MainActivity extends Activity {
    private static final int OVERLAY_PERMISSION_REQUEST = 1001;
    private static final int NOTIFICATION_PERMISSION_REQUEST = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(60, 120, 60, 60);
        layout.setBackgroundColor(0xFF0F0F1A);

        TextView status = new TextView(this);
        status.setText("Floating buttons: INACTIVE");
        status.setTextColor(0xFFFFB74D);
        status.setTextSize(18);
        status.setPadding(0, 0, 0, 40);

        Button startBtn = new Button(this);
        startBtn.setText("Start Floating Buttons");

        Button stopBtn = new Button(this);
        stopBtn.setText("Stop");

        startBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST);
                    return;
                }
            }
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST);
            } else {
                startFloating(status);
            }
        });

        stopBtn.setOnClickListener(v -> {
            stopService(new Intent(this, FloatingButtonService.class));
            status.setText("Floating buttons: INACTIVE");
        });

        layout.addView(status);
        layout.addView(startBtn);
        layout.addView(stopBtn);
        setContentView(layout);
    }

    private void startFloating(TextView status) {
        Intent i = new Intent(this, FloatingButtonService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(i);
        } else {
            startService(i);
        }
        status.setText("Floating buttons: ACTIVE");
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == OVERLAY_PERMISSION_REQUEST && Settings.canDrawOverlays(this)) {
            TextView status = (TextView)((android.widget.LinearLayout)
                findViewById(android.R.id.content).getRootView()
                .findViewById(android.R.id.content)).getChildAt(0);
            startFloating(new TextView(this));
        }
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] results) {
        if (req == NOTIFICATION_PERMISSION_REQUEST) {
            if (!Settings.canDrawOverlays(this)) {
                startActivityForResult(new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())),
                    OVERLAY_PERMISSION_REQUEST);
            }
        }
    }
}
