package com.example.music.recommendation;

import com.example.music.model.Song;
import com.example.music.repository.SongRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Combines every registered {@link RecommendationStrategy} into a weighted score,
 * excludes songs already in the playlist and returns the best candidates.
 */
@Service
public class RecommendationService {

    private final SongRepository songRepository;
    private final List<RecommendationStrategy> strategies;

    public RecommendationService(SongRepository songRepository, List<RecommendationStrategy> strategies) {
        this.songRepository = songRepository;
        this.strategies = strategies;
    }

    @Transactional(readOnly = true)
    public List<Song> recommend(List<Song> playlistSongs, int limit) {
        if (limit <= 0 || playlistSongs.isEmpty()) {
            return List.of();
        }
        Set<Long> existing = playlistSongs.stream().map(Song::getId).collect(Collectors.toSet());
        double totalWeight = strategies.stream().mapToDouble(RecommendationStrategy::weight).sum();

        record Scored(Song song, double score) {
        }
        return songRepository.findAll().stream()
                .filter(s -> !existing.contains(s.getId()))
                .map(s -> new Scored(s, strategies.stream()
                        .mapToDouble(st -> st.weight() * st.score(playlistSongs, s))
                        .sum() / totalWeight))
                .filter(sc -> sc.score() > 0)
                .sorted(Comparator.comparingDouble(Scored::score).reversed()
                        .thenComparing(sc -> sc.song().getId()))
                .limit(limit)
                .map(Scored::song)
                .toList();
    }
}
