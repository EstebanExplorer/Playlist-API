package com.esteban.playlistapi.presentation.mapper;

import com.esteban.playlistapi.application.playlist.dto.PlaylistResult;
import com.esteban.playlistapi.application.song.dto.AddSongCommand;
import com.esteban.playlistapi.application.song.dto.RemoveSongCommand;
import com.esteban.playlistapi.application.song.dto.SearchSongsQuery;
import com.esteban.playlistapi.application.song.dto.SongSearchResult;
import com.esteban.playlistapi.presentation.dto.request.AddSongRequest;
import com.esteban.playlistapi.presentation.dto.request.SearchSongsRequest;
import com.esteban.playlistapi.presentation.dto.response.SongResponse;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Mapper de presentación para las operaciones relacionadas con canciones (UC-007, UC-008, UC-009).
 * Traduce entre los DTOs HTTP (Presentation) y los DTOs internos (Application).
 * Pertenece exclusivamente a la capa Presentation.
 */
@Component
public class SongPresentationMapper {

    public AddSongCommand toAddSongCommand(UUID playlistId, AddSongRequest request, UUID userId) {
        Objects.requireNonNull(playlistId, "playlistId no puede ser nulo");
        Objects.requireNonNull(request, "request no puede ser nulo");
        Objects.requireNonNull(userId, "userId no puede ser nulo");

        return new AddSongCommand(playlistId, request.spotifyId(), userId);
    }

    public RemoveSongCommand toRemoveSongCommand(UUID playlistId, UUID songId, UUID userId) {
        Objects.requireNonNull(playlistId, "playlistId no puede ser nulo");
        Objects.requireNonNull(songId, "songId no puede ser nulo");
        Objects.requireNonNull(userId, "userId no puede ser nulo");

        return new RemoveSongCommand(playlistId, songId, userId);
    }

    public SearchSongsQuery toSearchQuery(SearchSongsRequest request) {
        Objects.requireNonNull(request, "request no puede ser nulo");
        Integer limit = request.limit() != null ? request.limit() : 10;
        return new SearchSongsQuery(request.query(), limit);
    }

    public SongResponse toResponse(SongSearchResult result) {
        Objects.requireNonNull(result, "result no puede ser nulo");

        return new SongResponse(
                result.songId(),
                result.spotifyId(),
                result.title(),
                result.artist(),
                result.album(),
                result.durationSeconds()
        );
    }

    public List<SongResponse> toResponseList(List<SongSearchResult> results) {
        if (results == null) {
            return List.of();
        }
        return results.stream()
                .map(this::toResponse)
                .toList();
    }

    public SongResponse toItemResponse(PlaylistResult.SongItemResult result) {
        Objects.requireNonNull(result, "result no puede ser nulo");

        return new SongResponse(
                result.songId(),
                result.spotifyId(),
                result.title(),
                result.artist(),
                result.album(),
                result.durationSeconds()
        );
    }

    public List<SongResponse> toItemResponseList(List<PlaylistResult.SongItemResult> results) {
        if (results == null) {
            return List.of();
        }
        return results.stream()
                .map(this::toItemResponse)
                .toList();
    }
}
