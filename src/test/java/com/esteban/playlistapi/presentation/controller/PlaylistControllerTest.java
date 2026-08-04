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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlaylistController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlaylistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreatePlaylistUseCase createPlaylistUseCase;

    @MockBean
    private GetPlaylistByIdUseCase getPlaylistByIdUseCase;

    @MockBean
    private ListUserPlaylistsUseCase listUserPlaylistsUseCase;

    @MockBean
    private UpdatePlaylistNameUseCase updatePlaylistNameUseCase;

    @MockBean
    private DeletePlaylistUseCase deletePlaylistUseCase;

    @MockBean
    private PlaylistPresentationMapper playlistPresentationMapper;

    private final UUID playlistId = UUID.randomUUID();
    private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private PlaylistResult mockResult;
    private PlaylistResponse mockResponse;
    private PlaylistSummaryResponse mockSummaryResponse;

    @BeforeEach
    void setUp() {
        mockResult = new PlaylistResult(
                playlistId,
                "Rock Clásico",
                userId,
                0,
                Collections.emptyList(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        mockResponse = new PlaylistResponse(
                playlistId,
                "Rock Clásico",
                userId,
                0,
                Collections.emptyList(),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString()
        );

        mockSummaryResponse = new PlaylistSummaryResponse(
                playlistId,
                "Rock Clásico",
                userId,
                0,
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString()
        );
    }

    @Nested
    @DisplayName("UC-002: Crear Playlist (POST /api/v1/playlists)")
    class CreatePlaylistTests {

        @Test
        @DisplayName("Debe retornar HTTP 201 Created con PlaylistResponse en la secuencia ordenada (InOrder)")
        void shouldReturnHttp201_whenCreatePlaylistIsSuccessful() throws Exception {
            // Arrange
            CreatePlaylistRequest request = new CreatePlaylistRequest("Rock Clásico");
            CreatePlaylistCommand command = new CreatePlaylistCommand("Rock Clásico", userId);

            when(playlistPresentationMapper.toCreateCommand(any(CreatePlaylistRequest.class), eq(userId))).thenReturn(command);
            when(createPlaylistUseCase.execute(any(CreatePlaylistCommand.class))).thenReturn(mockResult);
            when(playlistPresentationMapper.toResponse(any(PlaylistResult.class))).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(post("/api/v1/playlists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(playlistId.toString()))
                    .andExpect(jsonPath("$.name").value("Rock Clásico"));

            InOrder inOrder = inOrder(playlistPresentationMapper, createPlaylistUseCase);
            inOrder.verify(playlistPresentationMapper).toCreateCommand(any(CreatePlaylistRequest.class), eq(userId));
            inOrder.verify(createPlaylistUseCase).execute(any(CreatePlaylistCommand.class));
            inOrder.verify(playlistPresentationMapper).toResponse(any(PlaylistResult.class));
            inOrder.verifyNoMoreInteractions();

            verifyNoMoreInteractions(playlistPresentationMapper, createPlaylistUseCase);
        }

        @Test
        @DisplayName("Debe retornar HTTP 400 Bad Request sin interactuar con UseCase si el nombre es inválido")
        void shouldReturnHttp400_whenCreatePlaylistNameIsInvalid() throws Exception {
            // Arrange
            CreatePlaylistRequest invalidRequest = new CreatePlaylistRequest("");

            // Act & Assert
            mockMvc.perform(post("/api/v1/playlists")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(playlistPresentationMapper, createPlaylistUseCase);
        }
    }

    @Nested
    @DisplayName("UC-003: Consultar Playlist por ID (GET /api/v1/playlists/{id})")
    class GetPlaylistByIdTests {

        @Test
        @DisplayName("Debe retornar HTTP 200 OK con PlaylistResponse")
        void shouldReturnHttp200_whenGetPlaylistByIdIsSuccessful() throws Exception {
            // Arrange
            when(getPlaylistByIdUseCase.execute(any(GetPlaylistByIdQuery.class))).thenReturn(mockResult);
            when(playlistPresentationMapper.toResponse(any(PlaylistResult.class))).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(get("/api/v1/playlists/{playlistId}", playlistId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(playlistId.toString()))
                    .andExpect(jsonPath("$.name").value("Rock Clásico"));

            InOrder inOrder = inOrder(getPlaylistByIdUseCase, playlistPresentationMapper);
            inOrder.verify(getPlaylistByIdUseCase).execute(any(GetPlaylistByIdQuery.class));
            inOrder.verify(playlistPresentationMapper).toResponse(any(PlaylistResult.class));
            inOrder.verifyNoMoreInteractions();

            verifyNoMoreInteractions(getPlaylistByIdUseCase, playlistPresentationMapper);
        }
    }

    @Nested
    @DisplayName("UC-004: Listar Playlists del Usuario (GET /api/v1/playlists)")
    class ListUserPlaylistsTests {

        @Test
        @DisplayName("Debe retornar HTTP 200 OK con lista de PlaylistSummaryResponse")
        void shouldReturnHttp200_whenListUserPlaylistsIsSuccessful() throws Exception {
            // Arrange
            when(listUserPlaylistsUseCase.execute(any(ListUserPlaylistsQuery.class))).thenReturn(List.of(mockResult));
            when(playlistPresentationMapper.toSummaryResponseList(anyList())).thenReturn(List.of(mockSummaryResponse));

            // Act & Assert
            mockMvc.perform(get("/api/v1/playlists"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(playlistId.toString()))
                    .andExpect(jsonPath("$[0].name").value("Rock Clásico"));

            InOrder inOrder = inOrder(listUserPlaylistsUseCase, playlistPresentationMapper);
            inOrder.verify(listUserPlaylistsUseCase).execute(any(ListUserPlaylistsQuery.class));
            inOrder.verify(playlistPresentationMapper).toSummaryResponseList(anyList());
            inOrder.verifyNoMoreInteractions();

            verifyNoMoreInteractions(listUserPlaylistsUseCase, playlistPresentationMapper);
        }
    }

    @Nested
    @DisplayName("UC-005: Actualizar Nombre de Playlist (PUT /api/v1/playlists/{id})")
    class UpdatePlaylistNameTests {

        @Test
        @DisplayName("Debe retornar HTTP 200 OK con PlaylistResponse actualizada")
        void shouldReturnHttp200_whenUpdatePlaylistNameIsSuccessful() throws Exception {
            // Arrange
            UpdatePlaylistNameRequest request = new UpdatePlaylistNameRequest("Rock Clásico Vol 1");
            UpdatePlaylistNameCommand command = new UpdatePlaylistNameCommand(playlistId, "Rock Clásico Vol 1", userId);

            when(playlistPresentationMapper.toUpdateNameCommand(eq(playlistId), any(UpdatePlaylistNameRequest.class), eq(userId))).thenReturn(command);
            when(updatePlaylistNameUseCase.execute(any(UpdatePlaylistNameCommand.class))).thenReturn(mockResult);
            when(playlistPresentationMapper.toResponse(any(PlaylistResult.class))).thenReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(put("/api/v1/playlists/{playlistId}", playlistId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(playlistId.toString()));

            InOrder inOrder = inOrder(playlistPresentationMapper, updatePlaylistNameUseCase);
            inOrder.verify(playlistPresentationMapper).toUpdateNameCommand(eq(playlistId), any(UpdatePlaylistNameRequest.class), eq(userId));
            inOrder.verify(updatePlaylistNameUseCase).execute(any(UpdatePlaylistNameCommand.class));
            inOrder.verify(playlistPresentationMapper).toResponse(any(PlaylistResult.class));
            inOrder.verifyNoMoreInteractions();

            verifyNoMoreInteractions(playlistPresentationMapper, updatePlaylistNameUseCase);
        }
    }

    @Nested
    @DisplayName("UC-006: Eliminar Playlist (DELETE /api/v1/playlists/{id})")
    class DeletePlaylistTests {

        @Test
        @DisplayName("Debe retornar HTTP 204 No Content al eliminar exitosamente")
        void shouldReturnHttp204_whenDeletePlaylistIsSuccessful() throws Exception {
            // Arrange
            doNothing().when(deletePlaylistUseCase).execute(any(DeletePlaylistCommand.class));

            // Act & Assert
            mockMvc.perform(delete("/api/v1/playlists/{playlistId}", playlistId))
                    .andExpect(status().isNoContent());

            verify(deletePlaylistUseCase, times(1)).execute(any(DeletePlaylistCommand.class));
            verifyNoMoreInteractions(deletePlaylistUseCase);
        }
    }

    @Nested
    @DisplayName("Pruebas de Inyección por Constructor (Fail-Fast)")
    class ConstructorTests {

        @Test
        @DisplayName("Debe lanzar NullPointerException si alguna dependencia es nula")
        void shouldThrowNullPointerException_whenDependenciesAreNull() {
            assertThrows(NullPointerException.class, () -> new PlaylistController(null, getPlaylistByIdUseCase, listUserPlaylistsUseCase, updatePlaylistNameUseCase, deletePlaylistUseCase, playlistPresentationMapper));
            assertThrows(NullPointerException.class, () -> new PlaylistController(createPlaylistUseCase, getPlaylistByIdUseCase, listUserPlaylistsUseCase, updatePlaylistNameUseCase, deletePlaylistUseCase, null));
        }
    }
}
