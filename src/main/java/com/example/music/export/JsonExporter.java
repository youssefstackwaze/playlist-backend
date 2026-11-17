package com.example.music.export;

import com.example.music.dto.Dtos.PlaylistResponse;
import com.example.music.model.Playlist;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

@Component
public class JsonExporter implements PlaylistExporter {

    private final ObjectMapper objectMapper;

    public JsonExporter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public String format() {
        return "json";
    }

    @Override
    public String contentType() {
        return "application/json";
    }

    @Override
    public String fileExtension() {
        return "json";
    }

    @Override
    public String export(Playlist playlist) {
        try {
            return objectMapper.writeValueAsString(PlaylistResponse.from(playlist));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON export failed", e);
        }
    }
}
