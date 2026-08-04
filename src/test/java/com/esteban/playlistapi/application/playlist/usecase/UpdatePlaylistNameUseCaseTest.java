package com.esteban.playlistapi.application.playlist.usecase;

import com.esteban.playlistapi.application.playlist.dto.PlaylistResult;
import com.esteban.playlistapi.application.playlist.dto.UpdatePlaylistNameCommand;
import com.esteban.playlistapi.domain.exception.PlaylistNotFoundException;
import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.repository.PlaylistRepository;
import com.esteban.playlistapi.domain.service.PlaylistDomainService;
import com.esteban.playlistapi.shared.exception.InvalidCommandException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatePlaylistNameUseCaseTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private PlaylistDomainService playlistDomainService;

    private UpdatePlaylistNameUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private Playlist mockPlaylist;

    @BeforeEach
    void setUp() {
        useCase = new UpdatePlaylistNameUseCase(playlistRepository, playlistDomainService);
        mockPlaylist = Playlist.create("Nombre Antiguo", userId);
    }

    @Nested
    @DisplayName("Escenarios Exitosos")
    class SuccessfulScenarios {

        @Test
        @DisplayName("Debe actualizar el nombre de la playlist correctamente cuando la playlist existe y el comando es válido")
        void shouldUpdatePlaylistNameSuccessfully_whenPlaylistExistsAndCommandIsValid() {
            // Arrange
            String newName = "Nombre Actualizado";
            UpdatePlaylistNameCommand command = new UpdatePlaylistNameCommand(mockPlaylist.getId(), newName);

            when(playlistRepository.findById(mockPlaylist.getId())).thenReturn(Optional.of(mockPlaylist));
            when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            PlaylistResult result = useCase.execute(command);

            // Assert
            assertNotNull(result);
            assertEquals(mockPlaylist.getId(), result.id());
            assertEquals(newName, result.name());
            assertEquals(userId, result.userId());

            verify(playlistRepository, times(1)).findById(mockPlaylist.getId());
            verify(playlistRepository, times(1)).save(mockPlaylist);
            verifyNoMoreInteractions(playlistRepository);
            verifyNoInteractions(playlistDomainService);
        }
    }

    @Nested
    @DisplayName("Escenarios de Error y Excepciones")
    class FailureScenarios {

        @Test
        @DisplayName("Debe lanzar PlaylistNotFoundException cuando la playlist no existe en el repositorio")
        void shouldThrowPlaylistNotFoundException_whenPlaylistDoesNotExist() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            UpdatePlaylistNameCommand command = new UpdatePlaylistNameCommand(nonExistentId, "Nuevo Nombre");

            when(playlistRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            PlaylistNotFoundException exception = assertThrows(
                    PlaylistNotFoundException.class,
                    () -> useCase.execute(command)
            );

            assertTrue(exception.getMessage().contains(nonExistentId.toString()));
            verify(playlistRepository, times(1)).findById(nonExistentId);
            verify(playlistRepository, never()).save(any());
            verifyNoMoreInteractions(playlistRepository);
            verifyNoInteractions(playlistDomainService);
        }

        @Test
        @DisplayName("Debe lanzar InvalidCommandException cuando el comando recibido es nulo")
        void shouldThrowInvalidCommandException_whenCommandIsNull() {
            // Act & Assert
            InvalidCommandException exception = assertThrows(
                    InvalidCommandException.class,
                    () -> useCase.execute(null)
            );

            assertEquals("El comando para actualizar el nombre de la playlist no puede ser nulo.", exception.getMessage());
            verifyNoInteractions(playlistRepository, playlistDomainService);
        }

        @Test
        @DisplayName("Debe lanzar InvalidCommandException al construir un comando con playlistId nulo")
        void shouldThrowInvalidCommandException_whenPlaylistIdIsNull() {
            // Act & Assert
            InvalidCommandException exception = assertThrows(
                    InvalidCommandException.class,
                    () -> new UpdatePlaylistNameCommand(null, "Nuevo Nombre")
            );

            assertEquals("El ID de la playlist no puede ser nulo.", exception.getMessage());
            verifyNoInteractions(playlistRepository, playlistDomainService);
        }

        @Test
        @DisplayName("Debe lanzar InvalidCommandException al construir un comando con newName nulo o en blanco")
        void shouldThrowInvalidCommandException_whenNewNameIsEmptyOrNull() {
            // Act & Assert (null)
            InvalidCommandException exNull = assertThrows(
                    InvalidCommandException.class,
                    () -> new UpdatePlaylistNameCommand(mockPlaylist.getId(), null)
            );
            assertEquals("El nuevo nombre de la playlist no puede estar vacío.", exNull.getMessage());

            // Act & Assert (blank)
            InvalidCommandException exBlank = assertThrows(
                    InvalidCommandException.class,
                    () -> new UpdatePlaylistNameCommand(mockPlaylist.getId(), "   ")
            );
            assertEquals("El nuevo nombre de la playlist no puede estar vacío.", exBlank.getMessage());

            verifyNoInteractions(playlistRepository, playlistDomainService);
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException si las dependencias del constructor son nulas")
        void shouldThrowNullPointerException_whenConstructorDependenciesAreNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new UpdatePlaylistNameUseCase(null, playlistDomainService));
            assertThrows(NullPointerException.class, () -> new UpdatePlaylistNameUseCase(playlistRepository, null));
        }
    }
}
