package com.example.findmybuga;

import java.io.Serializable;

public class PoiPos implements Serializable {
    private String Description;
    private String Long;
    private String Lati;

    public PoiPos() {
    }

    public PoiPos(String description, String aLong, String lati) {
        Description = description;
        Long = aLong;
        Lati = lati;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getLong() {
        return Long;
    }

    public void setLong(String aLong) {
        Long = aLong;
    }

    public String getLati() {
        return Lati;
    }

    public void setLati(String lati) {
        Lati = lati;
    }

}
