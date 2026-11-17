package com.example.music.recommendation;

import com.example.music.model.Song;

import java.util.List;

/**
 * A scoring criterion used to recommend songs for a playlist.
 * Scores are in [0, 1] and are combined by {@link RecommendationService} using {@link #weight()}.
 */
public interface RecommendationStrategy {

    String name();

    double weight();

    double score(List<Song> playlistSongs, Song candidate);
}
