package com.example.music.controller;

import com.example.music.analysis.AnalysisService;
import com.example.music.dto.Dtos.*;
import com.example.music.export.ExporterRegistry;
import com.example.music.export.PlaylistExporter;
import com.example.music.facade.MusicFacade;
import com.example.music.model.Playlist;
import com.example.music.recommendation.RecommendationService;
import com.example.music.service.PlaylistService;
import com.example.music.shuffle.ShuffleStrategyRegistry;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;
    private final ShuffleStrategyRegistry shuffleRegistry;
    private final RecommendationService recommendationService;
    private final ExporterRegistry exporterRegistry;
    private final AnalysisService analysisService;
    private final MusicFacade musicFacade;

    public PlaylistController(PlaylistService playlistService, ShuffleStrategyRegistry shuffleRegistry,
                              RecommendationService recommendationService, ExporterRegistry exporterRegistry,
                              AnalysisService analysisService, MusicFacade musicFacade) {
        this.playlistService = playlistService;
        this.shuffleRegistry = shuffleRegistry;
        this.recommendationService = recommendationService;
        this.exporterRegistry = exporterRegistry;
        this.analysisService = analysisService;
        this.musicFacade = musicFacade;
    }

    // ---------- CRUD ----------

    @GetMapping
    public List<PlaylistResponse> list() {
        return playlistService.findAll().stream().map(PlaylistResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PlaylistResponse get(@PathVariable Long id) {
        return PlaylistResponse.from(playlistService.get(id));
    }

    @PostMapping
    public ResponseEntity<PlaylistResponse> create(@Valid @RequestBody PlaylistRequest request) {
        Playlist created = playlistService.create(request);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}").buildAndExpand(created.getId()).toUri())
                .body(PlaylistResponse.from(created));
    }

    @PutMapping("/{id}")
    public PlaylistResponse update(@PathVariable Long id, @Valid @RequestBody PlaylistRequest request) {
        return PlaylistResponse.from(playlistService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        playlistService.delete(id);
    }

    // ---------- Songs management ----------

    @PostMapping("/{id}/songs")
    public PlaylistResponse addSong(@PathVariable Long id, @Valid @RequestBody AddSongRequest request) {
        return PlaylistResponse.from(playlistService.addSong(id, request));
    }

    @DeleteMapping("/{id}/songs/{songId}")
    public PlaylistResponse removeSong(@PathVariable Long id, @PathVariable Long songId) {
        return PlaylistResponse.from(playlistService.removeSong(id, songId));
    }

    @PutMapping("/{id}/songs/order")
    public PlaylistResponse moveSong(@PathVariable Long id, @Valid @RequestBody MoveSongRequest request) {
        return PlaylistResponse.from(playlistService.moveSong(id, request));
    }

    // ---------- Advanced features ----------

    @GetMapping("/shuffle-strategies")
    public List<Map<String, String>> shuffleStrategies() {
        return shuffleRegistry.all().stream()
                .map(s -> Map.of("name", s.name(), "description", s.description()))
                .toList();
    }

    @GetMapping("/{id}/shuffle")
    public List<SongResponse> shuffle(@PathVariable Long id,
                                      @RequestParam(defaultValue = "random") String strategy) {
        Playlist playlist = playlistService.get(id);
        return SongResponse.from(shuffleRegistry.get(strategy).shuffle(playlist.getSongs()));
    }

    @GetMapping("/{id}/recommendations")
    public List<SongResponse> recommendations(@PathVariable Long id,
                                              @RequestParam(defaultValue = "5") int limit) {
        Playlist playlist = playlistService.get(id);
        return SongResponse.from(recommendationService.recommend(playlist.getSongs(), limit));
    }

    @GetMapping("/export-formats")
    public List<String> exportFormats() {
        return exporterRegistry.formats().stream().sorted().toList();
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<String> export(@PathVariable Long id,
                                         @RequestParam(defaultValue = "json") String format) {
        PlaylistExporter exporter = exporterRegistry.get(format);
        Playlist playlist = playlistService.get(id);
        String filename = playlist.getName().replaceAll("[^A-Za-z0-9._-]", "_") + "." + exporter.fileExtension();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(exporter.contentType() + ";charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8).build().toString())
                .body(exporter.export(playlist));
    }

    @GetMapping("/{id}/analysis")
    public Map<String, Object> analysis(@PathVariable Long id) {
        return analysisService.analyze(playlistService.get(id));
    }

    /** Multi-step operation: create + recommend + shuffle + play. */
    @PostMapping("/smart")
    @ResponseStatus(HttpStatus.CREATED)
    public SmartPlaylistResponse createSmart(@Valid @RequestBody SmartPlaylistRequest request) {
        return musicFacade.createSmartPlaylist(request);
    }
}
