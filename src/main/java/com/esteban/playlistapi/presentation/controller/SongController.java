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

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Controlador REST para la gestión y consulta de canciones (UC-007, UC-008, UC-009).
 * Actúa exclusivamente como un Adaptador de Entrada (Inbound Adapter) en la Arquitectura Hexagonal.
 * <p>
 * Responsabilidad:
 * Orquestar el flujo HTTP -> Mapper -> UseCase -> Mapper -> HTTP (Thin Controller Pattern).
 * Ausencia total de lógica de negocio o decisiones condicionales.
 */
@RestController
@RequestMapping("/api/v1")
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
    public ResponseEntity<Void> removeSongFromPlaylist(
            @PathVariable UUID playlistId,
            @PathVariable UUID songId) {
        UUID userId = getCurrentUserId();
        RemoveSongCommand command = songPresentationMapper.toRemoveSongCommand(playlistId, songId, userId);
        removeSongFromPlaylistUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/songs/search")
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
