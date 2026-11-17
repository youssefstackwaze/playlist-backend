package com.example.music.export;

import com.example.music.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ExporterRegistry {

    private final Map<String, PlaylistExporter> exporters;

    public ExporterRegistry(List<PlaylistExporter> exporters) {
        this.exporters = exporters.stream()
                .collect(Collectors.toUnmodifiableMap(e -> e.format().toLowerCase(Locale.ROOT), Function.identity()));
    }

    public PlaylistExporter get(String format) {
        String key = format == null ? "json" : format.toLowerCase(Locale.ROOT);
        PlaylistExporter exporter = exporters.get(key);
        if (exporter == null) {
            throw new BadRequestException("Unknown export format '" + format + "'. Available: " + exporters.keySet());
        }
        return exporter;
    }

    public Set<String> formats() {
        return exporters.keySet();
    }
}
