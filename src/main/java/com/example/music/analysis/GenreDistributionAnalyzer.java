package com.example.music.analysis;

import com.example.music.model.Genre;
import com.example.music.model.Playlist;
import com.example.music.model.Song;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class GenreDistributionAnalyzer implements PlaylistAnalyzer {

    @Override
    public String name() {
        return "genres";
    }

    @Override
    public Map<String, Object> analyze(Playlist playlist) {
        List<Song> songs = playlist.getSongs();
        Map<Genre, Long> counts = songs.stream()
                .collect(Collectors.groupingBy(Song::getGenre, () -> new EnumMap<>(Genre.class), Collectors.counting()));
        Map<String, Object> percentages = new LinkedHashMap<>();
        counts.forEach((g, c) -> percentages.put(g.name(), Math.round(c * 1000.0 / songs.size()) / 10.0));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("counts", counts);
        result.put("percentages", percentages);
        counts.entrySet().stream().max(Map.Entry.comparingByValue())
                .ifPresent(e -> result.put("dominantGenre", e.getKey()));
        return result;
    }
}
