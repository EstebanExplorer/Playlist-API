package com.esteban.playlistapi.infrastructure.configuration;

import com.esteban.playlistapi.application.auth.port.PasswordEncoderPort;
import com.esteban.playlistapi.application.auth.port.TokenProviderPort;
import com.esteban.playlistapi.application.auth.usecase.AuthenticateUserUseCase;
import com.esteban.playlistapi.application.playlist.usecase.CreatePlaylistUseCase;
import com.esteban.playlistapi.application.playlist.usecase.DeletePlaylistUseCase;
import com.esteban.playlistapi.application.playlist.usecase.GetPlaylistByIdUseCase;
import com.esteban.playlistapi.application.playlist.usecase.ListUserPlaylistsUseCase;
import com.esteban.playlistapi.application.playlist.usecase.UpdatePlaylistNameUseCase;
import com.esteban.playlistapi.application.song.usecase.AddSongToPlaylistUseCase;
import com.esteban.playlistapi.application.song.usecase.RemoveSongFromPlaylistUseCase;
import com.esteban.playlistapi.application.song.usecase.SearchSongsUseCase;
import com.esteban.playlistapi.application.recommendation.usecase.GenerateRecommendationUseCase;
import com.esteban.playlistapi.domain.repository.AiRecommendationPort;
import com.esteban.playlistapi.domain.repository.MusicCatalogPort;
import com.esteban.playlistapi.domain.repository.PlaylistRepository;
import com.esteban.playlistapi.domain.repository.UserRepository;
import com.esteban.playlistapi.domain.service.PlaylistDomainService;
import com.esteban.playlistapi.domain.service.RecommendationDomainService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración del IoC Container de Spring para registrar los Beans de la capa Application.
 * Permite que los Use Cases del núcleo sean inyectados sin acoplarlos con anotaciones @Service.
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public AuthenticateUserUseCase authenticateUserUseCase(
            UserRepository userRepository,
            PasswordEncoderPort passwordEncoderPort,
            TokenProviderPort tokenProviderPort) {
        return new AuthenticateUserUseCase(userRepository, passwordEncoderPort, tokenProviderPort);
    }

    @Bean
    public CreatePlaylistUseCase createPlaylistUseCase(PlaylistRepository playlistRepository) {
        return new CreatePlaylistUseCase(playlistRepository);
    }

    @Bean
    public GetPlaylistByIdUseCase getPlaylistByIdUseCase(PlaylistRepository playlistRepository) {
        return new GetPlaylistByIdUseCase(playlistRepository);
    }

    @Bean
    public ListUserPlaylistsUseCase listUserPlaylistsUseCase(PlaylistRepository playlistRepository) {
        return new ListUserPlaylistsUseCase(playlistRepository);
    }

    @Bean
    public UpdatePlaylistNameUseCase updatePlaylistNameUseCase(PlaylistRepository playlistRepository) {
        return new UpdatePlaylistNameUseCase(playlistRepository);
    }

    @Bean
    public DeletePlaylistUseCase deletePlaylistUseCase(PlaylistRepository playlistRepository) {
        return new DeletePlaylistUseCase(playlistRepository);
    }

    @Bean
    public AddSongToPlaylistUseCase addSongToPlaylistUseCase(
            PlaylistRepository playlistRepository,
            MusicCatalogPort musicCatalogPort,
            PlaylistDomainService playlistDomainService) {
        return new AddSongToPlaylistUseCase(playlistRepository, musicCatalogPort, playlistDomainService);
    }

    @Bean
    public RemoveSongFromPlaylistUseCase removeSongFromPlaylistUseCase(PlaylistRepository playlistRepository) {
        return new RemoveSongFromPlaylistUseCase(playlistRepository);
    }

    @Bean
    public SearchSongsUseCase searchSongsUseCase(MusicCatalogPort musicCatalogPort) {
        return new SearchSongsUseCase(musicCatalogPort);
    }

    @Bean
    public GenerateRecommendationUseCase generateRecommendationUseCase(
            PlaylistRepository playlistRepository,
            AiRecommendationPort aiRecommendationPort,
            RecommendationDomainService recommendationDomainService) {
        return new GenerateRecommendationUseCase(playlistRepository, aiRecommendationPort, recommendationDomainService);
    }

    @Bean
    public PlaylistDomainService playlistDomainService() {
        return new PlaylistDomainService();
    }

    @Bean
    public RecommendationDomainService recommendationDomainService() {
        return new RecommendationDomainService();
    }
}
