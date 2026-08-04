package com.esteban.playlistapi.infrastructure.persistence.adapter;

import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Song;
import com.esteban.playlistapi.infrastructure.persistence.entity.PlaylistJpaEntity;
import com.esteban.playlistapi.infrastructure.persistence.mapper.PlaylistPersistenceMapper;
import com.esteban.playlistapi.infrastructure.persistence.repository.SpringDataJpaPlaylistRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaPlaylistRepositoryAdapterTest {

    @Mock
    private SpringDataJpaPlaylistRepository jpaRepository;

    @Spy
    private PlaylistPersistenceMapper mapper;

    private JpaPlaylistRepositoryAdapter adapter;

    private final UUID userId = UUID.randomUUID();
    private Playlist mockPlaylist;
    private Song mockSong;

    @BeforeEach
    void setUp() {
        adapter = new JpaPlaylistRepositoryAdapter(jpaRepository, mapper);
        mockPlaylist = Playlist.create("Rock Clásico", userId);
        mockSong = Song.create("spotify-001", "Stairway to Heaven", "Led Zeppelin", "Led Zeppelin IV", 482);
    }

    @Nested
    @DisplayName("Pruebas de Guardado (save)")
    class SaveTests {

        @Test
        @DisplayName("Debe mapear a entidad, guardar en repositorio JPA y mapear a dominio en el orden exacto (InOrder)")
        void shouldSaveAndReturnDomainPlaylistInExactOrder() {
            // Arrange
            mockPlaylist.addSong(mockSong);
            PlaylistJpaEntity entityToSave = mapper.toEntity(mockPlaylist);
            when(jpaRepository.save(any(PlaylistJpaEntity.class))).thenReturn(entityToSave);

            // Act
            Playlist savedPlaylist = adapter.save(mockPlaylist);

            // Assert
            assertNotNull(savedPlaylist);
            assertEquals(mockPlaylist.getId(), savedPlaylist.getId());
            assertEquals("Rock Clásico", savedPlaylist.getName().getValue());
            assertEquals(1, savedPlaylist.getSongs().size());

            // Verificación del orden exacto con InOrder
            InOrder inOrder = inOrder(mapper, jpaRepository);
            inOrder.verify(mapper).toEntity(mockPlaylist);
            inOrder.verify(jpaRepository).save(any(PlaylistJpaEntity.class));
            inOrder.verify(mapper).toDomain(any(PlaylistJpaEntity.class));
            inOrder.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException al guardar un Agregado nulo (Fail-Fast)")
        void shouldThrowNullPointerException_whenSaveNullPlaylist() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> adapter.save(null)
            );
            assertEquals("El Agregado Playlist no puede ser nulo para persistir.", exception.getMessage());
            verifyNoInteractions(jpaRepository);
        }
    }

    @Nested
    @DisplayName("Pruebas de Búsqueda por ID (findById)")
    class FindByIdTests {

        @Test
        @DisplayName("Debe retornar Optional con el Agregado cuando existe en base de datos")
        void shouldReturnOptionalPlaylist_whenEntityExists() {
            // Arrange
            PlaylistJpaEntity entity = mapper.toEntity(mockPlaylist);
            when(jpaRepository.findById(mockPlaylist.getId())).thenReturn(Optional.of(entity));

            // Act
            Optional<Playlist> result = adapter.findById(mockPlaylist.getId());

            // Assert
            assertTrue(result.isPresent());
            assertEquals(mockPlaylist.getId(), result.get().getId());
            assertEquals("Rock Clásico", result.get().getName().getValue());

            verify(jpaRepository, times(1)).findById(mockPlaylist.getId());
            verifyNoMoreInteractions(jpaRepository);
        }

        @Test
        @DisplayName("Debe retornar Optional.empty cuando no existe en base de datos")
        void shouldReturnEmptyOptional_whenEntityDoesNotExist() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(jpaRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act
            Optional<Playlist> result = adapter.findById(nonExistentId);

            // Assert
            assertTrue(result.isEmpty());
            verify(jpaRepository, times(1)).findById(nonExistentId);
            verifyNoMoreInteractions(jpaRepository);
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException al consultar un ID nulo (Fail-Fast)")
        void shouldThrowNullPointerException_whenIdIsNull() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> adapter.findById(null)
            );
            assertEquals("El ID de la playlist no puede ser nulo para consultar.", exception.getMessage());
            verifyNoInteractions(jpaRepository);
        }
    }

    @Nested
    @DisplayName("Pruebas de Búsqueda por Usuario (findByUserId)")
    class FindByUserIdTests {

        @Test
        @DisplayName("Debe retornar lista de Agregados mapeados para el usuario")
        void shouldReturnListOfPlaylists_whenUserHasPlaylists() {
            // Arrange
            PlaylistJpaEntity entity = mapper.toEntity(mockPlaylist);
            when(jpaRepository.findByUserId(userId)).thenReturn(List.of(entity));

            // Act
            List<Playlist> results = adapter.findByUserId(userId);

            // Assert
            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals(mockPlaylist.getId(), results.get(0).getId());

            verify(jpaRepository, times(1)).findByUserId(userId);
            verifyNoMoreInteractions(jpaRepository);
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException al consultar con userId nulo (Fail-Fast)")
        void shouldThrowNullPointerException_whenUserIdIsNull() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> adapter.findByUserId(null)
            );
            assertEquals("El ID del usuario no puede ser nulo para consultar sus playlists.", exception.getMessage());
            verifyNoInteractions(jpaRepository);
        }
    }

    @Nested
    @DisplayName("Pruebas de Eliminación (delete / deleteById)")
    class DeleteTests {

        @Test
        @DisplayName("Debe mapear la entidad y delegar jpaRepository.delete(entity) en el orden exacto")
        void shouldMapToEntityAndDelete_whenDeletePlaylistIsInvoked() {
            // Arrange
            doNothing().when(jpaRepository).delete(any(PlaylistJpaEntity.class));

            // Act
            adapter.delete(mockPlaylist);

            // Assert
            InOrder inOrder = inOrder(mapper, jpaRepository);
            inOrder.verify(mapper).toEntity(mockPlaylist);
            inOrder.verify(jpaRepository).delete(any(PlaylistJpaEntity.class));
            inOrder.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("Debe delegar la eliminación directa por ID a Spring Data JPA")
        void shouldDelegateDeleteByIdToJpaRepository() {
            // Arrange
            UUID idToDelete = mockPlaylist.getId();
            doNothing().when(jpaRepository).deleteById(idToDelete);

            // Act
            adapter.deleteById(idToDelete);

            // Assert
            verify(jpaRepository, times(1)).deleteById(idToDelete);
            verifyNoMoreInteractions(jpaRepository);
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException al intentar eliminar con parámetros nulos (Fail-Fast)")
        void shouldThrowNullPointerException_whenDeleteParametersAreNull() {
            assertThrows(NullPointerException.class, () -> adapter.delete(null));
            assertThrows(NullPointerException.class, () -> adapter.deleteById(null));
            verifyNoInteractions(jpaRepository);
        }
    }

    @Nested
    @DisplayName("Pruebas de Comprobación de Existencia (existsById)")
    class ExistsByIdTests {

        @Test
        @DisplayName("Debe retornar true si Spring Data indica que el ID existe")
        void shouldReturnTrue_whenEntityExists() {
            // Arrange
            when(jpaRepository.existsById(mockPlaylist.getId())).thenReturn(true);

            // Act
            boolean exists = adapter.existsById(mockPlaylist.getId());

            // Assert
            assertTrue(exists);
            verify(jpaRepository, times(1)).existsById(mockPlaylist.getId());
            verifyNoMoreInteractions(jpaRepository);
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException si el ID es nulo (Fail-Fast)")
        void shouldThrowNullPointerException_whenIdIsNull() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> adapter.existsById(null)
            );
            assertEquals("El ID de la playlist no puede ser nulo para verificar su existencia.", exception.getMessage());
            verifyNoInteractions(jpaRepository);
        }
    }
}
