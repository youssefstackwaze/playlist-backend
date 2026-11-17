package com.example.music.analysis;

import com.example.music.model.Playlist;
import com.example.music.model.Song;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ArtistDiversityAnalyzer implements PlaylistAnalyzer {

    @Override
    public String name() {
        return "artists";
    }

    @Override
    public Map<String, Object> analyze(Playlist playlist) {
        List<Song> songs = playlist.getSongs();
        Map<String, Long> counts = songs.stream()
                .collect(Collectors.groupingBy(Song::getArtist, LinkedHashMap::new, Collectors.counting()));
        int consecutiveRepeats = 0;
        for (int i = 1; i < songs.size(); i++) {
            if (songs.get(i).getArtist().equalsIgnoreCase(songs.get(i - 1).getArtist())) {
                consecutiveRepeats++;
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("uniqueArtists", counts.size());
        result.put("songsPerArtist", counts);
        result.put("diversityRatio", songs.isEmpty() ? 0 : Math.round(counts.size() * 100.0 / songs.size()) / 100.0);
        result.put("consecutiveSameArtist", consecutiveRepeats);
        return result;
    }
}
