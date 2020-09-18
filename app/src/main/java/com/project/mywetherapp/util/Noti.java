package com.project.mywetherapp.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

public class Noti extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
//        intent.setAction(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//        stackBuilder.addParentStack(MainActivity.class);
//        stackBuilder.addNextIntent(intent);
//        PendingIntent pIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT).getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT); // 해당 requestCode로 이후 이 알람을 취소할 수 있음

        NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = null;
        if (notificationmanager.getNotificationChannel("01") == null) {
            notificationmanager.createNotificationChannel(new NotificationChannel("01", "noti", NotificationManager.IMPORTANCE_DEFAULT));
            builder = new NotificationCompat.Builder(context, "01");
        } else {
            builder = new NotificationCompat.Builder(context, "01");
        }

        builder.setContentTitle("오늘의 날씨입니다.")
                .setContentText("날씨가 이러쿵 저러쿵")
                .setSmallIcon(R.drawable.bg_intro)
                .setContentIntent(pIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);
//                .addAction(R.drawable.bg_intro, "열기", pIntent);

        AudioManager aManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        switch (aManager.getRingerMode()){
            case AudioManager.RINGER_MODE_NORMAL:
                builder.setDefaults(Notification.DEFAULT_SOUND);
                break;
            case AudioManager.RINGER_MODE_SILENT:
            case AudioManager.RINGER_MODE_VIBRATE:
                builder.setDefaults(Notification.DEFAULT_VIBRATE);
        }

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
