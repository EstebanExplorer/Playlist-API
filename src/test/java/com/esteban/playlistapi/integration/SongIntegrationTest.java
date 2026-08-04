package com.esteban.playlistapi.integration;

import com.esteban.playlistapi.domain.model.Song;
import com.esteban.playlistapi.domain.repository.AiRecommendationPort;
import com.esteban.playlistapi.domain.repository.MusicCatalogPort;
import com.esteban.playlistapi.domain.repository.UserRepository;
import com.esteban.playlistapi.infrastructure.persistence.repository.SpringDataJpaPlaylistRepository;
import com.esteban.playlistapi.presentation.dto.request.AddSongRequest;
import com.esteban.playlistapi.presentation.dto.request.CreatePlaylistRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SongIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringDataJpaPlaylistRepository jpaPlaylistRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MusicCatalogPort musicCatalogPort;

    @MockBean
    private AiRecommendationPort aiRecommendationPort;

    private Song testSong;

    @BeforeEach
    void setUp() {
        testSong = Song.create("spotify-001", "Stairway to Heaven", "Led Zeppelin", "Led Zeppelin IV", 482);
        com.esteban.playlistapi.domain.model.User mockUser = new com.esteban.playlistapi.domain.model.User(
                UUID.fromString("11111111-1111-1111-1111-111111111111"), "defaultuser", "default@example.com", "password");
        when(userRepository.findById(any())).thenReturn(Optional.of(mockUser));
    }

    @Nested
    @DisplayName("UC-007: Integración Agregar Canción (POST /api/v1/playlists/{playlistId}/songs)")
    class AddSongIntegrationTests {

        @Test
        @DisplayName("shouldAddSongToPlaylistAndPersistInDatabase_whenSongExistsInCatalog")
        void shouldAddSongToPlaylistAndPersistInDatabase_whenSongExistsInCatalog() throws Exception {
            // Arrange: Crear una playlist
            MvcResult createResult = mockMvc.perform(post("/api/v1/playlists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreatePlaylistRequest("Rock Legend"))))
                    .andExpect(status().isCreated())
                    .andReturn();

            String playlistIdStr = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();
            UUID playlistId = UUID.fromString(playlistIdStr);

            when(musicCatalogPort.getSongBySpotifyId("spotify-001")).thenReturn(Optional.of(testSong));

            // Act: Agregar canción a la playlist
            AddSongRequest addSongRequest = new AddSongRequest("spotify-001");
            mockMvc.perform(post("/api/v1/playlists/{playlistId}/songs", playlistId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addSongRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(playlistIdStr))
                    .andExpect(jsonPath("$.songCount").value(1))
                    .andExpect(jsonPath("$.songs[0].title").value("Stairway to Heaven"));

            // Assert: Verificar persistencia de la canción en la tabla relacional H2
            var jpaEntity = jpaPlaylistRepository.findById(playlistId).orElseThrow();
            assertEquals(1, jpaEntity.getSongs().size());
            assertEquals("Stairway to Heaven", jpaEntity.getSongs().get(0).getTitle());
        }

        @Test
        @DisplayName("shouldReturnHttp404NotFound_whenSongDoesNotExistInExternalCatalog")
        void shouldReturnHttp404NotFound_whenSongDoesNotExistInExternalCatalog() throws Exception {
            // Arrange: Crear una playlist
            MvcResult createResult = mockMvc.perform(post("/api/v1/playlists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreatePlaylistRequest("Prueba Catalog"))))
                    .andExpect(status().isCreated())
                    .andReturn();

            String playlistIdStr = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

            when(musicCatalogPort.getSongBySpotifyId("non-existent-spotify-id")).thenReturn(Optional.empty());

            // Act & Assert
            AddSongRequest addSongRequest = new AddSongRequest("non-existent-spotify-id");
            mockMvc.perform(post("/api/v1/playlists/{playlistId}/songs", playlistIdStr)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addSongRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("UC-008: Integración Eliminar Canción (DELETE /api/v1/playlists/{playlistId}/songs/{songId})")
    class RemoveSongIntegrationTests {

        @Test
        @DisplayName("shouldRemoveSongFromPlaylistInDatabase_whenSongIsPresent")
        void shouldRemoveSongFromPlaylistInDatabase_whenSongIsPresent() throws Exception {
            // Arrange: Crear playlist y agregar canción
            MvcResult createResult = mockMvc.perform(post("/api/v1/playlists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreatePlaylistRequest("Metal Favorites"))))
                    .andExpect(status().isCreated())
                    .andReturn();

            String playlistIdStr = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();
            UUID playlistId = UUID.fromString(playlistIdStr);

            when(musicCatalogPort.getSongBySpotifyId("spotify-001")).thenReturn(Optional.of(testSong));

            MvcResult addResult = mockMvc.perform(post("/api/v1/playlists/{playlistId}/songs", playlistId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AddSongRequest("spotify-001"))))
                    .andExpect(status().isOk())
                    .andReturn();

            String addedSongIdStr = objectMapper.readTree(addResult.getResponse().getContentAsString())
                    .get("songs").get(0).get("songId").asText();

            // Act: Eliminar la canción
            mockMvc.perform(delete("/api/v1/playlists/{playlistId}/songs/{songId}", playlistId, addedSongIdStr))
                    .andExpect(status().isNoContent());

            // Assert: Verificar remoción en la base de datos H2
            var jpaEntity = jpaPlaylistRepository.findById(playlistId).orElseThrow();
            assertTrue(jpaEntity.getSongs().isEmpty());
        }
    }

    @Nested
    @DisplayName("UC-009: Integración Buscar Canciones en Catálogo (GET /api/v1/songs/search)")
    class SearchSongsIntegrationTests {

        @Test
        @DisplayName("shouldReturnSearchResultsFromMusicCatalogPort")
        void shouldReturnSearchResultsFromMusicCatalogPort() throws Exception {
            // Arrange
            when(musicCatalogPort.searchSongs(anyString(), anyInt())).thenReturn(List.of(testSong));

            // Act & Assert
            mockMvc.perform(get("/api/v1/songs/search")
                            .param("query", "Led Zeppelin")
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Stairway to Heaven"))
                    .andExpect(jsonPath("$[0].artist").value("Led Zeppelin"));
        }
    }
}
