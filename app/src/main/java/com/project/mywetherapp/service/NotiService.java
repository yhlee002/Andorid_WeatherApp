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

        if (intent != null) {
            hour = intent.getIntExtra("hour", 0);
            minute = intent.getIntExtra("minute", 0);
            Log.i("[Noti Service]", "전송받은 시간 : " + hour + ":" + minute);

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
}
