# API Macronnect — Sistema de Ventas

API REST para administrar clientes, artículos y ventas, con autenticación JWT. Desarrollada como prueba técnica para el puesto de Backend Java / Spring Boot (nivel Junior) en Macronnect.

- Java 8
- Spring Boot 2.7.18 (Web, Data JPA, Validation, Security)
- MySQL 8 (persistencia)
- Maven (con Maven Wrapper incluido)
- JWT vía jjwt 0.11.5
- *pringdoc-openapi 1.7.0 (Swagger / OpenAPI)
- JUnit 5 + Mockito

## Requisitos previos

- **JDK 8 instalado y `JAVA_HOME` configurado. (No hace falta instalar Maven: se usa el wrapper `mvnw`.)
- MySQL Server 8 corriendo en `localhost:3306`.

## Configuración de la base de datos

Crear la base y un usuario dedicado (principio de mínimo privilegio: la app no se conecta como `root`):

```sql
CREATE DATABASE ventas_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ventas_user'@'localhost' IDENTIFIED BY 'cambia_esta_password';
GRANT ALL PRIVILEGES ON ventas_db.* TO 'ventas_user'@'localhost';
FLUSH PRIVILEGES;
```

Las tablas se crean automáticamente al arrancar (`spring.jpa.hibernate.ddl-auto=update`).

## Configuración de la aplicación

La configuración vive en `src/main/resources/application.properties` y admite sobreescritura por variables de entorno no hay que hardcodear las cosas:

| Variable | Descripción | Default (solo desarrollo) |
|---|---|---|
| `DB_USER` | Usuario de MySQL | `ventas_user` |
| `DB_PASSWORD` | Contraseña de MySQL | `cambia_esta_password` |
| `JWT_SECRET` | Clave Base64 de 256 bits para firmar los tokens | (valor de desarrollo incluido) |
| `ADMIN_USER` | Usuario administrador sembrado al arranque | `admin` |
| `ADMIN_PASSWORD` | Contraseña del administrador | `admin123` |

## Cómo levantar el proyecto

Windows:

```
.\mvnw.cmd spring-boot:run
```

La API queda en `http://localhost:8080`. Al arrancar, un seeder crea el usuario administrador y la secuencia de folios.

## Cómo correr los tests

```
.\mvnw.cmd test
```

Ejecuta las pruebas unitarias de la lógica de negocio (`VentaServiceTest`, con Mockito, sin base de datos).

> El test `ApiMacronnectApplicationTests.contextLoads()` (generado por Spring Initializr) levanta el contexto completo y requiere MySQL corriendo. Las pruebas unitarias de `VentaService` no dependen de la base de datos para no tocar la bd innecesariamente.

## Autenticación

1. Obtener token: `POST /auth/login` con `{ "username": "admin", "password": "admin123" }`.
2. La respuesta incluye un JWT. Enviarlo en el header `Authorization: Bearer <token>` en todos los demás endpoints.
3. El token expira en 1 hora (configurable en `app.jwt.expiration-ms`).

## Documentación (Swagger)

Con la app corriendo: `http://localhost:8080/swagger-ui.html`

Usar el botón Authorize (el del candadito) para pegar el token y probar los endpoints protegidos desde el navegador. La especificación cruda en `http://localhost:8080/v3/api-docs`.

## Endpoints principales

| Método | Ruta | Descripción | Protegido |
|---|---|---|---|
| POST | `/auth/login` | Login, devuelve JWT | No |
| GET/POST | `/categorias` | Listar (paginado) / crear categoría | Sí |
| GET/PUT/DELETE | `/categorias/{id}` | Obtener / actualizar / baja lógica | Sí |
| GET/POST | `/articulos` | Listar (paginado) / crear artículo | Sí |
| GET/PUT/DELETE | `/articulos/{id}` | Obtener / actualizar / baja lógica | Sí |
| GET/POST | `/clientes` | Listar (paginado) / crear cliente | Sí |
| GET/PUT/DELETE | `/clientes/{id}` | Obtener / actualizar / baja lógica | Sí |
| POST | `/ventas` | Registrar venta | Sí |
| GET | `/ventas` | Listar (paginado) con filtros `clienteId`, `desde`, `hasta` | Sí |
| GET | `/ventas/{id}` | Detalle de una venta con sus líneas | Sí |
| PATCH | `/ventas/{id}/cancelar` | Cancelar venta y reponer stock | Sí |

Paginación en todos los listados vía query params: `?page=0&size=10&sort=id,asc`.

## Estructura del proyecto (package-by-feature)

```
com.macronnect.api_macronnect
├── config/        SecurityConfig, OpenApiConfig, DataSeeder
├── common/        BaseEntity, SoftDeletableEntity, DTOs (ErrorResponse, PagedResponse),
│                  excepciones y GlobalExceptionHandler
├── auth/          Usuario, roles, JWT (servicio, filtro), login
├── categoria/     entidad, repositorio, servicio, controlador, DTOs
├── articulo/
├── cliente/
└── venta/         Venta, DetalleVenta, FolioSequence, servicio, controlador, DTOs
```

Cada feature respeta la separación en capas: `controller → service → repository`, con DTOs de entrada y salida separados para no exponer las entidades.

---

## Decisiones de diseño relevantes

### Java 8 y Spring Boot 2.7
Se espera que se use Java 8 para el desarrollo de esta api y la última línea de Spring Boot que soporta Java 8 es la **2.7.x** (Boot 3 requiere Java 17). Esto condiciona todo el stack: namespace `javax.*` (no `jakarta.*`), Spring Security 5.7 (`antMatchers`, no `requestMatchers`), jjwt 0.11.5 y springdoc 1.x.

