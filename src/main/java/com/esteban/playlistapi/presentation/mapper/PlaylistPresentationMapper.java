package com.esteban.playlistapi.presentation.mapper;

import com.esteban.playlistapi.application.playlist.dto.CreatePlaylistCommand;
import com.esteban.playlistapi.application.playlist.dto.PlaylistResult;
import com.esteban.playlistapi.application.playlist.dto.UpdatePlaylistNameCommand;
import com.esteban.playlistapi.presentation.dto.request.CreatePlaylistRequest;
import com.esteban.playlistapi.presentation.dto.request.UpdatePlaylistNameRequest;
import com.esteban.playlistapi.presentation.dto.response.PlaylistResponse;
import com.esteban.playlistapi.presentation.dto.response.PlaylistSummaryResponse;
import com.esteban.playlistapi.presentation.dto.response.SongResponse;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Mapper de presentación para el recurso Playlist (UC-002, UC-003, UC-004, UC-005, UC-006).
 * Traduce entre los DTOs HTTP (Presentation) y los DTOs internos (Application).
 * Pertenece exclusivamente a la capa Presentation.
 */
@Component
public class PlaylistPresentationMapper {

    private final SongPresentationMapper songPresentationMapper;

    public PlaylistPresentationMapper(SongPresentationMapper songPresentationMapper) {
        this.songPresentationMapper = Objects.requireNonNull(songPresentationMapper, "songPresentationMapper no puede ser nulo");
    }

    public CreatePlaylistCommand toCreateCommand(CreatePlaylistRequest request, UUID userId) {
        Objects.requireNonNull(request, "request no puede ser nulo");
        Objects.requireNonNull(userId, "userId no puede ser nulo");
        return new CreatePlaylistCommand(request.name(), userId);
    }

    public UpdatePlaylistNameCommand toUpdateNameCommand(UUID id, UpdatePlaylistNameRequest request, UUID userId) {
        Objects.requireNonNull(id, "id no puede ser nulo");
        Objects.requireNonNull(request, "request no puede ser nulo");
        Objects.requireNonNull(userId, "userId no puede ser nulo");
        return new UpdatePlaylistNameCommand(id, request.name(), userId);
    }

    public PlaylistResponse toResponse(PlaylistResult result) {
        Objects.requireNonNull(result, "result no puede ser nulo");

        List<SongResponse> songResponses = songPresentationMapper.toItemResponseList(result.songs());

        return new PlaylistResponse(
                result.id(),
                result.name(),
                result.userId(),
                result.songCount(),
                songResponses,
                result.createdAt() != null ? result.createdAt().toString() : null,
                result.updatedAt() != null ? result.updatedAt().toString() : null
        );
    }

    public PlaylistSummaryResponse toSummaryResponse(PlaylistResult result) {
        Objects.requireNonNull(result, "result no puede ser nulo");

        return new PlaylistSummaryResponse(
                result.id(),
                result.name(),
                result.userId(),
                result.songCount(),
                result.createdAt() != null ? result.createdAt().toString() : null,
                result.updatedAt() != null ? result.updatedAt().toString() : null
        );
    }

    public List<PlaylistSummaryResponse> toSummaryResponseList(List<PlaylistResult> results) {
        if (results == null) {
            return List.of();
        }
        return results.stream()
                .map(this::toSummaryResponse)
                .toList();
    }
}
