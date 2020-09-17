package com.project.mywetherapp.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.project.mywetherapp.R;

public class Noti extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "알람입니다.", Toast.LENGTH_SHORT).show();

//        NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
//        Notification.Builder builder = new Notification.Builder(context);
//        builder.setSmallIcon(R.drawable.bg_intro).setWhen(System.currentTimeMillis())
//                .setNumber(1).setContentTitle("날씨어때").setContentText("푸쉬내용")
//                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE).setContentIntent(pendingIntent).setAutoCancel(true);
//
//        Notification noti = builder.build();
//        notificationmanager.notify(1, noti);

        NotificationManager notificationmanager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = null;
        if (notificationmanager.getNotificationChannel("01") == null) {
            notificationmanager.createNotificationChannel(new NotificationChannel("01", "noti", NotificationManager.IMPORTANCE_DEFAULT));
            builder = new NotificationCompat.Builder(context, "01");
        } else {
            builder = new NotificationCompat.Builder(context, "01");
        }

        builder.setContentTitle("오늘의 날씨입니다.");
        builder.setContentText("날씨가 이러쿵 저러쿵");
        builder.setSmallIcon(R.drawable.bg_intro);

        Notification noti = builder.build();
        notificationmanager.notify(1, noti);
    }
}
