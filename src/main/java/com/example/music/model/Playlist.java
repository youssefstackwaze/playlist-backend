package com.example.music.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private String owner;

    private Instant createdAt;

    private Instant updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "playlist_songs",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id"))
    @OrderColumn(name = "position")
    private List<Song> songs = new ArrayList<>();

    protected Playlist() {
    }

    public Playlist(String name, String description, String owner) {
        this.name = name;
        this.description = description;
        this.owner = owner;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public void addSong(Song song, int position) {
        songs.add(position, song);
    }

    /** Removes all occurrences of the song; returns true if anything was removed. */
    public boolean removeSong(Long songId) {
        return songs.removeIf(s -> s.getId().equals(songId));
    }

    public void moveSong(int from, int to) {
        Song song = songs.remove(from);
        songs.add(to, song);
    }

    public int totalDurationSeconds() {
        return songs.stream().mapToInt(Song::getDurationSeconds).sum();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<Song> getSongs() { return songs; }
}
