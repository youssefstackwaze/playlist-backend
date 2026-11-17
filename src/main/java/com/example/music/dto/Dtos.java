package com.example.music.dto;

import com.example.music.model.Genre;
import com.example.music.model.Playlist;
import com.example.music.model.Song;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;
import java.util.List;

/** All request/response DTOs of the API, grouped as nested records. */
public final class Dtos {

    private Dtos() {
    }

    public record SongResponse(Long id, String title, String artist, String album,
                               Genre genre, int durationSeconds, String filePath) {
        public static SongResponse from(Song s) {
            return new SongResponse(s.getId(), s.getTitle(), s.getArtist(), s.getAlbum(),
                    s.getGenre(), s.getDurationSeconds(), s.getFilePath());
        }

        public static List<SongResponse> from(List<Song> songs) {
            return songs.stream().map(SongResponse::from).toList();
        }
    }

    public record PlaylistRequest(@NotBlank String name, String description, String owner,
                                  List<Long> songIds) {
    }

    public record PlaylistResponse(Long id, String name, String description, String owner,
                                   Instant createdAt, Instant updatedAt, int songCount,
                                   int totalDurationSeconds, List<SongResponse> songs) {
        public static PlaylistResponse from(Playlist p) {
            return new PlaylistResponse(p.getId(), p.getName(), p.getDescription(), p.getOwner(),
                    p.getCreatedAt(), p.getUpdatedAt(), p.getSongs().size(),
                    p.totalDurationSeconds(), SongResponse.from(p.getSongs()));
        }
    }

    public record AddSongRequest(@NotNull Long songId, @PositiveOrZero Integer position) {
    }

    public record MoveSongRequest(@NotNull @PositiveOrZero Integer from,
                                  @NotNull @PositiveOrZero Integer to) {
    }

    public record SmartPlaylistRequest(@NotBlank String name, String description, String owner,
                                       List<Long> seedSongIds,
                                       @Min(0) @Max(50) Integer recommendationCount,
                                       String shuffleStrategy,
                                       Boolean autoPlay) {
    }

    public record SmartPlaylistResponse(PlaylistResponse playlist,
                                        List<SongResponse> addedRecommendations,
                                        String shuffleStrategy,
                                        PlayerStateResponse player) {
    }

    public record PlayerStateResponse(String status, Long playlistId, String playlistName,
                                      SongResponse currentSong, int currentIndex, int queueSize,
                                      String shuffleStrategy, boolean repeat, int volume,
                                      List<SongResponse> queue) {
    }

    public record VolumeRequest(@NotNull @Min(0) @Max(100) Integer volume) {
    }

    public record ErrorResponse(Instant timestamp, int status, String error, String message) {
    }
}
