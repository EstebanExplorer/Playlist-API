package com.esteban.playlistapi.infrastructure.configuration;

import com.esteban.playlistapi.application.auth.port.PasswordEncoderPort;
import com.esteban.playlistapi.application.auth.port.TokenProviderPort;
import com.esteban.playlistapi.application.auth.usecase.AuthenticateUserUseCase;
import com.esteban.playlistapi.domain.repository.UserRepository;

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
}
