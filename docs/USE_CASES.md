# Use Cases

**Proyecto:** Playlist API  
**Documento:** Use Cases  
**Versión:** 1.0  
**Estado:** Draft  
**Autor:** Edwin Esteban Henao Velásquez  
**Fecha:** 03/08/2026

---

# 1. Objetivo

Este documento describe los casos de uso identificados para la solución, definiendo la interacción entre los actores y el sistema.

Los casos de uso representan el comportamiento esperado desde la perspectiva del usuario y constituyen la base para el diseño de la capa de aplicación, la API REST y las pruebas funcionales.

---

# 2. Actores

## Usuario Autenticado

Actor principal del sistema.

Responsabilidades:

- Administrar playlists.
- Gestionar canciones.
- Consultar información musical.
- Solicitar recomendaciones.

---

## Spotify API

Actor secundario.

Proporciona información musical requerida por el sistema.

---

## Proveedor de Inteligencia Artificial

Actor secundario.

Genera recomendaciones musicales bajo solicitud del sistema.

---

# 3. Diagrama Conceptual

```text
                Usuario
                    │
        ┌───────────┼────────────┐
        │           │            │
        ▼           ▼            ▼
 Crear Playlist  Gestionar   Solicitar
                 Canciones  Recomendación
                    │
                    ▼
              Consultar Spotify
```

---

# 4. Catálogo de Casos de Uso

| Código | Caso de Uso | Actor |
|---------|-------------|-------|
| UC-001 | Autenticar Usuario | Usuario |
| UC-002 | Crear Playlist | Usuario |
| UC-003 | Consultar Playlist | Usuario |
| UC-004 | Listar Playlists | Usuario |
| UC-005 | Actualizar Playlist | Usuario |
| UC-006 | Eliminar Playlist | Usuario |
| UC-007 | Agregar Canción | Usuario |
| UC-008 | Eliminar Canción | Usuario |
| UC-009 | Consultar Información Musical | Usuario |
| UC-010 | Obtener Recomendaciones | Usuario |

---

# UC-001 — Autenticar Usuario

## Objetivo

Permitir que un usuario obtenga un token JWT válido.

### Actor Principal

Usuario.

### Precondiciones

- El usuario existe.
- Las credenciales son válidas.

### Flujo Principal

1. El usuario envía sus credenciales.
2. El sistema valida la autenticación.
3. El sistema genera un JWT.
4. El sistema devuelve el token.

### Flujo Alternativo

- Credenciales inválidas.
- Usuario inexistente.

### Postcondiciones

El usuario queda autenticado para consumir la API.

---

# UC-002 — Crear Playlist

## Objetivo

Crear una nueva playlist.

### Actor Principal

Usuario autenticado.

### Precondiciones

- Usuario autenticado.
- Nombre válido para la playlist.

### Flujo Principal

1. El usuario solicita crear una playlist.
2. El sistema valida los datos.
3. El sistema crea la playlist.
4. El sistema almacena la información.
5. El sistema devuelve la playlist creada.

### Flujo Alternativo

- Nombre inválido.
- Error de persistencia.

### Postcondiciones

Existe una nueva playlist asociada al usuario.

---

# UC-003 — Consultar Playlist

## Objetivo

Obtener la información de una playlist.

### Precondiciones

- Usuario autenticado.
- La playlist existe.

### Flujo Principal

1. El usuario solicita una playlist.
2. El sistema consulta la información.
3. El sistema devuelve la playlist.

### Flujo Alternativo

- Playlist inexistente.

---

# UC-004 — Listar Playlists

## Objetivo

Consultar todas las playlists del usuario.

### Flujo Principal

1. El usuario realiza la consulta.
2. El sistema obtiene las playlists.
3. El sistema devuelve la colección.

---

# UC-005 — Actualizar Playlist

## Objetivo

Modificar la información de una playlist.

### Precondiciones

- Usuario autenticado.
- Playlist existente.

### Flujo Principal

1. El usuario solicita la actualización.
2. El sistema valida la operación.
3. El sistema actualiza la información.
4. El sistema confirma la actualización.

