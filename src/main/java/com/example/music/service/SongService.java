package com.example.music.service;

import com.example.music.exception.ResourceNotFoundException;
import com.example.music.model.Genre;
import com.example.music.model.Song;
import com.example.music.repository.SongRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SongService {

    private final SongRepository songRepository;

    public SongService(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    public List<Song> search(Genre genre, String artist) {
        boolean hasArtist = artist != null && !artist.isBlank();
        if (genre != null && hasArtist) return songRepository.findByGenreAndArtistIgnoreCase(genre, artist);
        if (genre != null) return songRepository.findByGenre(genre);
        if (hasArtist) return songRepository.findByArtistIgnoreCase(artist);
        return songRepository.findAll();
    }

    public Song get(Long id) {
        return songRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Song", id));
    }

    /** Loads songs keeping the requested order; fails if any id does not exist. */
    public List<Song> getAll(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Song> found = songRepository.findAllById(new LinkedHashSet<>(ids)).stream()
                .collect(Collectors.toMap(Song::getId, Function.identity()));
        List<Song> result = new ArrayList<>(ids.size());
        for (Long id : ids) {
            Song song = found.get(id);
            if (song == null) {
                throw new ResourceNotFoundException("Song", id);
            }
            result.add(song);
        }
        return result;
    }
}
