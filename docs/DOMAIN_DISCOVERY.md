# Domain Discovery

**Proyecto:** Playlist API  
**Documento:** Domain Discovery  
**Versión:** 1.0  
**Estado:** Draft  
**Autor:** Edwin Esteban Henao Velásquez  
**Fecha:** 03/08/2026

---

# 1. Objetivo

El propósito de este documento es identificar los conceptos fundamentales del dominio, comprender sus responsabilidades y establecer un lenguaje común antes de iniciar el modelado del dominio y la implementación.

Este análisis evita que las decisiones técnicas condicionen el diseño del negocio y constituye la base para el posterior Domain Model.

---

# 2. Descripción del Dominio

La solución consiste en una API REST que permite a usuarios autenticados gestionar listas de reproducción musicales.

El sistema permite crear, consultar, modificar y eliminar playlists, administrar las canciones que las componen, consultar información musical mediante Spotify y obtener recomendaciones utilizando un proveedor de Inteligencia Artificial.

El dominio principal está centrado en la gestión de playlists.

Las integraciones con Spotify y con el proveedor de IA enriquecen la funcionalidad del sistema, pero no forman parte del dominio de negocio.

---

# 3. Lenguaje Ubicuo (Ubiquitous Language)

Para evitar ambigüedades durante el desarrollo, se utilizarán los siguientes términos de forma consistente.

| Concepto | Descripción |
|----------|-------------|
| Usuario | Persona autenticada que interactúa con el sistema. |
| Playlist | Colección organizada de canciones administrada por un usuario. |
| Canción | Elemento musical que puede pertenecer a una o varias playlists. |
| Biblioteca | Conjunto de playlists pertenecientes a un usuario. |
| Recomendación | Resultado generado por el proveedor de IA. |
| Spotify | Servicio externo utilizado para consultar información musical. |
| JWT | Mecanismo utilizado para autenticar solicitudes. |

---

# 4. Conceptos del Dominio

## Usuario

### Descripción

Representa la persona autenticada que utiliza la aplicación.

### Responsabilidades

- Crear playlists.
- Consultar playlists.
- Actualizar playlists.
- Eliminar playlists.
- Solicitar recomendaciones.
- Administrar las canciones de sus playlists.

---

## Playlist

### Descripción

Representa una colección lógica de canciones creada por un usuario.

Es el concepto principal del dominio.

### Responsabilidades

- Mantener su identidad.
- Mantener su nombre.
- Administrar las canciones asociadas.
- Garantizar el cumplimiento de las reglas de negocio.

---

## Canción

### Descripción

Representa una obra musical disponible para ser incluida dentro de una playlist.

Puede existir independientemente de una playlist.

### Responsabilidades

- Mantener su información musical.
- Poder pertenecer a una o varias playlists.

---

## Recomendación

### Descripción

Representa el resultado generado por un proveedor de Inteligencia Artificial.

No constituye una entidad principal del dominio.

Su función es asistir al usuario durante la construcción de playlists.

---

# 5. Responsabilidades del Dominio

El dominio deberá ser capaz de:

- Crear playlists.
- Consultar playlists.
- Modificar playlists.
- Eliminar playlists.
- Agregar canciones.
- Remover canciones.
- Mantener la integridad de las playlists.
- Solicitar información musical.
- Solicitar recomendaciones musicales.

---

# 6. Reglas del Negocio Identificadas

A partir del análisis de la prueba se identifican las siguientes reglas de negocio.

## RN-001

Una playlist debe tener un nombre válido.

---

## RN-002

Toda playlist pertenece a un único usuario.

---

## RN-003

Una playlist puede contener múltiples canciones.

---

## RN-004

Una canción puede pertenecer a múltiples playlists.

---

## RN-005

Solo usuarios autenticados pueden modificar playlists.

---

## RN-006

Las recomendaciones generadas por IA no modifican automáticamente una playlist.

Su incorporación siempre requiere una acción explícita del usuario.

