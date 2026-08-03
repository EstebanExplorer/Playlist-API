# Project Structure

**Proyecto:** Playlist API  
**Documento:** Project Structure  
**Versión:** 2.1  
**Estado:** Final  
**Autor:** Edwin Esteban Henao Velásquez  
**Fecha:** 03/08/2026

---

# 1. Objetivo

Este documento define la estructura física del proyecto, las convenciones de organización del código y la distribución de responsabilidades entre las diferentes capas de la aplicación.

La solución implementa una **Arquitectura Hexagonal (Ports & Adapters)**, siguiendo las decisiones registradas en el **Architecture Decision Record (ADR)** y los principios de Clean Architecture.

La estructura propuesta busca garantizar:

- Separación clara de responsabilidades.
- Bajo acoplamiento.
- Alta cohesión.
- Escalabilidad.
- Testabilidad.
- Independencia de frameworks y tecnologías.

---

# 2. Principios de Organización

Durante el desarrollo se respetarán los siguientes principios:

- El dominio es completamente independiente de Spring Boot.
- El dominio no conoce JPA, JWT, HTTP ni servicios externos.
- Las dependencias siempre apuntan hacia el dominio.
- La infraestructura implementa contratos definidos por el dominio.
- Cada paquete representa una responsabilidad funcional claramente definida.

---

# 3. Estructura General del Proyecto

```text
playlist-api/
│
├── docs/
│   ├── 01_REQUIREMENTS_ANALYSIS.md
│   ├── 02_ARCHITECTURE_DECISION_RECORD.md
│   ├── 03_SOLUTION_BLUEPRINT.md
│   ├── 04_DOMAIN_DISCOVERY.md
│   ├── 05_DOMAIN_MODEL.md
│   ├── 06_USE_CASES.md
│   ├── 07_API_CONTRACT.md
│   ├── 08_TEST_STRATEGY.md
│   └── 09_PROJECT_STRUCTURE.md
│
├── src/
│   ├── main/
│   │
│   │   ├── java/
│   │   │
│   │   │   └── com/
│   │   │       └── esteban/
│   │   │           └── playlistapi/
│   │   │
│   │   │               ├── application/
│   │   │               ├── domain/
│   │   │               ├── infrastructure/
│   │   │               ├── presentation/
│   │   │               └── shared/
│   │
│   │   └── resources/
│   │
│   └── test/
│
├── README.md
├── AI_LOG.md
└── pom.xml
```

---

# 4. Arquitectura de Capas

La dirección de las dependencias será la siguiente:

```text
Presentation
        │
        ▼
Application
        │
        ▼
Domain
        ▲
        │
Infrastructure
```

La capa **Domain** constituye el núcleo de la aplicación y no tendrá dependencias hacia capas externas.

---

# 5. Organización por Capacidades (Feature-Based)

Las capas **Presentation** y **Application** estarán organizadas por capacidades funcionales del negocio.

## Application

```text
application/

auth/

playlist/

song/

recommendation/
```

Cada módulo contendrá exclusivamente los casos de uso relacionados con su funcionalidad.

Ejemplo:

```text
application/

playlist/

CreatePlaylistUseCase

UpdatePlaylistUseCase

DeletePlaylistUseCase

GetPlaylistUseCase

ListPlaylistsUseCase
```

---

## Presentation

```text
presentation/

auth/

playlist/

song/

recommendation/
```

Cada módulo contendrá:

- Controllers
- DTOs
- Mappers
- Validaciones específicas de entrada

Ejemplo:

```text
presentation/

playlist/

PlaylistController

PlaylistRequest

PlaylistResponse

PlaylistMapper
```

---

# 6. Dominio

El dominio representa el negocio y permanece completamente desacoplado de cualquier tecnología.

```text
domain/

model/

repository/

service/

event/

valueobject/

exception/
```

## Responsabilidades

### model

Entidades y Agregados del dominio.

### repository

Interfaces de persistencia.

### service

Servicios del dominio.

### valueobject

Objetos de Valor.

### event

Eventos del dominio.

### exception

Excepciones propias del negocio.

---

# 7. Infraestructura

Toda integración con tecnologías externas se implementará dentro de esta capa.

```text
infrastructure/

configuration/

persistence/

security/

spotify/

ai/

mapper/
```

## configuration

Configuración específica de Spring Boot.

Ejemplos:

- SecurityConfig
- OpenApiConfig
- Bean Configuration
- Jackson Configuration
- CORS
- RestTemplate / WebClient

---

## persistence

