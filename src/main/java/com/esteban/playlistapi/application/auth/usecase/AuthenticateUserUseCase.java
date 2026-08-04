package com.esteban.playlistapi.application.auth.usecase;

import com.esteban.playlistapi.application.auth.dto.AuthenticateUserCommand;
import com.esteban.playlistapi.application.auth.dto.AuthenticationResult;
import com.esteban.playlistapi.application.auth.exception.InvalidCredentialsException;
import com.esteban.playlistapi.application.auth.port.PasswordEncoderPort;
import com.esteban.playlistapi.application.auth.port.TokenProviderPort;
import com.esteban.playlistapi.domain.model.User;
import com.esteban.playlistapi.domain.repository.UserRepository;

import java.util.Objects;

/**
 * Caso de Uso UC-001: Autenticar Usuario.
 * Orquesta la validación de credenciales de usuario y la generación del token JWT
 * utilizando los puertos definidos de persistencia, encriptado y tokens.
 */
public class AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenProviderPort tokenProviderPort;

    public AuthenticateUserUseCase(
            UserRepository userRepository,
            PasswordEncoderPort passwordEncoderPort,
            TokenProviderPort tokenProviderPort) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository no puede ser nulo.");
        this.passwordEncoderPort = Objects.requireNonNull(passwordEncoderPort, "PasswordEncoderPort no puede ser nulo.");
        this.tokenProviderPort = Objects.requireNonNull(tokenProviderPort, "TokenProviderPort no puede ser nulo.");
    }

    public AuthenticationResult execute(AuthenticateUserCommand command) {
        Objects.requireNonNull(command, "El comando de autenticación no puede ser nulo.");

        User user = userRepository.findByUsername(command.username())
                .or(() -> userRepository.findByEmail(command.username()))
                .orElseThrow(InvalidCredentialsException::new);

        boolean passwordMatches = passwordEncoderPort.matches(command.password(), user.getPassword());
        if (!passwordMatches) {
            throw new InvalidCredentialsException();
        }

        String token = tokenProviderPort.generateToken(user);
        long expirationSeconds = tokenProviderPort.getExpirationTimeSeconds();

        return AuthenticationResult.bearer(token, expirationSeconds);
    }
}
