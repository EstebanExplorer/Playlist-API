# Playlist API

API REST para la gestión de playlists musicales, diseñada siguiendo los principios de **Arquitectura Hexagonal (Ports & Adapters)**, **Domain-Driven Design (DDD)**, **Clean Architecture** y **SOLID**.

La solución está construida con una arquitectura desacoplada, orientada al dominio y preparada para integrar proveedores externos como Spotify y servicios de Inteligencia Artificial, manteniendo un alto nivel de mantenibilidad, escalabilidad y testabilidad.

---

# Estado del Proyecto

## ✅ Etapa 1 — Diseño de la Arquitectura

Completada.

Incluye:

- Análisis de requisitos
- Modelo de dominio
- Arquitectura Hexagonal
- Arquitectura de la solución
- ADR (Architecture Decision Records)
- Contrato de la API
- Documentación técnica

---

## ✅ Etapa 2 — Capa Domain

Completada y validada.

Implementación del núcleo del negocio mediante Java puro.

Incluye:

- Agregados
- Entidades
- Value Objects
- Domain Services
- Domain Events
- Repositorios (Output Ports)
- Excepciones de dominio

Características:

- Sin dependencias de Spring
- Sin JPA
- Sin infraestructura
- Dominio completamente aislado

---

## ✅ Etapa 3 — Capa Application

Completada y validada.

Implementación de todos los casos de uso definidos para la solución.

### Casos de Uso Implementados

| Código | Caso de Uso | Estado |
|---------|-------------|--------|
| UC-001 | Authenticate User | ✅ |
| UC-002 | Create Playlist | ✅ |
| UC-003 | Get Playlist By Id | ✅ |
| UC-004 | List User Playlists | ✅ |
| UC-005 | Update Playlist Name | ✅ |
| UC-006 | Delete Playlist | ✅ |
| UC-007 | Add Song To Playlist | ✅ |
| UC-008 | Remove Song From Playlist | ✅ |
| UC-009 | Search Songs | ✅ |
| UC-010 | Generate AI Recommendations | ✅ |

La capa Application fue desarrollada bajo los siguientes principios:

- Arquitectura Hexagonal
- CQRS
- Constructor Injection
- Java Records (Java 21)
- Java puro
- Sin Spring Framework
- Sin JPA
- Sin Hibernate
- Sin Controllers
- Sin dependencias de infraestructura

---

# Cobertura Funcional

Actualmente el proyecto implementa completamente:

- ✅ Modelo de Dominio
- ✅ Capa Application
- ✅ Los 10 casos de uso definidos
- ✅ Pruebas unitarias
- ✅ Arquitectura Hexagonal
- ✅ Domain-Driven Design

---

# Arquitectura

```
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

Las dependencias siempre apuntan hacia el dominio, garantizando un núcleo de negocio independiente de frameworks y tecnologías externas.

---

# Tecnologías

- Java 21
- Maven
- JUnit 5
- Mockito
- Arquitectura Hexagonal
- Domain-Driven Design (DDD)
- Clean Architecture

---

# Principios Aplicados

- SOLID
- DRY
- KISS
- CQRS
- Fail Fast
- Tell, Don't Ask
- Dependency Inversion
- Aggregate Root
- Inmutabilidad
- Constructor Injection

---

# Pruebas

Todos los casos de uso cuentan con pruebas unitarias implementadas utilizando:

- JUnit 5
- Mockito

Características de la suite de pruebas:

- Sin SpringBootTest
- Sin contexto de Spring
- Mocks mediante Mockito
- Verificación de interacciones
- Escenarios exitosos
- Casos límite
- Validaciones Fail-Fast

---

# Próximas Etapas

## Etapa 4 — Infrastructure

Pendiente de implementación.

Incluirá:

- Adaptadores de persistencia
- Implementación JPA
- Integración con Spotify API
- Integración con proveedor de IA
- JWT
- BCrypt
- Configuración de persistencia

---

## Etapa 5 — Presentation

Pendiente de implementación.

Incluirá:

- REST Controllers
- DTOs HTTP
- Bean Validation
- Global Exception Handler
- OpenAPI / Swagger
- Configuración de seguridad

---

# Objetivos de Calidad

El proyecto sigue una estrategia de desarrollo incremental priorizando:

- Arquitectura desacoplada
- Alta cohesión
- Bajo acoplamiento
- Testabilidad
- Escalabilidad
- Mantenibilidad
- Evolución por capas

---

# Estructura del Proyecto

```
src
└── main
    └── java
        └── com.esteban.playlistapi
            ├── domain
            ├── application
            ├── infrastructure
            └── presentation
```

---

# Autor

**Edwin Esteban Henao Velásquez**