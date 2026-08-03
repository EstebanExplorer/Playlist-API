# Requirements Analysis

**Proyecto:** Playlist API  
**Documento:** Requirements Analysis  
**Versión:** 1.0  
**Estado:** Draft  
**Autor:** Edwin Esteban Henao Velásquez  
**Fecha:** 03/08/2026

---

# 1. Propósito

Este documento consolida el análisis funcional y técnico de la prueba técnica con el objetivo de establecer una comprensión común del problema antes de iniciar el diseño arquitectónico y el desarrollo.

Su finalidad es identificar requisitos funcionales y no funcionales, restricciones, riesgos, supuestos y criterios de aceptación, garantizando la trazabilidad entre el enunciado de la prueba y la solución implementada.

Este documento constituye la fuente de referencia para las decisiones de arquitectura, el modelado del dominio y el diseño de la API.

---

# 2. Objetivos de la Solución

La solución deberá:

- Implementar una API REST para la gestión de listas de reproducción.
- Proteger los recursos mediante autenticación basada en JWT.
- Persistir la información utilizando JPA sobre una base de datos H2.
- Integrar información musical proveniente de Spotify.
- Incorporar un endpoint que utilice un proveedor de Inteligencia Artificial.
- Demostrar buenas prácticas de arquitectura, diseño y desarrollo.
- Incluir pruebas automatizadas que validen el comportamiento esperado.
- Proporcionar la documentación necesaria para ejecutar y evaluar la solución.

---

# 3. Alcance

## Incluido

- API REST.
- Gestión de listas de reproducción.
- Gestión de canciones.
- Autenticación JWT.
- Integración con Spotify.
- Integración con IA.
- Persistencia local mediante H2.
- Pruebas unitarias.
- Documentación técnica.

## Fuera del alcance

- Despliegue en producción.
- Alta disponibilidad.
- Escalamiento horizontal.
- Balanceadores de carga.
- Observabilidad distribuida.
- Contenerización obligatoria.
- Persistencia distribuida.
- Microservicios.

---

# 4. Stakeholders

| Actor | Interés |
|---------|----------|
| Usuario | Gestionar playlists mediante la API |
| Spotify API | Proveedor de información musical |
| Proveedor IA | Generación de recomendaciones |
| Evaluador Técnico | Validar calidad técnica de la solución |

---

# 5. Requisitos Funcionales

## RF-001

El sistema deberá permitir la autenticación de usuarios mediante JWT.

---

## RF-002

El sistema deberá permitir crear listas de reproducción.

---

## RF-003

El sistema deberá permitir consultar listas de reproducción.

---

## RF-004

El sistema deberá permitir actualizar listas de reproducción.

---

## RF-005

El sistema deberá permitir eliminar listas de reproducción.

---

## RF-006

El sistema deberá permitir agregar canciones a una lista.

---

## RF-007

El sistema deberá permitir eliminar canciones de una lista.

---

## RF-008

El sistema deberá consultar información musical utilizando Spotify.

---

## RF-009

El sistema deberá generar recomendaciones utilizando un proveedor de Inteligencia Artificial.

---

## RF-010

El sistema deberá persistir la información mediante JPA utilizando H2.

---

## RF-011

El sistema deberá incluir pruebas unitarias.

---

## RF-012

El sistema deberá incluir documentación de uso.

---

# 6. Requisitos No Funcionales

## RNF-001 Seguridad

Las credenciales no deberán almacenarse dentro del código fuente.

---

## RNF-002 Testabilidad

La arquitectura deberá permitir pruebas unitarias mediante desacoplamiento de dependencias.

---

## RNF-003 Mantenibilidad

La solución deberá presentar una estructura clara y modular.

---

## RNF-004 Escalabilidad

La arquitectura deberá facilitar futuras ampliaciones funcionales.

---

## RNF-005 Calidad del Código

El código deberá seguir principios SOLID y buenas prácticas de ingeniería.

---

## RNF-006 Documentación

La solución deberá incluir documentación suficiente para su comprensión y ejecución.

---

## RNF-007 Resiliencia

La integración con servicios externos deberá manejar errores de forma controlada.

---

## RNF-008 Independencia Tecnológica

La lógica del dominio no deberá depender directamente de frameworks o servicios externos.

---

# 7. Restricciones Técnicas

| Restricción | Justificación |
|--------------|--------------|
| Java | Requisito de la prueba |
| Spring Boot | Framework esperado |
| Maven | Sistema de construcción |
| Spring Security | Implementación de seguridad |
| JWT | Autenticación obligatoria |
| JPA | Persistencia requerida |
| H2 | Base de datos requerida |
| Spotify API | Integración requerida |
| IA | Integración requerida |
| Pruebas Unitarias | Requisito obligatorio |

---

# 8. Supuestos

Durante el análisis se consideran los siguientes supuestos:

- Existe un único sistema consumidor de la API.
- Spotify mantiene disponibilidad durante las pruebas.
- El proveedor de IA responde dentro de tiempos razonables.
- La base de datos H2 es suficiente para el alcance de la prueba.
- No se requieren mecanismos avanzados de autorización distintos al uso de JWT.

---

# 9. Ambigüedades Identificadas

Durante el análisis se identificaron algunos aspectos no completamente especificados:

- No se define el modelo exacto de usuario.
- No se especifican reglas de negocio para las playlists.
- No se indican límites sobre la cantidad de canciones por lista.
- No se define el comportamiento esperado ante fallos de Spotify.
- No se especifica la estrategia de manejo de errores.
- No se establecen requisitos mínimos de cobertura de pruebas.
- No se especifica un proveedor concreto para la integración de IA.

Estas decisiones serán documentadas durante las fases de diseño arquitectónico.

---

# 10. Riesgos de Requisitos

| Riesgo | Impacto | Mitigación |
|---------|----------|------------|
| Ambigüedad en reglas de negocio | Alto | Documentar supuestos y decisiones |
| Cambios en APIs externas | Medio | Desacoplar mediante interfaces |
| Requisitos implícitos no identificados | Alto | Mantener trazabilidad con el enunciado |
| Exceso de complejidad | Medio | Mantener el alcance limitado a la prueba |

---

# 11. Criterios de Aceptación

| Requisito | Criterio de aceptación |
|------------|-----------------------|
| RF-001 | El usuario puede autenticarse y obtener un JWT válido. |
| RF-002 | Es posible crear una playlist mediante la API. |
| RF-003 | Es posible consultar playlists existentes. |
| RF-004 | Es posible actualizar una playlist. |
| RF-005 | Es posible eliminar una playlist. |
| RF-006 | Es posible agregar canciones a una playlist. |
| RF-007 | Es posible eliminar canciones de una playlist. |
| RF-008 | La API obtiene información desde Spotify correctamente. |
| RF-009 | La API genera recomendaciones mediante IA. |
| RF-010 | Los datos permanecen almacenados en H2 utilizando JPA. |
| RF-011 | Existen pruebas unitarias ejecutables. |
| RF-012 | El proyecto incluye documentación suficiente para su ejecución. |

---

# 12. Trazabilidad

| Documento | Propósito |
|------------|-----------|
| Requirements Analysis | Comprensión del problema |
| Competency Assessment | Identificar habilidades evaluadas |
| Architecture Decision Record | Justificar decisiones técnicas |
| Solution Blueprint | Definir la estructura de alto nivel |
| Domain Discovery | Descubrir conceptos del negocio |
| Domain Model | Modelar entidades y relaciones |
| Use Cases | Definir comportamiento funcional |
| API Contract | Diseñar la interfaz REST |

---
