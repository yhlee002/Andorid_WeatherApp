package com.project.mywetherapp.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.project.mywetherapp.model.wether.FcstInfo;
import com.project.mywetherapp.util.GpsTracker;
import com.project.mywetherapp.util.NotiReceiver;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// 매일 일정한 시간에 Noti를 실행하기 위한 서비스
public class NotiService extends Service {
    private static AlarmManager alarmManager;
    private static int hour, minute;
    private Map<String, String> wetherDataMap;

    private Intent intent;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("[Noti Service]", "onCreate() 호출");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;

        getWetherData();

        return START_REDELIVER_INTENT; // START_STICKY : 서비스 재실행 but. 인텐트는 null
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("[Noti Service]", "onDestroy() 호출");
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.i("[Noti Service]", "onRebind() 호출");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        startService(rootIntent);
        Log.i("[Noti Service]", "onTaskRemoved() 호출");
    }

    private void getWetherData() {
        GpsTracker gpsTracker = new GpsTracker(getApplicationContext());
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();
        String address = getCurrentAddress(latitude, longitude);
        String[] addressArr = address.split(" ");

        if (!(addressArr[0].equals("지오코더")||addressArr[0].equals("잘못된")||addressArr[0].equals("주소"))) {
            StringBuilder sb = new StringBuilder();
            sb.append(addressArr[1] + " ");
            sb.append(addressArr[2] + " ");
            sb.append(addressArr[3]);

            sendAddressData(sb.toString());
        }
    }

    // 지오코더 api를 사용해 x, y좌표를 이용해 현재 위치 가져옴
    public String getCurrentAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            return "주소 미발견";
        }

        android.location.Address address = addresses.get(0);
        return address.getAddressLine(0).toString() + "\n";
    }

    // Google Geocoding api를 위한 주소 전달
    private void sendAddressData(String address) {
        Intent intent = new Intent(getApplicationContext(), GridCoorService.class);
        intent.putExtra("address", address);
        intent.putExtra("resultReceiver", resultReceiver);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    // 날씨 정보를 위해 x, y좌표 전달
    private void sendXYforWetherInfo(String x, String y) {
        Intent intent = new Intent(getApplicationContext(), WetherService.class);
        intent.putExtra("x", x);
        intent.putExtra("y", y);
        intent.putExtra("resultReceiver", resultReceiver);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private Handler handler = new Handler();
    private ResultReceiver resultReceiver = new ResultReceiver(handler){
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if(resultCode == 1){ // 현재 주소 좌표를 통해 주소 데이터를 받아왔을 때
                Map<String, String> gridXYMap = (Map<String, String>) resultData.getSerializable("xyMap");
                String x = gridXYMap.get("x");
                String y = gridXYMap.get("y");

                sendXYforWetherInfo(x, y);
            } else if (resultCode == 2) { // 오늘 예보
                ArrayList<FcstInfo> infoList = (ArrayList<FcstInfo>) resultData.getSerializable("infoList");
                // 일최저온도, 일최고온도 측정
                String minTempStr = "";
                String maxTempStr = "";
                String popPercent = "0%";
                for (FcstInfo i : infoList) {
                    if (i.getCategoryMap().containsKey("POP")) {
                        if(popPercent != "0%"){
                            popPercent = i.getCategoryMap().get("POP");
                        }
                    }
                    if (i.getCategoryMap().containsKey("TMN")) {
                        minTempStr = i.getCategoryMap().get("TMN");
                        continue;
                    }
                    if (i.getCategoryMap().containsKey("TMX")) {
                        maxTempStr = i.getCategoryMap().get("TMX");
                        break;
                    }
                }

                wetherDataMap = new HashMap<>();
                wetherDataMap.put("POP", popPercent);
                wetherDataMap.put("TMN", minTempStr);
                wetherDataMap.put("TMX", maxTempStr);

                if (intent != null) {
                    hour = intent.getIntExtra("hour", 0);
                    minute = intent.getIntExtra("minute", 0);
                    Log.i("[Noti Service]", "전송받은 시간 : " + hour + ":" + minute);

                    // 메인 액티비티로의 전환
                    Intent intent2 = new Intent(getApplicationContext(), NotiReceiver.class); // 지정된 시간이 되어 알람이 발생할 경우 Noti클래스에 방송을 해주기 위해 명시
                    intent2.putExtra("wetherDataMap", (Serializable) wetherDataMap);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    // 지정한 시간에 매일 알림
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                }
            } else if(resultCode == 3){ // 오늘 현재 초단기 실황(필요X)

            } else if(resultCode == 4){ // 내일 날씨 예보(필요X)

            }
        }
    };
}
