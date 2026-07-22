package com.tablet.controller;

import android.util.Log;
import org.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.Response;
import okio.ByteString;

public class WebSocketService {
    private WebSocket ws;
    private MainActivity activity;
    private String myKey;
    private String laptopKey = "";

    public WebSocketService(MainActivity activity, String myKey) {
        this.activity = activity;
        this.myKey = myKey;
    }

    public void connect(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                ws = webSocket;
                try {
                    JSONObject reg = new JSONObject();
                    reg.put("type", "register");
                    reg.put("key", myKey);
                    ws.send(reg.toString());
                } catch (Exception e) {}
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JSONObject json = new JSONObject(text);
                    String type = json.getString("type");
                    if (type.equals("incoming")) {
                        laptopKey = json.getString("from");
                        activity.runOnUiThread(() -> {
                            MainActivity.webView.loadUrl("javascript:onIncoming('" + laptopKey + "')");
                        });
                    } else if (type.equals("connected")) {
                        activity.runOnUiThread(() -> {
                            MainActivity.webView.loadUrl("javascript:onConnected()");
                        });
                    } else if (type.equals("rejected")) {
                        activity.runOnUiThread(() -> {
                            MainActivity.webView.loadUrl("javascript:onRejected()");
                        });
                    } else if (type.equals("disconnected")) {
                        activity.runOnUiThread(() -> {
                            MainActivity.webView.loadUrl("javascript:onDisconnected()");
                        });
                    } else if (type.equals("control")) {
                        String action = json.getString("action");
                        if (action.equals("tap")) {
                            int x = json.getInt("x");
                            int y = json.getInt("y");
                            AccessibilityControlService.performTap(activity, x, y);
                        } else if (action.equals("swipe")) {
                            int x1 = json.getInt("x1");
                            int y1 = json.getInt("y1");
                            int x2 = json.getInt("x2");
                            int y2 = json.getInt("y2");
                            AccessibilityControlService.performSwipe(activity, x1, y1, x2, y2);
                        }
                    }
                } catch (Exception e) {
                    Log.e("WS", "Error", e);
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                // استقبال بيانات ثنائية (إذا أرسلها الخادم)
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.i("WS", "Closed: " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("WS", "Failure", t);
            }
        });
    }

    public void accept() {
        try {
            JSONObject accept = new JSONObject();
            accept.put("type", "accept");
            accept.put("from", myKey);
            accept.put("to", laptopKey);
            ws.send(accept.toString());
        } catch (Exception e) {}
    }

    public void reject() {
        try {
            JSONObject reject = new JSONObject();
            reject.put("type", "reject");
            reject.put("from", myKey);
            reject.put("to", laptopKey);
            ws.send(reject.toString());
        } catch (Exception e) {}
    }

    public void disconnect() {
        try {
            JSONObject dis = new JSONObject();
            dis.put("type", "disconnect");
            dis.put("from", myKey);
            dis.put("to", laptopKey);
            ws.send(dis.toString());
        } catch (Exception e) {}
    }

    public void sendBinary(byte[] data) {
        if (ws != null) {
            ws.send(ByteString.of(data));
        }
    }
}