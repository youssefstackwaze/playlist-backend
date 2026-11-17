package com.example.music.recommendation;

import com.example.music.model.Song;
import org.springframework.stereotype.Component;

import java.util.List;

/** Favours songs from artists already present in the playlist (proportionally to their share). */
@Component
public class ArtistSimilarityRecommendation implements RecommendationStrategy {

    @Override
    public String name() {
        return "artist-similarity";
    }

    @Override
    public double weight() {
        return 0.3;
    }

    @Override
    public double score(List<Song> playlistSongs, Song candidate) {
        if (playlistSongs.isEmpty()) {
            return 0;
        }
        long sameArtist = playlistSongs.stream()
                .filter(s -> s.getArtist().equalsIgnoreCase(candidate.getArtist()))
                .count();
        return (double) sameArtist / playlistSongs.size();
    }
}
