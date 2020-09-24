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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.project.mywetherapp.R;
import com.project.mywetherapp.model.wether.FcstInfo;
import com.project.mywetherapp.model.wether.Item;
import com.project.mywetherapp.model.wether.ResponseAll;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WetherService extends Service {

    private static RequestQueue requestQueue;

    private static String serviceKey;
    private static final String VilageUltraUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getUltraSrtNcst"; // 초단기 실황 조회
    private static final String VilageUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getVilageFcst"; // 동네 예보 조회
    private static final String dataType = "JSON"; // 데이터 타입(XML / JSON)
    private static String base_date, base_time; // 발표 일자, 발표 시간
    private static String base_date_curr, base_time_curr;
    private static final int numOfRows = 82;
    private static String nx;
    private static String ny;
    /**
     * 03시(List.get(0)) : 9개
     * 06시(List.get(1)) : 12개(11개 + 아침 최저 온도)
     * 09시(List.get(02)) : 9개
     * 12시(List.get(3)) : 11개
     * 15시(List.get(4)) : 10개(9개 + 낮 최고 온도)
     * 18시(List.get(5)) : 11개
     * 21시(List.get(6)) : 9개
     * 24시(List.get(7)) : 11개
     */

    private static String latitude;
    private static String longitude;

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

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        serviceKey = getString(R.string.wether_api_key);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final ResultReceiver receiver = intent.getParcelableExtra("resultReceiver");

        latitude = intent.getStringExtra("x");
        longitude = intent.getStringExtra("y");
        try {
            // 오늘 날씨
            Map<String, String> dateData = getDateAndTime();
            base_date = dateData.get("baseDate");
            base_time = dateData.get("baseTime");
            String urlStr_1 = createUri(VilageUrl, 1);

            // 현재 실황
            Map<String, String> dateData_curr = getCurrDateAndTime();
            base_date_curr = dateData_curr.get("baseDate");
            base_time_curr = dateData_curr.get("baseTime");
            String urlStr_2 = createUltraUri(VilageUltraUrl);

            // 내일 날씨
            String urlStr_3 = createUri(VilageUrl, 2);

            makeRequestVilage(urlStr_1, 2, receiver); // 동네 예보
            makeRequestUltraVilage(urlStr_2, receiver); // 초단기실황
            makeRequestVilage(urlStr_3, 4, receiver); // 내일 예보

//            makeRequestMidFcst(url, reciver); // 주간 날씨 가져오기(최저/최고 기온 가져오기 + 강수확률 가져오기 -----------> 매일의 최저/최고 기온과 강수확률에 따른 아이콘 변경)
            Log.i("[W Service - onC]", "동네 오늘 예보 url : " + urlStr_1);
            Log.i("[W Service - onC]", "동네 초단기 실황 : " + urlStr_2);
            Log.i("[W Service - onC]", "동네 내일 예보 url : " + urlStr_3);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void makeRequestVilage(String url, int requestCode, final ResultReceiver receiver) {
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            Bundle bundle1 = dataAdapter(response);
            if (bundle1 != null) {
                receiver.send(requestCode, bundle1);
            } else {
                Toast.makeText(getApplicationContext(), "통신에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.i("[W Service - Make Req]", "error : " + error.getMessage());
            error.printStackTrace();
        });
        request.setRetryPolicy(new DefaultRetryPolicy(500000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setShouldCache(false);
        requestQueue.add(request);

    }

    private void makeRequestUltraVilage(String url, ResultReceiver receiver) {
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            Bundle bundle2 = dataAdapterUltra(response);
            receiver.send(3, bundle2);
            stopSelf();
        }, error -> Log.i("[W Service - Make ReqU]", "error : " + error.getMessage()));
        request.setRetryPolicy(new DefaultRetryPolicy(500000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    // 받아온 데이터를 객체에 매핑
    private Bundle dataAdapter(String resp) {
        Gson gson = new Gson();
        ResponseAll responseAll = gson.fromJson(resp, ResponseAll.class);

        ArrayList<Item> itemList = responseAll.response.body.items.item;
        if (itemList != null) {
            ArrayList<FcstInfo> infoList = new ArrayList<>();

            infoList.add(getInfoByFcstTime(itemList, 0, 9)); // 03시 예보 정보
            infoList.add(getInfoByFcstTime(itemList, 9, 12)); // 06시 예보 정보
            infoList.add(getInfoByFcstTime(itemList, 21, 9)); // 09시 예보 정보
            infoList.add(getInfoByFcstTime(itemList, 30, 11)); // 12시 예보 정보
            infoList.add(getInfoByFcstTime(itemList, 41, 10)); // 15시 예보 정보
            infoList.add(getInfoByFcstTime(itemList, 51, 11)); // 18시 예보 정보
            infoList.add(getInfoByFcstTime(itemList, 62, 9)); // 21시 예보 정보
            infoList.add(getInfoByFcstTime(itemList, 71, 11)); // 24시 예보 정보

            Bundle bundle = new Bundle();
            bundle.putSerializable("infoList", infoList);

            return bundle; // itemList
        } else {
            Toast.makeText(getApplicationContext(), "통신에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private Bundle dataAdapterUltra(String resp) {
        Gson gson = new Gson();
        JsonObject responseAll = gson.fromJson(resp, JsonObject.class);
        JsonObject response = responseAll.get("response").getAsJsonObject();
        JsonObject body = response.get("body").getAsJsonObject();
        JsonObject items = body.get("items").getAsJsonObject();
        JsonArray item = items.get("item").getAsJsonArray();

        String baseDate = item.get(0).getAsJsonObject().get("baseDate").toString().replace("\"", "");
        String baseTime = item.get(0).getAsJsonObject().get("baseTime").toString().replace("\"", "");
        nx = item.get(0).getAsJsonObject().get("nx").toString().replace("\"", "");
        ny = item.get(0).getAsJsonObject().get("ny").toString().replace("\"", "");
        Map<String, String> categoryMap = new HashMap<>();
        for (int i = 0; i < item.size(); i++) {
            JsonObject obj = item.get(i).getAsJsonObject();
            categoryMap.put(obj.get("category").toString().replace("\"", ""), obj.get("obsrValue").toString().replace("\"", "")); // 각 카테고리와 그 값을 담음
        }

        FcstInfo info = new FcstInfo(baseDate, baseTime, categoryMap, nx, ny);
        Bundle bundle = new Bundle();
        bundle.putSerializable("info", info);

        return bundle;
    }

    public FcstInfo getInfoByFcstTime(ArrayList<Item> itemList, int index1, int numOfRows) {
        String fcstDate = itemList.get(index1).fcstDate;
        String fcstTime = itemList.get(index1).fcstTime;
        Map<String, String> categoryMap = new HashMap<>();
        for (int i = index1; i < index1 + numOfRows; i++) {
            String category = itemList.get(i).category; // .replace("\"", "")
            String fcstValue = itemList.get(i).fcstValue; // .replace("\"", "")

            switch (category) {
                case "POP": // 강수확률
                case "REH": // 습도
                    fcstValue += "%";
                    break;
                case "PTY": // 강수형태
                    switch (fcstValue) {
                        case "0":
                            fcstValue = "없음";
                            break;
                        case "1":
                            fcstValue = "비";
                            break;
                        case "2":
                            fcstValue = "비 + 눈";
                            break;
                        case "3":
                            fcstValue = "눈";
                            break;
                        case "4":
                            fcstValue = "소나기";
                            break;
                        case "5":
                            fcstValue = "빗방울";
                            break;
                        case "6":
                            fcstValue = "빗방울/흩날림";
                            break;
                        case "7":
                            fcstValue = "눈날림";
                            break;
                    }
                    break;
                case "R06": // 6시간 강수량
                    fcstValue += "mm";
                    break;
                case "S06": // 6시간 신적설
                    fcstValue += "cm";
                    break;
                case "SKY": // 하늘 상태"
                    switch (fcstValue) {
                        case "1":
                            fcstValue = "맑음";
                            break;
                        case "3":
                            fcstValue = "구름많음";
                            break;
                        case "4":
                            fcstValue = "흐림";
                            break;
                    }
                    break;
                case "T3H": // 낮 최고기온
                case "TMX": // 시간 기온
                case "TMN": // 아침 최저기온
                    fcstValue += "℃";
                    break;
                case "UUU": // 풍향(8방위)
                    Double fcstValD = Double.parseDouble(fcstValue);
                    int fcstVal = (int) Math.floor((fcstValD + 22.5 * 0.5) / 22.5);
                    if (fcstVal < 45) {
                        fcstValue = "북북동풍";
                    } else if (fcstVal >= 45 && fcstVal < 90) {
                        fcstValue = "북동동풍";
                    } else if (fcstVal >= 90 && fcstVal < 135) {
                        fcstValue = "동남동풍";
                    } else if (fcstVal >= 136 && fcstVal < 180) {
                        fcstValue = "남동남풍";
                    } else if (fcstVal >= 180 && fcstVal < 225) {
                        fcstValue = "남남서풍";
                    } else if (fcstVal >= 225 && fcstVal < 270) {
                        fcstValue = "남서서풍";
                    } else if (fcstVal >= 270 && fcstVal < 315) {
                        fcstValue = "서북서풍";
                    } else if (fcstVal >= 315 && fcstVal < 360) {
                        fcstValue = "북서북풍";
                    }
                    fcstValue += "m/s";
                    break;
                case "VEC": // 풍속(동서)
                    if (fcstValue.contains("+")) { // 동풍(+)
                        fcstValue.replace("+", "동 ");
                    } else { // 서풍(-)
                        fcstValue.replace("-", "서 ");
                    }
                    fcstValue += "m/s";
                    break;
                case "VVV": // 풍속(남북)
                    if (fcstValue.contains("+")) { // 동풍(+)
                        fcstValue.replace("+", "북 ");
                    } else { // 서풍(-)
                        fcstValue.replace("-", "남 ");
                    }
                    fcstValue += "m/s";
                    break;
                case "WSD": // 풍속
                    fcstValue += "m/s";
                    break;
            }
            categoryMap.put(category, fcstValue);
        }
        FcstInfo info = new FcstInfo(base_date, base_time, fcstDate, fcstTime, categoryMap, nx, ny);
        return info;
    }

    private String createUri(String url, int pageNo) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder(url);

        builder.append("?serviceKey=" + serviceKey)
                .append("&nx=" + latitude)
                .append("&ny=" + longitude)
                .append("&base_date=" + base_date)
                .append("&base_time=" + base_time)
                .append("&dataType=" + dataType)
                .append("&numOfRows=" + numOfRows)
                .append("&pageNo=" + pageNo);
        return builder.toString();
    }


    private String createUltraUri(String url) {
        StringBuilder builder = new StringBuilder(url);

        builder.append("?serviceKey=" + serviceKey)
                .append("&nx=" + latitude)
                .append("&ny=" + longitude)
                .append("&base_date=" + base_date_curr)
                .append("&base_time=" + base_time_curr)
                .append("&dataType=" + dataType)
                .append("&numOfRows=" + numOfRows);
        return builder.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private synchronized Map<String, String> getDateAndTime() {
        LocalDateTime day = null;
        LocalDateTime current = LocalDateTime.now();
        String currentTime = current.format(DateTimeFormatter.ofPattern("HH"));
        // 측정하는 예보일의 전날 밤 11시 발표(내일 날시도 같이)
        Map<String, String> dateData = new HashMap<>();
        String baseDate = "";
        String baseTime = "";

        if (Integer.parseInt(currentTime) >= 23) {
            day = LocalDateTime.now();
            baseDate = day.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseTime = "2300";
        } else {
            day = LocalDateTime.now().minusDays(1);
            baseDate = day.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            baseTime = "2300";
        }

//        Log.i("W Service - DateTime]", day.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ", base_date : " + baseDate + ", base_time : " + baseTime);

        dateData.put("baseDate", baseDate);
        dateData.put("baseTime", baseTime);

        return dateData;
    }

    @SuppressLint("LongLogTag")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private synchronized Map<String, String> getCurrDateAndTime() {
        Map<String, String> dateData = new HashMap<>();
        String baseTime = "";

        LocalDateTime day = LocalDateTime.now();
        String baseDate = day.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String hour = day.format(DateTimeFormatter.ofPattern("HH"));
        String minute = day.format(DateTimeFormatter.ofPattern("mm"));

//        Log.i("[W Service - CurrDateAndTime]", "baseDate : " + baseDate + ", hour : " + hour + ", minute : " + minute);

        if (Integer.parseInt(minute) >= 40) {
            baseTime = hour + "00";
        } else {
            baseTime = (Integer.parseInt(hour) - 1) + "00";
        }

        if (baseTime.length() < 4) {
            baseTime = "0" + baseTime;
        }

        if (Integer.parseInt(baseTime) < 0) {
            baseTime = "2400";
            day = LocalDateTime.now().minusDays(1);
            baseDate = day.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

//        Log.i("W Service - CurrDateAndTime]", day.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ", base_date : " + baseDate + ", base_time : " + baseTime);

        dateData.put("baseDate", baseDate);
        dateData.put("baseTime", baseTime);

        return dateData;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}
