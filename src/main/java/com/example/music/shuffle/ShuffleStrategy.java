package com.example.music.shuffle;

import com.example.music.model.Song;

import java.util.List;

/**
 * Strategy pattern: each implementation is a different shuffling algorithm,
 * selectable at runtime by its {@link #name()}.
 * Implementations must not modify the input list.
 */
public interface ShuffleStrategy {

    String name();

    String description();

    List<Song> shuffle(List<Song> songs);
}
