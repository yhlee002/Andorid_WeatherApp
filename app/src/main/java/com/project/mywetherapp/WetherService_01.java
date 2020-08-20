package com.project.mywetherapp;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

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
import com.project.mywetherapp.model.FcstInfo;
import com.project.mywetherapp.model.Item;
import com.project.mywetherapp.model.ResponseAll;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WetherService_01 extends Service {

    private static RequestQueue requestQueue;

    private static String serviceKey;
    private static final String VilageUltraUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getUltraSrtNcst"; // 초단기 실황 조회
    private static final String VilageUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getVilageFcst"; // 동네 예보 조회
    private static final String dataType = "JSON"; // 데이터 타입(XML / JSON)
    private static String base_date; // 발표 일자
    private static String base_time; // 발표 일시
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
        Log.i("[W Service - onCreate]", "onCreate() called");
        super.onCreate();

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
         serviceKey = getString(R.string.wether_api_key);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("[W Service - onStart]", "onStartCommand() called");
        final ResultReceiver receiver = intent.getParcelableExtra("resultReceiver");

        latitude = intent.getStringExtra("x");
        longitude = intent.getStringExtra("y");

        Map<String, String> dateData = getDateAndTime();
        base_date = dateData.get("baseDate");
        base_time = dateData.get("baseTime");
        Log.i("[W Service - onStart]", "latitude : " + latitude + ", longitude : " + longitude + ", base_date : " + base_date + ", base_time : " + base_time);

        try {
            String urlStr_1 = createUri(VilageUrl);
            String urlStr_2 = createUri(VilageUltraUrl);
            makeRequestVilage(urlStr_1, receiver); // 동네 예보
            makeRequestUltraVilage(urlStr_2, receiver); // 초단기예보

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

    private void makeRequestVilage(String url, final ResultReceiver receiver) {
        System.out.println("makeRequestVilage메소드의 url : " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            Log.i("[W Service - Make ReqV]", "response : " + response);

            Bundle bundle1 = dataAdapter(response);
            receiver.send(2, bundle1);
        }, error -> {
            Log.i("[W Service - Make ReqV]", "error : " + error.getMessage());
            error.printStackTrace();
        });

        request.setShouldCache(false);
        requestQueue.add(request); // 자동으로 웹 서버에 요청을 해주고 응답을 받음
    }

    private void makeRequestUltraVilage(String url, ResultReceiver receiver) {
        System.out.println("makeRequestUltraVilage메소드의 url : " + url);
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            Log.i("[W Service - Make ReqU]", "response : " + response);

            Bundle bundle2 = dataAdapterUltra(response);
            receiver.send(3, bundle2);
        }, error -> Log.i("[W Service - Make ReqU]", "error : " + error.getMessage()));
        request.setRetryPolicy(new DefaultRetryPolicy(500000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private Bundle dataAdapterUltra(String resp) {
        Log.i("[W Service - AdapterU]", "adapter called");
        Log.i("[W Service - AdapterU]", resp);
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
        Log.i("[W Service - AdapterU]", "categoryMap : " + categoryMap.toString());
        Log.i("[W Service - AdapterU]", "(Serializable) categoryMap : " + (Serializable) categoryMap.toString());

        FcstInfo info = new FcstInfo(baseDate, baseTime, categoryMap, nx, ny);
        Bundle bundle = new Bundle();
        bundle.putSerializable("info", info);

        return bundle;
    }

    //받아온 데이터를 객체에 매핑
    private Bundle dataAdapter(String resp) {
        Log.i("[W Service - Adapter]", "adapter called");
        Log.i("[W Service - Adapter]", resp);

        Gson gson = new Gson();

        ResponseAll responseAll = gson.fromJson(resp, ResponseAll.class);
        System.out.println(responseAll.toString());

        ArrayList<Item> itemList = responseAll.response.body.items.item;

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
                        case "2":
                            fcstValue = "구름많음";
                            break;
                        case "3":
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
                    int fcstVal = (int)Math.floor((fcstValD + 22.5 * 0.5) / 22.5);
                    if (fcstVal < 45){ fcstValue = "북북동풍";}
                    else if(fcstVal >= 45 && fcstVal < 90){ fcstValue = "북동동풍";}
                    else if(fcstVal >= 90 && fcstVal < 135){ fcstValue = "동남동풍";}
                    else if(fcstVal >= 136 && fcstVal < 180){ fcstValue = "남동남풍";}
                    else if (fcstVal >= 180 && fcstVal < 225) { fcstValue = "남남서풍";}
                    else if(fcstVal >= 225 && fcstVal < 270){ fcstValue = "남서서풍";}
                    else if(fcstVal >= 270 && fcstVal < 315){fcstValue = "서북서풍";}
                    else if(fcstVal >= 315 && fcstVal < 360){fcstValue = "북서북풍";}
                    fcstValue += "m/s";
                    break;
                case "VEC": // 풍속(동서)
                    if(fcstValue.contains("+")){ // 동풍(+)
                        fcstValue.replace("+", "동 ");
                    }else { // 서풍(-)
                        fcstValue.replace("-", "서 ");
                    }
                    fcstValue += "m/s";
                    break;
                case "VVV": // 풍속(남북)
                    if(fcstValue.contains("+")){ // 동풍(+)
                        fcstValue.replace("+", "북 ");
                    }else { // 서풍(-)
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

    private String createUri(String url) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder(url);

        builder.append("?serviceKey=" + serviceKey)
                .append("&nx=" + latitude)
                .append("&ny=" + longitude)
                .append("&base_date=" + base_date)
                .append("&base_time=" + base_time)
                .append("&dataType=" + dataType)
                .append("&numOfRows=" + numOfRows)
                .append("&pageNo=1");

        System.out.println("[WetherService - createUri] builder.toString() : " + builder.toString());
        return builder.toString();
    }

    private Map<String, String> getDateAndTime() {
        // 늘 측정하는 예보일의 전날 밤 11시 발표
        Map<String, String> dateData = new HashMap<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            Log.i("W Service - DateTime]", oneDayAgo.toString());
            base_date = oneDayAgo.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            base_time = "2300";

            dateData.put("baseDate", base_date);
            dateData.put("baseTime", base_time);
        }
        return dateData;
    }
}

