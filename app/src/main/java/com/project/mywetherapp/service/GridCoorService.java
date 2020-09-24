package com.project.mywetherapp.service;

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
import androidx.core.app.NotificationCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.project.mywetherapp.R;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
// 기상청 기준 지명이 아니라서 날씨 정보를 받아오지 못할 경우 구글 Geocoding api 를 사용해 구글 기준 주소를 받아옴
// http://www.kma.go.kr/DFSROOT/POINT/DATA/top.json.txt
// http://www.kma.go.kr/DFSROOT/POINT/DATA/mdl.[시 code].json.txt
// http://www.kma.go.kr/DFSROOT/POINT/DATA/leaf.[동/읍/면 code].json.txt
public class GridCoorService extends Service {
    private final static String API_URL = "https://maps.googleapis.com/maps/api/geocode/json?";
    private static String API_KEY;
    private static String address;

    private static RequestQueue requestQueue;
    private static ResultReceiver receiver;
    private static Map<String, String> locationMap;
    private static String topCode, midCode;
    private static String[] addressArr;
    private static Map<String, String> xyMap;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = manager.getNotificationChannel("02");
            if(channel == null){
                channel = new NotificationChannel("02", getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "02");
            builder.setContentTitle("날씨어때");
            builder.setContentText("날씨어때 어플이 실행중입니다.");
            builder.setAutoCancel(true);
            builder.setSmallIcon(R.mipmap.ic_launcher_round);

            Notification noti = builder.build();
            startForeground(1,noti);
            manager.cancelAll();
        }

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this);
        }

        API_KEY = getString(R.string.location_api_key);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String address = (String) intent.getStringExtra("address");
        receiver = intent.getParcelableExtra("resultReceiver");

        makeRequest(address.split(" "));
        return super.onStartCommand(intent, flags, startId);
    }

    private void findLocation(String[] addressArr) throws UnsupportedEncodingException {
        topCode = "0";
        midCode = "0";

        locationMap = new HashMap<>();

        GridCoorService.addressArr = addressArr;
        findTop(addressArr[0]); // '시, 도' 코드
    }

    private void makeRequest(String[] addressArr) {
        String url = null;
        address = addressArr[0] + " " + addressArr[1] + " " + addressArr[2];
        try {
            url = API_URL + "address=" + URLEncoder.encode(address, "UTF-8") + "&key="+API_KEY;
            Log.i("[GridCoorService- mR]", "url : " + url);
            StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
                xyMap = processLocation(response);
                if (xyMap == null) {
                    try {
                        findLocation(address.split(" "));

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("xyMap", (Serializable) xyMap);
                        receiver.send(1, bundle);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("xyMap", (Serializable) xyMap);
                    receiver.send(1, bundle);
                }
            }, error -> error.printStackTrace());
            request.setShouldCache(false);
            requestQueue.add(request);
            stopSelf();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> processLocation(String response) {
        String latitude = "";
        String longitude = "";

        JSONObject obj = (JSONObject) JSONValue.parse(response);
        JSONArray array = (JSONArray) obj.get("results");
        for (Object o : array) {
            JSONObject obj2 = (JSONObject) o;
            latitude = ((JSONObject) (((JSONObject) obj2.get("geometry")).get("location"))).get("lat").toString();
            longitude = ((JSONObject) (((JSONObject) obj2.get("geometry")).get("location"))).get("lng").toString();
        }

        Map<String, String> gridXYMap = getGridXY(latitude, longitude);
        return gridXYMap;
    }

    public Map<String, String> getGridXY(String lat, String lon) {
        double RE = 6371.00877; // 지구 반경(km)
        double GRID = 5.0; // 격자 간격(km)
        double SLAT1 = 30.0; // 투영 위도1(degree)
        double SLAT2 = 60.0; // 투영 위도2(degree)
        double OLON = 126.0; // 기준점 경도(degree)
        double OLAT = 38.0; // 기준점 위도(degree)
        double XO = 43; // 기준점 X좌표(GRID)
        double YO = 136; // 기1준점 Y좌표(GRID)

        double DEGRAD = Math.PI / 180.0; // double RADDEG = 180.0 / Math.PI;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double latD = Double.parseDouble(lat);
        double lonD = Double.parseDouble(lon);

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);

        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;

        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        Map<String, String> map = new HashMap<>();

        String latitude = Double.toString(latD);
        String longitude = Double.toString(lonD).substring(0, Double.toString(lonD).indexOf("."));

        map.put("lat", latitude);
        map.put("lng", longitude);
        double ra = Math.tan(Math.PI * 0.25 + (latD) * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = lonD * DEGRAD - olon;
        if (theta > Math.PI)
            theta -= 2.0 * Math.PI;
        if (theta < -Math.PI)
            theta += 2.0 * Math.PI;
        theta *= sn;

        String x = Double.toString(Math.floor(ra * Math.sin(theta) + XO + 0.5));
        String y = Double.toString(Math.floor(ro - ra * Math.cos(theta) + YO + 0.5));
        map.put("x", x.substring(0, x.indexOf(".")));
        map.put("y", y.substring(0, y.indexOf(".")));

        return map;
    }

    private void findTop(final String s) throws UnsupportedEncodingException {
        final String urlStr = "http://www.kma.go.kr/DFSROOT/POINT/DATA/top.json.txt";

        StringRequest request = new StringRequest(Request.Method.GET, urlStr, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                JsonArray responseArr = gson.fromJson(response, JsonArray.class);
                JsonObject jobj;

                for (int i = 0; i < responseArr.size(); i++) {
                    jobj = (JsonObject) responseArr.get(i);
                    String value = jobj.get("value").getAsString().replace("\"", "");
                    if (value.equals(s)) {
                        topCode = jobj.get("code").getAsString();
                        Log.i("[GridCoorService]", s + "코드 : " + topCode);
                        break;
                    }
                }

                findMid(topCode, addressArr[1]); // '시, 군, 구' 코드
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(GridCoorService.this, "통신에 실패했습니다.", Toast.LENGTH_SHORT).show();
                Log.i("[GridCoorService - fT]", " 통신 실패 : ");
                error.printStackTrace();
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    String utf8String = new String(response.data, "UTF-8");
                    return Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (Exception e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        request.setShouldCache(false);
//        requestQueue = Volley.newRequestQueue(this);
        GridCoorService.requestQueue.add(request);
    }

    private void findMid(final String topCode, final String s) {
        final String url = "http://www.kma.go.kr/DFSROOT/POINT/DATA/mdl." + topCode + ".json.txt";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                JsonArray responseArr = gson.fromJson(response, JsonArray.class);
                JsonObject jobj;

                for (int i = 0; i < responseArr.size(); i++) {
                    jobj = (JsonObject) responseArr.get(i);
                    String value = jobj.get("value").getAsString().replace("\"", "");
                    if (value.equals(s)) {
                        midCode = jobj.get("code").getAsString();
                        System.out.println(s + "코드 : " + midCode);
                        break;
                    }
                }

                findLeaf(midCode, addressArr[2]); // '동, 읍, 면, 리' 코드
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(GridCoorService.this, "통신에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    String utf8String = new String(response.data, "UTF-8");
                    return Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (Exception e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        request.setShouldCache(false);
        GridCoorService.requestQueue.add(request);
    }

    private void findLeaf(final String midCode, final String s) {
        final String url = "http://www.kma.go.kr/DFSROOT/POINT/DATA/leaf." + midCode + ".json.txt";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                JsonArray responseArr = gson.fromJson(response, JsonArray.class);
                JsonObject jobj;

                for (int i = 0; i < responseArr.size(); i++) {
                    jobj = (JsonObject) responseArr.get(i);
                    String value = jobj.get("value").getAsString().replace("\"", "");
                    if (value.equals(s)) {
                        locationMap.put("x", jobj.get("x").getAsString());
                        locationMap.put("y", jobj.get("y").getAsString());
                        break;
                    }
                }

                String x = locationMap.get("x");
                String y = locationMap.get("y");

                Log.i("[GridCoorService - onSC]", "x : " + x + ", y : " + y);

                Bundle bundle = new Bundle();
                Map<String, String> xyMap = new HashMap<>();
                xyMap.put("x", x);
                xyMap.put("y", y);
                bundle.putSerializable("xyMap", (Serializable) xyMap);
                receiver.send(1, bundle);
                stopSelf();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(GridCoorService.this, "통신에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    String utf8String = new String(response.data, "UTF-8");
                    return Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (Exception e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        request.setShouldCache(false);
        GridCoorService.requestQueue.add(request);
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