### Arquitectura por capas + package-by-feature
Se agrupa por funcionalidad (categoría, artículo, cliente, venta) en lugar de por capa técnica global, para que el código relacionado viva junto y el proyecto escale mejor. Dentro de cada feature se mantiene controller / service / repository / DTOs.

### Modelo de datos
Se modificó un poco el modelo sugerido, con lo siguiente
- Se agregó la entidad categoría (extra opcional). La relación Artículo → Categoría es obligatoria (FK `NOT NULL`): esto es porque todo articulo debería pertenecer a un grupo y no tener "productos huerfanos"
- Soft Delete en Cliente, Artículo y Categoría, mediante un flag `activo` y `@Where(clause = "activo = true")` de Hibernate, que filtra automáticamente los registros dados de baja en todas las consultas.
- La venta no usa soft delete: su ciclo de vida está dentro del campo "estado" y ver si está activa o cancelada. Una venta cancelada puede seguir siendo consultada porque no deja de ser información útil.
- Se agregaron timestamps para cuando se crea y actualiza algo.

### Dinero con BigDecimal
Todos los importes (`precio`, `precioUnitario`, `subtotal`, `total`) usan `BigDecimal` con `DECIMAL(x,2)`, nunca `double`/`float`, para evitar errores de redondeo. 

### Snapshot de precio en el detalle de venta
Cada línea guarda una copia del precio del artículo en el momento de la venta (`precioUnitario`), no una referencia viva. El precio del artículo puede cambiar después; una venta es un hecho histórico inmutable. El `subtotal` se persiste (dato derivado pero estable).

### Total calculado en el servidor
El total de la venta se deriva siempre de la suma de subtotales dentro de la entidad (`recalcularTotal`), y no tiene setter público: es imposible que el cliente inyecte un total arbitrario, cumpliendo la regla de "no confiar en el total enviado por el cliente".

### Concurrencia de stock 
Al registrar una venta, el artículo se lee con `@Lock(PESSIMISTIC_WRITE)` (`SELECT ... FOR UPDATE`) dentro de la transacción. Esto serializa el acceso a la fila y evita la sobreventa por condición de carrera. Se eligió el lock pesimista por legibilidad.

### Folio incremental
El folio es incremental, único y separado de la PK (mezclar la llave técnica con el folio de negocio es un smell). Se implementa con una tabla contadora (`folio_sequence`) leída con `SELECT ... FOR UPDATE` dentro de la transacción de la venta, garantizando folios consecutivos sin huecos y sin repetición bajo concurrencia. Inicialmente se intentó `@TableGenerator` de JPA, pero en este stack (Boot 2.7 + MySQL, transacción única) presentó problemas para obtener la conexión secundaria del generador; el contador manual resultó más robusto y explícito.

### Transaccionalidad
El registro de una venta es un único método `@Transactional`: si cualquier línea falla (stock insuficiente, artículo inexistente), se revierte toda la operación (descuentos previos incluidos). Las lecturas usan `@Transactional(readOnly = true)`.

### Autenticación y autorización
JWT stateless (`SessionCreationPolicy.STATELESS`, sin sesiones ni CSRF). Se implementaron roles (`ADMIN`, `USER`), para demostrar la distinción autenticación/autorización; la infraestructura queda lista (`@EnableGlobalMethodSecurity`) para restringir endpoints con una sola anotación. El secreto JWT es una clave Base64 de 256 bits inyectable por variable de entorno. Las contraseñas se almacenan con BCrypt.

### DTOs y mapeo
DTOs de entrada (`*Request`) y salida (`*Response`) separados; nunca se exponen las entidades. El mapeo entidad↔DTO se hace a mano con métodos estáticos (`fromEntity`), sin MapStruct, por simplicidad y legibilidad para el alcance del proyecto.

### Paginación
Los listados devuelven un `PagedResponse<T>` propio en lugar del `Page` de Spring, para no acoplar el contrato JSON de la API a la estructura interna de Spring (cuya serialización directa está marcada como inestable).

### Rendimiento (N+1)
Los listados que necesitan datos de entidades relacionadas usan `@EntityGraph` para traerlas en la misma consulta (JOIN) y evitar el problema N+1. Se apagó el Open-Session-In-View (`spring.jpa.open-in-view=false`) para forzar que el acceso a relaciones LAZY ocurra en la capa de servicio, no en la serialización.

### Manejo centralizado de errores
Un `@RestControllerAdvice` traduce cada excepción a su código HTTP correcto (400, 401, 403, 404, 405, 409, 415, 500) con un cuerpo `ErrorResponse` uniforme (timestamp, status, error, message, path y errores por campo cuando aplica).

## Testing

Pruebas unitarias con JUnit 5 + Mockito sobre `VentaService`: cálculo de total, descuento y validación de stock, generación de folio, cancelación con reposición de stock, y los caminos de error (cliente/artículo inexistente, venta ya cancelada). Se aíslan los repositorios con mocks para no depender de la base de datos.

## Qué haría distinto con más tiempo

- Refresh en los tokens, expiración configurable y logout; hoy solo hay access token.
- **`AccessDeniedHandler` personalizado** en `SecurityConfig` para que los 403 originados en la cadena de filtros usen el mismo formato `ErrorResponse`.
- Mayor cobertura de tests: servicios de Cliente/Artículo/Categoría y controllers.
- Dockerfile o docker-compose para levantar app + MySQL con un comando.

-Eric Amezcua 03/07/2026