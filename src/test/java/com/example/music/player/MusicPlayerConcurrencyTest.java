package com.example.music.player;

import com.example.music.dto.Dtos.PlayerStateResponse;
import com.example.music.model.Genre;
import com.example.music.model.Song;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

class MusicPlayerConcurrencyTest {

    private static List<Song> songs(int n) {
        List<Song> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Song s = new Song("t" + i, "artist" + i, "album", Genre.POP, 100, "/f" + i);
            s.setId((long) i);
            list.add(s);
        }
        return list;
    }

    @Test
    void basicPlaybackFlow() {
        MusicPlayer player = new MusicPlayer();
        player.load(1L, "p", songs(3), "random");
        assertThat(player.play().status()).isEqualTo("PLAYING");
        assertThat(player.next().currentIndex()).isEqualTo(1);
        assertThat(player.pause().status()).isEqualTo("PAUSED");
        assertThat(player.previous().currentIndex()).isZero();
        player.next();
        player.next();
        assertThat(player.next().status()).isEqualTo("STOPPED"); // end of queue without repeat
        player.setRepeat(true);
        assertThat(player.next().currentIndex()).isZero();
    }

    @Test
    void concurrentNextCallsAreAtomic() throws Exception {
        MusicPlayer player = new MusicPlayer();
        player.load(1L, "p", songs(1000), "random");
        player.play();

        int threads = 100;
        int callsPerThread = 5;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            futures.add(pool.submit(() -> {
                start.await();
                for (int i = 0; i < callsPerThread; i++) {
                    PlayerStateResponse state = player.next();
                    // a snapshot must always be internally consistent
                    assertThat(state.currentSong().id()).isEqualTo((long) state.currentIndex());
                    player.state();
                }
                return null;
            }));
        }
        start.countDown();
        for (Future<?> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        pool.shutdown();

        // No lost updates: exactly threads * callsPerThread increments
        assertThat(player.state().currentIndex()).isEqualTo(threads * callsPerThread);
    }

    @Test
    void unloadOnlyWhenSamePlaylist() {
        MusicPlayer player = new MusicPlayer();
        player.load(1L, "p", songs(2), "random");
        player.unloadIfPlaying(2L);
        assertThat(player.state().queueSize()).isEqualTo(2);
        player.unloadIfPlaying(1L);
        assertThat(player.state().queueSize()).isZero();
    }
}
