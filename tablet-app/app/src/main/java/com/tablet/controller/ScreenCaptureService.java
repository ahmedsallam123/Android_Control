package com.tablet.controller;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import androidx.core.app.NotificationCompat;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ScreenCaptureService extends Service {
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private Handler handler = new Handler();
    private boolean isRunning = false;
    private static WebSocketService webSocketService;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("resultCode") && intent.hasExtra("data")) {
            int resultCode = intent.getIntExtra("resultCode", 0);
            Intent data = intent.getParcelableExtra("data");
            MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            startScreenCapture();
        }
        return START_STICKY;
    }

    private void startScreenCapture() {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        int density = metrics.densityDpi;

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        virtualDisplay = mediaProjection.createVirtualDisplay(
                "ScreenCapture",
                width, height, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(), null, null
        );

        isRunning = true;
        handler.postDelayed(captureRunnable, 100);
    }

    private Runnable captureRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;
            Image image = imageReader.acquireLatestImage();
            if (image != null) {
                Bitmap bitmap = imageToBitmap(image);
                image.close();
                if (bitmap != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
                    byte[] jpegData = baos.toByteArray();
                    if (webSocketService != null) {
                        webSocketService.sendBinary(jpegData);
                    }
                }
            }
            handler.postDelayed(this, 150);
        }
    };

    private Bitmap imageToBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * image.getWidth();
        Bitmap bitmap = Bitmap.createBitmap(
                image.getWidth() + rowPadding / pixelStride,
                image.getHeight(),
                Bitmap.Config.ARGB_8888
        );
        bitmap.copyPixelsFromBuffer(buffer);
        return Bitmap.createBitmap(bitmap, 0, 0, image.getWidth(), image.getHeight());
    }

    public static void setWebSocketService(WebSocketService service) {
        webSocketService = service;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        handler.removeCallbacks(captureRunnable);
        if (virtualDisplay != null) { virtualDisplay.release(); }
        if (mediaProjection != null) { mediaProjection.stop(); }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, "screen_capture_channel")
                .setContentTitle("Screen Capture")
                .setContentText("جاري بث الشاشة...")
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "screen_capture_channel",
                    "Screen Capture",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}