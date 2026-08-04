package com.esteban.playlistapi.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = OpenApiConfiguration.class)
class OpenApiConfigurationTest {

    @Autowired
    private OpenAPI openAPI;

    @Test
    @DisplayName("Debe cargar el Bean OpenAPI con metadatos y esquema de seguridad bearerAuth")
    void shouldLoadOpenApiBeanWithMetadataAndSecurityScheme() {
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("Playlist API", openAPI.getInfo().getTitle());
        assertEquals("v1.0.0", openAPI.getInfo().getVersion());
        assertEquals("Edwin Esteban Henao", openAPI.getInfo().getContact().getName());

        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey(OpenApiConfiguration.SECURITY_SCHEME_NAME));

        var securityScheme = openAPI.getComponents().getSecuritySchemes().get(OpenApiConfiguration.SECURITY_SCHEME_NAME);
        assertEquals("bearer", securityScheme.getScheme());
        assertEquals("JWT", securityScheme.getBearerFormat());
    }
}
