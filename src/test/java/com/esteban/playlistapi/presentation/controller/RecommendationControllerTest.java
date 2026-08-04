package com.esteban.playlistapi.presentation.controller;

import com.esteban.playlistapi.application.recommendation.dto.GenerateRecommendationCommand;
import com.esteban.playlistapi.application.recommendation.dto.RecommendationResult;
import com.esteban.playlistapi.application.recommendation.usecase.GenerateRecommendationUseCase;
import com.esteban.playlistapi.presentation.dto.request.GenerateRecommendationRequest;
import com.esteban.playlistapi.presentation.dto.response.RecommendationResponse;
import com.esteban.playlistapi.presentation.mapper.RecommendationPresentationMapper;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecommendationController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GenerateRecommendationUseCase generateRecommendationUseCase;

    @MockBean
    private RecommendationPresentationMapper recommendationPresentationMapper;

    private final UUID playlistId = UUID.randomUUID();
    private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private RecommendationResult mockResult;
    private RecommendationResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResult = new RecommendationResult(
                playlistId,
                List.of("Bohemian Rhapsody - Queen", "Hotel California - Eagles"),
                LocalDateTime.now()
        );

        mockResponse = new RecommendationResponse(
                playlistId,
                List.of("Bohemian Rhapsody - Queen", "Hotel California - Eagles"),
                LocalDateTime.now().toString()
        );
    }

    @Nested
    @DisplayName("UC-010: Generar Recomendaciones (POST /api/v1/playlists/{playlistId}/recommendations)")
    class GenerateRecommendationTests {

        @Test
        @DisplayName("Debe retornar HTTP 200 OK con RecommendationResponse en la secuencia ordenada (InOrder)")
        void shouldReturnHttp200_whenGenerateRecommendationIsSuccessful() throws Exception {
            // Arrange
            GenerateRecommendationRequest request = new GenerateRecommendationRequest(5);
            GenerateRecommendationCommand command = new GenerateRecommendationCommand(playlistId, 5, userId);

            when(recommendationPresentationMapper.toCommand(eq(playlistId), any(GenerateRecommendationRequest.class), eq(userId))).thenReturn(command);
            when(generateRecommendationUseCase.execute(any(GenerateRecommendationCommand.class))).thenReturn(mockResult);
            when(recommendationPresentationMapper.toResponse(any(RecommendationResult.class))).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/playlists/{playlistId}/recommendations", playlistId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.playlistId").value(playlistId.toString()))
                    .andExpect(jsonPath("$.recommendations").isArray())
                    .andExpect(jsonPath("$.recommendations[0]").value("Bohemian Rhapsody - Queen"));

            InOrder inOrder = inOrder(recommendationPresentationMapper, generateRecommendationUseCase);
            inOrder.verify(recommendationPresentationMapper).toCommand(eq(playlistId), any(GenerateRecommendationRequest.class), eq(userId));
            inOrder.verify(generateRecommendationUseCase).execute(any(GenerateRecommendationCommand.class));
            inOrder.verify(recommendationPresentationMapper).toResponse(any(RecommendationResult.class));
            inOrder.verifyNoMoreInteractions();

            verifyNoMoreInteractions(recommendationPresentationMapper, generateRecommendationUseCase);
        }

        @Test
        @DisplayName("Debe retornar HTTP 400 Bad Request sin interactuar con UseCase si el límite es negativo")
        void shouldReturnHttp400_whenLimitIsInvalid() throws Exception {
            // Arrange
            GenerateRecommendationRequest invalidRequest = new GenerateRecommendationRequest(-1);

            // Act & Assert
            mockMvc.perform(post("/api/v1/playlists/{playlistId}/recommendations", playlistId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(recommendationPresentationMapper, generateRecommendationUseCase);
        }
    }

    @Nested
    @DisplayName("Pruebas de Inyección por Constructor (Fail-Fast)")
    class ConstructorTests {

        @Test
        @DisplayName("Debe lanzar NullPointerException si alguna dependencia es nula")
        void shouldThrowNullPointerException_whenDependenciesAreNull() {
            assertThrows(NullPointerException.class, () -> new RecommendationController(null, recommendationPresentationMapper));
            assertThrows(NullPointerException.class, () -> new RecommendationController(generateRecommendationUseCase, null));
        }
    }
}
