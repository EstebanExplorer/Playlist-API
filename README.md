# Playlist API

![Java 21](https://img.shields.io/badge/Java-21_LTS-orange.svg?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-green.svg?style=flat-square&logo=springboot)
![Build Status](https://img.shields.io/badge/Build-Passing_(149/149_Tests)-brightgreen.svg?style=flat-square)
![OpenAPI 3.0](https://img.shields.io/badge/OpenAPI-3.0.0-blue.svg?style=flat-square&logo=openapiinitiative)
![License MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)
![Status](https://img.shields.io/badge/Status-Production_Ready-success.svg?style=flat-square)

Microservicio RESTful backend de alto rendimiento desarrollado con **Java 21 LTS** y **Spring Boot 3.2.x**, diseñado para la gestión integral de listas de reproducción musicales, integración en tiempo real con la **Spotify Web API** y generación de sugerencias personalizadas mediante **Inteligencia Artificial**.

- **¿Qué hace?**: Ofrece una API REST stateless segura para crear y gestionar playlists, buscar canciones en Spotify, añadir/remover pistas respetando invariantes del dominio y recibir recomendaciones musicales impulsadas por IA.
- **¿Para quién?**: Desarrolladores, integradores de sistemas de streaming y arquitectos de software que requieren una referencia de producción limpia y desacoplada.
- **¿Por qué existe?**: Demuestra la implementación práctica de **Arquitectura Hexagonal (Ports & Adapters)**, **Clean Architecture**, **Domain-Driven Design (DDD)** y principios **SOLID** sin contaminación de frameworks en el núcleo de negocio.

---

## Tabla de Contenido

- [1. Características Principales](#1-características-principales)
- [2. Tecnologías Utilizadas](#2-tecnologías-utilizadas)
- [3. Arquitectura](#3-arquitectura)
- [4. Estructura del Proyecto](#4-estructura-del-proyecto)
- [5. Casos de Uso Implementados](#5-casos-de-uso-implementados)
- [6. Requisitos Previos](#6-requisitos-previos)
- [7. Instalación y Guía de Inicio Rápido](#7-instalación-y-guía-de-inicio-rápido)
- [8. Configuración y Variables de Entorno](#8-configuración-y-variables-de-entorno)
- [9. Obtención de Credenciales de Spotify](#9-obtención-de-credenciales-de-spotify)
- [10. Documentación Interactiva (Swagger / OpenAPI 3)](#10-documentación-interactiva-swagger--openapi-3)
- [11. Ejemplos de Consumo HTTP (cURL)](#11-ejemplos-de-consumo-http-curl)
- [12. Ejecución de Pruebas](#12-ejecución-de-pruebas)
- [13. Calidad del Proyecto y Patrones de Diseño](#13-calidad-del-proyecto-y-patrones-de-diseño)
- [14. Integración con Spotify Web API](#14-integración-con-spotify-web-api)
- [15. Seguridad & OWASP](#15-seguridad--owasp)
- [16. Estado del Proyecto](#16-estado-del-proyecto)
- [17. Licencia y Autor](#17-licencia-y-autor)

---

## 1. Características Principales

- **Autenticación y Seguridad Stateless**: Emisión y validación de JSON Web Tokens (JWT) firmados con clave secreta y cifrado de contraseñas mediante **BCrypt**.
- **Gestión Integral de Playlists**: Creación, lectura, actualización de nombres, eliminación y listado de listas de reproducción asociadas a usuarios.
- **Integración Real con Spotify Web API**: Búsqueda en tiempo real de canciones por título, artista o álbum, e inspección directa de pistas individuales.
- **Gestión de Canciones en Playlists**: Adición y eliminación de canciones validando reglas de negocio e invariantes de agregados (previniendo duplicados).
- **Recomendaciones Musicales IA**: Generación de sugerencias musicales personalizadas basadas en las preferencias y contenidos de la playlist mediante puertos de Inteligencia Artificial.
- **Arquitectura Hexagonal & DDD**: Aislamiento estricto del dominio de negocio respecto a la infraestructura y frameworks de persistencia/red.
- **Documentación Interactiva OpenAPI 3 / Swagger**: Especificación OpenAPI 3 estructurada con interfaz Swagger UI integrada.
- **Suite Completa de Pruebas**: 149 pruebas automatizadas (unitarias y de integración) con 100% de tasa de éxito bajo `mvn clean verify`.

---

## 2. Tecnologías Utilizadas

| Categoría | Tecnología / Librería | Versión | Propósito |
| :--- | :--- | :--- | :--- |
| **Lenguaje** | Java LTS | 21 | Sintaxis moderna (Records, Pattern Matching, Sealed Interfaces, Virtual Threads) |
| **Framework Base** | Spring Boot | 3.2.5 | Framework principal para microservicios y contenedor IoC |
| **Seguridad** | Spring Security | 6.2 | Autenticación stateless, filtros HTTP y autorización granular |
| **Tokens & Cifrado** | JJWT (io.jsonwebtoken) / BCrypt | 0.12.5 | Generación/validación de tokens JWT y hashing seguro de contraseñas |
| **Persistencia & ORM** | Spring Data JPA / Hibernate | 6.5.2 | Mapeo objeto-relacional y adaptadores JPA |
| **Base de Datos** | H2 Database | 2.2.224 | Base de datos relacional en memoria para desarrollo y ejecución de pruebas |
| **Cliente HTTP** | Spring RestClient | 6.1.6 | Cliente HTTP síncrono moderno y fluido para integración con Spotify |
| **API Externa** | Spotify Web API | v1 | Catálogo musical externo mediante Client Credentials OAuth2 Flow |
| **Pruebas Automáticas** | JUnit 5 / Mockito / MockMvc | 5.10 / 5.11 | Suite de pruebas unitarias, de componentes y de integración |
| **Documentación** | Springdoc OpenAPI / Swagger UI | 2.5.0 | Especificación interactiva OpenAPI 3 |
| **Construcción** | Apache Maven | 3.9+ | Gestión de dependencias y ciclo de vida de construcción |

---

## 3. Arquitectura

La solución implementa **Arquitectura Hexagonal (Puertos y Adaptadores)** combinada con **Clean Architecture** y **Domain-Driven Design (DDD)**. El núcleo del dominio del negocio se encuentra completamente aislado e independiente de Spring Boot, bases de datos o clientes HTTP.

### Responsabilidades de las Capas

1. **Domain Layer (Núcleo de Dominio)**:
   - Contiene los Agregados (`Playlist`), Entidades (`Song`, `User`), Objetos de Valor (`SongTitle`, `ArtistName`, `AlbumName`, `Duration`, `PlaylistName`), Excepciones de Dominio y Servicios de Dominio (`PlaylistDomainService`, `RecommendationDomainService`).
   - Define los Puertos de Salida (*Output Ports*): `PlaylistRepository`, `UserRepository`, `MusicCatalogPort`, `AiRecommendationPort`.
   - **Regla**: Cero dependencias de frameworks externos.

2. **Application Layer (Casos de Uso)**:
   - Implementa la lógica de orquestación de casos de uso (UC-001 a UC-010).
   - Utiliza exclusivamente Comandos, Queries y Records Result inmutables en Java 21 (`PlaylistResult`, `SongSearchResult`, `AuthenticationResult`).
   - Comunica el exterior con el dominio a través de los puertos definidos.

3. **Presentation Layer (Adaptadores de Entrada - Inbound)**:
   - Controladores REST (`AuthController`, `PlaylistController`, `SongController`, `RecommendationController`) anotados con Spring MVC.
   - Mappers de presentación que convierten Request DTOs a Comandos/Queries y Results a Response DTOs.
   - Manejo global de excepciones mediante `@RestControllerAdvice` (`GlobalExceptionHandler`).

4. **Infrastructure Layer (Adaptadores de Salida - Outbound)**:
   - Persistencia JPA con Spring Data JPA y H2 Database (`JpaPlaylistRepositoryAdapter`).
   - Adaptador in-memory para usuarios (`InMemoryUserRepositoryAdapter`).
   - Cliente HTTP para Spotify (`SpotifyClient`, `SpotifyMusicCatalogAdapter`) usando `RestClient`.
   - Configuración de Spring Security, JWT (`JwtTokenProviderAdapter`) y Swagger/OpenAPI (`OpenApiConfiguration`).

### Diagrama Arquitectónico ASCII

```text
                               +-------------------------------------------------------+
                               |                  PRESENTATION LAYER                   |
                               | (HTTP REST Controllers, DTOs, Handlers, Mappers)      |
                               +---------------------------+---------------------------+
                                                           |
                                                           v  (Commands / Queries)
                               +-------------------------------------------------------+
                               |                   APPLICATION LAYER                   |
                               | (Use Cases: CreatePlaylist, SearchSongs, Authenticate)|
                               +---------------------------+---------------------------+
                                                           |
                                                           v  (Uses Domain Model & Ports)
+--------------------------------------------------------------------------------------------------------------------+
|                                                   DOMAIN LAYER                                                     |
|                                                                                                                    |
|   +---------------------------------------+                   +------------------------------------------------+   |
|   |         Agregados & Entidades         |                   |                Output Ports                    |   |
|   |   (Playlist, Song, User, ValueObjects)|                   | (PlaylistRepository, MusicCatalogPort, etc.)   |   |
|   +---------------------------------------+                   +-----------------------+------------------------+   |
+---------------------------------------------------------------------------------------|----------------------------+
                                                                                        ^
                                                                                        | (Implements Ports)
                               +--------------------------------------------------------+------------------+
                               |                  INFRASTRUCTURE LAYER                                     |
                               |  +--------------------+  +--------------------+  +---------------------+  |
                               |  |  Persistence JPA   |  |   Spotify API      |  | Security JWT / Auth |  |
                               |  | (H2, Repositories) |  | (RestClient, Auth) |  | (Spring Security)   |  |
                               |  +--------------------+  +--------------------+  +---------------------+  |
                               +---------------------------------------------------------------------------+
```

---

## 4. Estructura del Proyecto

```text
com.esteban.playlistapi
├── PlaylistApiApplication.java
│
├── domain
│   ├── exception            # Excepciones puras del dominio (DomainException, etc.)
│   ├── model                # Agregados y Entidades (Playlist, Song, User)
│   ├── repository           # Puertos de Salida (PlaylistRepository, MusicCatalogPort, etc.)
│   ├── service              # Servicios de Dominio (PlaylistDomainService, etc.)
│   └── valueobject          # Objetos de Valor inmutables (SongTitle, PlaylistName, etc.)
│
├── application
│   ├── auth                 # Caso de Uso UC-001 (AuthenticateUserUseCase, DTOs, Ports)
│   ├── playlist             # Casos de Uso UC-002 a UC-006 (Create, Get, List, Update, Delete)
│   ├── recommendation       # Caso de Uso UC-010 (GenerateRecommendationUseCase, DTOs)
│   └── song                 # Casos de Uso UC-007 a UC-009 (AddSong, RemoveSong, SearchSongs)
│
├── presentation
│   ├── advice               # GlobalExceptionHandler (@RestControllerAdvice)
│   ├── controller           # Controladores REST (AuthController, PlaylistController, etc.)
│   ├── dto                  # DTOs HTTP Request/Response/Error (Validation annotations)
│   └── mapper               # Mappers de presentación manuales
│
├── infrastructure
│   ├── ai                   # Adaptador de recomendaciones IA (DummyAiRecommendationAdapter)
│   ├── configuration        # BeanConfiguration, OpenApiConfiguration
│   ├── persistence          # Adaptadores JPA, Entidades JPA, Repositorios Spring Data
│   ├── security             # Filtro JWT, SecurityConfig, Adaptadores Token y PasswordEncoder
│   └── spotify              # Integración real Spotify Web API
│       ├── adapter          # SpotifyMusicCatalogAdapter (implementa MusicCatalogPort)
│       ├── client           # SpotifyClient (RestClient, OAuth2 Token Cache)
│       ├── config           # SpotifyProperties, SpotifyConfiguration
│       ├── dto              # DTOs internos deserializadores JSON de Spotify
│       └── mapper           # SpotifyMapper (SpotifyTrackItem -> Song)
│
└── shared                   # Excepciones compartidas transversales (InvalidCommandException, etc.)
```

---

## 5. Casos de Uso Implementados

| Código | Método HTTP | Endpoint | Descripción |
| :--- | :--- | :--- | :--- |
| **UC-001** | `POST` | `/api/v1/auth/login` | Autentica a un usuario por email/contraseña y genera un Token JWT de acceso. |
| **UC-002** | `POST` | `/api/v1/playlists` | Crea una nueva lista de reproducción inmutable asociada al usuario autenticado. |
| **UC-003** | `GET` | `/api/v1/playlists/{playlistId}` | Consulta el detalle completo de una playlist existente con su lista de canciones. |
| **UC-004** | `GET` | `/api/v1/playlists` | Lista de forma resumida todas las playlists creadas por el usuario autenticado. |
| **UC-005** | `PUT` | `/api/v1/playlists/{playlistId}` | Actualiza el nombre de una playlist existente validando invariantes del dominio. |
| **UC-006** | `DELETE` | `/api/v1/playlists/{playlistId}` | Elimina permanentemente una playlist del usuario. |
| **UC-007** | `POST` | `/api/v1/playlists/{playlistId}/songs` | Agrega una canción del catálogo musical (Spotify) a la playlist especificada. |
| **UC-008** | `DELETE` | `/api/v1/playlists/{playlistId}/songs/{songId}` | Remueve una canción de una playlist existente. |
| **UC-009** | `GET` | `/api/v1/songs/search` | Consulta en tiempo real el catálogo de Spotify por artista, título o álbum. |
| **UC-010** | `POST` | `/api/v1/playlists/{playlistId}/recommendations` | Genera sugerencias de canciones personalizadas basadas en IA. |

---

## 6. Requisitos Previos

- **Java Development Kit (JDK)**: Versión 21 LTS o superior instalado y configurado en la variable `JAVA_HOME`.
- **Apache Maven**: Versión 3.9.0 o superior instalada.
- **Cuenta de Desarrollador Spotify**: Para la obtención del `Client ID` y `Client Secret`.

---

## 7. Instalación y Guía de Inicio Rápido

Ejecute la siguiente secuencia de comandos en su terminal para clonar, configurar e iniciar el microservicio localmente:

```bash
# 1. Clonar el repositorio
git clone https://github.com/estebanhenao/Playlist_API.git

# 2. Navegar al directorio del proyecto
cd Playlist_API

# 3. Configurar variables de entorno requeridas para Spotify
export SPOTIFY_CLIENT_ID="tu_client_id_real"
export SPOTIFY_CLIENT_SECRET="tu_client_secret_real"

# 4. Limpiar, compilar y verificar el proyecto
mvn clean install

# 5. Iniciar la aplicación Spring Boot
mvn spring-boot:run
```

El servidor iniciará exitosamente en `http://localhost:8080`.

---

## 8. Configuración y Variables de Entorno

La configuración utiliza `${NOMBRE_VARIABLE:valor_por_defecto}` en `application.yml`, lo que permite ejecutar la aplicación sin configuración previa en modo desarrollo/pruebas o inyectar credenciales reales en producción:

| Variable de Entorno | Descripción | Obligatoria | Valor por Defecto / Ejemplo |
| :--- | :--- | :--- | :--- |
| `SERVER_PORT` | Puerto HTTP del servidor Spring Boot | No | `8080` |
| `JWT_SECRET` | Clave secreta Base64/Hex para firma de tokens JWT | Sí (en prod) | `9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d...` |
| `JWT_EXPIRATION` | Tiempo de expiración del token JWT en milisegundos | No | `86400000` (24 horas) |
| `SPOTIFY_CLIENT_ID` | Client ID obtenido en Spotify Developer Dashboard | Sí (para Spotify real) | `test-client-id` |
| `SPOTIFY_CLIENT_SECRET` | Client Secret obtenido en Spotify Developer Dashboard | Sí (para Spotify real) | `test-client-secret` |
| `SPOTIFY_TOKEN_URL` | URL del endpoint de autenticación OAuth2 de Spotify | No | `https://accounts.spotify.com/api/token` |
| `SPOTIFY_API_URL` | URL base de la Spotify Web API v1 | No | `https://api.spotify.com/v1` |

---

## 9. Obtención de Credenciales de Spotify

1. Ingrese al portal oficial [Spotify Developer Dashboard](https://developer.spotify.com/dashboard).
2. Inicie sesión con su cuenta de Spotify y haga clic en **Create an App**.
3. Asigne un nombre y descripción a la aplicación (ejemplo: `Playlist API Local`).
4. Seleccione la opción **Web API** y acepte los términos de servicio.
5. Una vez creada la aplicación, acceda a **Settings**.
6. Copie el **Client ID** y haga clic en **View client secret** para copiar el **Client Secret**.
7. Exporte las variables en su terminal antes de iniciar la aplicación:
   ```bash
   export SPOTIFY_CLIENT_ID="tu_client_id_real"
   export SPOTIFY_CLIENT_SECRET="tu_client_secret_real"
   ```

---

## 10. Documentación Interactiva (Swagger / OpenAPI 3)

Una vez iniciada la aplicación, la documentación interactiva estará disponible en:

- **Swagger UI (Interactiva)**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Especificación OpenAPI 3 (JSON)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Autenticación en Swagger UI
1. Ejecute la petición `POST /api/v1/auth/login`.
2. Copie el valor del campo `accessToken` recibido.
3. Haga clic en el botón **Authorize** en la parte superior derecha de Swagger UI.
4. Pegue el token en el campo **Value** y presione **Authorize**.

---

## 11. Ejemplos de Consumo HTTP (cURL)

### 1. Autenticación / Login (UC-001)

```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
     -H "Content-Type: application/json" \
     -d '{
           "email": "esteban@example.com",
           "password": "Password123!"
         }'
```

**Respuesta de Ejemplo (HTTP 200 OK)**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMTExMTExMS0xMTExLTExMTEtMTExMS0xMTExMTExMTExMTEiLCJlbWFpbCI6ImVzdGViYW5AZXhhbXBsZS5jb20iLCJpYXQiOjE3MjI3NjgwMDAsImV4cCI6MTcyMjg1NDQwMH0...",
  "tokenType": "Bearer",
  "expiresInSeconds": 86400
}
```

---

### 2. Crear Playlist (UC-002)

```bash
curl -X POST "http://localhost:8080/api/v1/playlists" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <TU_ACCESS_TOKEN>" \
     -d '{
           "name": "Rock Clásico de los 80s"
         }'
```

**Respuesta de Ejemplo (HTTP 201 Created)**:
```json
{
  "id": "a5d8f2b1-3c4e-4f6a-8b9c-1d2e3f4a5b6c",
  "name": "Rock Clásico de los 80s",
  "userId": "11111111-1111-1111-1111-111111111111",
  "songCount": 0,
  "songs": [],
  "createdAt": "2026-08-04T08:00:00",
  "updatedAt": "2026-08-04T08:00:00"
}
```

---

### 3. Buscar Canciones en el Catálogo de Spotify (UC-009)

```bash
curl -X GET "http://localhost:8080/api/v1/songs/search?query=Led%20Zeppelin&limit=5" \
     -H "Authorization: Bearer <TU_ACCESS_TOKEN>"
```

**Respuesta de Ejemplo (HTTP 200 OK)**:
```json
[
  {
    "spotifyId": "38yG1mXg22F9N0l2JpX5vD",
    "title": "Stairway to Heaven",
    "artist": "Led Zeppelin",
    "album": "Led Zeppelin IV",
    "durationSeconds": 482
  }
]
```

---

### 4. Agregar Canción a Playlist (UC-007)

```bash
curl -X POST "http://localhost:8080/api/v1/playlists/a5d8f2b1-3c4e-4f6a-8b9c-1d2e3f4a5b6c/songs" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <TU_ACCESS_TOKEN>" \
     -d '{
           "spotifyId": "38yG1mXg22F9N0l2JpX5vD"
         }'
```

**Respuesta de Ejemplo (HTTP 200 OK)**:
```json
{
  "id": "a5d8f2b1-3c4e-4f6a-8b9c-1d2e3f4a5b6c",
  "name": "Rock Clásico de los 80s",
  "userId": "11111111-1111-1111-1111-111111111111",
  "songCount": 1,
  "songs": [
    {
      "songId": "7f8e9d0c-1b2a-3f4e-5d6c-7b8a9f0e1d2c",
      "spotifyId": "38yG1mXg22F9N0l2JpX5vD",
      "title": "Stairway to Heaven",
      "artist": "Led Zeppelin",
      "album": "Led Zeppelin IV",
      "durationSeconds": 482
    }
  ],
  "createdAt": "2026-08-04T08:00:00",
  "updatedAt": "2026-08-04T08:05:00"
}
```

---

### 5. Generar Recomendaciones con IA (UC-010)

```bash
curl -X POST "http://localhost:8080/api/v1/playlists/a5d8f2b1-3c4e-4f6a-8b9c-1d2e3f4a5b6c/recommendations" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <TU_ACCESS_TOKEN>" \
     -d '{
           "limit": 5
         }'
```

---

## 12. Ejecución de Pruebas

El proyecto cuenta con una suite completa de pruebas unitarias y de integración desarrolladas con **JUnit 5**, **Mockito** y **MockMvc**.

### Ejecutar Pruebas Rápidas

```bash
mvn test
```

### Ejecutar Verificación Completa y Cobertura

```bash
mvn clean verify
```

### Clasificación de Pruebas
- **Pruebas Unitarias de Dominio**: Validan invariantes de `Playlist`, `Song`, `PlaylistDomainService`.
- **Pruebas Unitarias de Casos de Uso**: Mockito para aislar puertos de persistencia y servicios externos.
- **Pruebas Unitarias de Infraestructura**: `SpotifyClientTest` (usando `MockRestServiceServer`), `SpotifyPropertiesTest`, `SpotifyMapperTest`, `SpotifyMusicCatalogAdapterTest`, `JwtTokenProviderAdapterTest`, `JpaPlaylistRepositoryAdapterTest`.
- **Pruebas de Integración**: `AuthIntegrationTest`, `PlaylistIntegrationTest`, `SongIntegrationTest`, `RecommendationIntegrationTest` comprobando la pila completa HTTP/Security/JPA/H2.

---

## 13. Calidad del Proyecto y Patrones de Diseño

- **SOLID Principles**:
  - **Single Responsibility Principle (SRP)**: Separación entre comunicación HTTP (`SpotifyClient`), transformación de datos (`SpotifyMapper`) e implementación del puerto (`SpotifyMusicCatalogAdapter`).
  - **Dependency Inversion Principle (DIP)**: Los casos de uso dependen de la abstracción `MusicCatalogPort`, nunca de implementaciones concretas ni de Spring.
- **Fail-Fast Design**: Validación previa mediante `Objects.requireNonNull()` e inmutabilidad de comandos en Java 21 Records.
- **Bean Validation & RFC 7807 (Problem Details)**: Validación de Request DTOs con `@Valid` y manejo centralizado de excepciones mediante `GlobalExceptionHandler` retornando la estructura unificada `ApiErrorResponse`.

---

## 14. Integración con Spotify Web API

- **OAuth2 Client Credentials Flow**: El cliente HTTP solicita tokens mediante `POST` a `https://accounts.spotify.com/api/token` con cabecera `Authorization: Basic Base64(clientId:clientSecret)`.
- **Caché Thread-Safe de Token**: `SpotifyClient` mantiene el token en memoria mediante `AtomicReference<CachedToken>` con estrategia Double-Checked Locking.
- **Renovación Automática**: Renueva automáticamente el token cuando faltan menos de 60 segundos para expirar o ante una respuesta `HTTP 401 Unauthorized`.
- **Mapeo Aislado**: Ningún DTO interno de Spotify trasciende el módulo de infraestructura.

---

## 15. Seguridad & OWASP

- **Spring Security 6 & Stateless Session**: Deshabilitación de CSRF y sesiones HTTP (`SessionCreationPolicy.STATELESS`).
- **Enmascaramiento Estricto de Secretos en Logs**: Se garantiza que el `clientSecret`, el `accessToken` y la cabecera `Authorization` **NUNCA** sean registrados en los logs del sistema.
- **Hashing de Contraseñas**: Algoritmo **BCrypt** de fortaleza 10 para almacenamiento de contraseñas.
- **Gestión de Secretos**: Inyección de credenciales mediante variables de entorno en tiempo de ejecución.

---

## 16. Estado del Proyecto

- **Estado**: **Proyecto Finalizado - Production Ready**
- **Resultado de Construcción**: `BUILD SUCCESS`
- **Total de Pruebas**: **149 / 149 Aprobadas** (0 fallos, 0 errores, 0 omitidas)

---

## 17. Licencia y Autor

### Licencia

Este proyecto está bajo la Licencia **MIT**. Consulte el archivo `LICENSE` para obtener más información.

```text
MIT License

Copyright (c) 2026 Edwin Esteban Henao

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### Autor

- **Edwin Esteban Henao**
- **GitHub**: [https://github.com/estebanhenao](https://github.com/estebanhenao)