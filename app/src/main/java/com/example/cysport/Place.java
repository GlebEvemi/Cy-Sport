package com.example.cysport;
import android.media.Image;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class Place{
    private String group;
    private String name;
    private String desc;
    private MarkerOptions marker;
    private Image[] img = new Image[]{};
    private LatLng location;

    public Place(String group, String name, String desc, MarkerOptions marker, Image[] img, LatLng location) {
        this.group = group;
        this.name = name;
        this.desc = desc;
        this.marker = marker;
        this.img = img;
        this.location = location;
    }

    public MarkerOptions getMarkerOptions() {
        return marker;
    }


}