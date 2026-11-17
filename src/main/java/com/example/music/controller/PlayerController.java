package com.example.music.controller;

import com.example.music.dto.Dtos.PlayerStateResponse;
import com.example.music.dto.Dtos.VolumeRequest;
import com.example.music.facade.MusicFacade;
import com.example.music.player.MusicPlayer;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/player")
public class PlayerController {

    private final MusicPlayer player;
    private final MusicFacade musicFacade;

    public PlayerController(MusicPlayer player, MusicFacade musicFacade) {
        this.player = player;
        this.musicFacade = musicFacade;
    }

    @GetMapping
    public PlayerStateResponse state() {
        return player.state();
    }

    @PostMapping("/load/{playlistId}")
    public PlayerStateResponse load(@PathVariable Long playlistId,
                                    @RequestParam(defaultValue = "random") String strategy,
                                    @RequestParam(defaultValue = "false") boolean autoPlay) {
        return musicFacade.loadIntoPlayer(playlistId, strategy, autoPlay);
    }

    @PostMapping("/play")
    public PlayerStateResponse play() {
        return player.play();
    }

    @PostMapping("/pause")
    public PlayerStateResponse pause() {
        return player.pause();
    }

    @PostMapping("/stop")
    public PlayerStateResponse stop() {
        return player.stop();
    }

    @PostMapping("/next")
    public PlayerStateResponse next() {
        return player.next();
    }

    @PostMapping("/previous")
    public PlayerStateResponse previous() {
        return player.previous();
    }

    @PutMapping("/repeat")
    public PlayerStateResponse repeat(@RequestParam boolean enabled) {
        return player.setRepeat(enabled);
    }

    @PutMapping("/volume")
    public PlayerStateResponse volume(@Valid @RequestBody VolumeRequest request) {
        return player.setVolume(request.volume());
    }
}
