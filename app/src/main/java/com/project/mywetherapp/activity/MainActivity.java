package com.project.mywetherapp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.project.mywetherapp.R;
import com.project.mywetherapp.model.air.Item;
import com.project.mywetherapp.model.wether.FcstInfo;
import com.project.mywetherapp.service.AirService;
import com.project.mywetherapp.service.LocationService;
import com.project.mywetherapp.service.NearbyMsrstnListService;
import com.project.mywetherapp.service.WetherService;
import com.project.mywetherapp.util.GpsTracker;
import com.project.mywetherapp.util.ViewPagerAdapter;
import com.project.mywetherapp.util.WetherAdapter01;
import com.project.mywetherapp.util.WetherAdapter02;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
// 첫 화면(첫번째 탭)에서는 위치 정보 사용에 동의하면 켤 때마다 해당 위치의 날씨 가져오기

/**
 * 위치 가져오기
 * LocationManager 이용
 * 1. LocationManager 객체를 참조
 * 2. Listener 구현 : LocationManager가 Listener를 통해 정보를 받게 됨
 * 3. requestLocationUpdate()를 이용해 업데이트 요청
 */
public class MainActivity extends FragmentActivity { //  implements AutoPermissionsListener //  AppCompatActivity

    GpsTracker gpsTracker;
    ProgressDialog dialog;
    TextView textview_address, baseTime01, baseTime02, temp, mois, wind, minTemp, maxTemp;
    WetherAdapter01 wetherAdapter;
    static String minTempStr, maxTempStr;

    private ViewPager mPager;
    private FragmentPagerAdapter pagerAdapter;
    private final static int num_page = 3;
//    private CircleIndicator3 mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        AutoPermissions.Companion.loadAllPermissions(this, 101);
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                getDatas();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(getApplicationContext(), "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage("위치 정보를 받아오기 위해서는 위치 정보 확인 권한이 필요합니다.")
                .setDeniedMessage("앱을 사용하기 위해서는 설정에서 권한을 허가해주세요.")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();

        Log.i("[Main A]", "loadAllPermissions");

        textview_address = findViewById(R.id.textView2);

//        Today_Fragment today = new Today_Fragment();
//        Tomorrow_Fragment tomorrow = new Tomorrow_Fragment();
//        DayAftTomorr_Fragment dat = new DayAftTomorr_Fragment();

        dialog = new ProgressDialog(MainActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("데이터를 가져오는 중 ...");

        mPager = (ViewPager) findViewById(R.id.container);
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), num_page);
        mPager.setAdapter(pagerAdapter);

        ImageButton ShowLocationButton = findViewById(R.id.imageButton);
        ShowLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDatas();
            }
        });
    }

    private void getDatas() {

        dialog.show();
        // 위치 정보 가져오기
        gpsTracker = new GpsTracker(MainActivity.this);

        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        // TM좌표로 변환(변환된 좌표를 주위 가까운 미세먼지 측정소 정보 조회 api에 사용)
        transCoor(latitude, longitude);

        String address = getCurrentAddress(latitude, longitude);
        System.out.println("address : " + address);
        // 위도, 경도를 얻어 도, 시군구, 읍면동의 정보 얻을 수 있음
        String[] addressArr = address.split(" ");
//                addressArr[3].substring()
        Log.i("[MAIN A]", "얻어진 주소(배열 형태) : " + Arrays.toString(addressArr));
        if(!addressArr[2].equals("사용불가")){
            StringBuilder sb = new StringBuilder();
            sb.append(addressArr[1] + " ");
            sb.append(addressArr[2] + " ");
            textview_address.setText(sb.toString());

            sb.append(addressArr[3]); // 뒷부분은 쓰지 않음
            sendAddressData(sb.toString());
        }
    }

    private void transCoor(double latitude, double longitude) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "https://dapi.kakao.com/v2/local/geo/transcoord.json?x=" + longitude + "&y=" + latitude + "&output_coord=TM";
        Log.i("[MAIN A - transCoor]", "생성된 url : " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("[MAIN A - transCoor]", "response : " + response);
                Map<String, String> XYMap = findTMCoorAdapter(response);
                String x = XYMap.get("x");
                String y = XYMap.get("y");
                Log.i("[MAIN A - transCoor]", "x : "+x+", y : "+y);
                sendXYforNearbyMsrstnList(x, y);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("[MAIN A - transCoor]", "error : " + error.getMessage());
                error.printStackTrace();
            }
        }) {
            @Override
            public Map getHeaders() throws AuthFailureError {
                Map params = new HashMap<>();
                params.put("Authorization", " KakaoAK 22651f413cb25cd590de919f6b94fe10");
                return params;
            }
        };
