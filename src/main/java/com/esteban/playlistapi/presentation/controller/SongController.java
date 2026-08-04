package com.esteban.playlistapi.presentation.controller;

import com.esteban.playlistapi.application.playlist.dto.PlaylistResult;
import com.esteban.playlistapi.application.song.dto.AddSongCommand;
import com.esteban.playlistapi.application.song.dto.RemoveSongCommand;
import com.esteban.playlistapi.application.song.dto.SearchSongsQuery;
import com.esteban.playlistapi.application.song.dto.SongSearchResult;
import com.esteban.playlistapi.application.song.usecase.AddSongToPlaylistUseCase;
import com.esteban.playlistapi.application.song.usecase.RemoveSongFromPlaylistUseCase;
import com.esteban.playlistapi.application.song.usecase.SearchSongsUseCase;
import com.esteban.playlistapi.infrastructure.configuration.OpenApiConfiguration;
import com.esteban.playlistapi.presentation.dto.error.ApiErrorResponse;
import com.esteban.playlistapi.presentation.dto.request.AddSongRequest;
import com.esteban.playlistapi.presentation.dto.request.SearchSongsRequest;
import com.esteban.playlistapi.presentation.dto.response.PlaylistResponse;
import com.esteban.playlistapi.presentation.dto.response.SongResponse;
import com.esteban.playlistapi.presentation.mapper.PlaylistPresentationMapper;
import com.esteban.playlistapi.presentation.mapper.SongPresentationMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Controlador REST para la gestión y consulta de canciones (UC-007, UC-008, UC-009).
 * Actúa exclusivamente como un Adaptador de Entrada (Inbound Adapter) en la Arquitectura Hexagonal.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Canciones y Catálogo", description = "Endpoints para la adición/remoción de canciones en playlists y búsquedas en catálogo (UC-007, UC-008, UC-009)")
@SecurityRequirement(name = OpenApiConfiguration.SECURITY_SCHEME_NAME)
public class SongController {

    private final AddSongToPlaylistUseCase addSongToPlaylistUseCase;
    private final RemoveSongFromPlaylistUseCase removeSongFromPlaylistUseCase;
    private final SearchSongsUseCase searchSongsUseCase;
    private final SongPresentationMapper songPresentationMapper;
    private final PlaylistPresentationMapper playlistPresentationMapper;

    public SongController(AddSongToPlaylistUseCase addSongToPlaylistUseCase,
                          RemoveSongFromPlaylistUseCase removeSongFromPlaylistUseCase,
                          SearchSongsUseCase searchSongsUseCase,
                          SongPresentationMapper songPresentationMapper,
                          PlaylistPresentationMapper playlistPresentationMapper) {
        this.addSongToPlaylistUseCase = Objects.requireNonNull(addSongToPlaylistUseCase, "addSongToPlaylistUseCase no puede ser nulo");
        this.removeSongFromPlaylistUseCase = Objects.requireNonNull(removeSongFromPlaylistUseCase, "removeSongFromPlaylistUseCase no puede ser nulo");
        this.searchSongsUseCase = Objects.requireNonNull(searchSongsUseCase, "searchSongsUseCase no puede ser nulo");
        this.songPresentationMapper = Objects.requireNonNull(songPresentationMapper, "songPresentationMapper no puede ser nulo");
        this.playlistPresentationMapper = Objects.requireNonNull(playlistPresentationMapper, "playlistPresentationMapper no puede ser nulo");
    }

    @PostMapping("/playlists/{playlistId}/songs")
    @Operation(summary = "Agregar canción a playlist", description = "Añade una canción del catálogo musical (Spotify) a la playlist especificada (UC-007).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Canción agregada exitosamente a la playlist",
                    content = @Content(schema = @Schema(implementation = PlaylistResponse.class))),
            @ApiResponse(responseCode = "400", description = "Spotify ID o solicitud inválida",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Prohibido - El usuario no es propietario de la playlist",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Playlist o canción no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto - La canción ya se encuentra en la playlist",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<PlaylistResponse> addSongToPlaylist(
            @PathVariable UUID playlistId,
            @Valid @RequestBody AddSongRequest request) {
        UUID userId = getCurrentUserId();
        AddSongCommand command = songPresentationMapper.toAddSongCommand(playlistId, request, userId);
        PlaylistResult result = addSongToPlaylistUseCase.execute(command);
        PlaylistResponse response = playlistPresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/playlists/{playlistId}/songs/{songId}")
    @Operation(summary = "Eliminar canción de playlist", description = "Remueve una canción de la playlist especificada (UC-008).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Canción removida exitosamente (Sin contenido)"),
            @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Prohibido - El usuario no es propietario de la playlist",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Playlist o canción no encontrada",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> removeSongFromPlaylist(
            @PathVariable UUID playlistId,
            @PathVariable UUID songId) {
        UUID userId = getCurrentUserId();
        RemoveSongCommand command = songPresentationMapper.toRemoveSongCommand(playlistId, songId, userId);
        removeSongFromPlaylistUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/songs/search")
    @Operation(summary = "Buscar canciones en el catálogo", description = "Consulta el catálogo musical externo (Spotify) buscando canciones por artista, título o álbum (UC-009).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados de búsqueda en el catálogo",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SongResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Término de búsqueda o límite inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado - Token JWT ausente o inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Error en comunicación con el catálogo externo (Spotify)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<SongResponse>> searchSongs(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit) {
        SearchSongsRequest request = new SearchSongsRequest(query, limit);
        SearchSongsQuery searchQuery = songPresentationMapper.toSearchQuery(request);
        List<SongSearchResult> results = searchSongsUseCase.execute(searchQuery);
        List<SongResponse> response = songPresentationMapper.toResponseList(results);
        return ResponseEntity.ok(response);
    }

    private UUID getCurrentUserId() {
        return UUID.fromString("11111111-1111-1111-1111-111111111111");
    }
}
