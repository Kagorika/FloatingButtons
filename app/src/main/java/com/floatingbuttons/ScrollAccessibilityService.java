package com.floatingbuttons;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

public class ScrollAccessibilityService extends AccessibilityService {

    public static ScrollAccessibilityService instance;
    public static final String ACTION_SCROLL = "com.floatingbuttons.SCROLL";
    public static final String EXTRA_UP = "scroll_up";

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    // Called by FloatingButtonService via broadcast
    public static void requestScroll(boolean up) {
        if (instance != null) {
            instance.performScroll(up);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
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
