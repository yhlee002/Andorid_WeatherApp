package com.project.mywetherapp.util;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.project.mywetherapp.R;
import com.project.mywetherapp.activity.MainActivity;
import com.project.mywetherapp.model.wether.FcstInfo;
import com.project.mywetherapp.service.GridCoorService;
import com.project.mywetherapp.service.NotiService;
import com.project.mywetherapp.service.WetherService;
import com.project.mywetherapp.util.Noti;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotiSettingDialog {
    private Context context;
    private int hour, minute;
    private TimePicker timePicker;

    public NotiSettingDialog(Context context){
        this.context = context;
    }

    public void create(){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.notisetting);
        dialog.show();

        timePicker = dialog.findViewById(R.id.timePicker);

        Button registButton = dialog.findViewById(R.id.registButton);
        registButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                hour = timePicker.getHour();
                minute = timePicker.getMinute();

                Intent intentNoti = new Intent(context, NotiService.class);
                intentNoti.putExtra("hour", hour);
                intentNoti.putExtra("minute", minute);
                context.startService(intentNoti);
                dialog.dismiss();

                Toast.makeText(context, hour + "시 " + minute + "분에 알림이 설정되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        Button unregistButton = dialog.findViewById(R.id.unregistButton);
        unregistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//                Intent intent = new Intent(context, Noti.class);
//                PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//                alarmManager.cancel(pIntent);

                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancelAll();

                dialog.dismiss();

                Toast.makeText(context, "알림이 해제되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}