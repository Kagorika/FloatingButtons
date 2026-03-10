package com.floatingbuttons;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int OVERLAY_PERMISSION_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView status = new TextView(this);
        Button startBtn = new Button(this);
        Button stopBtn = new Button(this);

        startBtn.setText("Start Floating Buttons");
        stopBtn.setText("Stop");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(60, 120, 60, 60);
        layout.setBackgroundColor(0xFF0F0F1A);

        status.setText("Floating buttons: INACTIVE");
        status.setTextColor(0xFFFFB74D);
        status.setTextSize(18);
        status.setPadding(0, 0, 0, 40);

        startBtn.setOnClickListener(v -> {
            if (hasOverlayPermission()) {
                startService(new Intent(this, FloatingButtonService.class));
                status.setText("Floating buttons: ACTIVE");
            } else {
                requestOverlayPermission();
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

    private boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST);
            Toast.makeText(this, "Please grant Display over other apps permission", Toast.LENGTH_LONG).show();
        }
    }
}
