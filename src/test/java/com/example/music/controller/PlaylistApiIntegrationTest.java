package com.example.music.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.main.banner-mode=off",
        "logging.level.root=WARN",
        "logging.level.org.springdoc=ERROR"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PlaylistApiIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    private long createPlaylist(String body) throws Exception {
        String json = mvc.perform(post("/api/playlists").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(json).get("id").asLong();
    }

    @Test
    void crudAndSongManagement() throws Exception {
        long id = createPlaylist("""
                {"name":"Rock classics","description":"best of","owner":"alice","songIds":[1,4,6]}""");

        mvc.perform(get("/api/playlists/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.songCount").value(3))
                .andExpect(jsonPath("$.songs[0].title").value("Bohemian Rhapsody"));

        // add at position 0
        mvc.perform(post("/api/playlists/{id}/songs", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"songId\":30,\"position\":0}"))
                .andExpect(jsonPath("$.songs[0].id").value(30))
                .andExpect(jsonPath("$.songCount").value(4));

        // move first to last
        mvc.perform(put("/api/playlists/{id}/songs/order", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"from\":0,\"to\":3}"))
                .andExpect(jsonPath("$.songs[3].id").value(30));

        // remove
        mvc.perform(delete("/api/playlists/{id}/songs/{songId}", id, 4))
                .andExpect(jsonPath("$.songCount").value(3))
                .andExpect(jsonPath("$.songs[*].id", not(hasItem(4))));

        // update metadata
        mvc.perform(put("/api/playlists/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Renamed\",\"owner\":\"bob\"}"))
                .andExpect(jsonPath("$.name").value("Renamed"))
                .andExpect(jsonPath("$.songCount").value(3));

        mvc.perform(delete("/api/playlists/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/playlists/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void validationAndErrors() throws Exception {
        mvc.perform(post("/api/playlists").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/playlists").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"songIds\":[999]}"))
                .andExpect(status().isNotFound());
        long id = createPlaylist("{\"name\":\"x\",\"songIds\":[1]}");
        mvc.perform(get("/api/playlists/{id}/shuffle?strategy=nope", id)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/playlists/{id}/export?format=nope", id)).andExpect(status().isBadRequest());
    }

    @Test
    void shuffleRecommendExportAnalyze() throws Exception {
        long id = createPlaylist("{\"name\":\"Mix\",\"songIds\":[1,2,3,4,13,14]}");

        mvc.perform(get("/api/playlists/{id}/shuffle?strategy=smart", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)));

        mvc.perform(get("/api/playlists/{id}/recommendations?limit=3", id))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].id", not(hasItems(1, 2, 3, 4, 13, 14))));

        mvc.perform(get("/api/playlists/{id}/export?format=m3u", id))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("Mix.m3u")))
                .andExpect(content().string(startsWith("#EXTM3U")));

        mvc.perform(get("/api/playlists/{id}/analysis", id))
                .andExpect(jsonPath("$.genres.dominantGenre").value("ROCK"))
                .andExpect(jsonPath("$.artists.uniqueArtists").value(4))
                .andExpect(jsonPath("$.duration.totalSeconds").isNumber());
    }

    @Test
    void smartPlaylistCreatesRecommendsAndPlays() throws Exception {
        String json = mvc.perform(post("/api/playlists/smart").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Smart jazz","seedSongIds":[13,14],"recommendationCount":4,
                                 "shuffleStrategy":"smart"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.addedRecommendations", hasSize(4)))
                .andExpect(jsonPath("$.playlist.songCount").value(6))
                .andExpect(jsonPath("$.player.status").value("PLAYING"))
                .andExpect(jsonPath("$.player.shuffleStrategy").value("smart"))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(json);
        long playlistId = node.at("/playlist/id").asLong();

        mvc.perform(get("/api/player"))
                .andExpect(jsonPath("$.playlistId").value(playlistId))
                .andExpect(jsonPath("$.queueSize").value(6));
        mvc.perform(post("/api/player/next")).andExpect(jsonPath("$.currentIndex").value(1));
        mvc.perform(post("/api/player/pause")).andExpect(jsonPath("$.status").value("PAUSED"));

        // deleting the playing playlist unloads the player
        mvc.perform(delete("/api/playlists/{id}", playlistId)).andExpect(status().isNoContent());
        mvc.perform(get("/api/player")).andExpect(jsonPath("$.queueSize").value(0));
    }

    @Test
    void corsAllowsConfiguredFrontendOriginOnly() throws Exception {
        mvc.perform(options("/api/songs")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
        mvc.perform(get("/api/songs").header("Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
        mvc.perform(options("/api/songs")
                        .header("Origin", "https://evil.example.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void swaggerAndActuatorAreExposed() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Music Playlist Management API"))
                .andExpect(jsonPath("$.paths['/api/playlists/smart']").exists());
        mvc.perform(get("/actuator/health")).andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void smartPlaylistWithUnknownSeedSongPersistsNothing() throws Exception {
        mvc.perform(post("/api/playlists/smart").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"bad\",\"seedSongIds\":[1,999]}"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/playlists")).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/player")).andExpect(jsonPath("$.queueSize").value(0));
    }

    @Test
    void smartPlaylistWithUnknownStrategyPersistsNothing() throws Exception {
        mvc.perform(post("/api/playlists/smart").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"bad\",\"seedSongIds\":[1],\"shuffleStrategy\":\"nope\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/playlists")).andExpect(jsonPath("$", hasSize(0)));
    }
}
