package com.maxclub.android.cryptotracker;

import android.net.Uri;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class CryptoCompareClient {

    private static final String TAG = "WebSocket";

    private static final String BASE_URL = "wss://streamer.cryptocompare.com/v2";
    private static final String API_KEY = "e14385139e83106810086de9fc23274fd0ff8329aeb5c3630d0d4cb3cb11cf3f";

    private WebSocket mWebSocket;

    public Observable<String> connect() {
        return Observable.create(subscriber -> {
            Uri uri = Uri.parse(BASE_URL)
                    .buildUpon()
                    .appendQueryParameter("api_key", API_KEY)
                    .build();

            OkHttpClient mClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(uri.toString())
                    .build();

            mWebSocket = mClient.newWebSocket(request, new WebSocketListener() {

                @Override
                public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                    super.onOpen(webSocket, response);

                    Log.i(TAG, "WebSocket.onOpen()");

                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put("5~CCCAGG~BTC~USD");
                    jsonArray.put("0~Coinbase~BTC~USD");
                    jsonArray.put("0~Cexio~BTC~USD");

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("action", "SubAdd");
                        jsonObject.put("subs", jsonArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    webSocket.send(jsonObject.toString());
                }

                @Override
                public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                    super.onMessage(webSocket, text);

                    Log.i(TAG, "WebSocket.onMessage() -> " + text);
                    subscriber.onNext(text);
                }

                @Override
                public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                    super.onFailure(webSocket, t, response);

                    Log.e(TAG, "WebSocket.onFailure()", t);

                    subscriber.onError(t);
                }

                @Override
                public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                    super.onClosing(webSocket, code, reason);

                    Log.i(TAG, "WebSocket.onClosing() -> " + reason);

                    webSocket.close(code, reason);
                    subscriber.onComplete();
                }

            });
            mClient.dispatcher().executorService().shutdown();
        });
    }

    public void disconnect() {
        if (mWebSocket != null) {
            mWebSocket.cancel();
        }
    }
}
