# Test Strategy

**Proyecto:** Playlist API  
**Documento:** Test Strategy  
**Versión:** 1.0  
**Estado:** Final  
**Autor:** Edwin Esteban Henao Velásquez  
**Fecha:** 03/08/2026

---

# 1. Objetivo

Este documento define la estrategia de pruebas que será utilizada durante el desarrollo de la Playlist API.

Su propósito es garantizar que la solución cumpla los requisitos funcionales, no funcionales y los criterios de evaluación de la prueba técnica mediante una combinación de pruebas unitarias, de integración y de resiliencia.

La estrategia busca detectar defectos de forma temprana y asegurar que el comportamiento del sistema permanezca estable frente a cambios futuros.

---

# 2. Objetivos de Calidad

Las pruebas deberán validar los siguientes aspectos:

- Correctitud funcional.
- Integridad del dominio.
- Seguridad mediante JWT.
- Integraciones con servicios externos.
- Manejo consistente de errores.
- Resiliencia ante fallos.
- Independencia de infraestructura.
- Cobertura adecuada de la lógica de negocio.

---

# 3. Alcance

Se probarán las siguientes capas del sistema.

| Capa | Tipo de prueba |
|------|----------------|
| Domain | Unitarias |
| Application | Unitarias |
| Presentation | Integración |
| Infrastructure | Integración |
| Seguridad | Integración |
| Adaptadores externos | Mock + Integración |

---

# 4. Pirámide de Pruebas

La estrategia seguirá la pirámide clásica de pruebas.

```text
                 End-to-End
                     ▲
              Integration Tests
                     ▲
                Unit Tests
```

La mayor cantidad de pruebas se concentrará en la capa de dominio.

---

# 5. Estrategia por Capa

## Domain

Se validará:

- Reglas de negocio.
- Objetos de Valor.
- Servicios del dominio.
- Eventos del dominio.
- Invariantes.

No dependerá de Spring Boot.

---

## Application

Se probarán:

- Casos de uso.
- Coordinación entre componentes.
- Validaciones.
- Manejo de errores.

Las dependencias externas serán simuladas mediante mocks.

---

## Infrastructure

Se validará:

- Persistencia JPA.
- Integración con H2.
- JWT.
- Adaptadores.
- Configuración.

---

## Presentation

Se probará:

- Endpoints REST.
- Códigos HTTP.
- Validaciones.
- Serialización JSON.
- Manejo de excepciones.

---

# 6. Estrategia para APIs Externas

Las APIs externas nunca serán utilizadas directamente en las pruebas unitarias.

Se utilizarán mocks para:

- Spotify.
- Proveedor de Inteligencia Artificial.

Se validarán escenarios como:

- Respuesta exitosa.
- Timeout.
- Error HTTP.
- Respuesta inválida.
- Servicio no disponible.

---

# 7. Estrategia de Seguridad

Se verificarán los siguientes escenarios.

## JWT válido

Resultado esperado:

Acceso permitido.

---

## JWT inválido

Resultado esperado:

HTTP 401 Unauthorized.

---

## Token expirado

Resultado esperado:

HTTP 401 Unauthorized.

---

## Usuario sin permisos

Resultado esperado:

HTTP 403 Forbidden.

---

# 8. Escenarios Críticos

Los siguientes casos tendrán prioridad alta.

## Playlist

- Crear playlist.
- Actualizar playlist.
- Eliminar playlist.
- Consultar playlist.

---

## Canciones

- Agregar canción.
- Eliminar canción.

---

## Spotify

- Consulta exitosa.
- Timeout.
- Error del proveedor.

---

## IA

- Recomendación exitosa.
- Timeout.
- Error del proveedor.
- Respuesta vacía.

---

# 9. Manejo de Errores

Se validarán:

- Recursos inexistentes.
- Datos inválidos.
- Errores de validación.
- Excepciones del dominio.
- Excepciones de persistencia.
- Errores de autenticación.
- Errores de autorización.
- Errores de servicios externos.

---

# 10. Datos de Prueba

Se utilizarán datos controlados.

Ejemplos:

- Usuarios ficticios.
- Playlists de prueba.
- Canciones simuladas.
- Tokens JWT de prueba.

Las pruebas deberán ser independientes entre sí.

---

# 11. Herramientas

Las pruebas se implementarán utilizando:

- JUnit 5
- Mockito
- Spring Boot Test
- MockMvc
- H2 Database
- JaCoCo

---

# 12. Cobertura Esperada

| Capa | Cobertura Objetivo |
|------|--------------------|
| Domain | ≥ 90 % |
| Application | ≥ 85 % |
| Infrastructure | ≥ 70 % |
| Presentation | ≥ 70 % |

La prioridad será cubrir completamente la lógica de negocio crítica.

---

# 13. Trazabilidad

| Caso de Uso | Pruebas |
|-------------|---------|
| UC-001 | Autenticación |
| UC-002 | Crear Playlist |
| UC-003 | Consultar Playlist |
| UC-004 | Listar Playlists |
| UC-005 | Actualizar Playlist |
| UC-006 | Eliminar Playlist |
| UC-007 | Agregar Canción |
| UC-008 | Eliminar Canción |
| UC-009 | Consulta Spotify |
| UC-010 | Recomendaciones IA |

Cada caso de uso deberá contar al menos con un escenario exitoso y un escenario de error.

---

# 14. Criterios de Aceptación

La implementación se considerará aceptable cuando:

- Todos los casos de uso críticos tengan pruebas automatizadas.
- Los servicios externos estén desacoplados mediante mocks.
- Las reglas del dominio estén completamente cubiertas.
- Los errores se gestionen de forma consistente.
- La autenticación y autorización funcionen correctamente.
- La cobertura alcance los objetivos definidos.

---

# 15. Riesgos Identificados

| Riesgo | Mitigación |
|---------|------------|
| Caída de Spotify | Mock + manejo de excepciones |
| Fallo del proveedor IA | Mock + timeout + respuesta controlada |
| Errores de autenticación | Pruebas JWT |
| Cambios en APIs externas | Adaptadores desacoplados |
| Regresiones | Pruebas automatizadas |

---

# 16. Relación con la Documentación

Esta estrategia implementa los lineamientos definidos en:

- 01_REQUIREMENTS_ANALYSIS.md
- 02_ARCHITECTURE_DECISION_RECORD.md
- 03_SOLUTION_BLUEPRINT.md
- 04_DOMAIN_DISCOVERY.md
- 05_DOMAIN_MODEL.md
- 06_USE_CASES.md
- 07_API_CONTRACT.md
- 09_PROJECT_STRUCTURE.md

Las pruebas deberán mantenerse alineadas con estos documentos durante todo el desarrollo.

---

