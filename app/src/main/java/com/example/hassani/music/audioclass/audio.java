package com.example.hassani.music.audioclass;

import java.io.Serializable;

public class audio implements Serializable {

    private String data;
    private String title;
    private String album;
    private String artist;
    private String duration;

    public audio(String data, String title, String album, String artist,String duration) {
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.duration=duration;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDuration() {
        return duration;
    }
}
