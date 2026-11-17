package com.example.music.player;

import com.example.music.dto.Dtos.PlayerStateResponse;
import com.example.music.dto.Dtos.SongResponse;
import com.example.music.exception.BadRequestException;
import com.example.music.model.Song;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Global music player: a single instance (Spring singleton bean) shared by the
 * whole application. All state is guarded by a {@link ReentrantReadWriteLock}:
 * many concurrent readers of the state, one writer at a time, and every
 * operation is atomic (state is never observed half-updated).
 */
@Component
public class MusicPlayer {

    public enum Status { STOPPED, PLAYING, PAUSED }

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    // ---- state guarded by lock ----
    private List<Song> queue = List.of();
    private Long playlistId;
    private String playlistName;
    private String shuffleStrategy;
    private int currentIndex = -1;
    private Status status = Status.STOPPED;
    private boolean repeat;
    private int volume = 50;

    /** Replaces the queue (immutable snapshot) and resets the position. */
    public PlayerStateResponse load(Long playlistId, String playlistName, List<Song> songs, String shuffleStrategy) {
        return write(() -> {
            this.queue = List.copyOf(songs);
            this.playlistId = playlistId;
            this.playlistName = playlistName;
            this.shuffleStrategy = shuffleStrategy;
            this.currentIndex = queue.isEmpty() ? -1 : 0;
            this.status = Status.STOPPED;
            return snapshot();
        });
    }

    public PlayerStateResponse play() {
        return write(() -> {
            if (queue.isEmpty()) {
                throw new BadRequestException("No playlist loaded in the player");
            }
            if (currentIndex < 0) {
                currentIndex = 0;
            }
            status = Status.PLAYING;
            return snapshot();
        });
    }

    public PlayerStateResponse pause() {
        return write(() -> {
            if (status == Status.PLAYING) {
                status = Status.PAUSED;
            }
            return snapshot();
        });
    }

    public PlayerStateResponse stop() {
        return write(() -> {
            status = Status.STOPPED;
            currentIndex = queue.isEmpty() ? -1 : 0;
            return snapshot();
        });
    }

    public PlayerStateResponse next() {
        return write(() -> {
            requireQueue();
            if (currentIndex < queue.size() - 1) {
                currentIndex++;
            } else if (repeat) {
                currentIndex = 0;
            } else {
                status = Status.STOPPED; // end of queue
            }
            return snapshot();
        });
    }

    public PlayerStateResponse previous() {
        return write(() -> {
            requireQueue();
            if (currentIndex > 0) {
                currentIndex--;
            } else if (repeat) {
                currentIndex = queue.size() - 1;
            }
            return snapshot();
        });
    }

    public PlayerStateResponse setRepeat(boolean repeat) {
        return write(() -> {
            this.repeat = repeat;
            return snapshot();
        });
    }

    public PlayerStateResponse setVolume(int volume) {
        return write(() -> {
            this.volume = Math.max(0, Math.min(100, volume));
            return snapshot();
        });
    }

    /** Called when a playlist is deleted: unloads it if it is the one playing. */
    public void unloadIfPlaying(Long deletedPlaylistId) {
        write(() -> {
            if (deletedPlaylistId != null && deletedPlaylistId.equals(playlistId)) {
                queue = List.of();
                playlistId = null;
                playlistName = null;
                shuffleStrategy = null;
                currentIndex = -1;
                status = Status.STOPPED;
            }
            return null;
        });
    }

    public PlayerStateResponse state() {
        lock.readLock().lock();
        try {
            return snapshot();
        } finally {
            lock.readLock().unlock();
        }
    }

    // ---- internals (must be called with a lock held) ----

    private void requireQueue() {
        if (queue.isEmpty()) {
            throw new BadRequestException("No playlist loaded in the player");
        }
    }

    private PlayerStateResponse snapshot() {
        SongResponse current = (currentIndex >= 0 && currentIndex < queue.size())
                ? SongResponse.from(queue.get(currentIndex)) : null;
        return new PlayerStateResponse(status.name(), playlistId, playlistName, current, currentIndex,
                queue.size(), shuffleStrategy, repeat, volume, SongResponse.from(queue));
    }

    private <T> T write(Supplier<T> action) {
        lock.writeLock().lock();
        try {
            return action.get();
        } finally {
            lock.writeLock().unlock();
        }
    }
}
