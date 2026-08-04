package com.esteban.playlistapi.application.song.usecase;

import com.esteban.playlistapi.application.song.dto.SearchSongsQuery;
import com.esteban.playlistapi.application.song.dto.SongSearchResult;
import com.esteban.playlistapi.domain.model.Song;
import com.esteban.playlistapi.domain.repository.MusicCatalogPort;
import com.esteban.playlistapi.shared.exception.InvalidQueryException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchSongsUseCaseTest {

    @Mock
    private MusicCatalogPort musicCatalogPort;

    private SearchSongsUseCase useCase;

    private Song mockSong1;
    private Song mockSong2;

    @BeforeEach
    void setUp() {
        useCase = new SearchSongsUseCase(musicCatalogPort);
        mockSong1 = Song.create("spotify-001", "Bohemian Rhapsody", "Queen", "A Night at the Opera", 354);
        mockSong2 = Song.create("spotify-002", "Don't Stop Me Now", "Queen", "Jazz", 209);
    }

    @Nested
    @DisplayName("Escenarios Exitosos")
    class SuccessfulScenarios {

        @Test
        @DisplayName("Debe retornar la lista de resultados mapeados correctamente cuando se encuentran canciones")
        void shouldReturnListOfSongSearchResults_whenQueryIsValidAndSongsFound() {
            // Arrange
            String searchTerm = "Queen";
            int limit = 10;
            SearchSongsQuery query = new SearchSongsQuery(searchTerm, limit);

            when(musicCatalogPort.searchSongs(searchTerm, limit)).thenReturn(List.of(mockSong1, mockSong2));

            // Act
            List<SongSearchResult> results = useCase.execute(query);

            // Assert
            assertNotNull(results);
            assertEquals(2, results.size());

            SongSearchResult res1 = results.get(0);
            assertEquals(mockSong1.getId(), res1.songId());
            assertEquals("spotify-001", res1.spotifyId());
            assertEquals("Bohemian Rhapsody", res1.title());
            assertEquals("Queen", res1.artist());
            assertEquals("A Night at the Opera", res1.album());
            assertEquals(354, res1.durationSeconds());

            SongSearchResult res2 = results.get(1);
            assertEquals(mockSong2.getId(), res2.songId());
            assertEquals("spotify-002", res2.spotifyId());
            assertEquals("Don't Stop Me Now", res2.title());
            assertEquals("Queen", res2.artist());

            verify(musicCatalogPort, times(1)).searchSongs(searchTerm, limit);
            verifyNoMoreInteractions(musicCatalogPort);
        }

        @Test
        @DisplayName("Debe retornar una lista vacía cuando no se encuentran canciones en el catálogo")
        void shouldReturnEmptyList_whenNoSongsFound() {
            // Arrange
            String searchTerm = "ArtistaInexistente";
            int limit = 5;
            SearchSongsQuery query = new SearchSongsQuery(searchTerm, limit);

            when(musicCatalogPort.searchSongs(searchTerm, limit)).thenReturn(Collections.emptyList());

            // Act
            List<SongSearchResult> results = useCase.execute(query);

            // Assert
            assertNotNull(results);
            assertTrue(results.isEmpty());

            verify(musicCatalogPort, times(1)).searchSongs(searchTerm, limit);
            verifyNoMoreInteractions(musicCatalogPort);
        }

        @Test
        @DisplayName("Debe manejar respuestas nulas del puerto y retornar una lista vacía inmutable")
        void shouldHandleNullFromPort_andReturnEmptyList() {
            // Arrange
            String searchTerm = "Queen";
            int limit = 5;
            SearchSongsQuery query = new SearchSongsQuery(searchTerm, limit);

            when(musicCatalogPort.searchSongs(searchTerm, limit)).thenReturn(null);

            // Act
            List<SongSearchResult> results = useCase.execute(query);

            // Assert
            assertNotNull(results);
            assertTrue(results.isEmpty());
            assertThrows(UnsupportedOperationException.class, () -> results.add(null));

            verify(musicCatalogPort, times(1)).searchSongs(searchTerm, limit);
            verifyNoMoreInteractions(musicCatalogPort);
        }
    }

    @Nested
    @DisplayName("Escenarios de Error y Excepciones")
    class FailureScenarios {

        @Test
        @DisplayName("Debe lanzar InvalidQueryException cuando la consulta enviada a execute es nula")
        void shouldThrowInvalidQueryException_whenQueryObjectIsNull() {
            // Act & Assert
            InvalidQueryException exception = assertThrows(
                    InvalidQueryException.class,
                    () -> useCase.execute(null)
            );

            assertEquals("La consulta de búsqueda no puede ser nula.", exception.getMessage());
            verifyNoInteractions(musicCatalogPort);
        }

        @Test
        @DisplayName("Debe lanzar InvalidQueryException al construir un query con término nulo o en blanco")
        void shouldThrowInvalidQueryException_whenQueryTextIsEmptyOrNull() {
            // Act & Assert (null)
            InvalidQueryException exNull = assertThrows(
                    InvalidQueryException.class,
                    () -> new SearchSongsQuery(null, 10)
            );
            assertEquals("El término de búsqueda no puede estar vacío.", exNull.getMessage());

            // Act & Assert (blank)
            InvalidQueryException exBlank = assertThrows(
                    InvalidQueryException.class,
                    () -> new SearchSongsQuery("   ", 10)
            );
            assertEquals("El término de búsqueda no puede estar vacío.", exBlank.getMessage());

            verifyNoInteractions(musicCatalogPort);
        }

        @Test
        @DisplayName("Debe lanzar InvalidQueryException al construir un query con límite igual o menor a cero")
        void shouldThrowInvalidQueryException_whenLimitIsZeroOrNegative() {
            // Act & Assert (limit = 0)
            InvalidQueryException exZero = assertThrows(
                    InvalidQueryException.class,
                    () -> new SearchSongsQuery("Queen", 0)
            );
            assertEquals("El límite debe ser mayor que cero.", exZero.getMessage());

            // Act & Assert (limit < 0)
            InvalidQueryException exNeg = assertThrows(
                    InvalidQueryException.class,
                    () -> new SearchSongsQuery("Queen", -5)
            );
            assertEquals("El límite debe ser mayor que cero.", exNeg.getMessage());

            verifyNoInteractions(musicCatalogPort);
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException si la dependencia del constructor es nula")
        void shouldThrowNullPointerException_whenConstructorDependencyIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new SearchSongsUseCase(null));
        }
    }
}
