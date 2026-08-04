package com.esteban.playlistapi.infrastructure.persistence.mapper;

import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Song;
import com.esteban.playlistapi.domain.valueobject.AlbumName;
import com.esteban.playlistapi.domain.valueobject.ArtistName;
import com.esteban.playlistapi.domain.valueobject.Duration;
import com.esteban.playlistapi.domain.valueobject.PlaylistName;
import com.esteban.playlistapi.domain.valueobject.SongTitle;
import com.esteban.playlistapi.infrastructure.persistence.entity.PlaylistJpaEntity;
import com.esteban.playlistapi.infrastructure.persistence.entity.SongJpaEntity;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Mapper manual de persistencia encargado de la conversión bidireccional entre
 * el Agregado Raíz de Dominio Playlist y la Entidad relacional PlaylistJpaEntity.
 * Pertenece exclusivamente a la capa Infrastructure.
 */
@Component
public class PlaylistPersistenceMapper {

    public PlaylistJpaEntity toEntity(Playlist domain) {
        if (domain == null) {
            return null;
        }

        PlaylistJpaEntity entity = new PlaylistJpaEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName().getValue());
        entity.setUserId(domain.getUserId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        List<SongJpaEntity> songEntities = new ArrayList<>();
        if (domain.getSongs() != null) {
            for (Song song : domain.getSongs()) {
                SongJpaEntity songEntity = new SongJpaEntity(
                        song.getId(),
                        song.getSpotifyId(),
                        song.getTitle().getValue(),
                        song.getArtist().getValue(),
                        song.getAlbum().getValue(),
                        song.getDuration().getSeconds(),
                        entity
                );
                songEntities.add(songEntity);
            }
        }
        entity.setSongs(songEntities);

        return entity;
    }

    public Playlist toDomain(PlaylistJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        List<Song> domainSongs = new ArrayList<>();
        if (entity.getSongs() != null) {
            for (SongJpaEntity songEntity : entity.getSongs()) {
                Song song = new Song(
                        songEntity.getId(),
                        songEntity.getSpotifyId(),
                        SongTitle.of(songEntity.getTitle()),
                        ArtistName.of(songEntity.getArtist()),
                        AlbumName.of(songEntity.getAlbum()),
                        Duration.ofSeconds(songEntity.getDurationSeconds())
                );
                domainSongs.add(song);
            }
        }

        return new Playlist(
                entity.getId(),
                PlaylistName.of(entity.getName()),
                entity.getUserId(),
                domainSongs,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
