package com.example.music.export;

import com.example.music.model.Playlist;
import com.example.music.model.Song;
import org.springframework.stereotype.Component;

@Component
public class CsvExporter implements PlaylistExporter {

    @Override
    public String format() {
        return "csv";
    }

    @Override
    public String contentType() {
        return "text/csv";
    }

    @Override
    public String fileExtension() {
        return "csv";
    }

    @Override
    public String export(Playlist playlist) {
        StringBuilder sb = new StringBuilder("position,title,artist,album,genre,durationSeconds,filePath\n");
        int pos = 1;
        for (Song s : playlist.getSongs()) {
            sb.append(pos++).append(',')
                    .append(escape(s.getTitle())).append(',')
                    .append(escape(s.getArtist())).append(',')
                    .append(escape(s.getAlbum())).append(',')
                    .append(s.getGenre()).append(',')
                    .append(s.getDurationSeconds()).append(',')
                    .append(escape(s.getFilePath())).append('\n');
        }
        return sb.toString();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }
}
