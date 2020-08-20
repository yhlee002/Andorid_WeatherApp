package com.project.mywetherapp.model;

import java.io.Serializable;

public class Item implements Serializable {
    public String baseDate; // 발표 일자
    public String baseTime; // 발표 시각
    public String category; // 자료구분
    public String fcstDate; // 예보 일자
    public String fcstTime; //예보 시간
    public String fcstValue; //예보 값
    public String nx; //예보 지점 x좌표
    public String ny; //예보 지점 y좌표
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

    @Override
    public String toString() {
        return "Item{" +
                "baseDate='" + baseDate + '\'' +
                ", baseTime='" + baseTime + '\'' +
                ", category='" + category + '\'' +
                ", fcstDate='" + fcstDate + '\'' +
                ", fcstTime='" + fcstTime + '\'' +
                ", fcstValue='" + fcstValue + '\'' +
                ", nx='" + nx + '\'' +
                ", ny='" + ny + '\'' +
                '}';
    }
}
