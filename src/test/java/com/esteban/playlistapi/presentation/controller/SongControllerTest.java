package com.esteban.playlistapi.presentation.controller;

import com.esteban.playlistapi.application.playlist.dto.PlaylistResult;
import com.esteban.playlistapi.application.song.dto.AddSongCommand;
import com.esteban.playlistapi.application.song.dto.RemoveSongCommand;
import com.esteban.playlistapi.application.song.dto.SearchSongsQuery;
import com.esteban.playlistapi.application.song.dto.SongSearchResult;
import com.esteban.playlistapi.application.song.usecase.AddSongToPlaylistUseCase;
import com.esteban.playlistapi.application.song.usecase.RemoveSongFromPlaylistUseCase;
import com.esteban.playlistapi.application.song.usecase.SearchSongsUseCase;
import com.esteban.playlistapi.presentation.dto.request.AddSongRequest;
import com.esteban.playlistapi.presentation.dto.request.SearchSongsRequest;
import com.esteban.playlistapi.presentation.dto.response.PlaylistResponse;
import com.esteban.playlistapi.presentation.dto.response.SongResponse;
import com.esteban.playlistapi.presentation.mapper.PlaylistPresentationMapper;
import com.esteban.playlistapi.presentation.mapper.SongPresentationMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SongController.class)
@AutoConfigureMockMvc(addFilters = false)
class SongControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AddSongToPlaylistUseCase addSongToPlaylistUseCase;

    @MockBean
    private RemoveSongFromPlaylistUseCase removeSongFromPlaylistUseCase;

    @MockBean
    private SearchSongsUseCase searchSongsUseCase;

    @MockBean
    private SongPresentationMapper songPresentationMapper;

    @MockBean
    private PlaylistPresentationMapper playlistPresentationMapper;

    private final UUID playlistId = UUID.randomUUID();
    private final UUID songId = UUID.randomUUID();
    private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private PlaylistResult mockPlaylistResult;
    private PlaylistResponse mockPlaylistResponse;
    private SongSearchResult mockSongSearchResult;
    private SongResponse mockSongResponse;

    @BeforeEach
    void setUp() {
        mockPlaylistResult = new PlaylistResult(
                playlistId,
                "Rock Clásico",
                userId,
                1,
                Collections.emptyList(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        mockPlaylistResponse = new PlaylistResponse(
                playlistId,
                "Rock Clásico",
                userId,
                1,
                Collections.emptyList(),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString()
        );

        mockSongSearchResult = new SongSearchResult(
                songId,
                "spotify-001",
                "Enter Sandman",
                "Metallica",
                "Metallica",
                331
        );

        mockSongResponse = new SongResponse(
                songId,
                "spotify-001",
                "Enter Sandman",
                "Metallica",
                "Metallica",
                331
        );
    }

    @Nested
    @DisplayName("UC-007: Agregar Canción a Playlist (POST /api/v1/playlists/{playlistId}/songs)")
    class AddSongTests {

        @Test
        @DisplayName("Debe retornar HTTP 200 OK con PlaylistResponse en la secuencia ordenada (InOrder)")
        void shouldReturnHttp200_whenAddSongIsSuccessful() throws Exception {
            // Arrange
            AddSongRequest request = new AddSongRequest("spotify-001");
            AddSongCommand command = new AddSongCommand(playlistId, "spotify-001", userId);

            when(songPresentationMapper.toAddSongCommand(eq(playlistId), any(AddSongRequest.class), eq(userId))).thenReturn(command);
            when(addSongToPlaylistUseCase.execute(any(AddSongCommand.class))).thenReturn(mockPlaylistResult);
            when(playlistPresentationMapper.toResponse(any(PlaylistResult.class))).thenReturn(mockPlaylistResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/playlists/{playlistId}/songs", playlistId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(playlistId.toString()))
                    .andExpect(jsonPath("$.songCount").value(1));

            InOrder inOrder = inOrder(songPresentationMapper, addSongToPlaylistUseCase, playlistPresentationMapper);
            inOrder.verify(songPresentationMapper).toAddSongCommand(eq(playlistId), any(AddSongRequest.class), eq(userId));
            inOrder.verify(addSongToPlaylistUseCase).execute(any(AddSongCommand.class));
            inOrder.verify(playlistPresentationMapper).toResponse(any(PlaylistResult.class));
            inOrder.verifyNoMoreInteractions();

            verifyNoMoreInteractions(songPresentationMapper, addSongToPlaylistUseCase, playlistPresentationMapper);
        }

        @Test
        @DisplayName("Debe retornar HTTP 400 Bad Request sin interactuar con los servicios si spotifyId es en blanco")
        void shouldReturnHttp400_whenAddSongRequestIsInvalid() throws Exception {
            // Arrange
            AddSongRequest invalidRequest = new AddSongRequest("");

            // Act & Assert
            mockMvc.perform(post("/api/v1/playlists/{playlistId}/songs", playlistId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(songPresentationMapper, addSongToPlaylistUseCase, playlistPresentationMapper);
        }
    }

    @Nested
    @DisplayName("UC-008: Eliminar Canción de Playlist (DELETE /api/v1/playlists/{playlistId}/songs/{songId})")
    class RemoveSongTests {

        @Test
        @DisplayName("Debe retornar HTTP 204 No Content en la secuencia ordenada (InOrder)")
        void shouldReturnHttp204_whenRemoveSongIsSuccessful() throws Exception {
            // Arrange
            RemoveSongCommand command = new RemoveSongCommand(playlistId, songId, userId);

            when(songPresentationMapper.toRemoveSongCommand(eq(playlistId), eq(songId), eq(userId))).thenReturn(command);
            doNothing().when(removeSongFromPlaylistUseCase).execute(any(RemoveSongCommand.class));

            // Act & Assert
            mockMvc.perform(delete("/api/v1/playlists/{playlistId}/songs/{songId}", playlistId, songId))
                    .andExpect(status().isNoContent());

            InOrder inOrder = inOrder(songPresentationMapper, removeSongFromPlaylistUseCase);
            inOrder.verify(songPresentationMapper).toRemoveSongCommand(eq(playlistId), eq(songId), eq(userId));
            inOrder.verify(removeSongFromPlaylistUseCase).execute(any(RemoveSongCommand.class));
            inOrder.verifyNoMoreInteractions();

            verifyNoMoreInteractions(songPresentationMapper, removeSongFromPlaylistUseCase);
        }
    }

    @Nested
    @DisplayName("UC-009: Buscar Canciones (GET /api/v1/songs/search)")
    class SearchSongsTests {

        @Test
        @DisplayName("Debe retornar HTTP 200 OK con lista de SongResponse")
        void shouldReturnHttp200_whenSearchSongsIsSuccessful() throws Exception {
            // Arrange
            SearchSongsQuery query = new SearchSongsQuery("Metallica", 10);

            when(songPresentationMapper.toSearchQuery(any(SearchSongsRequest.class))).thenReturn(query);
            when(searchSongsUseCase.execute(any(SearchSongsQuery.class))).thenReturn(List.of(mockSongSearchResult));
            when(songPresentationMapper.toResponseList(anyList())).thenReturn(List.of(mockSongResponse));

            // Act & Assert
            mockMvc.perform(get("/api/v1/songs/search")
                            .param("query", "Metallica")
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].songId").value(songId.toString()))
                    .andExpect(jsonPath("$[0].artist").value("Metallica"))
                    .andExpect(jsonPath("$[0].title").value("Enter Sandman"));

            InOrder inOrder = inOrder(songPresentationMapper, searchSongsUseCase);
            inOrder.verify(songPresentationMapper).toSearchQuery(any(SearchSongsRequest.class));
            inOrder.verify(searchSongsUseCase).execute(any(SearchSongsQuery.class));
            inOrder.verify(songPresentationMapper).toResponseList(anyList());
            inOrder.verifyNoMoreInteractions();

            verifyNoMoreInteractions(songPresentationMapper, searchSongsUseCase);
        }
    }

    @Nested
    @DisplayName("Pruebas de Inyección por Constructor (Fail-Fast)")
    class ConstructorTests {

        @Test
        @DisplayName("Debe lanzar NullPointerException si alguna dependencia es nula")
        void shouldThrowNullPointerException_whenDependenciesAreNull() {
            assertThrows(NullPointerException.class, () -> new SongController(null, removeSongFromPlaylistUseCase, searchSongsUseCase, songPresentationMapper, playlistPresentationMapper));
            assertThrows(NullPointerException.class, () -> new SongController(addSongToPlaylistUseCase, removeSongFromPlaylistUseCase, searchSongsUseCase, null, playlistPresentationMapper));
        }
    }
}