Implementaciones de acceso a datos.

Ejemplos:

- JpaPlaylistRepository
- JpaUserRepository
- Spring Data JPA
- Entidades JPA
- Mappers de persistencia

---

## security

Responsable de toda la infraestructura de autenticación.

Ejemplos:

- JwtProvider
- JwtFilter
- AuthenticationService
- UserDetailsService
- PasswordEncoder

---

## spotify

Adaptador encargado de consumir Spotify.

Ejemplos:

- SpotifyClient
- SpotifyAdapter
- SpotifyMapper
- SpotifyResponse

---

## ai

Adaptador encargado de consumir el proveedor de Inteligencia Artificial.

Ejemplos:

- AiRecommendationClient
- PromptBuilder
- AiMapper
- RecommendationAdapter

---

## mapper

Conversión entre modelos de infraestructura y objetos utilizados por la aplicación.

---

# 8. Componentes Compartidos

```text
shared/

constants/

exception/

util/

validation/
```

Contendrá únicamente componentes reutilizables.

No contendrá reglas del negocio.

---

# 9. Recursos

```text
resources/

application.yml

application-dev.yml

application-test.yml

messages.properties
```

Las credenciales nunca serán almacenadas en el repositorio.

Se utilizarán variables de entorno para información sensible.

---

# 10. Organización de las Pruebas

La estructura de pruebas replicará la organización del proyecto principal.

```text
src/test/

application/

domain/

infrastructure/

presentation/
```

Se implementarán:

- Pruebas unitarias.
- Pruebas de integración.
- Mocks para Spotify.
- Mocks para IA.
- Validación de escenarios de error.

---

# 11. Convenciones de Nombres

## Controllers

- PlaylistController
- RecommendationController

---

## Casos de Uso

- CreatePlaylistUseCase
- UpdatePlaylistUseCase
- DeletePlaylistUseCase
- AddSongUseCase

---

## Interfaces

- PlaylistRepository
- UserRepository

---

## Implementaciones

- JpaPlaylistRepository
- JpaUserRepository

---

## Clientes Externos

- SpotifyClient
- AiRecommendationClient

---

# 12. Dependencias Permitidas

```text
Presentation
      │
      ▼
Application
      │
      ▼
Domain
      ▲
      │
Infrastructure
```

No se permitirán dependencias inversas.

---

# 13. Dependencias Prohibidas

La capa de dominio no podrá importar:

- Spring Framework
- Spring Boot
- Spring Security
- Hibernate
- JPA
- Controllers
- DTOs
- Jackson
- Clientes HTTP
- SDK de Spotify
- SDK del proveedor de IA

---

# 14. Principios de Implementación

Durante el desarrollo se aplicarán los siguientes principios:

- SOLID.
- Clean Code.
- Arquitectura Hexagonal.
- Inversión de Dependencias.
- Programación contra Interfaces.
- Responsabilidad Única.
- Alta cohesión.
- Bajo acoplamiento.
- Fail Fast.
- Composición sobre herencia cuando sea apropiado.

---

# 15. Escalabilidad

La estructura propuesta facilita la incorporación de nuevas funcionalidades mediante nuevos adaptadores y módulos sin modificar el dominio.

Ejemplos:

- Integrar Apple Music.
- Integrar YouTube Music.
- Cambiar H2 por PostgreSQL.
- Incorporar un nuevo proveedor de IA.
- Exponer GraphQL además de REST.

Todas estas extensiones deberán implementarse en la capa de infraestructura, preservando la independencia del dominio.

---

# 16. Relación con la Documentación

La estructura definida en este documento implementa las decisiones establecidas en:

- REQUIREMENTS_ANALYSIS.md
- ARCHITECTURE_DECISION_RECORD.md
- SOLUTION_BLUEPRINT.md
- DOMAIN_DISCOVERY.md
- DOMAIN_MODEL.md
- USE_CASES.md
- API_CONTRACT.md
- TEST_STRATEGY.md

Cualquier cambio estructural durante la implementación deberá mantenerse alineado con estos documentos para garantizar la coherencia arquitectónica del proyecto.

---

# 17. Conclusión

La estructura propuesta proporciona una base sólida para el desarrollo de la aplicación, manteniendo una clara separación entre el dominio, la lógica de aplicación y la infraestructura.

Esta organización permitirá implementar los requisitos funcionales y no funcionales definidos en la prueba técnica, facilitará la escritura de pruebas automatizadas y permitirá la evolución del sistema sin comprometer la arquitectura establecida.