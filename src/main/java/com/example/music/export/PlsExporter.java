package com.example.music.export;

import com.example.music.model.Playlist;
import com.example.music.model.Song;
import org.springframework.stereotype.Component;

import java.util.List;

/** PLS (Winamp/Shoutcast) format. */
@Component
public class PlsExporter implements PlaylistExporter {

    @Override
    public String format() {
        return "pls";
    }

    @Override
    public String contentType() {
        return "audio/x-scpls";
    }

    @Override
    public String fileExtension() {
        return "pls";
    }

    @Override
    public String export(Playlist playlist) {
        List<Song> songs = playlist.getSongs();
        StringBuilder sb = new StringBuilder("[playlist]\n");
        for (int i = 0; i < songs.size(); i++) {
            Song s = songs.get(i);
            int n = i + 1;
            sb.append("File").append(n).append('=').append(s.getFilePath()).append('\n')
                    .append("Title").append(n).append('=').append(s.getArtist()).append(" - ").append(s.getTitle()).append('\n')
                    .append("Length").append(n).append('=').append(s.getDurationSeconds()).append('\n');
        }
        sb.append("NumberOfEntries=").append(songs.size()).append('\n');
        sb.append("Version=2\n");
        return sb.toString();
    }
}
