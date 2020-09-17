package com.project.mywetherapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

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
import com.project.mywetherapp.model.air.Item;

import org.json.JSONException;

public class AirService extends Service {
    private static RequestQueue requestQueue;

    private static String getMsrstnAcctoRltmMesureDnstyUrl = "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty";
    private static String serviceKey;
    private static String stationName;


    @Override
    public void onCreate() {
        super.onCreate();

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        serviceKey = getString(R.string.air_api_key);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("[A Service - onStart]", "onStartCommand() called");
        final ResultReceiver receiver = intent.getParcelableExtra("resultReceiver");

        stationName = intent.getStringExtra("station");
        String url = createUri();
        Log.i("[A Service - onSC]", "url : "+url);
        makeRequest(url, receiver);

        return super.onStartCommand(intent, flags, startId);
    }

    private void makeRequest(String url, final ResultReceiver receiver) {
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("[A Service - Make Req]", "response : " + response);

                try {
                    Bundle bundle = null;
                    bundle = dataAdapter(response);

                    if (bundle != null) {
                        receiver.send(8, bundle);
                    } else {
                        Toast.makeText(getApplicationContext(), "통신에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                    stopSelf();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("[A Service - Make Req]", "error : " + error.getMessage());
                error.printStackTrace();
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(500000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setShouldCache(false);
        requestQueue.add(request);

        stopSelf();
    }

    private Bundle dataAdapter(String response) throws JSONException {
        Log.i("[A Service - Adapter]", "adapter called");

        Gson gson = new Gson();
        JsonObject result = gson.fromJson(response, JsonObject.class);
        JsonArray list = result.get("list").getAsJsonArray();
        JsonObject currentAirInfo = list.get(0).getAsJsonObject();

        if (currentAirInfo != null) {
            // String dataTime, String pm10Value, String pm25Value, String pm10Grade, String pm25Grade, String pm10Grade1h, String pm25Grade1h
            Item airInfo = new Item(currentAirInfo.get("dataTime").getAsString(),
                    currentAirInfo.get("pm10Value").getAsString(),
                    currentAirInfo.get("pm25Value").getAsString(),
                    currentAirInfo.get("pm10Grade1h").getAsString(),
                    currentAirInfo.get("pm25Grade1h").getAsString());
            // 미세먼지와 초미세먼지 측정량 단위 붙이기
            airInfo.pm10Value += "㎍/㎥";
            airInfo.pm25Value += "㎍/㎥";
            // 현재(1시간 단위) 미세먼지 등급
            switch (airInfo.pm10Grade1h) {
                case "1":
                    airInfo.pm10Grade1h = "좋음";
                    break;
                case "2":
                    airInfo.pm10Grade1h = "보통";
                    break;
                case "3":
                    airInfo.pm10Grade1h = "나쁨";
                    break;
                case "4":
                    airInfo.pm10Grade1h = "매우나쁨";
                    break;
            }

            // 현재(1시간 단위) 초미세먼지 등급
            switch (airInfo.pm25Grade1h) {
                case "1":
                    airInfo.pm25Grade1h = "좋음";
                    break;
                case "2":
                    airInfo.pm25Grade1h = "보통";
                    break;
                case "3":
                    airInfo.pm25Grade1h = "나쁨";
                    break;
                case "4":
                    airInfo.pm25Grade1h = "매우나쁨";
                    break;
            }

            Bundle bundle = new Bundle();
            bundle.putSerializable("airInfo", airInfo);

            return bundle;
        } else {
            Toast.makeText(getApplicationContext(), "통신에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String createUri() {
        Log.i("[A Service - createUri]", "stationName  : "+stationName);

        StringBuilder builder = new StringBuilder();
        builder.append(getMsrstnAcctoRltmMesureDnstyUrl + "?")
                .append("ServiceKey=" + serviceKey)
                .append("&pageNo=1")
                .append("&stationName=" + stationName)
                .append("&dataTerm=DAILY")
                .append("&ver=1.3")
                .append("&_returnType=json");
        Log.i("[A Service - createUri]", "생성된 uri : "+builder.toString());
        return builder.toString();
    }

}
