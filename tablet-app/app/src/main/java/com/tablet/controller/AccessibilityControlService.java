package com.tablet.controller;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

public class AccessibilityControlService extends AccessibilityService {
    private static AccessibilityControlService instance;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onServiceConnected() {
        instance = this;
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    public static void performTap(Context context, int x, int y) {
        if (instance == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Path path = new Path();
            path.moveTo(x, y);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 1));
            instance.dispatchGesture(builder.build(), null, null);
        }
    }

    public static void performSwipe(Context context, int x1, int y1, int x2, int y2) {
        if (instance == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Path path = new Path();
            path.moveTo(x1, y1);
            path.lineTo(x2, y2);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 300));
            instance.dispatchGesture(builder.build(), null, null);
        }
    }
}