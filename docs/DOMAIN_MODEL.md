# Domain Model

**Proyecto:** Playlist API  
**Documento:** Domain Model (Strategic Domain Model)  
**Versión:** 1.0  
**Estado:** Draft  
**Autor:** Edwin Esteban Henao Velásquez  
**Fecha:** 03/08/2026

---

# 1. Objetivo

El propósito de este documento es transformar los conceptos identificados durante el Domain Discovery en un modelo de dominio estratégico.

Este modelo representa los elementos fundamentales del negocio, sus relaciones y responsabilidades, manteniendo completa independencia de tecnologías como Spring Boot, JPA o la base de datos.

No constituye un modelo de persistencia.

---

# 2. Principios del Modelo

El modelo se construye siguiendo los siguientes principios:

- El dominio es independiente de la infraestructura.
- Las reglas del negocio pertenecen al dominio.
- Las tecnologías son detalles de implementación.
- Cada concepto posee una responsabilidad claramente definida.
- Las dependencias externas se representan mediante abstracciones.

---

# 3. Vista Estratégica del Dominio

```text
                  Usuario
                      │
                  administra
                      │
                      ▼
                  Playlist
                      │
              contiene canciones
                      │
                      ▼
                    Canción

Playlist
     │
solicita
     │
     ▼
Recomendación
     │
generada mediante
     ▼
Proveedor IA

Canción
     │
información obtenida desde
     ▼
Spotify
```

---

# 4. Aggregate Root

## Playlist

La Playlist representa el Aggregate Root del dominio.

Todas las modificaciones relacionadas con la colección de canciones deberán realizarse a través de ella.

Responsabilidades:

- Mantener la consistencia del agregado.
- Controlar la incorporación y eliminación de canciones.
- Garantizar el cumplimiento de las reglas del dominio.

---

# 5. Entidades (Entities)

## Usuario

Representa al propietario de una o varias playlists.

Posee identidad propia.

---

## Playlist

Representa una colección organizada de canciones.

Constituye el centro del dominio.

Posee identidad propia.

---

## Canción

Representa una obra musical.

Puede existir independientemente de una playlist.

Posee identidad propia.

---

# 6. Objetos de Valor (Value Objects)

Se identifican los siguientes conceptos como candidatos a Value Objects.

| Value Object | Propósito |
|---------------|-----------|
| PlaylistName | Nombre de una playlist. |
| SongTitle | Nombre de una canción. |
| ArtistName | Nombre del artista. |
| AlbumName | Nombre del álbum. |
| Genre | Género musical. |
| Duration | Duración de la canción. |
| RecommendationScore | Nivel de relevancia de una recomendación. |

Estos objetos serán definidos durante la implementación si aportan valor al modelo.

---

# 7. Servicios de Dominio

Se identifican responsabilidades que no pertenecen naturalmente a una entidad.

## PlaylistService

Coordina operaciones complejas relacionadas con playlists.

---

## RecommendationService

Gestiona la obtención de recomendaciones musicales.

---

## MusicCatalogService

Gestiona la consulta de información musical proveniente de Spotify.

---

# 8. Repositorios del Dominio

Los repositorios representan contratos del dominio.

No definen detalles de persistencia.

## PlaylistRepository

Responsable del almacenamiento y recuperación de playlists.

---

## UserRepository

Responsable de administrar usuarios.

---

Los repositorios serán implementados posteriormente por la infraestructura utilizando JPA.

---

# 9. Eventos del Dominio

Se identifican los siguientes eventos significativos.

- PlaylistCreated
- PlaylistUpdated
- PlaylistDeleted
- SongAdded
- SongRemoved
- RecommendationRequested
- RecommendationGenerated

Los eventos representan cambios relevantes dentro del dominio y no dependen de la implementación técnica.

---

# 10. Relaciones del Dominio

## Usuario → Playlist

Un usuario administra múltiples playlists.

Cada playlist pertenece a un único usuario.

---

## Playlist → Canción

Una playlist contiene múltiples canciones.

Una canción puede formar parte de varias playlists.

---

## Playlist → Recomendación

Las recomendaciones complementan una playlist, pero no modifican automáticamente su contenido.

---

## Canción → Spotify

Spotify actúa únicamente como fuente de información.

No forma parte del dominio.

---

# 11. Invariantes del Dominio

Las siguientes reglas deberán mantenerse siempre.

- Toda playlist debe tener un nombre válido.
- Toda playlist pertenece a un usuario.
- Solo el propietario puede modificar una playlist.
- Una recomendación nunca modifica automáticamente una playlist.
- El dominio nunca depende directamente de Spotify.
- El dominio nunca depende del proveedor de IA.
- Las reglas del negocio permanecen independientes de la persistencia.

---

# 12. Límites del Dominio

## Dentro del dominio

- Usuario
- Playlist
- Canción
- Reglas del negocio
- Servicios del dominio
- Eventos del dominio
- Repositorios (interfaces)

---

## Fuera del dominio

- Spring Boot
- HTTP
- JWT
- JPA
- H2
- Spotify
- Inteligencia Artificial
- Logging
- Configuración

Estos componentes pertenecen a la infraestructura y serán implementados mediante adaptadores.

---

# 13. Dependencias

La dirección de las dependencias será la siguiente.

```text
Application
      │
      ▼
Domain
      ▲
      │
Infrastructure
```

El dominio nunca conocerá implementaciones concretas.

---

# 14. Correspondencia con la Arquitectura

Este modelo implementa las decisiones registradas en el Architecture Decision Record.

| ADR | Aplicación en el Modelo |
|------|-------------------------|
| Arquitectura Hexagonal | Dominio independiente de infraestructura |
| DTO | El dominio no expone objetos de transporte |
| Spotify Adapter | Spotify permanece fuera del dominio |
| IA Adapter | IA permanece desacoplada mediante puertos |
| JWT | Seguridad fuera del dominio |

---

# 15. Decisiones Pendientes

Durante la implementación deberán definirse:

- Atributos de las entidades.
- Métodos del dominio.
- Objetos de Valor definitivos.
- Reglas de validación.
- Casos de uso.
- Estrategia de persistencia.
- Diseño de la API REST.

---
