package com.example.music.analysis;

import com.example.music.model.Playlist;
import com.example.music.model.Song;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DurationAnalyzer implements PlaylistAnalyzer {

    @Override
    public String name() {
        return "duration";
    }

    @Override
    public Map<String, Object> analyze(Playlist playlist) {
        List<Song> songs = playlist.getSongs();
        int total = playlist.totalDurationSeconds();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalSeconds", total);
        result.put("formatted", String.format("%dh %02dm %02ds", total / 3600, (total % 3600) / 60, total % 60));
        result.put("averageSeconds", songs.isEmpty() ? 0 : total / songs.size());
        songs.stream().max(Comparator.comparingInt(Song::getDurationSeconds))
                .ifPresent(s -> result.put("longest", s.toString()));
        songs.stream().min(Comparator.comparingInt(Song::getDurationSeconds))
                .ifPresent(s -> result.put("shortest", s.toString()));
        return result;
    }
}
