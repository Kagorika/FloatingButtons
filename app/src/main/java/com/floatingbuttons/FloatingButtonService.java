package com.floatingbuttons;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;

public class FloatingButtonService extends Service {
    public static boolean isRunning = false;
    private WindowManager windowManager;
    private View floatingView;
    private Vibrator vibrator;
    private WindowManager.LayoutParams params;
    private int initialX, initialY;
    private float initialTouchX, initialTouchY;

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        createFloatingView();
    }

    private void createFloatingView() {
        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
            WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.END;
        params.x = 20;
        params.y = 300;

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(16, 16, 16, 16);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#DD1A1A2E"));
        bg.setCornerRadius(50);
        container.setBackground(bg);

        Button upBtn = new Button(this);
        upBtn.setText("▲");
        upBtn.setTextSize(22);
        upBtn.setTextColor(Color.WHITE);
        upBtn.setAllCaps(false);
        styleButton(upBtn);

        Button downBtn = new Button(this);
        downBtn.setText("▼");
        downBtn.setTextSize(22);
        downBtn.setTextColor(Color.WHITE);
        downBtn.setAllCaps(false);
        styleButton(downBtn);

        upBtn.setOnClickListener(v -> {
            doVibrate();
            Toast.makeText(FloatingButtonService.this, "▲ UP", Toast.LENGTH_SHORT).show();
            doScroll(true);
        });

        downBtn.setOnClickListener(v -> {
            doVibrate();
            Toast.makeText(FloatingButtonService.this, "▼ DOWN", Toast.LENGTH_SHORT).show();
            doScroll(false);
        });

        container.addView(upBtn);
        container.addView(downBtn);

        container.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = params.x;
                    initialY = params.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    return false;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - initialTouchX;
                    float dy = event.getRawY() - initialTouchY;
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        params.x = initialX - (int) dx;
                        params.y = initialY + (int) dy;
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                    }
                    return false;
            }
            return false;
        });

        floatingView = container;
        windowManager.addView(floatingView, params);
    }

    private void styleButton(Button btn) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.parseColor("#CC4A90D9"));
        btn.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(120, 120);
        lp.setMargins(0, 8, 0, 8);
        btn.setLayoutParams(lp);
        btn.setPadding(0, 0, 0, 0);
    }

    private void doVibrate() {
        if (vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(
                60, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(60);
        }
    }

    private void doScroll(boolean up) {
        if (ScrollAccessibilityService.instance != null) {
            ScrollAccessibilityService.instance.performScroll(up);
        }
    }

    @Override public int onStartCommand(Intent i, int f, int s) { return START_STICKY; }

    @Override public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (floatingView != null) windowManager.removeView(floatingView);
    }

    @Override public IBinder onBind(Intent i) { return null; }
}
