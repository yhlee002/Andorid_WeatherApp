package com.project.mywetherapp.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.project.mywetherapp.R;
import com.project.mywetherapp.activity.IntroActivity;
import com.project.mywetherapp.activity.MainActivity;

import java.util.List;
import java.util.Map;

public class Noti extends BroadcastReceiver {
//    private String minTemper;
//    private String maxTemper;
//    private String popPercent;
    private Map<String, String> wetherDatas;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        wetherDatas = intent.getParcelableExtra("wetherDataMap");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT).getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent pIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT); // 해당 requestCode로 이후 이 알람을 취소할 수 있음

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
                .setContentText("오늘 최저 온도는 "+wetherDatas.get("TMN")+", 최고 온도는 "+wetherDatas.get("TMX")+"이며, 강수확률은 "+wetherDatas.get("POP")+"입니다.")
                .setSmallIcon(R.drawable.bg_intro)
                .setContentIntent(pIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);

        // 기기의 소리 설정에 따른 알림 지정
        AudioManager aManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        switch (aManager.getRingerMode()){
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
        if(!tasks.isEmpty()){
            int taskSize = tasks.size();
            for(int i = 0; i < taskSize; i++){
                ActivityManager.RunningTaskInfo taskInfo = (ActivityManager.RunningTaskInfo) tasks.get(i);
                if(taskInfo.topActivity.getPackageName().equals("com.project.mywetherapp")){
                    acManager.moveTaskToFront(taskInfo.id, 0);
//                    noti.flags = Notification.FLAG_ONGOING_EVENT;
                }

            }
        }

        Notification noti = builder.build();
        notificationmanager.notify(1, noti);
    }
}
