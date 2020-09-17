package com.project.mywetherapp.model.air;

import java.io.Serializable;

public class Item implements Serializable {
//    public String _returnType;
    public String dataTime;
//    public String mangName;
//    public String so2Value;
//    public String coValue;
//    public String o3Value;
//    public String no2Value;
    public String pm10Value; // 미세먼지(PM10) 농도
//    public String pm10Value24;
    public String pm25Value; // 초미세먼지(PM2.5) 농도
//    public String pm25Value24;
//    public String khaiValue;
//    public String khaiGrade;
//    public String so2Grade;
//    public String coGrade;
//    public String o3Grade;
//    public String no2Grade;
//    public String pm10Grade; // 미세먼지(PM10) 등급(24시간)
//    public String pm25Grade; // 초미세먼지(PM2.5) 등급(24시간)
    public String pm10Grade1h; // 미세먼지(PM10) 1시간 등급
    public String pm25Grade1h; // 초미세먼지(PM2.5) 1시간 등급

    public Item(String dataTime, String pm10Value, String pm25Value, String pm10Grade1h, String pm25Grade1h) {
        this.dataTime = dataTime;
        this.pm10Value = pm10Value;
        this.pm25Value = pm25Value;
        this.pm10Grade1h = pm10Grade1h;
        this.pm25Grade1h = pm25Grade1h;
    }

    @Override
    public String toString() {
        return "Item{" +
                "dataTime='" + dataTime + '\'' +
                ", pm10Value='" + pm10Value + '\'' +
                ", pm25Value='" + pm25Value + '\'' +
                ", pm10Grade1h='" + pm10Grade1h + '\'' +
                ", pm25Grade1h='" + pm25Grade1h + '\'' +
                '}';
    }
}