---

# UC-006 — Eliminar Playlist

## Objetivo

Eliminar una playlist.

### Precondiciones

- Usuario autenticado.
- Playlist existente.

### Flujo Principal

1. El usuario solicita eliminar.
2. El sistema valida permisos.
3. El sistema elimina la playlist.
4. El sistema confirma la operación.

---

# UC-007 — Agregar Canción

## Objetivo

Agregar una canción a una playlist.

### Precondiciones

- Usuario autenticado.
- Playlist existente.
- Canción válida.

### Flujo Principal

1. El usuario selecciona una playlist.
2. El sistema valida la solicitud.
3. El sistema agrega la canción.
4. El sistema actualiza la playlist.

### Flujo Alternativo

- Playlist inexistente.
- Canción inválida.
- Canción ya existente en la playlist (si esta regla se implementa).

---

# UC-008 — Eliminar Canción

## Objetivo

Eliminar una canción de una playlist.

### Precondiciones

- Usuario autenticado.
- Playlist existente.

### Flujo Principal

1. El usuario selecciona una canción.
2. El sistema elimina la canción.
3. El sistema confirma la operación.

---

# UC-009 — Consultar Información Musical

## Objetivo

Consultar información de una canción utilizando Spotify.

### Actor Secundario

Spotify API.

### Precondiciones

- Usuario autenticado.
- Spotify disponible.

### Flujo Principal

1. El usuario realiza la búsqueda.
2. El sistema consulta Spotify.
3. Spotify devuelve la información.
4. El sistema presenta el resultado.

### Flujo Alternativo

- Spotify no disponible.
- Canción no encontrada.

---

# UC-010 — Obtener Recomendaciones

## Objetivo

Solicitar recomendaciones musicales mediante Inteligencia Artificial.

### Actor Secundario

Proveedor IA.

### Precondiciones

- Usuario autenticado.

### Flujo Principal

1. El usuario solicita recomendaciones.
2. El sistema recopila el contexto necesario.
3. El sistema consulta al proveedor de IA.
4. La IA genera recomendaciones.
5. El sistema devuelve el resultado.

### Flujo Alternativo

- Error del proveedor IA.
- Tiempo de espera excedido.

---

# 5. Relación con el Dominio

| Caso de Uso | Conceptos del Dominio |
|--------------|----------------------|
| Crear Playlist | Usuario, Playlist |
| Consultar Playlist | Playlist |
| Actualizar Playlist | Playlist |
| Eliminar Playlist | Playlist |
| Agregar Canción | Playlist, Canción |
| Eliminar Canción | Playlist, Canción |
| Consultar Spotify | Canción |
| Obtener Recomendaciones | Recomendación |

---

# 6. Relación con la Arquitectura

Los casos de uso serán implementados dentro de la **Application Layer**.

Cada caso de uso:

- Coordinará la ejecución del dominio.
- Invocará repositorios mediante interfaces.
- Utilizará puertos para comunicarse con Spotify y el proveedor de IA.
- No contendrá lógica de infraestructura.

---

# 7. Consideraciones de Seguridad

Todos los casos de uso, excepto la autenticación, requerirán un usuario autenticado mediante JWT.

Las validaciones de autenticación y autorización serán responsabilidad de la capa de seguridad.

---

# 8. Consideraciones de Error

Todos los casos de uso deberán manejar de forma consistente:

- Recursos inexistentes.
- Datos inválidos.
- Errores de validación.
- Errores de persistencia.
- Fallos de servicios externos.
- Errores de autenticación.
- Errores de autorización.

---

# 9. Trazabilidad

| Requisito | Caso de Uso |
|------------|-------------|
| RF-001 | UC-001 |
| RF-002 | UC-002 |
| RF-003 | UC-003, UC-004 |
| RF-004 | UC-005 |
| RF-005 | UC-006 |
| RF-006 | UC-007 |
| RF-007 | UC-008 |
| RF-008 | UC-009 |
| RF-009 | UC-010 |

---

