package com.project.mywetherapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.project.mywetherapp.fragment.DayAftTomorr_Fragment;
import com.project.mywetherapp.fragment.Today_Fragment;
import com.project.mywetherapp.fragment.Tomorrow_Fragment;
import com.project.mywetherapp.model.FcstInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
public class MainActivity extends AppCompatActivity { //  implements AutoPermissionsListener

    GpsTracker gpsTracker;
    ProgressDialog dialog;
    TabLayout tabLayout;
    TextView textview_address, baseTime, temp, mois, wind, minTemp, maxTemp;
    WetherAdapter wetherAdapter;
    static String minTempStr, maxTempStr;

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

        Today_Fragment today = new Today_Fragment();
        Tomorrow_Fragment tomorrow = new Tomorrow_Fragment();
        DayAftTomorr_Fragment dat = new DayAftTomorr_Fragment();

        dialog = new ProgressDialog(MainActivity.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("데이터를 가져오는 중 ...");

        getSupportFragmentManager().beginTransaction().replace(R.id.container, today).commit();

        tabLayout = findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                Fragment selected = null;

                // tab의 상태가 선택 상태로 변경.
                int position = tab.getPosition();
                if (position == 0) { // 첫번째 탭
                    selected = today;
                } else if (position == 1) { // 두번째 탭
                    selected = tomorrow;
                } else { // 세번째 탭
                    selected = dat;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.container, selected).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // tab의 상태가 선택되지 않음으로 변경.
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //이미 선택된 tab이 다시 선택 상태로 변경
            }
        });


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

        String address = getCurrentAddress(latitude, longitude);
        System.out.println("address : " + address);
        // 위도, 경도를 얻어 도, 시군구, 읍면동의 정보 얻을 수 있음
        String[] addressArr = address.split(" ");
//                addressArr[3].substring()
        System.out.println("addressArr : " + Arrays.toString(addressArr));
        StringBuilder sb = new StringBuilder();
        sb.append(addressArr[1] + " ");
        sb.append(addressArr[2] + " ");
        sb.append(addressArr[3]); // 뒷부분은 쓰지 않음
        textview_address.setText(sb.toString());

        sendAddressData(sb.toString());
    }

    private void sendAddressData(String address) {
        Intent intent = new Intent(this, LocationService.class);
        Log.i("[Main A - sendA]", "들어온 address : " + address);
        intent.putExtra("address", address);
        intent.putExtra("resultReceiver", resultReceiver);

        startService(intent);
    }

    private void sendXYforWetherInfo(String x, String y) {
        Intent intent = new Intent(getApplicationContext(), WetherService_01.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("x", x);
        intent.putExtra("y", y);
        intent.putExtra("resultReceiver", resultReceiver);
        startService(intent);
    }

    private Handler handler = new Handler();

    private ResultReceiver resultReceiver = new ResultReceiver(handler) {

        @SuppressLint("SetTextI18n")
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            Log.i("[MAIN A - RECEIVER]", "메인 액티비티에서 리시버 작동!!");
            if (resultCode == 1) {
                Log.i("[MAIN A - RECEIVER]", "결과코드 1");

                // 주소 데이터를 받아왔을 때
//                String x = resultData.getString("x");
//                String y = resultData.getString("y");
                Map<String, String> gridXYMap = (Map<String, String>) resultData.getSerializable("xyMap");
                String x = gridXYMap.get("x");
                String y = gridXYMap.get("y");
                Log.i("[MAIN A - RECEIVER]", "x : " + x + ", y : " + y);
                sendXYforWetherInfo(x, y); // 다시 다른 서비스(날씨 정보 받아오는 서비스)로 데이터 전송하는 메소드 호출
            } else if (resultCode == 2) {
                Log.i("[MAIN A - RECEIVER]", "결과코드 2");

                ArrayList<FcstInfo> infoList = (ArrayList<FcstInfo>) resultData.getSerializable("infoList");
                Log.i("[MAIN A - RECEIVER]", "infoList.size : " + infoList.size() + ", infoList : " + infoList.toString());
                FcstInfo info = null;
                wetherAdapter = new WetherAdapter();
                for (int i = 0; i < infoList.size(); i++) {
                    info = infoList.get(i);
                    Log.i("[MAIN A - RECEIVER]", (i + 1) + "번째 info : " + info.toString());

                    wetherAdapter.addInfo(info);
                }
                //일최저온도, 일최고온도 측정
                minTemp = findViewById(R.id.textView17);
                maxTemp = findViewById(R.id.textView18);

                for (FcstInfo i : infoList) {
                    if (i.getCategoryMap().containsKey("TMN")) {
                        Log.i("[MAIN A - RECEIVER]", "(1) TMN : " + i.getCategoryMap().get("TMN"));
                        minTempStr = i.getCategoryMap().get("TMN");
                        minTemp.setText(minTempStr);
                        continue;
                    }
                    if (i.getCategoryMap().containsKey("TMX")) {
                        Log.i("[MAIN A - RECEIVER]", "(2) TMX : " + i.getCategoryMap().get("TMX"));
                        maxTempStr = i.getCategoryMap().get("TMX");
                        maxTemp.setText(maxTempStr);
                        break;
                    }
//                    if(i.getCategoryMap().get("TMN") != null){
//                        Log.i("[MAIN A - RECEIVER]", "(3) TMN : "+i.getCategoryMap().get("TMN"));
//                        minTempStr = i.getCategoryMap().get("TMN");
//                    }
                }
                Log.i("[MAIN A - RECEIVER]", "minTempStr : " + minTempStr + ", maxTempStr : " + maxTempStr);

                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(wetherAdapter);

                dialog.dismiss();
            } else if (resultCode == 3) {
                Log.i("[MAIN A - RECEIVER]", "결과코드 3 맞음!!");

                baseTime = findViewById(R.id.textView4);
                temp = findViewById(R.id.textView16);
                mois = findViewById(R.id.textView14);
                wind = findViewById(R.id.textView15);
                ImageView skyInfo = findViewById(R.id.imageView2);

                FcstInfo info = (FcstInfo) resultData.getSerializable("info"); // FcstInfo info = new FcstInfo(baseDate, baseTime, categoryMap, nx, ny);
                if (info != null) {
                    Log.i("[MAIN A - RECEIVER]", "info : " + info.toString());
                    String bd = info.getBaseDate();
                    baseTime.setText("(" + bd.substring(0, 4) + "년 " + bd.substring(4, 6) + "월 " + bd.substring(6, 8) + "일 " + info.getBaseTime().substring(0, 2) + "시 발표)");
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
                    Log.i("[MAIN A - RECEIVER]", "windVla : " + windVal + ", fcstValD : " + fcstValD);
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
                    Log.i("[MAIN A - RECEIVER]", "info is null");
                }

            } else {
                Log.i("[MAIN A - RECEIVER]", "no selected resultCode");
            }

        }

    };

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
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }
}