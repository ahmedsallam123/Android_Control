package com.tablet.controller;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.webkit.JavascriptInterface;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static WebView webView;
    private WebSocketService wsService;
    private String myKey = "tablet_" + System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        MainActivity.webView = webView;

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new Bridge(), "Android");
        webView.loadUrl("file:///android_asset/index.html");

        wsService = new WebSocketService(this, myKey);
        // ✅ غيّر IP هنا إلى IP اللابتوب الفعلي
        wsService.connect("ws://192.168.0.163:8080");
        ScreenCaptureService.setWebSocketService(wsService);
    }

    private class Bridge {
        @JavascriptInterface
        public String getKey() { return myKey; }

        @JavascriptInterface
        public void accept() {
            wsService.accept();
            // ✅ طلب تصوير الشاشة بعد القبول
            startScreenCapture();
        }

        @JavascriptInterface
        public void reject() { wsService.reject(); }

        @JavascriptInterface
        public void disconnect() {
            wsService.disconnect();
            stopScreenCapture();
        }

        @JavascriptInterface
        public void openAccessibilitySettings() {
            // ✅ توجيه المستخدم لتفعيل خدمة الوصول
            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
    }

    private void startScreenCapture() {
        MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionManager.createScreenCaptureIntent(), 100);
    }

    private void stopScreenCapture() {
        stopService(new Intent(this, ScreenCaptureService.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Intent serviceIntent = new Intent(this, ScreenCaptureService.class);
            serviceIntent.putExtra("resultCode", resultCode);
            serviceIntent.putExtra("data", data);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }
}