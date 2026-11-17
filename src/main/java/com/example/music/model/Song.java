package com.example.music.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "songs")
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    private String album;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genre genre;

    private int durationSeconds;

    private String filePath;

    protected Song() {
    }

    public Song(String title, String artist, String album, Genre genre, int durationSeconds, String filePath) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.durationSeconds = durationSeconds;
        this.filePath = filePath;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public Genre getGenre() { return genre; }
    public int getDurationSeconds() { return durationSeconds; }
    public String getFilePath() { return filePath; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Song other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return artist + " - " + title;
    }
}
