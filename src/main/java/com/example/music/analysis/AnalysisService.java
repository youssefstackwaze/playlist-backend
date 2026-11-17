package com.example.music.analysis;

import com.example.music.model.Playlist;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalysisService {

    private final List<PlaylistAnalyzer> analyzers;

    public AnalysisService(List<PlaylistAnalyzer> analyzers) {
        this.analyzers = analyzers;
    }

    public Map<String, Object> analyze(Playlist playlist) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("playlistId", playlist.getId());
        report.put("playlistName", playlist.getName());
        report.put("songCount", playlist.getSongs().size());
        for (PlaylistAnalyzer analyzer : analyzers) {
            report.put(analyzer.name(), analyzer.analyze(playlist));
        }
        return report;
    }
}
