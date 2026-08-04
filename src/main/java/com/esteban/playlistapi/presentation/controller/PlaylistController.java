package com.esteban.playlistapi.presentation.controller;

import com.esteban.playlistapi.application.playlist.dto.CreatePlaylistCommand;
import com.esteban.playlistapi.application.playlist.dto.DeletePlaylistCommand;
import com.esteban.playlistapi.application.playlist.dto.GetPlaylistByIdQuery;
import com.esteban.playlistapi.application.playlist.dto.ListUserPlaylistsQuery;
import com.esteban.playlistapi.application.playlist.dto.PlaylistResult;
import com.esteban.playlistapi.application.playlist.dto.UpdatePlaylistNameCommand;
import com.esteban.playlistapi.application.playlist.usecase.CreatePlaylistUseCase;
import com.esteban.playlistapi.application.playlist.usecase.DeletePlaylistUseCase;
import com.esteban.playlistapi.application.playlist.usecase.GetPlaylistByIdUseCase;
import com.esteban.playlistapi.application.playlist.usecase.ListUserPlaylistsUseCase;
import com.esteban.playlistapi.application.playlist.usecase.UpdatePlaylistNameUseCase;
import com.esteban.playlistapi.infrastructure.configuration.OpenApiConfiguration;
import com.esteban.playlistapi.presentation.dto.error.ApiErrorResponse;
import com.esteban.playlistapi.presentation.dto.request.CreatePlaylistRequest;
import com.esteban.playlistapi.presentation.dto.request.UpdatePlaylistNameRequest;
import com.esteban.playlistapi.presentation.dto.response.PlaylistResponse;
import com.esteban.playlistapi.presentation.dto.response.PlaylistSummaryResponse;
import com.esteban.playlistapi.presentation.mapper.PlaylistPresentationMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Controlador REST para la gestión del recurso Playlist (UC-002 a UC-006).
 * Actúa exclusivamente como un Adaptador de Entrada (Inbound Adapter) en la Arquitectura Hexagonal.
 */
@RestController
@RequestMapping("/api/v1/playlists")
@Tag(name = "Playlists", description = "Endpoints para la gestión del ciclo de vida de las listas de reproducción (UC-002 a UC-006)")
@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME_NAME)
public class PlaylistController {

    private final CreatePlaylistUseCase createPlaylistUseCase;
    private final GetPlaylistByIdUseCase getPlaylistByIdUseCase;
    private final ListUserPlaylistsUseCase listUserPlaylistsUseCase;
    private final UpdatePlaylistNameUseCase updatePlaylistNameUseCase;
    private final DeletePlaylistUseCase deletePlaylistUseCase;
    private final PlaylistPresentationMapper playlistPresentationMapper;

    public PlaylistController(CreatePlaylistUseCase createPlaylistUseCase,
                              GetPlaylistByIdUseCase getPlaylistByIdUseCase,
                              ListUserPlaylistsUseCase listUserPlaylistsUseCase,
                              UpdatePlaylistNameUseCase updatePlaylistNameUseCase,
                              DeletePlaylistUseCase deletePlaylistUseCase,
                              PlaylistPresentationMapper playlistPresentationMapper) {
        this.createPlaylistUseCase = Objects.requireNonNull(createPlaylistUseCase, "createPlaylistUseCase no puede ser nulo");
        this.getPlaylistByIdUseCase = Objects.requireNonNull(getPlaylistByIdUseCase, "getPlaylistByIdUseCase no puede ser nulo");
        this.listUserPlaylistsUseCase = Objects.requireNonNull(listUserPlaylistsUseCase, "listUserPlaylistsUseCase no puede ser nulo");
        this.updatePlaylistNameUseCase = Objects.requireNonNull(updatePlaylistNameUseCase, "updatePlaylistNameUseCase no puede ser nulo");
        this.deletePlaylistUseCase = Objects.requireNonNull(deletePlaylistUseCase, "deletePlaylistUseCase no puede ser nulo");
        this.playlistPresentationMapper = Objects.requireNonNull(playlistPresentationMapper, "playlistPresentationMapper no puede ser nulo");
    }

    @PostMapping
    @Operation(summary = "Crear nueva playlist", description = "Crea una nueva lista de reproducción asociada al usuario autenticado (UC-002).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Playlist creada exitosamente",
                    content = @Content(schema = @Schema(implementation = PlaylistResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida o nombre con formato incorrecto",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PlaylistResponse> createPlaylist(@Valid @RequestBody CreatePlaylistRequest request) {
        UUID userId = getCurrentUserId();
        CreatePlaylistCommand command = playlistPresentationMapper.toCreateCommand(request, userId);
        PlaylistResult result = createPlaylistUseCase.execute(command);
        PlaylistResponse response = playlistPresentationMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{playlistId}")
    @Operation(summary = "Consultar playlist por ID", description = "Obtiene los detalles completos de una playlist existente con su lista de canciones (UC-003).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Playlist encontrada",
                    content = @Content(schema = @Schema(implementation = PlaylistResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Prohibido - El usuario no es propietario de la playlist",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Playlist no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PlaylistResponse> getPlaylistById(@PathVariable UUID playlistId) {
        UUID userId = getCurrentUserId();
        GetPlaylistByIdQuery query = new GetPlaylistByIdQuery(playlistId, userId);
        PlaylistResult result = getPlaylistByIdUseCase.execute(query);
        PlaylistResponse response = playlistPresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar playlists del usuario", description = "Obtiene una lista resumida de todas las playlists creadas por el usuario autenticado (UC-004).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de playlists del usuario",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaylistSummaryResponse.class)))),
            @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<PlaylistSummaryResponse>> listUserPlaylists() {
        UUID userId = getCurrentUserId();
        ListUserPlaylistsQuery query = new ListUserPlaylistsQuery(userId);
        List<PlaylistResult> results = listUserPlaylistsUseCase.execute(query);
        List<PlaylistSummaryResponse> response = playlistPresentationMapper.toSummaryResponseList(results);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{playlistId}")
    @Operation(summary = "Actualizar nombre de playlist", description = "Actualiza el nombre de una playlist existente del usuario autenticado (UC-005).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nombre de playlist actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = PlaylistResponse.class))),
            @ApiResponse(responseCode = "400", description = "Nuevo nombre inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Prohibido - El usuario no es propietario de la playlist",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Playlist no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PlaylistResponse> updatePlaylistName(
            @PathVariable UUID playlistId,
            @Valid @RequestBody UpdatePlaylistNameRequest request) {
        UUID userId = getCurrentUserId();
        UpdatePlaylistNameCommand command = playlistPresentationMapper.toUpdateNameCommand(playlistId, request, userId);
        PlaylistResult result = updatePlaylistNameUseCase.execute(command);
        PlaylistResponse response = playlistPresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{playlistId}")
    @Operation(summary = "Eliminar playlist", description = "Elimina permanentemente una playlist del usuario autenticado (UC-006).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Playlist eliminada exitosamente (Sin contenido)"),
            @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Prohibido - El usuario no es propietario de la playlist",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Playlist no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> deletePlaylist(@PathVariable UUID playlistId) {
        UUID userId = getCurrentUserId();
        DeletePlaylistCommand command = new DeletePlaylistCommand(playlistId, userId);
        deletePlaylistUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }

    private UUID getCurrentUserId() {
        return UUID.fromString("11111111-1111-1111-1111-111111111111");
    }
}
