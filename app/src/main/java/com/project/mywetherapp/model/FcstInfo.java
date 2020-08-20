package com.project.mywetherapp.model;

import java.io.Serializable;
import java.util.Map;

public class FcstInfo implements Serializable {
    private String baseDate; // 발표 일자
    private String baseTime; // 발표 시각
    private String fcstDate; // 예보 일자
    private String fcstTime; //예보 시간
    private Map<String, String> categoryMap;
    // category : 자료구분, fcstValue : 예보 값
    private String nx; //예보 지점 x좌표
    private String ny; //예보 지점 y좌표
    /*
     * category values
     * POP	강수확률	 %
     * PTY	강수형태	코드값
     * R06	6시간 강수량	범주 (1 mm)
     * REH	습도	 %
     * S06	6시간 신적설	범주(1 cm)
     * SKY	하늘상태	코드값
     * T3H	3시간 기온	 ℃
     * TMN	아침 최저기온	 ℃
     * TMX	낮 최고기온	 ℃
     * UUU	풍속(동서성분)	 m/s
     * VVV	풍속(남북성분)	 m/s
     */

    public FcstInfo() {
    }

    public FcstInfo(String baseDate, String baseTime, Map<String, String> categoryMap, String nx, String ny) {
        this.baseDate = baseDate;
        this.baseTime = baseTime;
        this.categoryMap = categoryMap;
        this.nx = nx;
        this.ny = ny;
    }

    public FcstInfo(String baseDate, String baseTime, String fcstDate, String fcstTime, Map<String, String> categoryMap, String nx, String ny) {
        this.baseDate = baseDate;
        this.baseTime = baseTime;
        this.fcstDate = fcstDate;
        this.fcstTime = fcstTime;
        this.categoryMap = categoryMap;
        this.nx = nx;
        this.ny = ny;
    }

    public String getBaseDate() {
        return baseDate;
    }

    public void setBaseDate(String baseDate) {
        this.baseDate = baseDate;
    }

    public String getBaseTime() {
        return baseTime;
    }

    public void setBaseTime(String baseTime) {
        this.baseTime = baseTime;
    }

    public String getFcstDate() {
        return fcstDate;
    }

    public void setFcstDate(String fcstDate) {
        this.fcstDate = fcstDate;
    }

    public String getFcstTime() {
        return fcstTime;
    }

    public void setFcstTime(String fcstTime) {
        this.fcstTime = fcstTime;
    }

    public Map<String, String> getCategoryMap() {
        return categoryMap;
    }

    public void setCategoryMap(Map<String, String> categoryMap) {
        this.categoryMap = categoryMap;
    }

    public String getNx() {
        return nx;
    }

    public void setNx(String nx) {
        this.nx = nx;
    }

    public String getNy() {
        return ny;
    }

    public void setNy(String ny) {
        this.ny = ny;
    }

    @Override
    public String toString() {
        return "FcstInfo{" +
                "baseDate='" + baseDate + '\'' +
                ", baseTime='" + baseTime + '\'' +
                ", fcstDate='" + fcstDate + '\'' +
                ", fcstTime='" + fcstTime + '\'' +
                ", categoryMap=" + categoryMap +
                ", nx='" + nx + '\'' +
                ", ny='" + ny + '\'' +
                '}';
    }
}