---

## RN-007

Spotify actúa únicamente como fuente de información.

No modifica directamente el dominio.

---

# 7. Casos de Negocio Identificados

Durante el descubrimiento del dominio se identifican las siguientes capacidades principales.

- Crear playlist.
- Consultar playlist.
- Listar playlists.
- Actualizar playlist.
- Eliminar playlist.
- Agregar canción.
- Eliminar canción.
- Consultar información musical.
- Solicitar recomendación musical.

Estos casos serán desarrollados posteriormente durante la etapa de diseño de casos de uso.

---

# 8. Eventos del Dominio

Los siguientes eventos representan cambios significativos dentro del sistema.

- Playlist creada.
- Playlist actualizada.
- Playlist eliminada.
- Canción agregada a una playlist.
- Canción eliminada de una playlist.
- Recomendación solicitada.
- Recomendación generada.

Estos eventos permiten comprender la evolución del estado del dominio independientemente de su implementación técnica.

---

# 9. Servicios del Dominio

Se identifican responsabilidades que probablemente serán implementadas como servicios de dominio.

## Gestión de Playlists

Responsable de coordinar las operaciones principales sobre playlists.

---

## Gestión de Recomendaciones

Responsable de solicitar recomendaciones al proveedor de IA.

---

## Consulta Musical

Responsable de obtener información musical desde Spotify.

---

# 10. Sistemas Externos

Los siguientes componentes colaboran con el dominio pero no forman parte de él.

## Spotify

Responsabilidad:

Proporcionar información musical requerida por la aplicación.

Interacción prevista:

Dominio → Puerto → Adaptador Spotify → Spotify API

---

## Proveedor de Inteligencia Artificial

Responsabilidad:

Generar recomendaciones musicales.

Interacción prevista:

Dominio → Puerto → Adaptador IA → Modelo de IA

---

## JWT

Responsabilidad:

Autenticar y autorizar solicitudes.

Pertenece completamente a la infraestructura.

No contiene lógica del negocio.

---

# 11. Límites del Dominio (Bounded Context)

## Dentro del dominio

- Usuario.
- Playlist.
- Canción.
- Reglas de negocio.
- Gestión de playlists.

## Fuera del dominio

- HTTP.
- Spring Boot.
- Spring Security.
- JWT.
- Spotify.
- Inteligencia Artificial.
- Persistencia.
- H2.
- JPA.

Estos componentes serán responsabilidad de la infraestructura definida por la Arquitectura Hexagonal.

---

# 12. Supuestos del Dominio

Durante el descubrimiento se adoptan los siguientes supuestos.

- Cada playlist pertenece a un único usuario.
- Una playlist puede existir aunque inicialmente no contenga canciones.
- Spotify constituye la fuente principal de información musical.
- Las recomendaciones son opcionales para el usuario.
- El sistema no modifica automáticamente la información obtenida desde Spotify.

Estos supuestos podrán ajustarse si durante la implementación aparecen nuevos requisitos.

---

# 13. Preguntas Abiertas

Durante el análisis permanecen abiertas las siguientes decisiones.

- ¿Se permiten playlists con nombres repetidos para un mismo usuario?
- ¿Existe un límite máximo de canciones por playlist?
- ¿Puede una playlist quedar vacía?
- ¿Debe mantenerse un historial de modificaciones?
- ¿Las recomendaciones pueden almacenarse?
- ¿Qué comportamiento debe tener el sistema cuando Spotify no responda?

Estas preguntas serán respondidas durante el diseño detallado del dominio.

---

# 14. Conclusiones

El análisis confirma que el núcleo del negocio está compuesto por tres conceptos principales:

- Usuario.
- Playlist.
- Canción.

Spotify, el proveedor de Inteligencia Artificial y el mecanismo de autenticación JWT representan dependencias externas que deberán integrarse mediante puertos y adaptadores, manteniendo el dominio completamente independiente de las tecnologías utilizadas.

---
