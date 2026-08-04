package com.esteban.playlistapi.presentation.controller;

import com.esteban.playlistapi.application.recommendation.dto.GenerateRecommendationCommand;
import com.esteban.playlistapi.application.recommendation.dto.RecommendationResult;
import com.esteban.playlistapi.application.recommendation.usecase.GenerateRecommendationUseCase;
import com.esteban.playlistapi.infrastructure.configuration.OpenApiConfiguration;
import com.esteban.playlistapi.presentation.dto.error.ApiErrorResponse;
import com.esteban.playlistapi.presentation.dto.request.GenerateRecommendationRequest;
import com.esteban.playlistapi.presentation.dto.response.RecommendationResponse;
import com.esteban.playlistapi.presentation.mapper.RecommendationPresentationMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

/**
 * Controlador REST para la generación de recomendaciones musicales impulsadas por Inteligencia Artificial (UC-010).
 * Actúa exclusivamente como un Adaptador de Entrada (Inbound Adapter) en la Arquitectura Hexagonal.
 */
@RestController
@RequestMapping("/api/v1/playlists")
@Tag(name = "Recomendaciones IA", description = "Endpoints para la generación de sugerencias musicales mediante Inteligencia Artificial (UC-010)")
@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME_NAME)
public class RecommendationController {

    private final GenerateRecommendationUseCase generateRecommendationUseCase;
    private final RecommendationPresentationMapper recommendationPresentationMapper;

    public RecommendationController(GenerateRecommendationUseCase generateRecommendationUseCase,
                                    RecommendationPresentationMapper recommendationPresentationMapper) {
        this.generateRecommendationUseCase = Objects.requireNonNull(generateRecommendationUseCase, "generateRecommendationUseCase no puede ser nulo");
        this.recommendationPresentationMapper = Objects.requireNonNull(recommendationPresentationMapper, "recommendationPresentationMapper no puede ser nulo");
    }

    @PostMapping("/{playlistId}/recommendations")
    @Operation(summary = "Generar recomendaciones con IA", description = "Solicita al proveedor de Inteligencia Artificial (Gemini / OpenAI) recomendaciones de canciones personalizadas basadas en el contenido de la playlist (UC-010).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recomendaciones generadas exitosamente",
                    content = @Content(schema = @Schema(implementation = RecommendationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Límite o solicitud inválida",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Prohibido - El usuario no es propietario de la playlist",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Playlist no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Error en comunicación con el servicio de Inteligencia Artificial",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
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
