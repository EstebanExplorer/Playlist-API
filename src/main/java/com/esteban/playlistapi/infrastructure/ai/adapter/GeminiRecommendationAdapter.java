package com.esteban.playlistapi.infrastructure.ai.adapter;

import com.esteban.playlistapi.domain.model.Playlist;
import com.esteban.playlistapi.domain.model.Recommendation;
import com.esteban.playlistapi.domain.model.Song;
import com.esteban.playlistapi.domain.repository.MusicCatalogPort;
import com.esteban.playlistapi.domain.repository.RecommendationEnginePort;
import com.esteban.playlistapi.infrastructure.ai.client.GeminiClient;
import com.esteban.playlistapi.infrastructure.ai.dto.GeminiRecommendationItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Adaptador de Infraestructura que implementa {@link RecommendationEnginePort}
 * utilizando Google Gemini como motor de Inteligencia Artificial.
 *
 * <p><b>Flujo de ejecución:</b>
 * <ol>
 *   <li>Extrae canciones existentes de la playlist como contexto para el prompt</li>
 *   <li>Identifica los artistas predominantes mediante conteo de frecuencia</li>
 *   <li>Busca canciones candidatas en Spotify vía {@link MusicCatalogPort}</li>
 *   <li>Construye un prompt diseñado con Prompt Engineering para minimizar alucinaciones</li>
 *   <li>Invoca {@link GeminiClient} para obtener selección y explicación en JSON</li>
 *   <li>Filtra duplicados y mapea la respuesta al modelo de dominio</li>
 * </ol>
 *
 * <p><b>Arquitectura Hexagonal:</b> Este adaptador pertenece exclusivamente a la capa
 * de Infraestructura. El Dominio y la capa de Aplicación no conocen su existencia.
 * NO es un bean Spring — lo instancia e inyecta {@code GeminiConfiguration}.
 */
public class GeminiRecommendationAdapter implements RecommendationEnginePort {

    private static final Logger log = LoggerFactory.getLogger(GeminiRecommendationAdapter.class);
    private static final int CANDIDATES_PER_ARTIST = 10;
    private static final int MAX_TOP_ARTISTS = 3;
    private static final List<String> FALLBACK_QUERIES = List.of("Top Hits", "Rock", "Pop", "Indie");

    private final GeminiClient geminiClient;
    private final MusicCatalogPort musicCatalogPort;

    public GeminiRecommendationAdapter(GeminiClient geminiClient, MusicCatalogPort musicCatalogPort) {
        this.geminiClient = Objects.requireNonNull(geminiClient, "GeminiClient no puede ser nulo.");
        this.musicCatalogPort = Objects.requireNonNull(musicCatalogPort, "MusicCatalogPort no puede ser nulo.");
    }

