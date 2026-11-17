package com.example.music.analysis;

import com.example.music.model.Playlist;

import java.util.Map;

/** Pluggable analysis: each analyzer contributes one section of the analysis report. */
public interface PlaylistAnalyzer {

    String name();

    Map<String, Object> analyze(Playlist playlist);
}
