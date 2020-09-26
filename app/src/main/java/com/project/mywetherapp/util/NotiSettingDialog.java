package com.project.mywetherapp.util;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.project.mywetherapp.R;
import com.project.mywetherapp.service.NotiService;

public class NotiSettingDialog {
    private Context context;
    private int hour, minute;
    private TimePicker timePicker;

    public NotiSettingDialog(Context context) {
        this.context = context;
    }

    public void create() {
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

                Toast.makeText(context, hour + "시 " + (minute >= 10 ? minute + "" : "0" + minute) + "분에 알림이 설정되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        Button unregistButton = dialog.findViewById(R.id.unregistButton);
        unregistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancelAll();

                dialog.dismiss();

                Toast.makeText(context, "알림이 해제되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}