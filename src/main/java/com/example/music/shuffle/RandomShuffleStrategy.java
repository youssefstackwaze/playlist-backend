package com.example.music.shuffle;

import com.example.music.model.Song;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Plain Fisher-Yates shuffle. */
@Component
public class RandomShuffleStrategy implements ShuffleStrategy {

    @Override
    public String name() {
        return "random";
    }

    @Override
    public String description() {
        return "Uniform random order";
    }

    @Override
    public List<Song> shuffle(List<Song> songs) {
        List<Song> result = new ArrayList<>(songs);
        Collections.shuffle(result, ThreadLocalRandom.current());
        return result;
    }
}
