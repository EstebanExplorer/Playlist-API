package com.esteban.playlistapi.infrastructure.ai.client;

import com.esteban.playlistapi.domain.exception.ExternalServiceException;
import com.esteban.playlistapi.infrastructure.ai.config.GeminiProperties;
import com.esteban.playlistapi.infrastructure.ai.dto.GeminiRecommendationItem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GeminiClient — Pruebas Unitarias")
class GeminiClientTest {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    private GeminiProperties properties;
    private GeminiClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        when(restClientBuilder.build()).thenReturn(restClient);
        properties = buildTestProperties();
        client = new GeminiClient(restClientBuilder, properties, objectMapper);
    }

    // ─── Construcción / Fail-Fast ─────────────────────────────────────────────

    @Nested
    @DisplayName("Constructor — Validaciones Fail-Fast")
    class ConstructorTests {

        @Test
        @DisplayName("Debe lanzar NullPointerException si GeminiProperties es nulo")
        void shouldThrow_whenPropertiesIsNull() {
            assertThrows(NullPointerException.class,
                    () -> new GeminiClient(restClientBuilder, null, objectMapper));
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException si RestClient.Builder es nulo")
        void shouldThrow_whenBuilderIsNull() {
            assertThrows(NullPointerException.class,
                    () -> new GeminiClient(null, properties, objectMapper));
        }

        @Test
        @DisplayName("Debe lanzar NullPointerException si ObjectMapper es nulo")
        void shouldThrow_whenObjectMapperIsNull() {
            assertThrows(NullPointerException.class,
                    () -> new GeminiClient(restClientBuilder, properties, null));
        }
    }

    // ─── parseRecommendationsJson ─────────────────────────────────────────────

    @Nested
    @DisplayName("parseRecommendationsJson — Parseo JSON de respuesta Gemini")
    class ParseRecommendationsJsonTests {

        @Test
        @DisplayName("Debe parsear JSON válido y retornar lista de ítems")
        void shouldParseValidJson_andReturnItems() {
            String json = """
                    {"recommendations":[
                        {"title":"Bohemian Rhapsody","artist":"Queen","reason":"Classic rock anthem"},
                        {"title":"Stairway to Heaven","artist":"Led Zeppelin","reason":"Epic guitar work"}
                    ]}""";

            List<GeminiRecommendationItem> result = client.parseRecommendationsJson(json);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Bohemian Rhapsody", result.get(0).title());
            assertEquals("Queen", result.get(0).artist());
            assertEquals("Classic rock anthem", result.get(0).reason());
        }

        @Test
        @DisplayName("Debe limpiar bloque markdown ```json y parsear correctamente")
        void shouldStripMarkdownCodeBlock_andParse() {
            String jsonWithMarkdown = """
                    ```json
                    {"recommendations":[{"title":"Kashmir","artist":"Led Zeppelin","reason":"Epic riff"}]}
                    ```""";

            List<GeminiRecommendationItem> result = client.parseRecommendationsJson(jsonWithMarkdown);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Kashmir", result.get(0).title());
        }

        @Test
        @DisplayName("Debe limpiar bloque markdown ``` y parsear correctamente")
        void shouldStripPlainMarkdownBlock_andParse() {
            String json = "```\n{\"recommendations\":[{\"title\":\"T\",\"artist\":\"A\",\"reason\":\"R\"}]}\n```";

            List<GeminiRecommendationItem> result = client.parseRecommendationsJson(json);

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Debe retornar lista vacía para JSON nulo")
        void shouldReturnEmptyList_forNullInput() {
            List<GeminiRecommendationItem> result = client.parseRecommendationsJson(null);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Debe retornar lista vacía para cadena en blanco")
        void shouldReturnEmptyList_forBlankInput() {
            List<GeminiRecommendationItem> result = client.parseRecommendationsJson("   ");
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Debe retornar lista vacía y no lanzar excepción para JSON malformado")
        void shouldReturnEmptyList_forMalformedJson() {
            List<GeminiRecommendationItem> result = client.parseRecommendationsJson("{invalid json}");
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Debe retornar lista vacía para recommendations array vacío")
        void shouldReturnEmptyList_forEmptyRecommendationsArray() {
            List<GeminiRecommendationItem> result = client.parseRecommendationsJson("{\"recommendations\":[]}");
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Debe filtrar ítems con título nulo o vacío")
        void shouldFilterInvalidItems_withNullTitle() {
            String json = """
                    {"recommendations":[
                        {"title":"","artist":"Artist","reason":"reason"},
                        {"title":"Valid Title","artist":"Valid Artist","reason":"reason"}
                    ]}""";

            List<GeminiRecommendationItem> result = client.parseRecommendationsJson(json);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Valid Title", result.get(0).title());
        }

        @Test
        @DisplayName("Debe manejar JSON con campo 'recommendations' nulo")
        void shouldReturnEmptyList_whenRecommendationsFieldIsNull() {
            List<GeminiRecommendationItem> result = client.parseRecommendationsJson("{\"recommendations\":null}");
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Debe manejar prompt nulo en generateRecommendations")
        void shouldReturnEmptyList_whenPromptIsNull() {
            List<GeminiRecommendationItem> result = client.generateRecommendations(null);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Debe manejar prompt en blanco en generateRecommendations")
        void shouldReturnEmptyList_whenPromptIsBlank() {
            List<GeminiRecommendationItem> result = client.generateRecommendations("   ");
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private GeminiProperties buildTestProperties() {
        GeminiProperties props = new GeminiProperties();
        props.setApiKey("test-api-key-1234567890");
        props.setModel("gemini-2.5-flash");
        props.setApiUrl("https://generativelanguage.googleapis.com/v1beta/models");
        props.setTimeout(5);
        props.setMaxRetries(1);
        return props;
    }
}
