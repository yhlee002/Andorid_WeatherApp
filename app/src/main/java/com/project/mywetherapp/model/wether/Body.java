package com.project.mywetherapp.model.wether;

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
