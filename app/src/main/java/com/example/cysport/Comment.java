package com.example.cysport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;

public class Comment {
    private String username;
    private String text;
    private int rating;
    private int areaId;

    public Comment(String username, String text, int rating, int areaId) {
        this.username = username;
        this.text = text;
        this.rating = rating;
        this.areaId = areaId;
    }

    public String toJson() throws IOException {
        ObjectMapper Obj = new ObjectMapper();
        return Obj.writeValueAsString(this);
    }

    public Comment() {

    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }
}
