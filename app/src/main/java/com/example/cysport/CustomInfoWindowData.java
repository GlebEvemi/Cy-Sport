package com.example.cysport;

public class CustomInfoWindowData {
    private int id;
    private String title;
    private int placeGroup;


    private String desc;

    private String image;

    private double lat;

    private double lon;
    private float rating;

    public CustomInfoWindowData(int id, String title, int placeGroup, String desc, String image, double lat, double lon, float rating) {
        this.id = id;
        this.placeGroup = placeGroup;
        this.title = title;
        this.desc = desc;
        this.image = image;
        this.lat = lat;
        this.lon = lon;
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getPlaceGroup() {
        return placeGroup;
    }

    public void setPlaceGroup(int placeGroup) {
        this.placeGroup = placeGroup;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    // Геттеры
    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getImage() {
        return image;
    }

    // Сеттеры (если нужны)
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setImage(String image) {
        this.image = image;
    }

}