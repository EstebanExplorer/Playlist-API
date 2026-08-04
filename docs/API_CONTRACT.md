# API Contract

**Proyecto:** Playlist API  
**Documento:** API Contract  
**Versión:** 1.0  
**Estado:** Draft  
**Autor:** Edwin Esteban Henao Velásquez  
**Fecha:** 03/08/2026

---

# 1. Objetivo

Este documento define el contrato de comunicación entre los consumidores de la API y el sistema.

Describe los recursos disponibles, las operaciones soportadas, los formatos de entrada y salida, las reglas de autenticación, los códigos de respuesta y las convenciones utilizadas.

No representa una implementación específica de Spring Boot ni de ningún framework.

---

# 2. Principios del Diseño

La API seguirá los siguientes principios.

- Arquitectura REST.
- Comunicación mediante JSON.
- Stateless.
- Versionamiento mediante URI.
- Recursos identificados mediante UUID.
- Uso consistente de códigos HTTP.
- Contratos independientes de la implementación.

---

# 3. URL Base

```
/api/v1
```

---

# 4. Autenticación

Todos los endpoints, excepto la autenticación, requerirán un JWT válido.

Ejemplo:

```
Authorization: Bearer <token>
```

---

# 5. Recursos

La API expone los siguientes recursos.

- Authentication
- Users
- Playlists
- Songs
- Recommendations

---

# 6. Endpoints

---

## Authentication

### POST /auth/login

Obtiene un JWT válido.

### Request

```json
{
  "username": "string",
  "password": "string"
}
```

### Response

```json
{
  "accessToken": "jwt",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### Responses

| Código | Descripción |
|---------|-------------|
| 200 | Autenticación exitosa |
| 401 | Credenciales inválidas |

---

## Playlists

### POST /playlists

Crear playlist.

### Request

```json
{
  "name": "My Playlist"
}
```

### Response

```json
{
  "id": "uuid",
  "name": "My Playlist",
  "createdAt": "2026-08-03T10:00:00Z"
}
```

### Responses

| Código | Descripción |
|---------|-------------|
| 201 | Playlist creada |
| 400 | Datos inválidos |
| 401 | No autenticado |

---

### GET /playlists

Lista todas las playlists.

### Response

```json
[
  {
    "id": "uuid",
    "name": "Workout"
  }
]
```

### Responses

| Código | Descripción |
|---------|-------------|
| 200 | Consulta exitosa |
| 401 | No autenticado |

---

### GET /playlists/{playlistId}

Obtiene una playlist específica.

### Response

```json
{
  "id": "uuid",
  "name": "Workout",
  "songs": []
}
```

### Responses

| Código | Descripción |
|---------|-------------|
| 200 | Playlist encontrada |
| 404 | Playlist inexistente |

---

### PUT /playlists/{playlistId}

Actualiza una playlist.

### Request

```json
{
  "name": "New Playlist Name"
}
```

### Responses

| Código | Descripción |
|---------|-------------|
| 200 | Actualización exitosa |
| 400 | Datos inválidos |
| 404 | Playlist inexistente |

---

### DELETE /playlists/{playlistId}

Elimina una playlist.

### Responses

| Código | Descripción |
|---------|-------------|
| 204 | Eliminación exitosa |
| 404 | Playlist inexistente |

---

## Songs

### POST /playlists/{playlistId}/songs

Agregar canción.

### Request

```json
{
  "spotifyId": "string"
}
```

### Responses

| Código | Descripción |
|---------|-------------|
| 201 | Canción agregada |
| 404 | Playlist inexistente |

---

### DELETE /playlists/{playlistId}/songs/{songId}

Eliminar canción.

### Responses

| Código | Descripción |
|---------|-------------|
| 204 | Canción eliminada |
| 404 | Canción inexistente |

---

## Spotify

### GET /songs/search

Consulta información musical.

### Query Parameters

| Parámetro | Obligatorio |
|------------|------------|
| query | Sí |

Ejemplo:

```
GET /songs/search?query=Coldplay
```

### Response

```json
[
  {
    "spotifyId": "...",
    "title": "...",
    "artist": "...",
    "album": "..."
  }
]
```

---

## Recommendations

### POST /recommendations

Solicita recomendaciones.

### Request

```json
{
  "playlistId": "uuid"
}
```

### Response

```json
{
  "recommendations": [
    {
      "title": "...",
      "artist": "...",
      "reason": "..."
    }
  ]
}
```

---

# 7. DTO Conceptuales

## LoginRequest

- username
- password

---

## LoginResponse

- accessToken
- tokenType
- expiresIn

---

## PlaylistRequest

- name

---

## PlaylistResponse

- id
- name
- createdAt

---

## SongRequest

- spotifyId

---

## SongResponse

- id
- spotifyId
- title
- artist
- album

---

## RecommendationRequest

- playlistId

---

## RecommendationResponse

- recommendations

---

# 8. Convenciones HTTP

| Operación | Método |
|------------|--------|
| Crear | POST |
| Consultar | GET |
| Actualizar | PUT |
| Eliminar | DELETE |

---

# 9. Códigos HTTP

| Código | Significado |
|----------|------------|
| 200 | OK |
| 201 | Created |
| 204 | No Content |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict |
| 500 | Internal Server Error |
| 503 | Service Unavailable |

---

# 10. Modelo de Error

Todas las respuestas de error seguirán un formato uniforme.

```json
{
  "timestamp": "2026-08-03T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Playlist not found.",
  "path": "/api/v1/playlists/123"
}
```

---

# 11. Versionamiento

La API utilizará versionamiento mediante URI.

```
/api/v1
```

Futuras versiones podrán coexistir.

```
/api/v2
```

---

# 12. Seguridad

La seguridad será responsabilidad de Spring Security.

El contrato únicamente establece:

- uso de JWT.
- endpoints protegidos.
- respuestas de autenticación.

---

# 13. Correspondencia con Casos de Uso

| Caso de Uso | Endpoint |
|--------------|----------|
| UC-001 | POST /auth/login |
| UC-002 | POST /playlists |
| UC-003 | GET /playlists/{id} |
| UC-004 | GET /playlists |
| UC-005 | PUT /playlists/{id} |
| UC-006 | DELETE /playlists/{id} |
| UC-007 | POST /playlists/{id}/songs |
| UC-008 | DELETE /playlists/{id}/songs/{songId} |
| UC-009 | GET /songs/search |
| UC-010 | POST /recommendations |

---

# 14. Compatibilidad con OpenAPI

Este contrato servirá como base para generar la documentación OpenAPI (Swagger).

La implementación deberá mantener consistencia con este documento.

---

