package com.project.mywetherapp;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;

import com.android.volley.RequestQueue;

// 생활지수 api 활용
public class WetherService_02 extends Service {
    private static RequestQueue requestQueue;

    private static final String ServiceKey = "8gA2A8NFYG3D%2FenW2vQ7O6UwSULM6PKpj%2FzK8wgD6Cc%2Bywysc4ovitXhnyeC8did2cRmODX14y3dCZ5GUOEEmA%3D%3D";
    private static final String url = "http://apis.data.go.kr/1360000/LivingWthrIdxService";
    private static final String dataType = "JSON"; // 데이터 타입(XML / JSON)
    private static String time; // yyyyMMddHH
    private static String areaNo;
    private static String numOfRows; // 필수 X
    private int pageNo = 1; // 필수 X

    static ResultReceiver receiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        receiver = intent.getParcelableExtra("resultReceiver");
        Bundle bundle = intent.getBundleExtra("bundle");

        return super.onStartCommand(intent, flags, startId);
    }

}
