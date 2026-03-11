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

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        createFloatingView();
    }

    private void createFloatingView() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(20, 20, 20, 20);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#CC1A1A2E"));
        bg.setCornerRadius(40);
        container.setBackground(bg);

        TextView upBtn = makeButton("▲");
        TextView downBtn = makeButton("▼");

        // Separate click listeners on each button
        upBtn.setOnClickListener(v -> {
            vibrate();
            scroll(true);
            Toast.makeText(this, "▲", Toast.LENGTH_SHORT).show();
        });

        downBtn.setOnClickListener(v -> {
            vibrate();
            scroll(false);
            Toast.makeText(this, "▼", Toast.LENGTH_SHORT).show();
        });

        container.addView(upBtn);
        container.addView(downBtn);

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
            WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            150, WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.END;
        params.x = 20;
        params.y = 300;

        // Fixed touch listener - properly separates drag from click
        container.setOnTouchListener(new View.OnTouchListener() {
            int ix, iy;
            float tx, ty;
            boolean drag;

            public boolean onTouch(View v, MotionEvent e) {
                WindowManager.LayoutParams lp =
                    (WindowManager.LayoutParams) floatingView.getLayoutParams();

                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ix = lp.x;
                        iy = lp.y;
                        tx = e.getRawX();
                        ty = e.getRawY();
                        drag = false;
                        return false; // let child views get the event first

                    case MotionEvent.ACTION_MOVE:
                        int dx = (int)(e.getRawX() - tx);
                        int dy = (int)(e.getRawY() - ty);
                        if (Math.abs(dx) > 15 || Math.abs(dy) > 15) {
                            drag = true;
                        }
                        if (drag) {
                            lp.x = ix - dx;
                            lp.y = iy + dy;
                            windowManager.updateViewLayout(floatingView, lp);
                            return true;
                        }
                        return false;

                    case MotionEvent.ACTION_UP:
                        return drag; // only consume if was dragging
                }
                return false;
            }
        });

        floatingView = container;
        windowManager.addView(floatingView, params);
    }

    private void vibrate() {
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(
                    50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50);
            }
        }
    }

    private TextView makeButton(String text) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextSize(24);
        btn.setTextColor(Color.WHITE);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(20, 20, 20, 20);
        btn.setClickable(true);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.parseColor("#CC4A90D9"));
        btn.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(100, 100);
        lp.setMargins(0, 8, 0, 8);
        btn.setLayoutParams(lp);
        return btn;
    }

    private void scroll(boolean up) {
        // Try static instance first
        if (ScrollAccessibilityService.instance != null) {
            ScrollAccessibilityService.instance.performScroll(up);
            return;
        }
        // Try requesting via static method as fallback
        ScrollAccessibilityService.requestScroll(up);
    }

    @Override public int onStartCommand(Intent i, int f, int s) { return START_STICKY; }

    @Override public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (floatingView != null) windowManager.removeView(floatingView);
    }

    @Override public IBinder onBind(Intent i) { return null; }
}
