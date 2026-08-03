# Solution Blueprint

**Proyecto:** Playlist API  
**Autor:** Edwin Esteban Henao Velásquez  
**Fecha:** 03/08/2026  
**Versión:** 1.0

---

# 1. Objetivo

Este documento describe la composición de alto nivel de la solución propuesta para la prueba técnica.

Su propósito es definir la estructura general del sistema antes de iniciar el modelado del dominio y la implementación, asegurando que las decisiones arquitectónicas registradas en el Architecture Decision Record (ADR) puedan materializarse de forma consistente.

Este documento no define clases, entidades, controladores ni detalles de implementación.

---

# 2. Objetivos Arquitectónicos

La solución se diseña priorizando los siguientes atributos de calidad:

- Bajo acoplamiento.
- Alta cohesión.
- Testabilidad.
- Mantenibilidad.
- Seguridad.
- Separación de responsabilidades.
- Independencia de frameworks.
- Facilidad para integrar servicios externos.

---

# 3. Alcance del Sistema

La aplicación permitirá:

- Autenticar usuarios mediante JWT.
- Administrar listas de reproducción.
- Gestionar canciones asociadas a las listas.
- Consultar información musical desde Spotify.
- Generar recomendaciones mediante un proveedor de Inteligencia Artificial.
- Persistir la información utilizando JPA sobre H2.

---

# 4. Vista de Contexto

```text
                    Usuario
                        │
                  HTTP / JSON
                        │
              ┌─────────────────┐
              │   Playlist API  │
              └─────────────────┘
                │      │      │
                │      │      │
             JWT   Spotify   IA
                │      │      │
                └──────┼──────┘
                       │
                      H2
```

El usuario interactúa exclusivamente mediante la API REST.

La aplicación actúa como orquestador entre el dominio y los servicios externos.

---

# 5. Componentes Principales

## Presentation Layer

Responsabilidades:

- Exponer endpoints REST.
- Validar solicitudes.
- Transformar DTOs.
- Gestionar códigos HTTP.
- Delegar la ejecución a los casos de uso.

No contiene reglas de negocio.

---

## Application Layer

Responsabilidades:

- Ejecutar casos de uso.
- Coordinar operaciones.
- Aplicar reglas de aplicación.
- Orquestar el dominio.

No conoce detalles de infraestructura.

---

## Domain Layer

Es el núcleo del sistema.

Responsabilidades:

- Reglas de negocio.
- Objetos del dominio.
- Servicios de dominio.
- Interfaces (Ports).

No depende de:

- Spring Boot.
- Hibernate.
- Spotify.
- JWT.
- Inteligencia Artificial.
- Base de datos.

---

## Infrastructure Layer

Implementa las dependencias externas.

Responsabilidades:

- Persistencia JPA.
- Cliente Spotify.
- Seguridad JWT.
- Integración con IA.
- Configuración de Spring.
- Adaptadores.

No contiene reglas de negocio.

---

# 6. Dependencias Arquitectónicas

La dirección de las dependencias será siempre hacia el dominio.

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

El dominio nunca dependerá de tecnologías externas.

Las implementaciones de infraestructura dependerán de las interfaces definidas por el dominio.

---

# 7. Integraciones Externas

## Spotify

Responsabilidad:

Obtener información musical requerida por la aplicación.

Interacción:

```
Caso de Uso
      │
Puerto de Salida
      │
Spotify Adapter
      │
Spotify Web API
```

---

## Proveedor de Inteligencia Artificial

Responsabilidad:

Generar recomendaciones musicales.

Interacción:

```
Caso de Uso
      │
Puerto de Salida
      │
AI Adapter
      │
Proveedor IA
```

La implementación concreta podrá cambiar sin afectar el dominio.

---

## JWT

Responsabilidad:

Autenticar y autorizar solicitudes.

Interacción:

```
Cliente
   │
JWT Filter
   │
Spring Security
   │
Casos de Uso
```

---

# 8. Flujo General del Sistema

## Flujo CRUD

```text
Cliente

↓

REST Controller

↓

Caso de Uso

↓

Repositorio (Puerto)

↓

Repositorio JPA (Adaptador)

↓

H2
```

---

## Flujo Spotify

```text
Cliente

↓

REST Controller

↓

Caso de Uso

↓

Puerto Spotify

↓

Spotify Adapter

↓

Spotify API
```

---

## Flujo IA

```text
Cliente

↓

REST Controller

↓

Caso de Uso

↓

Puerto IA

↓

AI Adapter

↓

Proveedor IA
```

---

# 9. Principios Arquitectónicos

Durante todo el desarrollo se respetarán los siguientes principios:

- El dominio no depende del framework.
- Toda integración externa se implementa mediante puertos y adaptadores.
- Ninguna entidad del dominio será expuesta directamente mediante la API.
- Las responsabilidades estarán claramente separadas.
- Las dependencias apuntarán siempre hacia el dominio.
- La infraestructura podrá reemplazarse sin modificar la lógica de negocio.
- La solución deberá ser fácilmente testeable mediante inversión de dependencias.

---

# 10. Correspondencia con las Decisiones Arquitectónicas (ADR)

| ADR | Impacto en la Solución |
|------|-------------------------|
| ADR-001 | Define la Arquitectura Hexagonal |
| ADR-002 | Establece Spring Boot como framework principal |
| ADR-003 | Define la estrategia de persistencia |
| ADR-004 | Establece JWT como mecanismo de autenticación |
| ADR-005 | Define el adaptador para Spotify |
| ADR-006 | Define el adaptador para IA |
| ADR-007 | Establece el uso de DTOs |

---

# 11. Componentes Pendientes de Diseño

Las siguientes actividades corresponden a las próximas etapas del proyecto:

- Descubrimiento del dominio.
- Modelo del dominio.
- Casos de uso.
- Contratos REST.
- Modelo de persistencia.
- Estrategia de pruebas.
- Diseño del frontend.

---
