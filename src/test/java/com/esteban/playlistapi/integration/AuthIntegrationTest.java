package com.esteban.playlistapi.integration;

import com.esteban.playlistapi.domain.model.User;
import com.esteban.playlistapi.domain.repository.UserRepository;
import com.esteban.playlistapi.domain.repository.MusicCatalogPort;
import com.esteban.playlistapi.domain.repository.AiRecommendationPort;
import com.esteban.playlistapi.presentation.dto.request.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MusicCatalogPort musicCatalogPort;

    @MockBean
    private AiRecommendationPort aiRecommendationPort;

    private User testUser;
    private String rawPassword;

    @BeforeEach
    void setUp() {
        rawPassword = "Password123!";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        testUser = User.create("Esteban Henao", "esteban@example.com", encodedPassword);
    }

    @Nested
    @DisplayName("Pruebas de Integración de Autenticación (UC-001)")
    class AuthenticateUserIntegrationTests {

        @Test
        @DisplayName("shouldAuthenticateSuccessfullyAndReturnJwt_whenCredentialsAreValid")
        void shouldAuthenticateSuccessfullyAndReturnJwt_whenCredentialsAreValid() throws Exception {
            // Arrange
            when(userRepository.findByEmail("esteban@example.com")).thenReturn(Optional.of(testUser));
            LoginRequest request = new LoginRequest("esteban@example.com", rawPassword);

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresInSeconds").value(86400));
        }

        @Test
        @DisplayName("shouldReturnHttp401Unauthorized_whenPasswordIsIncorrect")
        void shouldReturnHttp401Unauthorized_whenPasswordIsIncorrect() throws Exception {
            // Arrange
            when(userRepository.findByEmail("esteban@example.com")).thenReturn(Optional.of(testUser));
            LoginRequest request = new LoginRequest("esteban@example.com", "WrongPassword123!");

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("shouldReturnHttp401Unauthorized_whenUserDoesNotExist")
        void shouldReturnHttp401Unauthorized_whenUserDoesNotExist() throws Exception {
            // Arrange
            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
            LoginRequest request = new LoginRequest("nonexistent@example.com", rawPassword);

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("shouldReturnHttp400BadRequest_whenEmailIsMalformed")
        void shouldReturnHttp400BadRequest_whenEmailIsMalformed() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("malformed-email", rawPassword);

            // Act & Assert
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.validationErrors").isArray());
        }
    }
}
