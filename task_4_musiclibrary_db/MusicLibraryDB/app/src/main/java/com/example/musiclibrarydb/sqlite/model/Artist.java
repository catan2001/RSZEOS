package com.example.musiclibrarydb.sqlite.model;

public class Artist {
    private int id;
    private String name;
    private int genreId;      // ID of the genre
    private String genre;      // Name of the genre for display

    public Artist() {}

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

    public int getGenreId() {
        return genreId;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
