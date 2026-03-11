package com.floatingbuttons;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

public class ScrollAccessibilityService extends AccessibilityService {

    public static ScrollAccessibilityService instance;
    public static final String ACTION_SCROLL = "com.floatingbuttons.SCROLL";
    public static final String EXTRA_UP = "scroll_up";

    private BroadcastReceiver scrollReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean up = intent.getBooleanExtra(EXTRA_UP, true);
            performScroll(up);
        }
    };

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        IntentFilter filter = new IntentFilter(ACTION_SCROLL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(scrollReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(scrollReceiver, filter);
        }
    }

    public static void requestScroll(boolean up) {
        if (instance != null) {
            instance.performScroll(up);
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        try { unregisterReceiver(scrollReceiver); } catch (Exception e) {}
    }

    public void performScroll(boolean scrollUp) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;

        android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenHeight = dm.heightPixels;
        int screenWidth  = dm.widthPixels / 2;

        int fromY = scrollUp ? (screenHeight * 3 / 4) : (screenHeight / 4);
        int toY   = scrollUp ? (screenHeight / 4)     : (screenHeight * 3 / 4);

        Path path = new Path();
        path.moveTo(screenWidth, fromY);
        path.lineTo(screenWidth, toY);

        GestureDescription gesture = new GestureDescription.Builder()
            .addStroke(new GestureDescription.StrokeDescription(path, 0, 400))
            .build();

        dispatchGesture(gesture, null, null);
    }
}
