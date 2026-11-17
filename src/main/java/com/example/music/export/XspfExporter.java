package com.example.music.export;

import com.example.music.model.Playlist;
import com.example.music.model.Song;
import org.springframework.stereotype.Component;

/** XSPF (XML Shareable Playlist Format). */
@Component
public class XspfExporter implements PlaylistExporter {

    @Override
    public String format() {
        return "xspf";
    }

    @Override
    public String contentType() {
        return "application/xspf+xml";
    }

    @Override
    public String fileExtension() {
        return "xspf";
    }

    @Override
    public String export(Playlist playlist) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<playlist version=\"1\" xmlns=\"http://xspf.org/ns/0/\">\n")
                .append("  <title>").append(xml(playlist.getName())).append("</title>\n");
        if (playlist.getOwner() != null) {
            sb.append("  <creator>").append(xml(playlist.getOwner())).append("</creator>\n");
        }
        sb.append("  <trackList>\n");
        for (Song s : playlist.getSongs()) {
            sb.append("    <track>\n")
                    .append("      <location>").append(xml(s.getFilePath())).append("</location>\n")
                    .append("      <title>").append(xml(s.getTitle())).append("</title>\n")
                    .append("      <creator>").append(xml(s.getArtist())).append("</creator>\n")
                    .append("      <album>").append(xml(s.getAlbum())).append("</album>\n")
                    .append("      <duration>").append(s.getDurationSeconds() * 1000L).append("</duration>\n")
                    .append("    </track>\n");
        }
        sb.append("  </trackList>\n</playlist>\n");
        return sb.toString();
    }

    private static String xml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
