# Documento de Arquitectura

## API Reactiva de Franquicias, Sucursales y Productos

> Clean Architecture · Spring WebFlux · MongoDB Reactivo · Redis · Docker · Terraform
>
> Prueba técnica — Backend Developer
> Documento de diseño previo a implementación

---

## Tabla de contenido

- [0. Enunciado de la prueba y criterios de evaluación](#0-enunciado-de-la-prueba-y-criterios-de-evaluación)
- [1. Contexto y mapeo de requerimientos](#1-contexto-y-mapeo-de-requerimientos)
- [2. Decisiones arquitectónicas (ADR)](#2-decisiones-arquitectónicas-adr)
- [3. Stack tecnológico y justificación de cada pieza](#3-stack-tecnológico-y-justificación-de-cada-pieza)
- [4. Patrones de diseño aplicados](#4-patrones-de-diseño-aplicados)
- [5. Estructura del proyecto](#5-estructura-del-proyecto)
- [6. Fases del proyecto — paso a paso](#6-fases-del-proyecto--paso-a-paso)
- [7. Glosario de anotaciones y tipos usados](#7-glosario-de-anotaciones-y-tipos-usados)
- [8. Notas finales](#8-notas-finales)

---

## 0. Enunciado de la prueba y criterios de evaluación

Se requiere construir un API para manejar una lista de franquicias. Una franquicia se compone de un nombre y una lista de sucursales; a su vez, una sucursal está compuesta por un nombre y un listado de productos ofertados en la sucursal. Un producto se compone de un nombre y una cantidad de stock.

### Aspectos fundamentales de evaluación

- Aplicación de programación reactiva
- Inclusión de unit tests (pruebas unitarias)
- Uso de Docker para contenerización
- Implementación de Infrastructure as Code (IaC)
- Estructura basada en Clean Architecture
- Adopción de buenas prácticas de desarrollo: organización del código, legibilidad y mantenimiento

### Criterios de aceptación

1. El proyecto debe ser desarrollado en Spring Boot.
2. Exponer endpoint para agregar una nueva franquicia.
3. Exponer endpoint para agregar una nueva sucursal a la franquicia.
4. Exponer endpoint para agregar un nuevo producto a la sucursal.
5. Exponer endpoint para eliminar un producto de una sucursal.
6. Exponer endpoint para modificar el stock de un producto.
7. Exponer endpoint que permita mostrar cuál es el producto que más stock tiene por sucursal para una franquicia puntual. Debe retornar un listado de productos que indiquen a qué sucursal pertenecen.
8. Utilizar sistemas de persistencia de datos como Redis, MySQL, MongoDB, Dynamo, en algún proveedor de nube. Queda abierto a libre escogencia.

### Puntos extra

- Plus si se empaqueta la aplicación con Docker.
- Plus si se utiliza programación funcional/reactiva. Queda abierto a libre escogencia.
- Plus si se expone endpoint que permita actualizar el nombre de la franquicia.
- Plus si se expone endpoint que permita actualizar el nombre de la sucursal.
- Plus si se expone endpoint que permita actualizar el nombre del producto.
- Plus si se aprovisiona la persistencia de datos como infraestructura como código (Terraform, CloudFormation, etc.). Queda a libre escogencia.
- Plus si toda la solución se despliega en la nube.

### Notas de entrega

- Se tendrá en cuenta el flujo de trabajo usando Git; la prueba debe presentarse en un repositorio de código con acceso público (GitHub, Bitbucket, etc.).
- Se debe incluir documentación que permita entender cómo desplegar la aplicación desde un entorno local. Se sugiere utilizar un archivo `README.md`.
- Plazo de entrega: dos (2) días hábiles a partir del envío de la prueba.

---

## 1. Contexto y mapeo de requerimientos

El dominio del problema es jerárquico: una **Franquicia** agrupa **Sucursales**, y cada Sucursal agrupa **Productos** con su respectivo stock. La siguiente tabla mapea cada criterio de aceptación de la prueba con el componente de la arquitectura que lo resuelve, de modo que la trazabilidad requerimiento → diseño quede explícita desde el inicio.

| # | Requerimiento | Dónde se resuelve |
|---|---|---|
| 1 | Spring Boot | Todo el proyecto (WebFlux starter) |
| 2 | Crear franquicia | `CreateFranchiseUseCase` + `POST /franchises` |
| 3 | Agregar sucursal | `AddBranchUseCase` + `POST /franchises/{id}/branches` |
| 4 | Agregar producto | `AddProductUseCase` + `POST /branches/{id}/products` |
| 5 | Eliminar producto | `DeleteProductUseCase` + `DELETE /products/{id}` |
| 6 | Modificar stock | `UpdateProductStockUseCase` + `PATCH /products/{id}/stock` |
| 7 | Producto con más stock por sucursal (de una franquicia) | `GetTopStockByFranchiseUseCase` + `GET /franchises/{id}/top-stock` |
| 8 | Persistencia (libre elección) | MongoDB reactivo como fuente de verdad |
| Plus | Docker | Fase 8 |
| Plus | Reactivo / funcional | WebFlux + `RouterFunction` |
| Plus | Update nombre franquicia/sucursal/producto | 3 use cases adicionales |
| Plus | IaC | Terraform, Fase 9 |
| Plus | Despliegue en la nube | Fase 9 / Fase 10 |

---

## 2. Decisiones arquitectónicas (ADR)

### ADR-01 — Clean Architecture / Hexagonal

El dominio no conoce Spring, ni Mongo, ni Redis. Depende solo de sus propios ports (interfaces). La infraestructura depende del dominio, nunca al revés. Esta dirección de dependencias es lo primero que revisa un evaluador senior.

### ADR-02 — Modelo de datos normalizado, no embebido

`franchises`, `branches` y `products` se modelan como colecciones separadas, cada hijo con el ID de su padre.

**Trade-off:** se pierde la atomicidad de un único documento, pero se gana la capacidad de hacer updates puntuales baratos (por ejemplo, modificar solo el stock de un producto) y queries de agregación limpias, justo lo que exige el requerimiento 7. Con documentos anidados, cada escritura de stock implicaría reescribir el árbol completo y arriesgar el límite de 16MB por documento de MongoDB.

### ADR-03 — Redis como cache-aside, no como base de datos primaria

Se cachea el resultado de "producto con más stock por sucursal" usando `franchiseId` como parte de la clave. Se invalida activamente el cache en cualquier escritura que afecte el stock de esa franquicia (nuevo producto, producto eliminado, stock actualizado), con un TTL corto como red de seguridad adicional ante invalidaciones que se escapen.

### ADR-04 — WebFlux funcional (Router / Handler), no anotado

Se usa `RouterFunction` + `HandlerFunction` en lugar de `@RestController`. Esto cubre explícitamente el plus de programación funcional y reactiva, y facilita testear cada handler como una función pura `ServerRequest → Mono<ServerResponse>`, aislada del contenedor de Spring.

### ADR-05 — Un Use Case = una responsabilidad

En lugar de un `FranchiseService` con diez métodos, cada operación de negocio es su propia clase con un único método de entrada. Alta cohesión, bajo acoplamiento y tests triviales de escribir: es la diferencia entre un diseño junior y uno senior.

---

## 3. Stack tecnológico y justificación de cada pieza

Cada tecnología se eligió para resolver un problema puntual, no "porque la prueba lo sugiere". La siguiente tabla explica qué es, por qué se usa en este proyecto específico y qué aporta.

| Tecnología | Qué es | Por qué aquí / qué aporta |
|---|---|---|
| Spring WebFlux | Framework reactivo no bloqueante sobre Reactor y Netty | Requerimiento explícito de programación reactiva; alta concurrencia con pocos hilos, I/O no bloqueante hacia Mongo y Redis |
| Project Reactor (`Mono` / `Flux`) | Implementación de Reactive Streams, motor de WebFlux | Composición declarativa de operaciones asíncronas y backpressure nativo |
| MongoDB Reactive (Spring Data) | Driver reactivo no bloqueante para MongoDB | El dominio es jerárquico y de escritura frecuente en stock; esquema flexible y updates parciales eficientes por documento |
| Redis (Reactive) | Almacén clave-valor en memoria | Cachea la consulta de agregación del requerimiento 7 (cache-aside); reduce latencia y carga sobre Mongo en lecturas frecuentes |
| Docker / docker-compose | Contenerización | Empaqueta app + Mongo + Redis de forma reproducible; se levanta con un solo comando |
| Terraform | IaC declarativo | Aprovisiona la persistencia en la nube sin pasos manuales; infraestructura versionada |
| JUnit 5 + Mockito + StepVerifier | Testing | `StepVerifier` es el estándar para testear flujos `Mono`/`Flux` paso a paso (next, error, complete) |
| Testcontainers | Contenedores efímeros para tests | Levanta Mongo y Redis reales durante el test de integración, sin mocks ni infra externa |
| Lombok | Generación de boilerplate en compilación | Reduce ruido en DTOs; usar con moderación en el dominio para no ocultar invariantes |

---

## 4. Patrones de diseño aplicados

| Patrón | Dónde se aplica | Por qué es la elección correcta |
|---|---|---|
| Builder | Entidades de dominio: `Franchise`, `Branch`, `Product` | Constructores con varios parámetros se vuelven ilegibles; el Builder fuerza construcción explícita y valida invariantes en `build()` |
| Ports & Adapters (Hexagonal) | Todo el proyecto | El dominio define qué necesita; la infraestructura define cómo. Cambiar Mongo por DynamoDB no toca una línea de negocio |
| Use Case / Command | Capa de aplicación | Cada caso de uso es una unidad de negocio testeable en aislamiento, con una sola responsabilidad |
| Decorator | `ProductRepositoryCacheDecorator` sobre el adapter de Mongo | Añade cache de forma transparente sin ensuciar el adapter ni el use case, que ni se entera de que existe Redis |
| DTO + Mapper | Entry point (capa web) | El dominio nunca se serializa directo a JSON; protege el modelo interno de cambios en el contrato HTTP |
| Value Object | `StockQuantity` (envuelve un `int` con invariante >= 0) | Evita primitive obsession; la regla "el stock no puede ser negativo" vive en un solo lugar |
| Strategy (implícita) | Los ports de persistencia | Si se pide soportar DynamoDB además de Mongo, se agrega un adapter nuevo sin `if/else` de proveedor regado por el código |

---

## 5. Estructura del proyecto

Antes de entrar al detalle fase por fase, esta es la estructura completa de carpetas y paquetes que resulta de aplicar Clean Architecture con Ports & Adapters. Todas las fases posteriores van llenando esta estructura, nunca la reinventan.

```
franchise-api/
├── domain/
│   ├── model/
│   │   ├── Franchise.java
│   │   ├── Branch.java
│   │   ├── Product.java
│   │   ├── StockQuantity.java              (Value Object)
│   │   └── exceptions/
│   │       ├── NegativeStockException.java
│   │       └── EntityNotFoundException.java
│   └── port/
│       ├── FranchiseRepositoryPort.java
│       ├── BranchRepositoryPort.java
│       ├── ProductRepositoryPort.java
│       └── ProductStockCachePort.java
│
├── application/
│   └── usecase/
│       ├── CreateFranchiseUseCase.java
│       ├── UpdateFranchiseNameUseCase.java
│       ├── AddBranchUseCase.java
│       ├── UpdateBranchNameUseCase.java
│       ├── AddProductUseCase.java
│       ├── DeleteProductUseCase.java
│       ├── UpdateProductStockUseCase.java
│       ├── UpdateProductNameUseCase.java
│       └── GetTopStockByFranchiseUseCase.java
│
├── infrastructure/
│   ├── driven-adapters/
│   │   ├── persistence-mongo/
│   │   │   ├── document/
│   │   │   │   ├── FranchiseDocument.java
│   │   │   │   ├── BranchDocument.java
│   │   │   │   └── ProductDocument.java
│   │   │   ├── repository/         (interfaces ReactiveMongoRepository)
│   │   │   ├── mapper/             (Document <-> Domain)
│   │   │   └── adapter/
│   │   │       ├── FranchiseMongoAdapter.java
│   │   │       ├── BranchMongoAdapter.java
│   │   │       └── ProductMongoAdapter.java
│   │   └── cache-redis/
│   │       ├── ReactiveRedisConfig.java
│   │       ├── ProductStockRedisAdapter.java
│   │       └── ProductRepositoryCacheDecorator.java
│   │
│   └── entry-points/
│       └── reactive-web/
│           ├── router/
│           │   └── ApiRouter.java  (RouterFunction, agrupado por dominio)
│           ├── handler/
│           │   ├── FranchiseHandler.java
│           │   ├── BranchHandler.java
│           │   └── ProductHandler.java
│           ├── dto/
│           │   ├── request/
│           │   └── response/
│           ├── mapper/             (DTO <-> Domain)
│           └── errorhandling/
│               └── GlobalErrorWebExceptionHandler.java
│
├── config/
│   └── BeanConfiguration.java       (wiring de use cases con sus ports)
│
├── infra/
│   └── terraform/
│       ├── main.tf
│       ├── variables.tf
│       ├── outputs.tf
│       └── environments/
│
├── docker-compose.yml
├── Dockerfile
└── FranchiseApiApplication.java
```

**Regla de dependencia:** `domain` no importa nada de `infrastructure`. `application` solo importa `domain`. `infrastructure` importa `domain` y `application`, nunca al revés. Es lo primero que revisa un evaluador senior al abrir el repositorio.

### 5.1 Convención de branching y versionado (aplica a todas las fases)

Estrategia trunk-based simplificada: `main` protegido, una rama `feature/*` por fase, merge por Pull Request (aunque sea contra ti mismo, deja rastro de proceso). Commits en formato Conventional Commits. Versionado SemVer, con un tag al cerrar cada fase que aporta funcionalidad visible.

| Elemento | Convención |
|---|---|
| Rama | `feature/NN-nombre-fase` |
| Commit | `tipo(alcance): descripción` — tipos: `feat`, `fix`, `test`, `chore`, `docs`, `refactor` |
| Versión durante desarrollo | `v0.x.0` por cada fase con funcionalidad significativa |
| Versión de entrega final | `v1.0.0` — funcional, testeada y documentada |
| Tag | Se crea inmediatamente después del merge de cada fase a `main` |

---

## 6. Fases del proyecto — paso a paso

Cada fase se desarrolla en su propia rama, se cierra con un merge a `main` y se marca con un tag. El detalle de "paso a paso" de cada fase está pensado para ejecutarse en orden estricto: cada paso depende del anterior.

### Fase 0 — Bootstrap del proyecto

**Objetivo:** dejar el repositorio y el esqueleto de Spring Boot listos, sin ninguna lógica de negocio todavía.

#### Paso a paso

1. Crear el repositorio en GitHub/GitLab con visibilidad pública, licencia y `.gitignore` de Java/Maven.
2. Generar el proyecto base con Spring Initializr, seleccionando únicamente el starter de WebFlux (sin web MVC).
3. Crear la estructura de carpetas vacía descrita en la sección 5 (`domain`, `application`, `infrastructure`, `config`).
4. Configurar `application.yml` con perfiles `local` y `docker` (placeholders de conexión a Mongo y Redis).
5. Redactar el `README.md` inicial con el título del proyecto y una sección "En construcción".
6. Hacer el primer commit y push a `main`.

#### Configuración de Spring Initializr (detalle)

| Campo | Valor |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | Última versión GA estable (evitar SNAPSHOT o M1) |
| Group | `com.tunombre` |
| Artifact | `franchise-api` |
| Package name | `com.tunombre.franchiseapi` |
| Packaging | Jar |
| Configuration | YAML |
| Java | 21 (LTS) |

Dependencias a agregar en Initializr:

- **Spring Reactive Web** (starter WebFlux) — no agregar "Spring Web" (MVC); el proyecto exige reactivo puro (ADR-04).
- **Spring Data Reactive MongoDB** — persistencia reactiva (ADR-02).
- **Spring Data Reactive Redis** — cache-aside del requerimiento 7 (ADR-03).
- **Lombok** (opcional) — reduce boilerplate en DTOs; usar con moderación en el dominio.

#### Control de versiones

- **Rama:** `feature/00-bootstrap`
- **Commits:**
  - `chore: initial project structure`
  - `chore: add spring webflux dependencies`
  - `docs: initial README skeleton`
- **Tag al cerrar la fase:** `v0.1.0`
- **Criterios de aceptación cubiertos:** base habilitante para el requerimiento 1 (Spring Boot).

---

### Fase 1 — Modelado de dominio

**Objetivo:** construir las entidades de negocio puras, sin ninguna dependencia de framework.

#### Paso a paso

1. Crear la clase `StockQuantity` como Value Object: constructor privado, factory estático `of(int)`, y validación que lanza `NegativeStockException` si el valor es negativo.
2. Crear `Product` con Builder: `id`, `branchId`, `name`, `StockQuantity`. Validar `name` no vacío dentro del `build()`.
3. Crear `Branch` con Builder: `id`, `franchiseId`, `name`. Misma validación de nombre.
4. Crear `Franchise` con Builder: `id`, `name`.
5. Crear las excepciones de dominio `EntityNotFoundException` y `NegativeStockException`, sin ninguna referencia a HTTP ni a Spring.
6. Escribir tests unitarios puros (sin Spring context) que verifiquen los invariantes: stock negativo falla, nombre vacío falla, Builder construye correctamente un objeto válido.

#### Modelos de dominio — ficha por clase

Son 4 clases de modelo (3 entidades + 1 Value Object) y 2 excepciones. Todas son POJOs puros: sin anotaciones de Spring ni de Mongo, inmutables, construidas con Builder propio.

La jerarquía es:

```
Franchise (1) ──< Branch (N) ──< Product (N)
                                    └── stock: StockQuantity
```

Cada hijo guarda el ID de su padre (`Branch.franchiseId`, `Product.branchId`) — modelo normalizado, no anidado (ver ADR-02).

##### `Franchise` — entidad raíz

Representa una franquicia. Es el punto de entrada del agregado: todo cuelga de ella.

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `String` | Identificador único (lo genera Mongo al persistir) |
| `name` | `String` | Nombre de la franquicia |

- **Regla:** `name` no puede ser vacío ni en blanco — se valida en `build()`.
- **Mutabilidad:** inmutable. Renombrar = crear una nueva instancia vía `UpdateFranchiseNameUseCase`.

##### `Branch` — sucursal

Sucursal que pertenece a una franquicia.

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `String` | Identificador único |
| `franchiseId` | `String` | ID de la franquicia dueña (la relación padre-hijo) |
| `name` | `String` | Nombre de la sucursal |

- **Regla:** `name` no vacío (validado en `build()`). Que `franchiseId` exista lo valida `AddBranchUseCase`, no la clase.
- **Mutabilidad:** inmutable.

##### `Product` — producto con stock

Producto ofertado en una sucursal.

| Atributo | Tipo | Descripción |
|---|---|---|
| `id` | `String` | Identificador único |
| `branchId` | `String` | ID de la sucursal dueña |
| `name` | `String` | Nombre del producto |
| `stock` | `StockQuantity` | Cantidad en stock (nunca negativa, garantizado por el VO) |

- **Regla:** `name` no vacío. El stock siempre es válido porque `StockQuantity` ya lo garantiza.
- **Mutabilidad:** inmutable. `UpdateProductStockUseCase` reconstruye el `Product` con el Builder en lugar de mutar el stock.

##### `StockQuantity` — Value Object

Envuelve el `int` del stock para que la regla "no puede ser negativo" viva en un solo lugar (evita primitive obsession).

| Atributo | Tipo | Descripción |
|---|---|---|
| `value` | `int` (privado) | Cantidad de stock, siempre `>= 0` |

- **Regla:** se crea solo con el factory `StockQuantity.of(int)`; si el valor es negativo lanza `NegativeStockException`.
- **Mutabilidad:** inmutable, sin setters. Cambiar el stock = nueva instancia.

##### Excepciones de dominio

Ambas extienden `RuntimeException` y no saben nada de HTTP; la traducción a códigos de estado la hace `GlobalErrorWebExceptionHandler` en la Fase 6.

| Excepción | Cuándo se lanza | HTTP resultante |
|---|---|---|
| `EntityNotFoundException` | Se referencia una franquicia/sucursal/producto que no existe (ej. `"Franchise not found: {id}"`) | 404 |
| `NegativeStockException` | `StockQuantity.of(int)` recibe un valor `< 0` | 400 |

#### Control de versiones

- **Rama:** `feature/01-domain-model`
- **Commits:**
  - `feat(domain): add StockQuantity value object`
  - `feat(domain): add Franchise, Branch and Product entities with builder`
  - `test(domain): unit tests for domain invariants`
- **Tag al cerrar la fase:** `v0.2.0`
- **Criterios de aceptación cubiertos:** base de negocio para los requerimientos 2 a 7.

---

### Fase 2 — Ports (contratos del dominio)

**Objetivo:** definir qué necesita el negocio de la infraestructura, sin decidir todavía cómo se implementa.

#### Paso a paso

1. Definir `FranchiseRepositoryPort` con los métodos `save(Franchise)` y `updateName(id, name)`, ambos retornando `Mono<Franchise>`.
2. Definir `BranchRepositoryPort` con `save(Branch)` y `updateName(id, name)`.
3. Definir `ProductRepositoryPort` con `save`, `deleteById`, `updateStock`, `updateName` y `findTopStockByBranchIds(List<String>)`, este último retornando `Flux<Product>`.
4. Definir `ProductStockCachePort` con `get(franchiseId)`, `put(franchiseId, resultado)` y `evict(franchiseId)`.
5. Revisar que ningún método de estas interfaces exponga tipos de Mongo, Redis o Spring — solo tipos de dominio y `Mono`/`Flux` de Reactor.

#### Clases a crear

| Clase | Responsabilidad | Anotaciones / Tipo |
|---|---|---|
| `FranchiseRepositoryPort` | Contrato de persistencia para `Franchise` | Interface de dominio |
| `BranchRepositoryPort` | Contrato de persistencia para `Branch` | Interface de dominio |
| `ProductRepositoryPort` | Contrato de persistencia para `Product`, incluye consulta agregada | Interface de dominio |
| `ProductStockCachePort` | Contrato de cache para el resultado del top-stock | Interface de dominio |

#### Control de versiones

- **Rama:** `feature/02-domain-ports`
- **Commits:**
  - `feat(domain): define repository ports`
  - `feat(domain): define product stock cache port`
- **Tag al cerrar la fase:** `v0.3.0`
- **Criterios de aceptación cubiertos:** prepara el terreno para desacoplar negocio de infraestructura en las fases 4 y 5.

---

### Fase 3 — Casos de uso (aplicación)

**Objetivo:** orquestar el negocio contra los ports. En esta fase los use cases son testeables completamente con mocks, sin infraestructura real todavía.

#### Paso a paso

1. Implementar `CreateFranchiseUseCase`: recibe un nombre, construye un `Franchise` vía Builder y delega en `FranchiseRepositoryPort.save`.
2. Implementar `AddBranchUseCase` y `AddProductUseCase` con la misma lógica, validando la existencia del padre antes de asociar (lanzando `EntityNotFoundException` si no existe).
3. Implementar `DeleteProductUseCase` y `UpdateProductStockUseCase`, delegando en `ProductRepositoryPort` e invalidando el cache del `franchiseId` afectado.
4. Implementar `UpdateFranchiseNameUseCase`, `UpdateBranchNameUseCase` y `UpdateProductNameUseCase` (los tres plus de actualización de nombre).
5. Implementar `GetTopStockByFranchiseUseCase`: primero consulta `ProductStockCachePort.get(franchiseId)`; si es vacío, obtiene las sucursales de la franquicia, consulta `ProductRepositoryPort.findTopStockByBranchIds`, arma el resultado y lo guarda en cache antes de retornarlo.
6. Escribir tests unitarios de cada use case con Mockito para mockear los ports y `StepVerifier` para verificar la secuencia reactiva (`expectNext`, `expectError`, `verifyComplete`).

#### Clases a crear

| Clase | Responsabilidad | Anotaciones / Tipo |
|---|---|---|
| `CreateFranchiseUseCase` | Alta de una franquicia nueva | Ninguna (POJO, inyectado por constructor) |
| `AddBranchUseCase` / `AddProductUseCase` | Alta de sucursal / producto validando existencia del padre | Ninguna |
| `DeleteProductUseCase` / `UpdateProductStockUseCase` | Baja y modificación de stock, con invalidación de cache | Ninguna |
| `UpdateFranchiseNameUseCase` / `UpdateBranchNameUseCase` / `UpdateProductNameUseCase` | Renombrado de cada entidad (plus) | Ninguna |
| `GetTopStockByFranchiseUseCase` | Orquesta cache + repositorio para el requerimiento 7 | Ninguna |

#### Control de versiones

- **Rama:** `feature/03-usecases`
- **Commits:**
  - `feat(usecase): add CreateFranchiseUseCase`
  - `feat(usecase): add branch and product use cases`
  - `feat(usecase): add GetTopStockByFranchiseUseCase with cache-aside orchestration`
  - `test(usecase): unit tests with Mockito and StepVerifier`
- **Tag al cerrar la fase:** `v0.4.0`
- **Criterios de aceptación cubiertos:** lógica de negocio de los requerimientos 2 a 7 y los tres plus de actualización de nombre.

---

### Fase 4 — Adapter de persistencia MongoDB

**Objetivo:** implementar los ports de repositorio contra MongoDB reactivo.

#### Paso a paso

1. Crear `FranchiseDocument`, `BranchDocument` y `ProductDocument` anotados con `@Document`, `@Id` y `@Field`, reflejando el modelo normalizado (referencias por ID, no anidado).
2. Crear las interfaces `FranchiseReactiveMongoRepository`, `BranchReactiveMongoRepository` y `ProductReactiveMongoRepository` extendiendo `ReactiveMongoRepository`.
3. Agregar en `ProductReactiveMongoRepository` el método derivado `findByBranchIdInOrderByStockDesc(List<String> branchIds)` para soportar la consulta del requerimiento 7.
4. Crear los mappers Document <-> Domain (manuales o con MapStruct) en el paquete `mapper`, evitando que el dominio conozca las anotaciones de Mongo.
5. Implementar `FranchiseMongoAdapter`, `BranchMongoAdapter` y `ProductMongoAdapter`, cada uno implementando su port correspondiente y usando el mapper para traducir entre Document y entidad de dominio.
6. Escribir tests de integración con Testcontainers levantando una instancia real de MongoDB, verificando que los adapters persisten y recuperan correctamente.

#### Colecciones de MongoDB (las "tablas")

Cada entidad de dominio tiene su propia colección y su propia clase `*Document` (la versión con anotaciones de Mongo, separada del dominio):

| Colección | Clase Document | Campos |
|---|---|---|
| `franchises` | `FranchiseDocument` | `id: String`, `name: String` |
| `branches` | `BranchDocument` | `id: String`, `franchiseId: String`, `name: String` |
| `products` | `ProductDocument` | `id: String`, `branchId: String`, `name: String`, `stock: int` |

Nota: en `ProductDocument` el stock se guarda como `int` plano; el Value Object `StockQuantity` existe solo en el dominio y el mapper hace la conversión en ambos sentidos.

#### Clases a crear

| Clase | Responsabilidad | Anotaciones / Tipo |
|---|---|---|
| `FranchiseDocument` / `BranchDocument` / `ProductDocument` | Modelos de persistencia Mongo, separados del dominio | `@Document`, `@Id`, `@Field` |
| `FranchiseReactiveMongoRepository` / `BranchReactiveMongoRepository` / `ProductReactiveMongoRepository` | Repositorios reactivos generados por Spring Data | `ReactiveMongoRepository<T, String>` |
| `FranchiseMongoAdapter` / `BranchMongoAdapter` / `ProductMongoAdapter` | Implementación de los ports de dominio usando Mongo | `@Component` |

#### Control de versiones

- **Rama:** `feature/04-mongo-adapter`
- **Commits:**
  - `feat(infra): add mongo documents and repositories`
  - `feat(infra): implement FranchiseMongoAdapter, BranchMongoAdapter, ProductMongoAdapter`
  - `test(infra): integration tests with Testcontainers MongoDB`
- **Tag al cerrar la fase:** `v0.5.0`
- **Criterios de aceptación cubiertos:** requerimiento 8 (persistencia).

---

### Fase 5 — Adapter de cache Redis

**Objetivo:** implementar el cache-aside para la consulta de mayor stock por sucursal, de forma transparente para el use case.

#### Paso a paso

1. Configurar `ReactiveRedisConfig` con un `ReactiveRedisTemplate` serializando claves como String y valores como JSON.
2. Definir `CacheKeyGenerator` para construir la clave determinística `top-stock:{franchiseId}`.
3. Implementar `ProductStockRedisAdapter` implementando `ProductStockCachePort`: `get`, `put` con TTL configurable (ej. 5 minutos) y `evict`.
4. Implementar `ProductRepositoryCacheDecorator`: envuelve a `ProductMongoAdapter`, expone el mismo port `ProductRepositoryPort`, y en las operaciones de escritura (`save`, `updateStock`, `deleteById`) invoca `evict` sobre el cache del `franchiseId` afectado.
5. Ajustar el `BeanConfiguration` para que el use case reciba el decorator en lugar del adapter de Mongo directamente (Decorator transparente).
6. Escribir tests de integración con Testcontainers de Redis, verificando hit, miss e invalidación del cache.

#### Clases a crear

| Clase | Responsabilidad | Anotaciones / Tipo |
|---|---|---|
| `ReactiveRedisConfig` | Configuración del cliente reactivo de Redis | `@Configuration`, `@Bean` |
| `CacheKeyGenerator` | Utilidad para construir claves de cache consistentes | Ninguna |
| `ProductStockRedisAdapter` | Implementación de `ProductStockCachePort` | `@Component` |
| `ProductRepositoryCacheDecorator` | Decorator que añade cache-aside sobre `ProductMongoAdapter` | `@Primary` (para que se inyecte por defecto) |

#### Control de versiones

- **Rama:** `feature/05-redis-cache`
- **Commits:**
  - `feat(infra): add reactive redis configuration`
  - `feat(infra): implement cache-aside decorator for top-stock query`
  - `test(infra): integration tests for cache hit/miss/eviction`
- **Tag al cerrar la fase:** `v0.6.0`
- **Criterios de aceptación cubiertos:** optimiza el requerimiento 7 y demuestra criterio de diseño más allá de "usar Redis porque se pide".

---

### Fase 6 — Entry point reactivo (API funcional)

**Objetivo:** exponer todos los use cases vía HTTP usando el estilo funcional de WebFlux.

#### Paso a paso

1. Definir los DTOs de request y response por cada operación (`CreateFranchiseRequest`, `AddBranchRequest`, `AddProductRequest`, `UpdateStockRequest`, `ProductResponse`, `TopStockProductResponse`, etc.).
2. Crear los mappers DTO <-> Domain para que ningún controlador serialice directamente una entidad de dominio.
3. Implementar `FranchiseHandler`, `BranchHandler` y `ProductHandler`: cada método recibe un `ServerRequest` y retorna `Mono<ServerResponse>`, delegando en el use case correspondiente.
4. Definir `ApiRouter` como una clase de configuración que compone las rutas con `RouterFunctions.route()`, agrupadas lógicamente por recurso (franchises, branches, products).
5. Implementar `GlobalErrorWebExceptionHandler` traduciendo `EntityNotFoundException` a 404 y `NegativeStockException` (u otras validaciones) a 400, con un cuerpo de error consistente.
6. Probar manualmente cada endpoint con una colección de Postman/Insomnia antes de pasar a la fase de testing formal.

#### Clases a crear

| Clase | Responsabilidad | Anotaciones / Tipo |
|---|---|---|
| `ApiRouter` | Define todas las rutas de forma funcional | `RouterFunction<ServerResponse>`, `@Configuration`, `@Bean` |
| `FranchiseHandler` / `BranchHandler` / `ProductHandler` | Traducen HTTP a llamadas a use cases | `HandlerFunction<ServerResponse>`, `@Component` |
| Request/Response DTOs | Contrato público de la API, desacoplado del dominio | Records o POJOs simples |
| `GlobalErrorWebExceptionHandler` | Traduce excepciones de dominio a códigos HTTP | `ErrorWebExceptionHandler`, `@Order` |

#### Control de versiones

- **Rama:** `feature/06-web-entrypoint`
- **Commits:**
  - `feat(api): add franchise endpoints`
  - `feat(api): add branch endpoints`
  - `feat(api): add product endpoints and top-stock query`
  - `feat(api): centralized error handling`
- **Tag al cerrar la fase:** `v0.7.0`
- **Criterios de aceptación cubiertos:** expone públicamente los requerimientos 2 a 7 y los tres plus de actualización de nombre.

---

### Fase 7 — Testing integral

**Objetivo:** cerrar la cobertura de pruebas combinando las distintas capas: unitarias, integración y extremo a extremo.

#### Paso a paso

1. Revisar que `domain` y `application` ya tengan cobertura unitaria completa de las fases 1 y 3 (invariantes de negocio y orquestación de use cases).
2. Revisar que `infrastructure` tenga tests de integración con Testcontainers de las fases 4 y 5 (Mongo y Redis reales).
3. Escribir tests end-to-end con `WebTestClient` sobre el `ApiRouter` completo, simulando el flujo real: crear franquicia, agregar sucursal, agregar productos, consultar top-stock y verificar el resultado.
4. Agregar un test específico que verifique la invalidación de cache: consultar top-stock, actualizar stock de un producto, volver a consultar y confirmar que el resultado refleja el cambio.
5. Configurar el reporte de cobertura (JaCoCo) y fijar un umbral mínimo razonable (ej. 80% en `domain` y `application`).

#### Control de versiones

- **Rama:** `feature/07-testing`
- **Commits:**
  - `test: add WebTestClient e2e tests for franchise flow`
  - `test: verify cache invalidation on stock update`
  - `chore: configure JaCoCo coverage report`
- **Tag al cerrar la fase:** `v0.8.0`
- **Criterios de aceptación cubiertos:** da soporte de calidad y confiabilidad a todos los requerimientos funcionales.

---

### Fase 8 — Contenerización

**Objetivo:** empaquetar la aplicación y sus dependencias de infraestructura para que cualquier persona la levante con un solo comando.

#### Paso a paso

1. Escribir un `Dockerfile` multi-stage: primera etapa construye el jar con Maven sobre una imagen JDK, segunda etapa copia solo el jar final sobre una imagen JRE liviana (ej. `eclipse-temurin:21-jre-alpine`).
2. Escribir `docker-compose.yml` con tres servicios: `app`, `mongo` y `redis`, conectados en una red común, con variables de entorno para las URLs de conexión.
3. Definir volúmenes persistentes para Mongo, de modo que los datos no se pierdan al reiniciar el contenedor durante pruebas locales.
4. Agregar un `.dockerignore` excluyendo `target`, `.git` y archivos de IDE.
5. Probar el levantamiento completo con `docker-compose up --build` y verificar que los endpoints respondan correctamente desde el contenedor.

#### Control de versiones

- **Rama:** `feature/08-docker`
- **Commits:**
  - `chore: add multi-stage Dockerfile`
  - `chore: add docker-compose with mongo and redis services`
  - `chore: add .dockerignore`
- **Tag al cerrar la fase:** `v0.9.0`
- **Criterios de aceptación cubiertos:** plus de Docker.

---

### Fase 9 — Infraestructura como código

**Objetivo:** aprovisionar la persistencia en la nube (Mongo y Redis) de forma reproducible y versionada.

#### Paso a paso

1. Definir el proveedor cloud a usar (por ejemplo MongoDB Atlas + Redis Cloud, o AWS DocumentDB + ElastiCache) según cuenta disponible y tiempo restante.
2. Crear `infra/terraform/variables.tf` con las variables de entorno, región y tamaños de instancia.
3. Crear `infra/terraform/main.tf` con los recursos del cluster de base de datos documental y de la instancia de cache, incluyendo reglas de red mínimas necesarias.
4. Crear `infra/terraform/outputs.tf` exponiendo las cadenas de conexión como outputs sensibles, para inyectarlas luego como variables de entorno en el despliegue de la app.
5. Ejecutar `terraform init`, `terraform plan` y `terraform apply` en un entorno de prueba, documentando los comandos exactos en el README.
6. Versionar los `.tf` en el repositorio, nunca los archivos de estado (`.tfstate`) ni credenciales.

#### Control de versiones

- **Rama:** `feature/09-iac-terraform`
- **Commits:**
  - `chore(infra): provision managed mongodb cluster`
  - `chore(infra): provision managed redis instance`
  - `docs(infra): document terraform apply steps`
- **Tag al cerrar la fase:** `v0.9.5`
- **Criterios de aceptación cubiertos:** plus de IaC y de persistencia gestionada en la nube.

---

### Fase 10 — Documentación y entrega final

**Objetivo:** dejar el repositorio en condición de ser evaluado por un tercero sin contexto previo.

#### Paso a paso

1. Completar el `README.md` con: descripción del problema, diagrama de arquitectura (puede ser el árbol de la sección 5), decisiones técnicas resumidas (basadas en la sección 2), instrucciones para levantar en local con `docker-compose up`, instrucciones para correr los tests, y variables de entorno necesarias.
2. Adjuntar una colección de Postman o Insomnia exportada con todos los endpoints y ejemplos de request/response.
3. Si se completó la fase 9, documentar también cómo desplegar la infraestructura cloud y cómo apuntar la app a esos recursos.
4. Revisar que el repositorio sea público y que el historial de commits refleje el orden de fases descrito en este documento.
5. Hacer el merge final a `main` y crear el tag de entrega.

#### Control de versiones

- **Rama:** `main` (o `feature/10-docs`)
- **Commits:**
  - `docs: complete README with setup and deployment instructions`
  - `docs: add Postman collection`
- **Tag al cerrar la fase:** `v1.0.0`
- **Criterios de aceptación cubiertos:** repositorio público, documentación de despliegue local, flujo de Git visible.

---

## 7. Glosario de anotaciones y tipos usados

| Anotación / Tipo | Capa | Qué hace |
|---|---|---|
| `@SpringBootApplication` | app | Marca la clase de arranque; habilita auto-configuración y component scan |
| `@Configuration` / `@Bean` | config | Define el wiring manual de use cases con sus ports, manteniendo el dominio libre de anotaciones Spring |
| `@EnableReactiveMongoRepositories` | infra-mongo | Activa el escaneo de interfaces `ReactiveMongoRepository` |
| `@Document(collection = "...")` | infra-mongo | Mapea una clase a una colección de MongoDB |
| `@Id` | infra-mongo | Marca el campo identificador del documento |
| `@Field("...")` | infra-mongo | Nombre explícito del campo en Mongo si difiere del atributo Java |
| `Mono<T>` | todas | Publisher reactivo de 0 o 1 elemento (ej. resultado de crear una franquicia) |
| `Flux<T>` | todas | Publisher reactivo de 0..N elementos (ej. listado de productos por sucursal) |
| `RouterFunction<ServerResponse>` | entry-point | Define rutas de forma funcional: path + método HTTP → handler |
| `HandlerFunction<ServerResponse>` | entry-point | Función pura `ServerRequest → Mono<ServerResponse>` |
| `ServerResponse` | entry-point | Builder para construir la respuesta HTTP de forma reactiva |
| `@ExtendWith(MockitoExtension.class)` | tests | Habilita mocks de Mockito en JUnit 5 sin inicialización manual |
| `StepVerifier` | tests | Suscribe a un `Mono`/`Flux` y verifica la secuencia de eventos |
| `@Testcontainers` / `@Container` | tests | Levanta contenedores Docker reales durante el ciclo de vida del test |
| `@DynamicPropertySource` | tests | Inyecta las URLs de los contenedores de Testcontainers en las properties de Spring |
| `ReactiveRedisTemplate` | infra-redis | Cliente reactivo para operar sobre Redis con `Mono`/`Flux` |

---

## 8. Notas finales

- El historial de commits es en sí mismo evidencia del criterio técnico frente al evaluador: cada fase con su propio tag cuenta la historia de cómo se pensó el problema, no solo el resultado final.
- Las fases 9 y 10 deben ajustarse según el proveedor cloud elegido (AWS, GCP o MongoDB Atlas) antes de escribir el Terraform real.
- Este documento es de arquitectura, no de código: cada clase se describe por su responsabilidad, no por su implementación, en línea con el criterio de la prueba de valorar el análisis propio del candidato.
