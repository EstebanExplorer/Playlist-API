package com.esteban.playlistapi.presentation.advice;

import com.esteban.playlistapi.application.auth.exception.InvalidCredentialsException;
import com.esteban.playlistapi.domain.exception.DuplicateSongException;
import com.esteban.playlistapi.domain.exception.PlaylistNotFoundException;
import com.esteban.playlistapi.presentation.dto.request.LoginRequest;
import com.esteban.playlistapi.shared.exception.InvalidCommandException;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @RestController
    static class TestController {

        @GetMapping("/test/invalid-credentials")
        public void throwInvalidCredentials() {
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        @GetMapping("/test/playlist-not-found")
        public void throwPlaylistNotFound() {
            throw new PlaylistNotFoundException(UUID.randomUUID());
        }

        @GetMapping("/test/duplicate-song")
        public void throwDuplicateSong() {
            throw new DuplicateSongException("La canción ya se encuentra en la playlist");
        }

        @GetMapping("/test/invalid-command")
        public void throwInvalidCommand() {
            throw new InvalidCommandException("Command malformado");
        }

        @PostMapping("/test/validation")
        public void throwValidation(@Valid @RequestBody LoginRequest request) {
            // Se activa Bean Validation si request es inválido
        }

        @GetMapping("/test/unhandled")
        public void throwUnhandled() {
            throw new NullPointerException("NullPointer inesperado en lógica interna");
        }
    }

    @Nested
    @DisplayName("Pruebas de Mapeo de Excepciones a HTTP Status")
    class ExceptionMappingTests {

        @Test
        @DisplayName("Debe capturar InvalidCredentialsException y retornar HTTP 401 Unauthorized")
        void shouldHandleInvalidCredentials_andReturnHttp401() throws Exception {
            mockMvc.perform(get("/test/invalid-credentials"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.message").value("Credenciales inválidas"))
                    .andExpect(jsonPath("$.path").value("/test/invalid-credentials"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Debe capturar PlaylistNotFoundException y retornar HTTP 404 Not Found")
        void shouldHandlePlaylistNotFound_andReturnHttp404() throws Exception {
            mockMvc.perform(get("/test/playlist-not-found"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.path").value("/test/playlist-not-found"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Debe capturar DuplicateSongException y retornar HTTP 409 Conflict")
        void shouldHandleDuplicateSong_andReturnHttp409() throws Exception {
            mockMvc.perform(get("/test/duplicate-song"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value("La canción 'La canción ya se encuentra en la playlist' ya existe en esta playlist."))
                    .andExpect(jsonPath("$.path").value("/test/duplicate-song"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Debe capturar InvalidCommandException y retornar HTTP 400 Bad Request")
        void shouldHandleInvalidCommand_andReturnHttp400() throws Exception {
            mockMvc.perform(get("/test/invalid-command"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Command malformado"))
                    .andExpect(jsonPath("$.path").value("/test/invalid-command"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Debe capturar MethodArgumentNotValidException y retornar HTTP 400 con lista de validationErrors")
        void shouldHandleMethodArgumentNotValid_andReturnHttp400WithFieldErrors() throws Exception {
            LoginRequest invalidRequest = new LoginRequest("email-invalido", "");

            mockMvc.perform(post("/test/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.validationErrors").isArray())
                    .andExpect(jsonPath("$.validationErrors").isNotEmpty());
        }

        @Test
        @DisplayName("Debe capturar excepciones no controladas y retornar HTTP 500 sin exponer detalles de implementación interna (OWASP ASVS)")
        void shouldNotExposeInternalImplementation_whenUnhandledExceptionOccurs() throws Exception {
            mockMvc.perform(get("/test/unhandled"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").value("Ocurrió un error interno en el servidor. Por favor consulte al administrador."))
                    .andExpect(jsonPath("$.path").value("/test/unhandled"))
                    .andExpect(jsonPath("$.timestamp").exists())

                    // Verificación explícita de seguridad OWASP ASVS: 0 exposición de stacktrace o campos internos
                    .andExpect(jsonPath("$.stackTrace").doesNotExist())
                    .andExpect(jsonPath("$.trace").doesNotExist())
                    .andExpect(jsonPath("$.exception").doesNotExist())
                    .andExpect(jsonPath("$.cause").doesNotExist())
                    .andExpect(jsonPath("$.localizedMessage").doesNotExist());
        }
    }
}