//        request.
        request.setShouldCache(false);
        queue.add(request);
    }

    private Map<String, String> findTMCoorAdapter(String response) {
        Gson gson = new Gson();
        Map<String, String> XYMap = null;

        JsonObject result = gson.fromJson(response, JsonObject.class);
        if (result != null) {
            JsonArray documents = result.get("documents").getAsJsonArray();
            JsonObject coor = documents.get(0).getAsJsonObject();
            if (documents != null) {
                String x = coor.get("x").getAsString();
                String y = coor.get("y").getAsString();

                if (!(x.equals("NaN") || y.equals("NaN"))) {
                    XYMap = new HashMap<>();
                    XYMap.put("x", x);
                    XYMap.put("y", y);
                }
            }
        }
        return XYMap;
    }

    private void sendAddressData(String address) {
        Intent intent = new Intent(this, LocationService.class);
        Log.i("[Main A - sendA]", "들어온 address : " + address);
        intent.putExtra("address", address);
        intent.putExtra("resultReceiver", resultReceiver);

//        startService(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void sendXYforWetherInfo(String x, String y) {
        Intent intent = new Intent(getApplicationContext(), WetherService.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("x", x);
        intent.putExtra("y", y);
        intent.putExtra("resultReceiver", resultReceiver);
//        startService(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @SuppressLint("LongLogTag")
    private void sendStationDataforAirInfo(String station) {
        Intent intent = new Intent(getApplicationContext(), AirService.class);

        intent.putExtra("station", station);
        intent.putExtra("resultReceiver", resultReceiver);

//        startService(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private Handler handler = new Handler();

    private ResultReceiver resultReceiver = new ResultReceiver(handler) {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @SuppressLint("SetTextI18n")
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            // 현재 주소 좌표
            if (resultCode == 1) {
                // 주소 데이터를 받아왔을 때
                Map<String, String> gridXYMap = (Map<String, String>) resultData.getSerializable("xyMap");
                String x = gridXYMap.get("x");
                String y = gridXYMap.get("y");
//                Log.i("[MAIN A - RECEIVER 1]", "x : " + x + ", y : " + y);
                sendXYforWetherInfo(x, y); // 다시 다른 서비스(날씨 정보 받아오는 서비스)로 데이터 전송하는 메소드 호출
                // 오늘 예보
            } else if (resultCode == 2) {
                ArrayList<FcstInfo> infoList = (ArrayList<FcstInfo>) resultData.getSerializable("infoList");
//                Log.i("[MAIN A - RECEIVER 2]", "infoList.size : " + infoList.size() + ", infoList : " + infoList.toString());

                baseTime01 = findViewById(R.id.textView23);
                String bd = infoList.get(0).getBaseDate();
                baseTime01.setText("(" + bd.substring(0, 4) + "년 " + bd.substring(4, 6) + "월 " + bd.substring(6, 8) + "일 " + infoList.get(0).getBaseTime().substring(0, 2) + "시 발표)");


                FcstInfo info = null;
                wetherAdapter = new WetherAdapter01();
                for (int i = 0; i < infoList.size(); i++) {
                    info = infoList.get(i);
                    wetherAdapter.addInfo(info);
                }
                // 일최저온도, 일최고온도 측정
                minTemp = findViewById(R.id.textView17);
                maxTemp = findViewById(R.id.textView18);

                for (FcstInfo i : infoList) {
                    if (i.getCategoryMap().containsKey("TMN")) {
                        ;
                        minTempStr = i.getCategoryMap().get("TMN");
                        minTemp.setText(minTempStr);
                        continue;
                    }
                    if (i.getCategoryMap().containsKey("TMX")) {
                        maxTempStr = i.getCategoryMap().get("TMX");
                        maxTemp.setText(maxTempStr);
                        break;
                    }
                }

                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(wetherAdapter);

                // 열한시가 넘은 경우 다음날 날씨가 제공됨을 알림
                LocalDateTime now = LocalDateTime.now();
                String currentTime = now.format(DateTimeFormatter.ofPattern("HH"));
                if (Integer.parseInt(currentTime) >= 23) {
                    Toast.makeText(getApplicationContext(), "열한시 이후로는 다음날 날씨를 제공해드립니다.", Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();

                // 오늘 현재 실황
            } else if (resultCode == 3) {
                baseTime02 = findViewById(R.id.textView4);
                temp = findViewById(R.id.textView16);
                mois = findViewById(R.id.textView14);
                wind = findViewById(R.id.textView15);
                ImageView skyInfo = findViewById(R.id.imageView2);

                FcstInfo info = (FcstInfo) resultData.getSerializable("info");
                if (info != null) {
//                    Log.i("[MAIN A - RECEIVER 3]", "info : " + info.toString());
                    String bd = info.getBaseDate();
                    baseTime02.setText("(" + bd.substring(0, 4) + "년 " + bd.substring(4, 6) + "월 " + bd.substring(6, 8) + "일 " + info.getBaseTime().substring(0, 2) + "시 발표)");
                    temp.setText(info.getCategoryMap().get("T1H") + "℃");
                    mois.setText(info.getCategoryMap().get("REH") + "%");
                    String windVal = info.getCategoryMap().get("WSD");
                    Double fcstValD = Double.parseDouble(windVal);
                    if (fcstValD < 4) {
                        windVal += "m/s\n(약한 바람)";
                    } else if (fcstValD >= 4 && fcstValD < 9) {
                        windVal += "m/s\n(약간 강한 바람)";
                    } else if (fcstValD >= 9 && fcstValD < 14) {
                        windVal += "m/s\n(강한 바람)";
                    } else if (fcstValD >= 14) {
                        windVal += "m/s\n(매우 강한 바람)";
                    }
                    wind.setText(windVal);

                    String pty = info.getCategoryMap().get("PTY");
                    switch (pty) {
                        case "0": // 없음
                            skyInfo.setImageResource(R.drawable.icons_big_sun);
                            break;
                        case "1": // 비
                        case "4": // 소나기
                            skyInfo.setImageResource(R.drawable.icons_big_rainy_weather);
                            break;
                        case "5": // 빗방울
                        case "6": // 빗방울/눈날림
                            skyInfo.setImageResource(R.drawable.icons_big_rain);
                            break;
                        case "2": // 비/눈
                            skyInfo.setImageResource(R.drawable.icons_big_rain);
                            break;
                        case "3": // 눈
                        case "7": // 눈날림
                            skyInfo.setImageResource(R.drawable.icons_big_light_snow);
                            break;
                    }
                } else {
                    Log.i("[MAIN A - RECEIVER 3]", "info is null");
                }

                // 내일 예보
            } else if (resultCode == 4) {
                ArrayList<FcstInfo> infoList = (ArrayList<FcstInfo>) resultData.getSerializable("infoList");
//                Log.i("[MAIN A - RECEIVER 4]", "infoList.size : " + infoList.size() + ", infoList : " + infoList.toString());

                FcstInfo info = null;
                WetherAdapter02 wetherAdapter = new WetherAdapter02();
                for (int i = 0; i < infoList.size(); i++) {
                    info = infoList.get(i);

                    wetherAdapter.addInfo(info);
                }

                //일최저온도, 일최고온도 측정
                minTemp = findViewById(R.id.textView37);
                maxTemp = findViewById(R.id.textView36);

                for (FcstInfo i : infoList) {
                    if (i.getCategoryMap().containsKey("TMN")) {
                        minTempStr = i.getCategoryMap().get("TMN");
                        minTemp.setText(minTempStr);
                        continue;
                    }
                    if (i.getCategoryMap().containsKey("TMX")) {
                        maxTempStr = i.getCategoryMap().get("TMX");
                        maxTemp.setText(maxTempStr);
                        break;
                    }
                }
//                Log.i("[MAIN A - RECEIVER 4]", "minTempStr : " + minTempStr + ", maxTempStr : " + maxTempStr);

                RecyclerView recyclerView2 = findViewById(R.id.recyclerView2);
                LinearLayoutManager layoutManager2 = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                recyclerView2.setLayoutManager(layoutManager2);
                recyclerView2.setAdapter(wetherAdapter);

                // 모레 예보
            } else if (resultCode == 5) {
//                Log.i("[MAIN A - RECEIVER]", "결과코드 5");
//
//                ArrayList<FcstInfo> infoList = (ArrayList<FcstInfo>) resultData.getSerializable("infoList");
//                Log.i("[MAIN A - RECEIVER]", "infoList.size : " + infoList.size() + ", infoList : " + infoList.toString());
//
//                FcstInfo info = null;
//                wetherAdapter = new WetherAdapter01();
//                for (int i = 0; i < infoList.size(); i++) {
//                    info = infoList.get(i);
//
//                    wetherAdapter.addInfo(info);
//                }
//
//                //일최저온도, 일최고온도 측정
//                minTemp = findViewById(R.id.textView30);
//                maxTemp = findViewById(R.id.textView32);
//
//                for (FcstInfo i : infoList) {
//                    if (i.getCategoryMap().containsKey("TMN")) {
//                        Log.i("[MAIN A - RECEIVER]", "(1) TMN : " + i.getCategoryMap().get("TMN"));
//                        minTempStr = i.getCategoryMap().get("TMN");
//                        minTemp.setText(minTempStr);
//                        continue;
//                    }
//                    if (i.getCategoryMap().containsKey("TMX")) {
//                        Log.i("[MAIN A - RECEIVER]", "(2) TMX : " + i.getCategoryMap().get("TMX"));
//                        maxTempStr = i.getCategoryMap().get("TMX");
//                        maxTemp.setText(maxTempStr);
//                        break;
//                    }
//                }
//                Log.i("[MAIN A - RECEIVER]", "minTempStr : " + minTempStr + ", maxTempStr : " + maxTempStr);
//
//                RecyclerView recyclerView = findViewById(R.id.recyclerView2);
//                LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
//                recyclerView.setLayoutManager(layoutManager);
//                recyclerView.setAdapter(wetherAdapter);
            } else if (resultCode == 6) {
                Log.i("[MAIN A - RECEIVER 6]", "결과 코드 6");
                String stationName = (String) resultData.get("stationName");

                // 가까운 측정소를 찾고 나면 이 측정소의 미세먼지 측정 정보 요청
                sendStationDataforAirInfo(stationName);

            } else if (resultCode == 7) { // 미세먼지 측정
                Log.i("[MAIN A - RECEIVER 7]", "결과코드 7");


            } else if (resultCode == 8) {
                Log.i("[MAIN A - RECEIVER 8]", "결과코드 8");

                Item airInfo = (Item) resultData.getSerializable("airInfo");
                if (airInfo != null) {
                    // 미세먼지 측정량
                    TextView air10 = findViewById(R.id.textView26);
                    air10.setText(airInfo.pm10Value);
                    // 초미세먼지 측정량
                    TextView air25 = findViewById(R.id.textView27);
                    air25.setText(airInfo.pm25Value);
                    // 미세먼지 등급
                    TextView air10Grade = findViewById(R.id.textView28);
                    air10Grade.setText("(" + airInfo.pm10Grade1h + ")");
                    // 초미세먼지 등급
                    TextView air25Grade = findViewById(R.id.textView38);
                    air25Grade.setText("(" + airInfo.pm25Grade1h + ")");
                }
            } else {
                Log.i("[MAIN A - RECEIVER]", "no selected resultCode");
            }
        }
    };

    // TM x, y좌표를 이용해 현재 위치에서 가까운 측정소 목록 요청
    private void sendXYforNearbyMsrstnList(String x, String y) {
        Log.i("[MAIN A - NearByMList]", "가까운 측정소 목록을 요청합니다.");
        Intent intent = new Intent(getApplicationContext(), NearbyMsrstnListService.class);
        intent.putExtra("x", x);
        intent.putExtra("y", y);
        intent.putExtra("resultReceiver", resultReceiver);
//        startService(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    // 지오코더 api를 사용해 x, y좌표를 이용해 현재 위치 가져옴
    public String getCurrentAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<android.location.Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }

        android.location.Address address = addresses.get(0);
        return address.getAddressLine(0).toString() + "\n";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("[MAIN A]", "onResume() 호출");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("[MAIN A]", "onRestart() 호출");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("[MAIN A]", "onDestroy() 호출");
    }
}