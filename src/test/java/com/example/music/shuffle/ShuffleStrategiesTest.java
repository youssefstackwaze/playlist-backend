package com.example.music.shuffle;

import com.example.music.exception.BadRequestException;
import com.example.music.model.Genre;
import com.example.music.model.Song;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShuffleStrategiesTest {

    private static long ids = 1;

    static Song song(String artist, Genre genre) {
        Song s = new Song("t" + ids, artist, "album", genre, 200, "/f" + ids + ".mp3");
        s.setId(ids++);
        return s;
    }

    /** 4 songs from A, 3 from B, 2 from C, 1 from D - a valid no-repeat order exists. */
    static List<Song> sample() {
        List<Song> songs = new ArrayList<>();
        for (int i = 0; i < 4; i++) songs.add(song("A", Genre.ROCK));
        for (int i = 0; i < 3; i++) songs.add(song("B", Genre.POP));
        for (int i = 0; i < 2; i++) songs.add(song("C", Genre.JAZZ));
        songs.add(song("D", Genre.METAL));
        return songs;
    }

    @Test
    void randomKeepsAllSongsAndDoesNotModifyInput() {
        List<Song> input = sample();
        List<Song> copy = List.copyOf(input);
        List<Song> result = new RandomShuffleStrategy().shuffle(input);
        assertThat(result).containsExactlyInAnyOrderElementsOf(input);
        assertThat(input).containsExactlyElementsOf(copy);
    }

    @RepeatedTest(50)
    void smartNeverPlaysSameArtistConsecutively() {
        List<Song> result = new SmartShuffleStrategy().shuffle(sample());
        assertThat(result).hasSize(10);
        for (int i = 1; i < result.size(); i++) {
            assertThat(result.get(i).getArtist()).isNotEqualTo(result.get(i - 1).getArtist());
        }
    }

    @Test
    void smartHandlesUnavoidableRepetition() {
        List<Song> songs = List.of(song("A", Genre.ROCK), song("A", Genre.ROCK), song("A", Genre.ROCK), song("B", Genre.POP));
        assertThat(new SmartShuffleStrategy().shuffle(songs)).containsExactlyInAnyOrderElementsOf(songs);
    }

    @RepeatedTest(20)
    void genreBalancedSpreadsGenres() {
        List<Song> songs = new ArrayList<>();
        for (int i = 0; i < 6; i++) songs.add(song("A" + i, Genre.ROCK));
        for (int i = 0; i < 6; i++) songs.add(song("B" + i, Genre.JAZZ));
        List<Song> result = new GenreBalancedShuffleStrategy().shuffle(songs);
        assertThat(result).containsExactlyInAnyOrderElementsOf(songs);
        // Each half of the playlist must contain both genres (no clustering).
        assertThat(result.subList(0, 6)).extracting(Song::getGenre).contains(Genre.ROCK, Genre.JAZZ);
        assertThat(result.subList(6, 12)).extracting(Song::getGenre).contains(Genre.ROCK, Genre.JAZZ);
    }

    @Test
    void registrySelectsStrategyAtRuntime() {
        ShuffleStrategyRegistry registry = new ShuffleStrategyRegistry(List.of(
                new RandomShuffleStrategy(), new SmartShuffleStrategy(), new GenreBalancedShuffleStrategy()));
        assertThat(registry.get("SMART")).isInstanceOf(SmartShuffleStrategy.class);
        assertThat(registry.get(null)).isInstanceOf(RandomShuffleStrategy.class);
        assertThatThrownBy(() -> registry.get("unknown")).isInstanceOf(BadRequestException.class);
    }
}
