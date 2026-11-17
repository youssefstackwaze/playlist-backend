package com.example.music.recommendation;

import com.example.music.model.Genre;
import com.example.music.model.Song;
import com.example.music.repository.SongRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    SongRepository songRepository;

    private static Song song(long id, String artist, Genre genre) {
        Song s = new Song("t" + id, artist, "a", genre, 100, "/f" + id);
        s.setId(id);
        return s;
    }

    private RecommendationService service() {
        return new RecommendationService(songRepository,
                List.of(new GenreCompatibilityRecommendation(), new ArtistSimilarityRecommendation()));
    }

    @Test
    void ranksSameGenreThenCompatibleGenreAndExcludesExistingSongs() {
        Song jazz1 = song(1, "Miles", Genre.JAZZ);
        Song jazz2 = song(2, "Coltrane", Genre.JAZZ);
        Song blues = song(3, "BB King", Genre.BLUES);       // compatible with jazz
        Song metal = song(4, "Metallica", Genre.METAL);     // incompatible
        Song milesAgain = song(5, "Miles", Genre.JAZZ);     // same genre + same artist
        when(songRepository.findAll()).thenReturn(List.of(jazz1, jazz2, blues, metal, milesAgain));

        List<Song> result = service().recommend(List.of(jazz1), 10);

        assertThat(result).containsExactly(milesAgain, jazz2, blues);
    }

    @Test
    void emptyPlaylistOrZeroLimitGivesNothing() {
        assertThat(service().recommend(List.of(), 5)).isEmpty();
        assertThat(service().recommend(List.of(song(1, "A", Genre.ROCK)), 0)).isEmpty();
        verifyNoInteractions(songRepository);
    }
}
