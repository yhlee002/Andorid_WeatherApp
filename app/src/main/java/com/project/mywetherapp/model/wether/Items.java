package com.project.mywetherapp.model.wether;

import java.io.Serializable;
import java.util.ArrayList;

public class Items implements Serializable {
    public ArrayList<Item> item;
    public String pageNo;
    public String numOfRows;
    public String totalCount;

    @Override
    public String toString() {
        return "Items{" +
                "item=" + item +
                ", pageNo='" + pageNo + '\'' +
                ", numOfRows='" + numOfRows + '\'' +
                ", totalCount='" + totalCount + '\'' +
                '}';
    }
}
