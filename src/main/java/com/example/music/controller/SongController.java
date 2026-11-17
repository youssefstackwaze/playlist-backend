package com.example.music.controller;

import com.example.music.dto.Dtos.SongResponse;
import com.example.music.model.Genre;
import com.example.music.service.SongService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @GetMapping
    public List<SongResponse> search(@RequestParam(required = false) Genre genre,
                                     @RequestParam(required = false) String artist) {
        return SongResponse.from(songService.search(genre, artist));
    }

    @GetMapping("/{id}")
    public SongResponse get(@PathVariable Long id) {
        return SongResponse.from(songService.get(id));
    }
}
