package com.example.music.shuffle;

import com.example.music.model.Song;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Avoids playing the same artist twice in a row.
 * Greedy algorithm: at each step pick, among artists different from the previous one,
 * the artist with the most remaining songs (ties broken randomly). Picking the largest
 * group first guarantees no repetition whenever that is mathematically possible
 * (i.e. no artist has more than ceil(n/2) songs).
 */
@Component
public class SmartShuffleStrategy implements ShuffleStrategy {

    @Override
    public String name() {
        return "smart";
    }

    @Override
    public String description() {
        return "Random order avoiding consecutive songs by the same artist";
    }

    @Override
    public List<Song> shuffle(List<Song> songs) {
        Random random = ThreadLocalRandom.current();
        Map<String, Deque<Song>> byArtist = songs.stream()
                .collect(Collectors.groupingBy(s -> s.getArtist().toLowerCase(Locale.ROOT),
                        LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    List<Song> list = new ArrayList<>(e.getValue());
                    Collections.shuffle(list, random);
                    return new ArrayDeque<>(list);
                }, (a, b) -> a, LinkedHashMap::new));

        List<Song> result = new ArrayList<>(songs.size());
        String previousArtist = null;
        while (result.size() < songs.size()) {
            String prev = previousArtist;
            List<String> candidates = byArtist.entrySet().stream()
                    .filter(e -> !e.getValue().isEmpty())
                    .filter(e -> !e.getKey().equals(prev))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toCollection(ArrayList::new));
            if (candidates.isEmpty()) {
                // Only the previous artist remains: repetition is unavoidable.
                candidates.add(prev);
            }
            Collections.shuffle(candidates, random);
            String chosen = candidates.stream()
                    .max(Comparator.comparingInt(a -> byArtist.get(a).size()))
                    .orElseThrow();
            result.add(byArtist.get(chosen).poll());
            previousArtist = chosen;
        }
        return result;
    }
}
