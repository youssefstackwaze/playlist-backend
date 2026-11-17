package com.example.music.shuffle;

import com.example.music.model.Genre;
import com.example.music.model.Song;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Spreads genres evenly across the playlist.
 * Each song gets a "slot" = (index within its genre + random jitter) / genre size,
 * then all songs are sorted by slot. Every genre is thus distributed proportionally
 * over the whole playlist instead of being clustered.
 */
@Component
public class GenreBalancedShuffleStrategy implements ShuffleStrategy {

    @Override
    public String name() {
        return "genre-balanced";
    }

    @Override
    public String description() {
        return "Genres evenly interleaved across the playlist";
    }

    @Override
    public List<Song> shuffle(List<Song> songs) {
        Random random = ThreadLocalRandom.current();
        Map<Genre, List<Song>> byGenre = new EnumMap<>(Genre.class);
        for (Song s : songs) {
            byGenre.computeIfAbsent(s.getGenre(), g -> new ArrayList<>()).add(s);
        }

        record Slotted(Song song, double slot) {
        }
        List<Slotted> slotted = new ArrayList<>(songs.size());
        for (List<Song> group : byGenre.values()) {
            Collections.shuffle(group, random);
            int size = group.size();
            double offset = random.nextDouble() / size;
            for (int i = 0; i < size; i++) {
                double jitter = (random.nextDouble() - 0.5) * 0.2 / size;
                slotted.add(new Slotted(group.get(i), (double) i / size + offset + jitter));
            }
        }
        slotted.sort(Comparator.comparingDouble(Slotted::slot));
        return slotted.stream().map(Slotted::song).toList();
    }
}
