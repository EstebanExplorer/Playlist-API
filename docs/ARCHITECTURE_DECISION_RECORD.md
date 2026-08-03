# Architecture Decision Record (ADR)

**Proyecto:** Playlist API  
**Autor:** Edwin Esteban Henao Velásquez  
**Fecha:** 03/08/2026  
**Estado:** En definición

---

# 1. Objetivo

Este documento registra las decisiones arquitectónicas tomadas antes del desarrollo de la solución.

Su propósito es justificar las tecnologías, patrones y enfoques seleccionados para cumplir los requerimientos de la prueba técnica, reducir la incertidumbre del diseño y facilitar futuras revisiones técnicas.

Este documento complementa el README y el AI-LOG, pero no reemplaza ninguno de ellos.

---

# 2. Restricciones Arquitectónicas

Las siguientes restricciones provienen directamente del enunciado de la prueba técnica.

| Restricción | Origen | Impacto Arquitectónico |
|-------------|--------|------------------------|
| Backend desarrollado en Java | Prueba técnica | Se adopta Java como lenguaje principal |
| Maven como sistema de construcción | Prueba técnica | Se utilizará estructura estándar Maven |
| Persistencia mediante JPA | Prueba técnica | Se desacopla el dominio de la implementación ORM |
| Base de datos H2 | Prueba técnica | No se requiere infraestructura externa |
| Autenticación mediante JWT | Prueba técnica | Arquitectura Stateless |
| Integración con Spotify API | Prueba técnica | Se requiere un adaptador para un servicio externo |
| Endpoint de Inteligencia Artificial | Prueba técnica | Se desacopla el proveedor mediante interfaces |
| Pruebas unitarias | Prueba técnica | El diseño debe favorecer la testabilidad |
| Frontend básico | Prueba técnica | La API debe ser fácilmente consumible |

---

# 3. Drivers Arquitectónicos

Los siguientes atributos de calidad guiarán todas las decisiones de diseño.

| Driver | Prioridad |
|----------|-----------|
| Mantenibilidad | Muy Alta |
| Testabilidad | Muy Alta |
| Bajo acoplamiento | Muy Alta |
| Seguridad | Muy Alta |
| Simplicidad | Alta |
| Extensibilidad | Alta |
| Escalabilidad | Media |
| Rendimiento | Media |

---

# 4. Riesgos Arquitectónicos

| Riesgo | Probabilidad | Impacto | Estrategia de Mitigación |
|---------|--------------|----------|--------------------------|
| Indisponibilidad de Spotify | Alta | Alta | Adaptador desacoplado y manejo centralizado de errores |
| Expiración del token Spotify | Alta | Media | Renovación automática del token |
| Configuración incorrecta de JWT | Media | Alta | Variables de entorno y configuración centralizada |
| Acoplamiento con Spring Framework | Media | Media | Dominio independiente del framework |
| Baja cobertura de pruebas | Alta | Alta | Arquitectura basada en inversión de dependencias |
| Exposición de credenciales | Baja | Muy Alta | Variables de entorno y exclusión del repositorio |
| Cambios futuros del proveedor IA | Media | Alta | Abstracción mediante interfaces |

---

# 5. Decisiones Arquitectónicas (ADR)

---

## ADR-001

### Título

Arquitectura Hexagonal (Ports & Adapters)

### Estado

Aceptada

### Contexto

La solución requiere integración con múltiples sistemas externos (Spotify, JWT, IA y persistencia) sin acoplar el dominio a tecnologías específicas.

### Decisión

Adoptar Arquitectura Hexagonal.

### Justificación

Permite mantener el dominio independiente de:

- Spring Boot
- JPA
- Spotify
- Proveedor de IA
- Base de datos

Facilita las pruebas unitarias y el reemplazo de adaptadores sin afectar la lógica de negocio.

### Consecuencias

**Ventajas**

- Bajo acoplamiento
- Alta mantenibilidad
- Excelente testabilidad
- Fácil evolución

**Desventajas**

- Mayor número de clases
- Curva de aprendizaje superior

---

## ADR-002

### Título

Spring Boot como framework principal

### Estado

Aceptada

### Contexto

