package com.project.mywetherapp.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.project.mywetherapp.R;
// TM좌표를 기반으로 가까운 대기오염 측정소의 목록 조회(가장 가까운 측정소의 측정소명을 반환)
public class NearbyMsrstnListService extends Service {

    private static RequestQueue requestQueue;

    private static String getNearbyMsrstnListUrl = "http://openapi.airkorea.or.kr/openapi/services/rest/MsrstnInfoInqireSvc/getNearbyMsrstnList";
    private static String ServiceKey;
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = manager.getNotificationChannel("02");
            if (channel == null) {
                channel = new NotificationChannel("02", getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
            }
            Notification noti = new NotificationCompat.Builder(this, "02").build();
            startForeground(1, noti);
            manager.cancelAll();
        }

        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        ServiceKey = getString(R.string.air_api_key);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final ResultReceiver receiver = intent.getParcelableExtra("resultReceiver");
        String x = intent.getStringExtra("x");
        String y = intent.getStringExtra("y");

        String url = createUri(x, y);
        makeRequest(url, receiver);

        return super.onStartCommand(intent, flags, startId); // START_REDELIVER_INTENT;
    }

    private void makeRequest(String url, ResultReceiver receiver) {
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(String response) {
                Log.i("[N Service - makeRequest]", "response : "+response);
                Bundle bundle = dataAdapter(response);
                receiver.send(6, bundle);
            }
        }, new Response.ErrorListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("[N Service - makeRequest]", "error : "+error.getMessage());
                error.printStackTrace();
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(500000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setShouldCache(false);
        requestQueue.add(request);
        stopSelf();
    }

    private Bundle dataAdapter(String response) {
        Gson gson = new Gson();
        JsonObject result = gson.fromJson(response, JsonObject.class);
        JsonArray list = result.get("list").getAsJsonArray();
        JsonObject mStation = list.get(0).getAsJsonObject();
        String mStationName = mStation.get("stationName").getAsString();

        Bundle bundle = new Bundle();
        bundle.putString("stationName", mStationName);

        return bundle;
    }

    private String createUri(String x, String y) {
        StringBuilder builder = new StringBuilder();
        builder.append(getNearbyMsrstnListUrl+"?")
                .append("ServiceKey="+ServiceKey)
                .append("&tmX="+x)
                .append("&tmY="+y)
                .append("&_returnType=json");
//                .append("&ver=1.0"); // 도로명주소검색 API가 제공하는 좌표로 가까운 측정소 표출

        Log.i("[N Service - createUri]", "생성된 uri : "+builder.toString());
        return builder.toString();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}
