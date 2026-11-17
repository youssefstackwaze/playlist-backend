package com.example.music.recommendation;

import com.example.music.model.Song;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scores a candidate by how well its genre matches the playlist's genre profile:
 * same genre counts 1.0, compatible genre counts 0.5, weighted by the share of
 * each genre in the playlist.
 */
@Component
public class GenreCompatibilityRecommendation implements RecommendationStrategy {

    @Override
    public String name() {
        return "genre-compatibility";
    }

    @Override
    public double weight() {
        return 0.7;
    }

    @Override
    public double score(List<Song> playlistSongs, Song candidate) {
        if (playlistSongs.isEmpty()) {
            return 0;
        }
        double total = 0;
        for (Song s : playlistSongs) {
            if (s.getGenre() == candidate.getGenre()) {
                total += 1.0;
            } else if (s.getGenre().isCompatibleWith(candidate.getGenre())) {
                total += 0.5;
            }
        }
        return total / playlistSongs.size();
    }
}
