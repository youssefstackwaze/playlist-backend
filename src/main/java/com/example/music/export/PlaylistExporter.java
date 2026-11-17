package com.example.music.export;

import com.example.music.model.Playlist;

/** One export format. New formats are added by declaring a new {@code @Component}. */
public interface PlaylistExporter {

    /** Format key used in the API, e.g. "m3u". */
    String format();

    String contentType();

    String fileExtension();

    String export(Playlist playlist);
}