    @Override
    public List<Recommendation> generateRecommendations(Playlist playlist, int limit) {
        if (playlist == null) {
            log.warn("[GeminiAdapter] Playlist nula. Retornando lista vacía.");
            return List.of();
        }

        int validLimit = (limit <= 0) ? 10 : Math.min(limit, 20);
        List<Song> existingSongs = playlist.getSongs() != null ? playlist.getSongs() : List.of();

        Set<String> existingSpotifyIds = existingSongs.stream()
                .map(Song::getSpotifyId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        Set<String> existingTitlesLower = existingSongs.stream()
                .map(s -> s.getTitle().getValue().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        // Paso 1: Obtener canciones candidatas desde Spotify
        List<Song> candidates = fetchCandidates(existingSongs, existingSpotifyIds, existingTitlesLower, validLimit);

        // Paso 2: Construir prompt con contexto completo
        String prompt = buildPrompt(playlist, existingSongs, candidates, validLimit);
        log.info("[GeminiAdapter] Prompt para playlist '{}' → {} canciones existentes, {} candidatos Spotify.",
                playlist.getName().getValue(), existingSongs.size(), candidates.size());

        // Paso 3: Invocar Gemini
        List<GeminiRecommendationItem> geminiItems = geminiClient.generateRecommendations(prompt);
        log.info("[GeminiAdapter] Gemini retornó {} ítems de recomendación.", geminiItems.size());

        // Paso 4: Mapear a dominio con filtrado de duplicados
        return mapToDomainRecommendations(geminiItems, existingTitlesLower, validLimit);
    }

    // ─── Candidatos desde Spotify ─────────────────────────────────────────────

    private List<Song> fetchCandidates(List<Song> existingSongs,
                                       Set<String> existingSpotifyIds,
                                       Set<String> existingTitlesLower,
                                       int limit) {
        List<String> topArtists = findTopArtists(existingSongs);
        List<Song> candidates = new ArrayList<>();
        Set<String> addedKeys = new HashSet<>();

        for (String artist : topArtists) {
            if (candidates.size() >= limit * 3) break;
            try {
                List<Song> found = musicCatalogPort.searchSongs(artist, CANDIDATES_PER_ARTIST);
                addUniqueCandidates(found, candidates, addedKeys, existingSpotifyIds, existingTitlesLower);
            } catch (Exception e) {
                log.warn("[GeminiAdapter] Error buscando canciones del artista '{}' en Spotify: {}", artist, e.getMessage());
            }
        }

        if (candidates.size() < limit) {
            for (String query : FALLBACK_QUERIES) {
                if (candidates.size() >= limit * 2) break;
                try {
                    List<Song> found = musicCatalogPort.searchSongs(query, CANDIDATES_PER_ARTIST);
                    addUniqueCandidates(found, candidates, addedKeys, existingSpotifyIds, existingTitlesLower);
                } catch (Exception e) {
                    log.warn("[GeminiAdapter] Error buscando candidatos fallback '{}': {}", query, e.getMessage());
                }
            }
        }

        return Collections.unmodifiableList(candidates);
    }

    private void addUniqueCandidates(List<Song> source, List<Song> destination,
                                     Set<String> addedKeys,
                                     Set<String> existingSpotifyIds, Set<String> existingTitlesLower) {
        for (Song song : source) {
            if (isAlreadyInPlaylist(song, existingSpotifyIds, existingTitlesLower)) continue;
            String key = song.getArtist().getValue().toLowerCase(Locale.ROOT)
                    + ":" + song.getTitle().getValue().toLowerCase(Locale.ROOT);
            if (addedKeys.add(key)) destination.add(song);
        }
    }

    private boolean isAlreadyInPlaylist(Song song, Set<String> existingIds, Set<String> existingTitles) {
        if (song == null) return true;
        if (song.getSpotifyId() != null && !song.getSpotifyId().isBlank()
                && existingIds.contains(song.getSpotifyId())) return true;
        return song.getTitle() != null
                && existingTitles.contains(song.getTitle().getValue().toLowerCase(Locale.ROOT));
    }

    private List<String> findTopArtists(List<Song> songs) {
        if (songs == null || songs.isEmpty()) return List.of();
        return songs.stream()
                .filter(s -> s.getArtist() != null && s.getArtist().getValue() != null)
                .collect(Collectors.groupingBy(s -> s.getArtist().getValue(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(MAX_TOP_ARTISTS)
                .toList();
    }

    // ─── Prompt Engineering ───────────────────────────────────────────────────

    /**
     * Construye el prompt enviado a Gemini.
     *
     * <p>Principios de Prompt Engineering aplicados:
     * <ul>
     *   <li>Rol explícito: "music recommendation expert AI"</li>
     *   <li>Contexto completo: playlist name, existing songs, candidate list</li>
     *   <li>Anti-alucinación: "ONLY from CANDIDATE SONGS — NEVER invent"</li>
     *   <li>Límite de palabras en reason: máximo 25 palabras</li>
     *   <li>Formato JSON estricto: responseMimeType=application/json + instrucción explícita</li>
     *   <li>Ejemplo de JSON embebido en el prompt para guiar el formato</li>
     *   <li>Caso de lista vacía manejado explícitamente</li>
     * </ul>
     */
    private String buildPrompt(Playlist playlist, List<Song> existingSongs,
                               List<Song> candidates, int limit) {
        String playlistName = playlist.getName().getValue();
        String existingList = formatSongList(existingSongs, "  (empty playlist — no songs yet)");
        String candidateList = formatSongList(candidates, "  (no candidates available from catalog)");

        return """
                You are a music recommendation expert AI assistant.

                PLAYLIST NAME: "%s"

                SONGS CURRENTLY IN THE PLAYLIST:
                %s

                CANDIDATE SONGS (sourced exclusively from Spotify catalog search):
                %s

                STRICT RULES — FOLLOW EXACTLY:
                1. Select EXACTLY %d songs from the CANDIDATE SONGS list above.
                2. ONLY recommend songs that appear in the CANDIDATE SONGS list.
                3. NEVER invent, hallucinate, or include songs not present in the CANDIDATE SONGS list.
                4. NEVER recommend songs already in the playlist.
                5. The "reason" field must be concise — MAXIMUM 25 WORDS.
                6. If fewer than %d good matches exist, recommend only the good ones.
                7. If no good matches exist, return an empty recommendations array.

                ANALYSIS:
                - Analyze the musical style, genre, mood, tempo, and era of the existing playlist songs.
                - Select candidates that best complement the playlist's style and mood.

                MANDATORY OUTPUT — respond with ONLY this JSON, no markdown, no extra text:
                {"recommendations":[{"title":"exact title from candidates","artist":"exact artist from candidates","reason":"max 25 words explaining the fit"}]}

                Empty result: {"recommendations":[]}
                """.formatted(playlistName, existingList, candidateList, limit, limit);
    }

    private String formatSongList(List<Song> songs, String emptyMessage) {
        if (songs == null || songs.isEmpty()) return emptyMessage;
        return songs.stream()
                .map(s -> "  - \"" + s.getTitle().getValue() + "\" by " + s.getArtist().getValue())
                .collect(Collectors.joining("\n"));
    }

    // ─── Mapeo a dominio ──────────────────────────────────────────────────────

    private List<Recommendation> mapToDomainRecommendations(List<GeminiRecommendationItem> items,
                                                             Set<String> existingTitlesLower,
                                                             int limit) {
        if (items == null || items.isEmpty()) return List.of();

        List<Recommendation> result = new ArrayList<>();
        Set<String> addedKeys = new HashSet<>();

        for (int i = 0; i < items.size() && result.size() < limit; i++) {
            GeminiRecommendationItem item = items.get(i);
            if (!item.isValid()) continue;

            String titleLower = item.title().toLowerCase(Locale.ROOT);
            if (existingTitlesLower.contains(titleLower)) continue;

            String key = item.artist().toLowerCase(Locale.ROOT) + ":" + titleLower;
            if (!addedKeys.add(key)) continue;

            // Score decreciente por posición: 1° = 0.97, 2° = 0.94, ... mínimo 0.70
            double score = Math.max(0.70, 0.97 - (i * 0.03));
            String reason = (item.reason() != null && !item.reason().isBlank())
                    ? item.reason()
                    : "AI-selected based on playlist style and genre compatibility.";

            result.add(Recommendation.create(item.title(), item.artist(), reason, score));
        }

        return Collections.unmodifiableList(result);
    }
}
