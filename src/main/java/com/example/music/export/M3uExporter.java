package com.example.music.export;

import com.example.music.model.Playlist;
import com.example.music.model.Song;
import org.springframework.stereotype.Component;

/** Extended M3U format. */
@Component
public class M3uExporter implements PlaylistExporter {

    @Override
    public String format() {
        return "m3u";
    }

    @Override
    public String contentType() {
        return "audio/x-mpegurl";
    }

    @Override
    public String fileExtension() {
        return "m3u";
    }

    @Override
    public String export(Playlist playlist) {
        StringBuilder sb = new StringBuilder("#EXTM3U\n");
        sb.append("#PLAYLIST:").append(playlist.getName()).append('\n');
        for (Song s : playlist.getSongs()) {
            sb.append("#EXTINF:").append(s.getDurationSeconds()).append(',')
                    .append(s.getArtist()).append(" - ").append(s.getTitle()).append('\n')
                    .append(s.getFilePath()).append('\n');
        }
        return sb.toString();
    }
}
