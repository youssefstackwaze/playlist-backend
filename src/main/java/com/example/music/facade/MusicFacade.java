package com.example.music.facade;

import com.example.music.dto.Dtos.*;
import com.example.music.model.Playlist;
import com.example.music.player.MusicPlayer;
import com.example.music.service.PlaylistService;
import com.example.music.service.PlaylistService.CreatedWithRecommendations;
import com.example.music.shuffle.ShuffleStrategy;
import com.example.music.shuffle.ShuffleStrategyRegistry;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Facade orchestrating the multi-step "create + recommend + play" operation.
 * The database steps run in one {@code @Transactional} service call
 * ({@link PlaylistService#createWithRecommendations}); the player is only
 * touched after that transaction has committed successfully.
 */
@Service
public class MusicFacade {

    private static final int DEFAULT_RECOMMENDATIONS = 5;

    private final PlaylistService playlistService;
    private final ShuffleStrategyRegistry shuffleRegistry;
    private final MusicPlayer player;

    public MusicFacade(PlaylistService playlistService, ShuffleStrategyRegistry shuffleRegistry, MusicPlayer player) {
        this.playlistService = playlistService;
        this.shuffleRegistry = shuffleRegistry;
        this.player = player;
    }

    public SmartPlaylistResponse createSmartPlaylist(SmartPlaylistRequest request) {
        // Validate the strategy first so we fail before writing anything.
        ShuffleStrategy strategy = shuffleRegistry.get(request.shuffleStrategy());
        int count = Objects.requireNonNullElse(request.recommendationCount(), DEFAULT_RECOMMENDATIONS);
        boolean autoPlay = Objects.requireNonNullElse(request.autoPlay(), Boolean.TRUE);

        // 1 + 2. create + recommend (single transaction)
        CreatedWithRecommendations created = playlistService.createWithRecommendations(
                new PlaylistRequest(request.name(), request.description(), request.owner(), request.seedSongIds()),
                count);

        // 3. shuffle + play
        Playlist playlist = created.playlist();
        PlayerStateResponse state = player.load(playlist.getId(), playlist.getName(),
                strategy.shuffle(playlist.getSongs()), strategy.name());
        if (autoPlay && !playlist.getSongs().isEmpty()) {
            state = player.play();
        }
        return new SmartPlaylistResponse(PlaylistResponse.from(playlist),
                SongResponse.from(created.recommendations()), strategy.name(), state);
    }

    public PlayerStateResponse loadIntoPlayer(Long playlistId, String strategyName, boolean autoPlay) {
        ShuffleStrategy strategy = shuffleRegistry.get(strategyName);
        Playlist playlist = playlistService.get(playlistId);
        PlayerStateResponse state = player.load(playlist.getId(), playlist.getName(),
                strategy.shuffle(playlist.getSongs()), strategy.name());
        return autoPlay && !playlist.getSongs().isEmpty() ? player.play() : state;
    }
}
