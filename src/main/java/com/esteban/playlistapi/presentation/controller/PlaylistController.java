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
import com.esteban.playlistapi.presentation.dto.request.CreatePlaylistRequest;
import com.esteban.playlistapi.presentation.dto.request.UpdatePlaylistNameRequest;
import com.esteban.playlistapi.presentation.dto.response.PlaylistResponse;
import com.esteban.playlistapi.presentation.dto.response.PlaylistSummaryResponse;
import com.esteban.playlistapi.presentation.mapper.PlaylistPresentationMapper;

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
 * <p>
 * Responsabilidad:
 * Orquestar el flujo HTTP -> Mapper -> UseCase -> Mapper -> HTTP (Thin Controller Pattern).
 * Ausencia total de lógica de negocio o decisiones condicionales.
 */
@RestController
@RequestMapping("/api/v1/playlists")
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
    public ResponseEntity<PlaylistResponse> createPlaylist(@Valid @RequestBody CreatePlaylistRequest request) {
        UUID userId = getCurrentUserId();
        CreatePlaylistCommand command = playlistPresentationMapper.toCreateCommand(request, userId);
        PlaylistResult result = createPlaylistUseCase.execute(command);
        PlaylistResponse response = playlistPresentationMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistResponse> getPlaylistById(@PathVariable UUID playlistId) {
        UUID userId = getCurrentUserId();
        GetPlaylistByIdQuery query = new GetPlaylistByIdQuery(playlistId, userId);
        PlaylistResult result = getPlaylistByIdUseCase.execute(query);
        PlaylistResponse response = playlistPresentationMapper.toResponse(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PlaylistSummaryResponse>> listUserPlaylists() {
        UUID userId = getCurrentUserId();
        ListUserPlaylistsQuery query = new ListUserPlaylistsQuery(userId);
        List<PlaylistResult> results = listUserPlaylistsUseCase.execute(query);
        List<PlaylistSummaryResponse> response = playlistPresentationMapper.toSummaryResponseList(results);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{playlistId}")
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