La prueba requiere construir rápidamente una API REST segura y mantenible.

### Decisión

Utilizar Spring Boot.

### Justificación

- Ecosistema maduro
- Integración con Spring Security
- Integración con Spring Data JPA
- Productividad
- Amplia documentación

---

## ADR-003

### Título

Persistencia mediante Spring Data JPA

### Estado

Aceptada

### Contexto

La prueba exige persistencia utilizando JPA.

### Decisión

Utilizar Spring Data JPA sobre Hibernate.

### Justificación

Reduce código repetitivo.

Permite mantener la lógica de negocio independiente del mecanismo de persistencia.

---

## ADR-004

### Título

Autenticación Stateless mediante JWT

### Estado

Aceptada

### Contexto

La autenticación forma parte de los requisitos.

### Decisión

Implementar autenticación basada en JWT.

### Justificación

- No requiere sesiones
- Escalable
- Compatible con APIs REST

---

## ADR-005

### Título

Spotify como adaptador externo

### Estado

Aceptada

### Contexto

Spotify representa una dependencia externa.

### Decisión

Encapsular toda comunicación dentro de un puerto de salida.

### Justificación

El dominio no debe depender de una API específica.

Esto permitirá:

- Mockear Spotify durante pruebas.
- Reemplazar el proveedor en el futuro.
- Centralizar manejo de errores.

---

## ADR-006

### Título

Proveedor de IA desacoplado

### Estado

Aceptada

### Contexto

La prueba requiere consumir un modelo de IA.

### Decisión

Crear una interfaz para el proveedor IA.

### Justificación

Evita dependencias directas con OpenAI, Gemini, Ollama u otros proveedores.

---

## ADR-007

### Título

Mapeo mediante DTO

### Estado

Aceptada

### Contexto

Las entidades del dominio no deben exponerse directamente.

### Decisión

Separar:

- Entidades
- DTO
- Objetos de respuesta

### Justificación

Mayor encapsulamiento.

Mayor estabilidad del contrato REST.

---

# 6. Stack Tecnológico

| Categoría | Tecnología | Justificación |
|-----------|------------|---------------|
| Lenguaje | Java 21 LTS | Versión estable y moderna |
| Framework | Spring Boot | Productividad y ecosistema |
| Build | Maven | Requisito de la prueba |
| Persistencia | Spring Data JPA | Integración con Hibernate |
| Base de datos | H2 | Requisito de la prueba |
| Seguridad | Spring Security + JWT | Seguridad estándar |
| Testing | JUnit 5 + Mockito | Estándar de facto |
| Documentación | OpenAPI / Swagger | Facilita la evaluación |
| Mapping | MapStruct | Reduce código repetitivo |
| Logging | SLF4J + Logback | Estándar empresarial |

---

# 7. Alternativas Evaluadas

| Alternativa | Resultado | Motivo |
|--------------|-----------|--------|
| Jakarta EE | Descartada | Mayor configuración manual |
| Micronaut | Descartada | No aporta ventajas significativas para esta prueba |
| Quarkus | Descartada | Optimizado para otros escenarios |
| JDBC puro | Descartada | Incrementa complejidad y código repetitivo |
| Sesiones HTTP | Descartada | Contradice arquitectura REST Stateless |

---

# 8. Principios de Diseño

Durante el desarrollo se respetarán los siguientes principios:

- SOLID
- Clean Code
- Clean Architecture
- Arquitectura Hexagonal
- Separation of Concerns
- Dependency Inversion
- DRY
- KISS
- YAGNI
- Composition over Inheritance
- Fail Fast

---

# 9. Decisiones Pendientes

Las siguientes decisiones serán tomadas durante las siguientes fases del proyecto:

- Modelo del dominio.
- Diseño detallado de entidades.
- Diseño de casos de uso.
- Contratos REST.
- Estrategia de manejo de excepciones.
- Estrategia de pruebas.
- Organización final de paquetes.

---

# 10. Próxima Etapa

Una vez definidas las decisiones arquitectónicas, el siguiente paso será elaborar el **Blueprint del Sistema**, donde se representarán los componentes principales y sus relaciones sin entrar aún en el diseño de clases o implementación.