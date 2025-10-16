package com.example.musiclibrarydb.sqlite.model;


public class Song {
    int id;
    String name;
    String genre;
    String artist;

    public Song() {
    }

    public Song(String name, String genre, String artist) {
        this.name = name;
        this.genre = genre;
        this.artist = artist;
    }

    public Song(int id, String name, String genre, String artist) {
        this.id = id;
        this.name = name;
        this.genre = genre;
        this.artist = artist;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}