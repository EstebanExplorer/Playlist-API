package com.esteban.playlistapi.presentation.controller;

import com.esteban.playlistapi.application.recommendation.dto.GenerateRecommendationCommand;
import com.esteban.playlistapi.application.recommendation.dto.RecommendationResult;
import com.esteban.playlistapi.application.recommendation.usecase.GenerateRecommendationUseCase;
import com.esteban.playlistapi.presentation.dto.request.GenerateRecommendationRequest;
import com.esteban.playlistapi.presentation.dto.response.RecommendationResponse;
import com.esteban.playlistapi.presentation.mapper.RecommendationPresentationMapper;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

/**
 * Controlador REST para la generación de recomendaciones musicales impulsadas por Inteligencia Artificial (UC-010).
 * Actúa exclusivamente como un Adaptador de Entrada (Inbound Adapter) en la Arquitectura Hexagonal.
 * <p>
 * Responsabilidad:
 * Orquestar el flujo HTTP -> Mapper -> UseCase -> Mapper -> HTTP (Thin Controller Pattern).
 * Ausencia total de lógica de negocio o decisiones condicionales.
 */
@RestController
@RequestMapping("/api/v1/playlists")
public class RecommendationController {

    private final GenerateRecommendationUseCase generateRecommendationUseCase;
    private final RecommendationPresentationMapper recommendationPresentationMapper;

    public RecommendationController(GenerateRecommendationUseCase generateRecommendationUseCase,
                                    RecommendationPresentationMapper recommendationPresentationMapper) {
        this.generateRecommendationUseCase = Objects.requireNonNull(generateRecommendationUseCase, "generateRecommendationUseCase no puede ser nulo");
        this.recommendationPresentationMapper = Objects.requireNonNull(recommendationPresentationMapper, "recommendationPresentationMapper no puede ser nulo");
    }

    @PostMapping("/{playlistId}/recommendations")
    public ResponseEntity<RecommendationResponse> generateRecommendations(
            @PathVariable UUID playlistId,
            @Valid @RequestBody GenerateRecommendationRequest request) {
        UUID userId = getCurrentUserId();
        GenerateRecommendationCommand command = recommendationPresentationMapper.toCommand(playlistId, request, userId);
        RecommendationResult result = generateRecommendationUseCase.execute(command);
        RecommendationResponse response = recommendationPresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    private UUID getCurrentUserId() {
        return UUID.fromString("11111111-1111-1111-1111-111111111111");
    }
}
