package com.example.music.facade;

import com.example.music.dto.Dtos.PlaylistRequest;
import com.example.music.dto.Dtos.SmartPlaylistRequest;
import com.example.music.dto.Dtos.SmartPlaylistResponse;
import com.example.music.exception.BadRequestException;
import com.example.music.exception.ResourceNotFoundException;
import com.example.music.model.Genre;
import com.example.music.model.Playlist;
import com.example.music.model.Song;
import com.example.music.player.MusicPlayer;
import com.example.music.service.PlaylistService;
import com.example.music.service.PlaylistService.CreatedWithRecommendations;
import com.example.music.shuffle.RandomShuffleStrategy;
import com.example.music.shuffle.ShuffleStrategyRegistry;
import com.example.music.shuffle.SmartShuffleStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MusicFacadeTest {

    @Mock
    PlaylistService playlistService;

    @Spy
    MusicPlayer player = new MusicPlayer();

    MusicFacade facade;

    @BeforeEach
    void setUp() {
        ShuffleStrategyRegistry registry = new ShuffleStrategyRegistry(
                List.of(new RandomShuffleStrategy(), new SmartShuffleStrategy()));
        facade = new MusicFacade(playlistService, registry, player);
    }

    private static Song song(long id, String artist) {
        Song s = new Song("t" + id, artist, "a", Genre.ROCK, 100, "/f" + id);
        s.setId(id);
        return s;
    }

    @Test
    void createRecommendAndPlay() {
        Playlist playlist = new Playlist("Mix", null, "alice");
        playlist.setId(7L);
        List<Song> recs = List.of(song(3, "B"), song(4, "C"));
        playlist.addSong(song(1, "A"));
        playlist.addSong(song(2, "A"));
        recs.forEach(playlist::addSong);
        when(playlistService.createWithRecommendations(any(), anyInt()))
                .thenReturn(new CreatedWithRecommendations(playlist, recs));

        SmartPlaylistResponse response = facade.createSmartPlaylist(
                new SmartPlaylistRequest("Mix", null, "alice", List.of(1L, 2L), 2, "smart", null));

        ArgumentCaptor<PlaylistRequest> captor = ArgumentCaptor.forClass(PlaylistRequest.class);
        verify(playlistService).createWithRecommendations(captor.capture(), eq(2));
        assertThat(captor.getValue().songIds()).containsExactly(1L, 2L);

        verify(player).load(eq(7L), eq("Mix"), anyList(), eq("smart"));
        verify(player).play();
        assertThat(response.addedRecommendations()).hasSize(2);
        assertThat(response.player().status()).isEqualTo("PLAYING");
        assertThat(response.player().queueSize()).isEqualTo(4);
    }

    @Test
    void unknownStrategyFailsBeforeAnyWrite() {
        assertThatThrownBy(() -> facade.createSmartPlaylist(
                new SmartPlaylistRequest("Mix", null, null, List.of(1L), 2, "nope", true)))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(playlistService);
        verify(player, never()).load(any(), any(), any(), any());
    }

    @Test
    void playerUntouchedWhenTransactionFails() {
        when(playlistService.createWithRecommendations(any(), anyInt()))
                .thenThrow(new ResourceNotFoundException("Song", 999));

        assertThatThrownBy(() -> facade.createSmartPlaylist(
                new SmartPlaylistRequest("Mix", null, null, List.of(999L), 2, "random", true)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(player, never()).load(any(), any(), any(), any());
        verify(player, never()).play();
    }
}
