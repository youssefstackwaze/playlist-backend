package com.example.music.export;

import com.example.music.model.Genre;
import com.example.music.model.Playlist;
import com.example.music.model.Song;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExportersTest {

    private static Playlist playlist() {
        Playlist p = new Playlist("Road Trip", "desc", "me");
        Song s1 = new Song("Bohemian Rhapsody", "Queen", "A Night at the Opera", Genre.ROCK, 355, "/q/br.mp3");
        s1.setId(1L);
        Song s2 = new Song("Take, Five", "Dave \"B\"", "Time Out", Genre.JAZZ, 324, "/b/tf.mp3");
        s2.setId(2L);
        p.addSong(s1);
        p.addSong(s2);
        return p;
    }

    @Test
    void m3u() {
        assertThat(new M3uExporter().export(playlist())).isEqualTo("""
                #EXTM3U
                #PLAYLIST:Road Trip
                #EXTINF:355,Queen - Bohemian Rhapsody
                /q/br.mp3
                #EXTINF:324,Dave "B" - Take, Five
                /b/tf.mp3
                """);
    }

    @Test
    void csvEscapesSpecialCharacters() {
        String csv = new CsvExporter().export(playlist());
        assertThat(csv).contains("2,\"Take, Five\",\"Dave \"\"B\"\"\",Time Out,JAZZ,324,/b/tf.mp3");
    }

    @Test
    void xspfEscapesXml() {
        String xml = new XspfExporter().export(playlist());
        assertThat(xml).contains("<creator>Dave &quot;B&quot;</creator>").contains("<duration>355000</duration>");
    }

    @Test
    void pls() {
        String pls = new PlsExporter().export(playlist());
        assertThat(pls).startsWith("[playlist]").contains("File1=/q/br.mp3").contains("NumberOfEntries=2");
    }
}
