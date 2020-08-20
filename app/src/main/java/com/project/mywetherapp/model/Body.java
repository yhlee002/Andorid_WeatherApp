package com.project.mywetherapp.model;

public class Body {
    public String dataType;
    public Items items;

    @Override
    public String toString() {
        return "Body{" +
                "dataType='" + dataType + '\'' +
                ", items=" + items +
                '}';
    }
}
