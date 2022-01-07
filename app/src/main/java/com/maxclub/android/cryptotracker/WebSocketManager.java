package com.maxclub.android.cryptotracker;

import android.net.Uri;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketManager {

    private static final String BASE_URL = "wss://streamer.cryptocompare.com/v2";
    private static final String API_KEY = "e14385139e83106810086de9fc23274fd0ff8329aeb5c3630d0d4cb3cb11cf3f";

    private WebSocketListener mWebSocketListener;

    public WebSocketManager(WebSocketListener webSocketListener) {
        mWebSocketListener = webSocketListener;

        Uri uri = Uri.parse(BASE_URL)
                .buildUpon()
                .appendQueryParameter("api_key", API_KEY)
                .build();

        OkHttpClient mClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(uri.toString())
                .build();

        WebSocket webSocket = mClient.newWebSocket(request, mWebSocketListener);
        mClient.dispatcher().executorService().shutdown();
    }

    public WebSocketListener getWebSocketListener() {
        return mWebSocketListener;
    }

    public void setWebSocketListener(WebSocketListener webSocketListener) {
        mWebSocketListener = webSocketListener;
    }
}
