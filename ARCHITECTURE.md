# Arquitectura — Franchise API

API para gestionar franquicias, sus sucursales y los productos que vende cada sucursal. Cada producto tiene un stock, y hay un endpoint para saber cuál es el producto con más stock por sucursal dentro de una franquicia.

Está hecha en Spring Boot con WebFlux, con Clean Architecture: el dominio no depende de Spring, Mongo ni Redis.

## Stack

- Spring Boot 4 + WebFlux, sin `@RestController`, todo con `RouterFunction`/`HandlerFunction`
- MongoDB reactivo como persistencia
- Redis como cache
- Docker / docker-compose para levantar todo junto
- Terraform para MongoDB Atlas + Redis (Upstash) en la nube
- JUnit 5, Mockito, StepVerifier y Testcontainers para tests

## Decisiones técnicas

- Franquicia, sucursal y producto están en colecciones separadas en Mongo, no anidadas. Así se puede actualizar solo el stock de un producto sin tocar el resto del árbol.
- Redis guarda el resultado de "mayor stock por sucursal" con la clave `top-stock:{franchiseId}`, y se borra cada vez que se crea, elimina o actualiza un producto de esa franquicia.
- El cache está en una clase aparte (`CachedProductRepository`) que envuelve al repositorio de Mongo, para no mezclar la lógica de cache con la de persistencia.
- Cada caso de uso es su propia clase con un método (`ejecutar`), en vez de tener todo junto en una sola clase.
- Si algo no existe o el stock es inválido, se lanza `EntityNotFound` o `InvalidStock`. `GlobalErrorWebExceptionHandler` los convierte en 404, 400 o 500.
- `BeanConfiguration` conecta cada caso de uso con su repositorio a mano, sin anotaciones de Spring en el dominio.

## Estructura de carpetas

```
franchise-api/
├── domain/
│   ├── model/               Franchise, Branch, Product, Stock, exceptions/
│   └── port/                interfaces: FranchiseRepository, BranchRepository, ProductRepository, TopStockCache
├── application/
│   └── usecase/             una clase por operación de negocio
├── infrastructure/
│   ├── drivenadapters/
│   │   ├── persistencemongo/  documentos, repos de Spring Data, mappers, adapters
│   │   └── cacheredis/        config de Redis, cache de top-stock, decorator
│   └── entrypoints/
│       └── reactiveweb/       router, handlers, dtos, mappers, manejo de errores
├── config/                  BeanConfiguration (conecta los casos de uso)
├── infra/terraform/         main.tf, variables.tf, outputs.tf
├── docker-compose.yml
├── Dockerfile
└── FranchiseApiApplication.java
```

## Endpoints

| Método | Ruta                             | Qué hace                                                 |
| ------ | -------------------------------- | -------------------------------------------------------- |
| POST   | `/api/franchises`                | Crea una franquicia                                      |
| PUT    | `/api/franchises/{id}/name`      | Renombra una franquicia                                  |
| POST   | `/api/franchises/{id}/branches`  | Agrega una sucursal                                      |
| PUT    | `/api/branches/{id}/name`        | Renombra una sucursal                                    |
| POST   | `/api/branches/{id}/products`    | Agrega un producto                                       |
| DELETE | `/api/products/{id}`             | Elimina un producto                                      |
| PATCH  | `/api/products/{id}/stock`       | Actualiza el stock de un producto                        |
| PUT    | `/api/products/{id}/name`        | Renombra un producto                                     |
| GET    | `/api/franchises/{id}/top-stock` | Producto con más stock por sucursal, para una franquicia |

## Levantar con Docker

```
docker compose up -d --build
```

Levanta la app, MongoDB y Redis en una red compartida. La app expone el puerto 8080.

## Correr los tests

```
mvn test
```

Los tests de dominio y casos de uso corren con mocks, sin nada externo. Los de integración y el E2E usan Testcontainers, así que necesitan Docker corriendo en la máquina.
