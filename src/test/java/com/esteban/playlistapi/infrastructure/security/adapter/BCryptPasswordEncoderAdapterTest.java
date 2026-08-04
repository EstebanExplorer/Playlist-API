package com.esteban.playlistapi.infrastructure.security.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BCryptPasswordEncoderAdapterTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    private BCryptPasswordEncoderAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BCryptPasswordEncoderAdapter(passwordEncoder);
    }

    @Nested
    @DisplayName("Pruebas de Codificación (encode)")
    class EncodeTests {

        @Test
        @DisplayName("Debe delegar la codificación al PasswordEncoder subyacente y retornar el hash")
        void shouldEncodePasswordSuccessfully() {
            // Arrange
            String rawPassword = "Password123!";
            String expectedHash = "$2a$10$abcdefghijklmnopqrstuvwxyz0123456789";
            when(passwordEncoder.encode(rawPassword)).thenReturn(expectedHash);

            // Act
            String result = adapter.encode(rawPassword);

            // Assert
            assertEquals(expectedHash, result);
            verify(passwordEncoder, times(1)).encode(rawPassword);
            verifyNoMoreInteractions(passwordEncoder);
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException al intentar codificar una contraseña nula (Fail-Fast)")
        void shouldThrowNullPointerException_whenRawPasswordIsNull() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> adapter.encode(null)
            );
            assertEquals("La contraseña en texto plano no puede ser nula para codificar.", exception.getMessage());
            verifyNoInteractions(passwordEncoder);
        }
    }

    @Nested
    @DisplayName("Pruebas de Verificación (matches)")
    class MatchesTests {

        @Test
        @DisplayName("Debe retornar true cuando la contraseña en texto plano coincide con el hash")
        void shouldReturnTrue_whenPasswordMatchesHash() {
            // Arrange
            String rawPassword = "Password123!";
            String encodedPassword = "$2a$10$abcdefghijklmnopqrstuvwxyz0123456789";
            when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

            // Act
            boolean matches = adapter.matches(rawPassword, encodedPassword);

            // Assert
            assertTrue(matches);
            verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
            verifyNoMoreInteractions(passwordEncoder);
        }

        @Test
        @DisplayName("Debe retornar false cuando la contraseña en texto plano no coincide")
        void shouldReturnFalse_whenPasswordDoesNotMatchHash() {
            // Arrange
            String wrongPassword = "WrongPassword!";
            String encodedPassword = "$2a$10$abcdefghijklmnopqrstuvwxyz0123456789";
            when(passwordEncoder.matches(wrongPassword, encodedPassword)).thenReturn(false);

            // Act
            boolean matches = adapter.matches(wrongPassword, encodedPassword);

            // Assert
            assertFalse(matches);
            verify(passwordEncoder, times(1)).matches(wrongPassword, encodedPassword);
            verifyNoMoreInteractions(passwordEncoder);
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException al verificar con parámetros nulos (Fail-Fast)")
        void shouldThrowNullPointerException_whenParametersAreNull() {
            assertThrows(NullPointerException.class, () -> adapter.matches(null, "encoded"));
            assertThrows(NullPointerException.class, () -> adapter.matches("raw", null));
            verifyNoInteractions(passwordEncoder);
        }
    }

    @Test
    @DisplayName("Debe lanzar NullPointerException al instanciar el adaptador con dependencia nula")
    void shouldThrowNullPointerException_whenConstructorDependencyIsNull() {
        assertThrows(NullPointerException.class, () -> new BCryptPasswordEncoderAdapter(null));
    }
}
