package com.esteban.playlistapi.integration;

import com.esteban.playlistapi.domain.repository.AiRecommendationPort;
import com.esteban.playlistapi.domain.repository.MusicCatalogPort;
import com.esteban.playlistapi.domain.repository.UserRepository;
import com.esteban.playlistapi.infrastructure.persistence.repository.SpringDataJpaPlaylistRepository;
import com.esteban.playlistapi.presentation.dto.request.CreatePlaylistRequest;
import com.esteban.playlistapi.presentation.dto.request.UpdatePlaylistNameRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PlaylistIntegrationTest {

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

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        com.esteban.playlistapi.domain.model.User mockUser = new com.esteban.playlistapi.domain.model.User(
                java.util.UUID.fromString("11111111-1111-1111-1111-111111111111"), "defaultuser", "default@example.com", "password");
        org.mockito.Mockito.when(userRepository.findById(org.mockito.ArgumentMatchers.any())).thenReturn(java.util.Optional.of(mockUser));
    }

    @Nested
    @DisplayName("UC-002: Integración Crear Playlist (POST /api/v1/playlists)")
    class CreatePlaylistIntegrationTests {

        @Test
        @DisplayName("shouldCreateAndPersistPlaylistInDatabase_whenRequestIsValid")
        void shouldCreateAndPersistPlaylistInDatabase_whenRequestIsValid() throws Exception {
            // Arrange
            CreatePlaylistRequest request = new CreatePlaylistRequest("Rock de los 80s");

            // Act
            MvcResult result = mockMvc.perform(post("/api/v1/playlists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.name").value("Rock de los 80s"))
                    .andExpect(jsonPath("$.songCount").value(0))
                    .andReturn();

            // Assert: Verificación de persistencia real en la base de datos H2
            String responseBody = result.getResponse().getContentAsString();
            String idStr = objectMapper.readTree(responseBody).get("id").asText();
            UUID createdId = UUID.fromString(idStr);

            assertTrue(jpaPlaylistRepository.existsById(createdId));
            assertEquals("Rock de los 80s", jpaPlaylistRepository.findById(createdId).get().getName());
        }

        @Test
        @DisplayName("shouldReturnHttp400BadRequest_whenPlaylistNameIsBlank")
        void shouldReturnHttp400BadRequest_whenPlaylistNameIsBlank() throws Exception {
            // Arrange
            CreatePlaylistRequest invalidRequest = new CreatePlaylistRequest("");

            // Act & Assert
            mockMvc.perform(post("/api/v1/playlists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    @DisplayName("UC-003: Integración Consultar Playlist por ID (GET /api/v1/playlists/{id})")
    class GetPlaylistByIdIntegrationTests {

        @Test
        @DisplayName("shouldReturnPlaylistDetailsFromDatabase_whenPlaylistExists")
        void shouldReturnPlaylistDetailsFromDatabase_whenPlaylistExists() throws Exception {
            // Arrange: Crear primero una playlist vía POST
            CreatePlaylistRequest createRequest = new CreatePlaylistRequest("Jazz & Blues");
            MvcResult createResult = mockMvc.perform(post("/api/v1/playlists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String createdId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

            // Act & Assert: Consultar la playlist persistida en la base de datos H2
            mockMvc.perform(get("/api/v1/playlists/{playlistId}", createdId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(createdId))
                    .andExpect(jsonPath("$.name").value("Jazz & Blues"));
        }

        @Test
        @DisplayName("shouldReturnHttp404NotFound_whenPlaylistDoesNotExistInDatabase")
        void shouldReturnHttp404NotFound_whenPlaylistDoesNotExistInDatabase() throws Exception {
            UUID nonExistentId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/playlists/{playlistId}", nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("UC-004: Integración Listar Playlists del Usuario (GET /api/v1/playlists)")
    class ListUserPlaylistsIntegrationTests {

        @Test
        @DisplayName("shouldReturnAllUserPlaylistsStoredInDatabase")
        void shouldReturnAllUserPlaylistsStoredInDatabase() throws Exception {
            // Arrange: Crear dos playlists en la base de datos
            mockMvc.perform(post("/api/v1/playlists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new CreatePlaylistRequest("Playlist A"))));

            mockMvc.perform(post("/api/v1/playlists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new CreatePlaylistRequest("Playlist B"))));

            // Act & Assert
            mockMvc.perform(get("/api/v1/playlists"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    @Nested
    @DisplayName("UC-005: Integración Actualizar Nombre (PUT /api/v1/playlists/{id})")
    class UpdatePlaylistNameIntegrationTests {

        @Test
        @DisplayName("shouldUpdatePlaylistNameInDatabase_whenRequestIsValid")
        void shouldUpdatePlaylistNameInDatabase_whenRequestIsValid() throws Exception {
            // Arrange: Crear una playlist
            MvcResult createResult = mockMvc.perform(post("/api/v1/playlists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreatePlaylistRequest("Nombre Antiguo"))))
                    .andExpect(status().isCreated())
                    .andReturn();

            String createdIdStr = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();
            UUID playlistId = UUID.fromString(createdIdStr);

            // Act: Actualizar el nombre
            UpdatePlaylistNameRequest updateRequest = new UpdatePlaylistNameRequest("Nombre Nuevo");
            mockMvc.perform(put("/api/v1/playlists/{playlistId}", playlistId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Nombre Nuevo"));

            // Assert: Verificar el cambio en la base de datos relacional H2
            assertEquals("Nombre Nuevo", jpaPlaylistRepository.findById(playlistId).get().getName());
        }
    }

    @Nested
    @DisplayName("UC-006: Integración Eliminar Playlist (DELETE /api/v1/playlists/{id})")
    class DeletePlaylistIntegrationTests {

        @Test
        @DisplayName("shouldDeletePlaylistFromDatabase_whenDeleteIsRequested")
        void shouldDeletePlaylistFromDatabase_whenDeleteIsRequested() throws Exception {
            // Arrange: Crear una playlist
            MvcResult createResult = mockMvc.perform(post("/api/v1/playlists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreatePlaylistRequest("Para Eliminar"))))
                    .andExpect(status().isCreated())
                    .andReturn();

            String createdIdStr = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();
            UUID playlistId = UUID.fromString(createdIdStr);

            // Act: Eliminar la playlist
            mockMvc.perform(delete("/api/v1/playlists/{playlistId}", playlistId))
                    .andExpect(status().isNoContent());

            // Assert: Verificar que ya no existe en la base de datos relacional H2
            assertFalse(jpaPlaylistRepository.existsById(playlistId));
        }
    }
}
