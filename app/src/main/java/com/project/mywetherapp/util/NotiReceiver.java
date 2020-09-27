package com.project.mywetherapp.util;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.project.mywetherapp.R;
import com.project.mywetherapp.activity.MainActivity;
import com.project.mywetherapp.model.wether.FcstInfo;
import com.project.mywetherapp.service.GridCoorService;
import com.project.mywetherapp.service.WetherService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotiReceiver extends BroadcastReceiver {
    private Map<String, String> wetherDatas;
    private Context context;
    private Intent intent;
    private static Map<String, String> wetherDataMap;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        this.intent = intent;

        getWetherData();

    }

    class setNoti implements Runnable {

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            try {
                Thread.sleep(1000);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(intent);
                PendingIntent pIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT).getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                // 기존에 등록되어 알림이 있다면 모두 제거
                notificationmanager.cancelAll();

                // 새로운 알림 생성
                NotificationCompat.Builder builder = null;
                if (notificationmanager.getNotificationChannel("01") == null) {
                    notificationmanager.createNotificationChannel(new NotificationChannel("01", "noti", NotificationManager.IMPORTANCE_DEFAULT));
                    builder = new NotificationCompat.Builder(context, "01");
                } else {
                    builder = new NotificationCompat.Builder(context, "01");
                }

                builder.setContentTitle("오늘의 날씨입니다.")
                        .setContentText("오늘 기온은 최저 " + wetherDatas.get("TMN") + ", 최고 " + wetherDatas.get("TMX") + "이며,\n 강수확률은 " + wetherDatas.get("POP") + "입니다.")
                        .setSmallIcon(R.drawable.bg_intro)
                        .setContentIntent(pIntent)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true);

                // 기기의 소리 설정에 따른 알림 지정
                AudioManager aManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                switch (aManager.getRingerMode()) {
                    case AudioManager.RINGER_MODE_NORMAL: // 소리
                        builder.setDefaults(Notification.DEFAULT_SOUND);
                        break;
                    case AudioManager.RINGER_MODE_SILENT: // 무음
                    case AudioManager.RINGER_MODE_VIBRATE: // 진동
                        builder.setDefaults(Notification.DEFAULT_VIBRATE);
                }

                // 어플이 실행중이라면 알림을 클릭할 경우 해당 어플의 마지막 스택으로 이동(재실행X)
                ActivityManager acManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                List tasks = acManager.getRunningTasks(30);
                if (!tasks.isEmpty()) {
                    int taskSize = tasks.size();
                    for (int i = 0; i < taskSize; i++) {
                        ActivityManager.RunningTaskInfo taskInfo = (ActivityManager.RunningTaskInfo) tasks.get(i);
                        if (taskInfo.topActivity.getPackageName().equals("com.project.mywetherapp")) {
                            acManager.moveTaskToFront(taskInfo.id, 0);
                        }

                    }
                }

                Notification noti = builder.build();

                PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, "My:Tag");
                wakeLock.acquire(5000);

                notificationmanager.notify(1, noti);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void getWetherData() {
        GpsTracker gpsTracker = new GpsTracker(context);
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();
        String address = getCurrentAddress(latitude, longitude);
        String[] addressArr = address.split(" ");

        if (!(addressArr[0].equals("지오코더") || addressArr[0].equals("잘못된") || addressArr[0].equals("주소"))) {
            StringBuilder sb = new StringBuilder();
            sb.append(addressArr[1] + " ");
            sb.append(addressArr[2] + " ");
            sb.append(addressArr[3]);

            sendAddressData(sb.toString());
        }
    }

    // 지오코더 api를 사용해 x, y좌표를 이용해 현재 위치 가져옴
    public String getCurrentAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
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
        Intent intent = new Intent(context, GridCoorService.class);
        intent.putExtra("address", address);
        intent.putExtra("resultReceiver", resultReceiver);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    // 날씨 정보를 위해 x, y좌표 전달
    private void sendXYforWetherInfo(String x, String y) {
        Intent intent = new Intent(context, WetherService.class);
        intent.putExtra("x", x);
        intent.putExtra("y", y);
        intent.putExtra("resultReceiver", resultReceiver);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    private Handler handler = new Handler();
    private ResultReceiver resultReceiver = new ResultReceiver(handler) {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == 1) { // 현재 주소 좌표를 통해 주소 데이터를 받아왔을 때
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
                        if (popPercent != "0%") {
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

                wetherDatas = new HashMap<>();
                wetherDatas.put("POP", popPercent);
                wetherDatas.put("TMN", minTempStr);
                wetherDatas.put("TMX", maxTempStr);


                Log.i("[Noti]", "최저온도 : " + wetherDatas.get("TMN") + ", 최고온도 : " + wetherDatas.get("TMX") + ", 강수확률 : " + wetherDatas.get("POP"));
                // 스레드 실행
                setNoti setNoti = new setNoti();
                setNoti.run();

            } else {

            }
        }
    };
}
