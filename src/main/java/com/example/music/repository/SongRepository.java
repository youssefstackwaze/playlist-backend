package com.example.music.repository;

import com.example.music.model.Genre;
import com.example.music.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    List<Song> findByGenre(Genre genre);

    List<Song> findByArtistIgnoreCase(String artist);

    List<Song> findByGenreAndArtistIgnoreCase(Genre genre, String artist);
}
