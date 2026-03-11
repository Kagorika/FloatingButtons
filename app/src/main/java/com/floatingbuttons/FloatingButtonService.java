
package com.floatingbuttons;

import android.app.Service;
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

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
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

        upBtn.setOnClickListener(v -> scroll(true));
        downBtn.setOnClickListener(v -> scroll(false));

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

        container.setOnTouchListener(new View.OnTouchListener() {
            int ix, iy; float tx, ty; boolean drag;
            public boolean onTouch(View v, MotionEvent e) {
                WindowManager.LayoutParams lp = (WindowManager.LayoutParams) floatingView.getLayoutParams();
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    ix = lp.x; iy = lp.y; tx = e.getRawX(); ty = e.getRawY(); drag = false;
                } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    int dx = (int)(e.getRawX()-tx), dy = (int)(e.getRawY()-ty);
                    if (Math.abs(dx)>10||Math.abs(dy)>10) drag = true;
                    if (drag) { lp.x=ix-dx; lp.y=iy+dy; windowManager.updateViewLayout(floatingView,lp); }
                }
                return drag;
            }
        });

        floatingView = container;
        windowManager.addView(floatingView, params);
    }

    private TextView makeButton(String text) {
        TextView btn = new TextView(this);
        btn.setText(text);
        btn.setTextSize(24);
        btn.setTextColor(Color.WHITE);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(20, 20, 20, 20);
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
        // Get screen dimensions
        android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenHeight = dm.heightPixels;
        int screenWidth  = dm.widthPixels / 2;

        // Swipe from bottom-third to top-third (scroll up) or vice-versa (scroll down)
        int fromY = up ? (screenHeight * 3 / 4) : (screenHeight / 4);
        int toY   = up ? (screenHeight / 4)      : (screenHeight * 3 / 4);

        new Thread(() -> {
            try {
                long t = SystemClock.uptimeMillis();

                MotionEvent evDown = MotionEvent.obtain(
                    t, t, MotionEvent.ACTION_DOWN, screenWidth, fromY, 0);

                // Send several MOVE events to simulate a real swipe
                int steps = 10;
                for (int i = 1; i <= steps; i++) {
                    float y = fromY + (toY - fromY) * i / (float) steps;
                    MotionEvent evMove = MotionEvent.obtain(
                        t, t + i * 20L, MotionEvent.ACTION_MOVE, screenWidth, y, 0);
                    dispatchToWindow(evMove);
                    evMove.recycle();
                    SystemClock.sleep(20);
                }

                MotionEvent evUp = MotionEvent.obtain(
                    t, t + steps * 20L, MotionEvent.ACTION_UP, screenWidth, toY, 0);

                dispatchToWindow(evDown);
                dispatchToWindow(evUp);
                evDown.recycle();
                evUp.recycle();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Dispatch a MotionEvent to whatever window is currently in focus
     * (i.e. the app underneath the overlay).
     */
    private void dispatchToWindow(MotionEvent event) {
        try {
            // Use Instrumentation to inject the event system-wide
            new android.app.Instrumentation().sendPointerSync(event);
        } catch (Exception e) {
            // Instrumentation may throw on non-rooted devices for cross-window injection.
            // Fall back to the UiAutomation approach via shell — works on many MIUI builds.
            try {
                android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
                int x = (int) event.getX();
                int y = (int) event.getY();
                Runtime.getRuntime().exec(
                    new String[]{"sh", "-c",
                        "input tap " + x + " " + y});
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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
