package com.example.music.service;

import com.example.music.dto.Dtos.AddSongRequest;
import com.example.music.dto.Dtos.MoveSongRequest;
import com.example.music.dto.Dtos.PlaylistRequest;
import com.example.music.exception.BadRequestException;
import com.example.music.exception.ResourceNotFoundException;
import com.example.music.model.Playlist;
import com.example.music.model.Song;
import com.example.music.player.MusicPlayer;
import com.example.music.recommendation.RecommendationService;
import com.example.music.repository.PlaylistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final SongService songService;
    private final MusicPlayer musicPlayer;
    private final RecommendationService recommendationService;

    public PlaylistService(PlaylistRepository playlistRepository, SongService songService,
                           MusicPlayer musicPlayer, RecommendationService recommendationService) {
        this.playlistRepository = playlistRepository;
        this.songService = songService;
        this.musicPlayer = musicPlayer;
        this.recommendationService = recommendationService;
    }

    @Transactional(readOnly = true)
    public List<Playlist> findAll() {
        return playlistRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Playlist get(Long id) {
        return playlistRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Playlist", id));
    }

    public Playlist create(PlaylistRequest request) {
        Playlist playlist = new Playlist(request.name(), request.description(), request.owner());
        songService.getAll(request.songIds()).forEach(playlist::addSong);
        return playlistRepository.save(playlist);
    }

    /** Updates metadata; if songIds is provided, it replaces the whole content (in the given order). */
    public Playlist update(Long id, PlaylistRequest request) {
        Playlist playlist = get(id);
        playlist.setName(request.name());
        playlist.setDescription(request.description());
        playlist.setOwner(request.owner());
        if (request.songIds() != null) {
            List<Song> songs = songService.getAll(request.songIds());
            playlist.getSongs().clear();
            playlist.getSongs().addAll(songs);
        }
        return playlistRepository.saveAndFlush(playlist);
    }

    public void delete(Long id) {
        Playlist playlist = get(id);
        playlistRepository.delete(playlist);
        musicPlayer.unloadIfPlaying(id);
    }

    public Playlist addSong(Long playlistId, AddSongRequest request) {
        Playlist playlist = get(playlistId);
        Song song = songService.get(request.songId());
        if (request.position() == null) {
            playlist.addSong(song);
        } else {
            if (request.position() > playlist.getSongs().size()) {
                throw new BadRequestException("Position out of range: " + request.position());
            }
            playlist.addSong(song, request.position());
        }
        return playlistRepository.saveAndFlush(playlist);
    }

    public record CreatedWithRecommendations(Playlist playlist, List<Song> recommendations) {
    }

    /**
     * Multi-step write in a single transaction: create the playlist from seed songs,
     * compute recommendations and append them. Any failure rolls everything back.
     */
    @Transactional
    public CreatedWithRecommendations createWithRecommendations(PlaylistRequest request, int recommendationCount) {
        Playlist playlist = create(request);
        List<Song> recommendations = recommendationService.recommend(playlist.getSongs(), recommendationCount);
        recommendations.forEach(playlist::addSong);
        return new CreatedWithRecommendations(playlistRepository.saveAndFlush(playlist), recommendations);
    }

    public Playlist removeSong(Long playlistId, Long songId) {
        Playlist playlist = get(playlistId);
        if (!playlist.removeSong(songId)) {
            throw new ResourceNotFoundException("Song in playlist " + playlistId, songId);
        }
        return playlistRepository.saveAndFlush(playlist);
    }

    public Playlist moveSong(Long playlistId, MoveSongRequest request) {
        Playlist playlist = get(playlistId);
        int size = playlist.getSongs().size();
        if (request.from() >= size || request.to() >= size) {
            throw new BadRequestException("Positions must be between 0 and " + (size - 1));
        }
        playlist.moveSong(request.from(), request.to());
        return playlistRepository.saveAndFlush(playlist);
    }
}
