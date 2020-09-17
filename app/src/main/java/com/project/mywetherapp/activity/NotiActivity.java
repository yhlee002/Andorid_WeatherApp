package com.project.mywetherapp.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.project.mywetherapp.R;
import com.project.mywetherapp.util.Noti;

import java.util.Calendar;

public class NotiActivity extends AppCompatActivity {
    private TimePicker timePicker;
    private AlarmManager alarmManager;
    private int hour, minute;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noti);
        Toast.makeText(getApplicationContext(), "NotiActivity 실행", Toast.LENGTH_SHORT).show();

        timePicker = findViewById(R.id.timePicker);

        Button registButton = findViewById(R.id.registButton);
        registButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hour = timePicker.getHour();
                minute = timePicker.getMinute();

                Toast.makeText(getApplicationContext(), "hour : "+hour+", minute : "+minute, Toast.LENGTH_SHORT).show();

                // 메인 액티비티로의 전환
                Intent intent = new Intent(getApplicationContext(), Noti.class); // 지정된 시간이 되어 알람이 발생할 경우 Noti클래스에 방송을 해주기 위해 명시
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                // 지정한 시간에 매일 알림
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                Toast.makeText(getApplicationContext(), "알림이 설정되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        Button unregistButton = findViewById(R.id.unregistButton);
        unregistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "click! 2", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), Noti.class);
                PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
                alarmManager.cancel(pIntent);

                finish();
            }
        });
    }
}