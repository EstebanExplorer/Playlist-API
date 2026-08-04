package com.esteban.playlistapi.integration;

import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Recommendation;
import com.esteban.playlistapi.domain.repository.AiRecommendationPort;
import com.esteban.playlistapi.domain.repository.MusicCatalogPort;
import com.esteban.playlistapi.domain.repository.UserRepository;
import com.esteban.playlistapi.presentation.dto.request.CreatePlaylistRequest;
import com.esteban.playlistapi.presentation.dto.request.GenerateRecommendationRequest;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecommendationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MusicCatalogPort musicCatalogPort;

    @MockBean
    private AiRecommendationPort aiRecommendationPort;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        com.esteban.playlistapi.domain.model.User mockUser = new com.esteban.playlistapi.domain.model.User(
                UUID.fromString("11111111-1111-1111-1111-111111111111"), "defaultuser", "default@example.com", "password");
        when(userRepository.findById(any())).thenReturn(java.util.Optional.of(mockUser));
    }

    @Nested
    @DisplayName("UC-010: Integración Recomendaciones IA (POST /api/v1/playlists/{id}/recommendations)")
    class GenerateRecommendationIntegrationTests {

        @Test
        @DisplayName("shouldGenerateAiRecommendations_whenPlaylistExistsInDatabase")
        void shouldGenerateAiRecommendations_whenPlaylistExistsInDatabase() throws Exception {
            // Arrange: Crear una playlist en la base de datos
            MvcResult createResult = mockMvc.perform(post("/api/v1/playlists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new CreatePlaylistRequest("Playlist para IA"))))
                    .andExpect(status().isCreated())
                    .andReturn();

            String playlistIdStr = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();
            UUID playlistId = UUID.fromString(playlistIdStr);

            List<Recommendation> mockRecommendations = List.of(
                    Recommendation.create("Kashmir", "Led Zeppelin", "Similitud de estilo", 0.95),
                    Recommendation.create("Sweet Child O' Mine", "Guns N' Roses", "Similitud de estilo", 0.90)
            );

            when(aiRecommendationPort.generateRecommendations(any(Playlist.class), anyInt()))
                    .thenReturn(mockRecommendations);

            // Act & Assert
            GenerateRecommendationRequest request = new GenerateRecommendationRequest(5);
            mockMvc.perform(post("/api/v1/playlists/{playlistId}/recommendations", playlistId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.playlistId").value(playlistIdStr))
                    .andExpect(jsonPath("$.recommendations").isArray())
                    .andExpect(jsonPath("$.recommendations[0]").value("Led Zeppelin - Kashmir"));
        }
    }
}
