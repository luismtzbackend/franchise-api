# Franchise API

API para franquicias, sucursales y productos. Cada producto tiene stock, y hay un endpoint para ver cuál producto tiene más stock por sucursal dentro de una franquicia.

Spring Boot + WebFlux, Clean Architecture: el dominio no depende de Spring, Mongo ni Redis.

## Stack

- Spring Boot 4 + WebFlux, sin `@RestController`, todo con `RouterFunction`/`HandlerFunction`
- MongoDB reactivo
- Redis como cache
- Docker / docker-compose
- Terraform para MongoDB Atlas + Redis en Upstash
- JUnit 5, Mockito, StepVerifier, Testcontainers

## Levantar con Docker

```
docker compose up -d --build
```

Levanta la app (8080), MongoDB (27017) y Redis (6379) en la misma red. `docker logs -f franchise-api` para ver qué pasa, `docker compose down` para bajar todo.

## Endpoints

| #   | Método | Ruta                             | Body                                 |
| --- | ------ | -------------------------------- | ------------------------------------ |
| 1   | POST   | `/api/franchises`                | `{ "nombre": "Mi Franquicia" }`      |
| 2   | PUT    | `/api/franchises/{id}/name`      | `{ "nombre": "Nuevo Nombre" }`       |
| 3   | POST   | `/api/franchises/{id}/branches`  | `{ "nombre": "Sucursal Centro" }`    |
| 4   | PUT    | `/api/branches/{id}/name`        | `{ "nombre": "Nuevo Nombre" }`       |
| 5   | POST   | `/api/branches/{id}/products`    | `{ "nombre": "Cafe", "stock": 100 }` |
| 6   | DELETE | `/api/products/{id}`             | sin body                             |
| 7   | PATCH  | `/api/products/{id}/stock`       | `{ "stock": 250 }`                   |
| 8   | PUT    | `/api/products/{id}/name`        | `{ "nombre": "Nuevo Nombre" }`       |
| 9   | GET    | `/api/franchises/{id}/top-stock` | sin body                             |

## Despliegue en AWS EC2

Corriendo en una instancia EC2, con el puerto 8080 abierto en el security group.

`http://3.19.237.215:8080` — sin dominio DuckDNS todavía, se pega directo a la IP.

Prueba real:

```
POST http://3.19.237.215:8080/api/franchises
Body: {"nombre":"PruebaEC2"}
201 Created — {"id":"6a95198cafa78a8fc9dbebb7","nombre":"PruebaEC2"}
```

## Variables de entorno

Nada hardcodeado, todo con default para desarrollo local:

- `MONGO_ROOT_USER` (default `admin`), `MONGO_ROOT_PASSWORD` (default `password`)
- `SPRING_DATA_REDIS_HOST` (default `redis`), `SPRING_DATA_REDIS_PORT` (default `6379`)

Ya están puestas en `docker-compose.yml`. Para usar otras credenciales, expórtalas antes de levantar:

```
export MONGO_ROOT_USER=otro_usuario
export MONGO_ROOT_PASSWORD=otro_password
docker compose up -d --build
```

## Tests

```
mvn test
```

Dominio y casos de uso corren con mocks. Integración y E2E usan Testcontainers, necesitan Docker corriendo.

## Estructura

```
franchise-api/
├── domain/
│   ├── model/               Franchise, Branch, Product, Stock, exceptions/
│   └── port/                FranchiseRepository, BranchRepository, ProductRepository, TopStockCache
├── application/
│   └── usecase/             una clase por operación de negocio
├── infrastructure/
│   ├── drivenadapters/
│   │   ├── persistencemongo/  documentos, repositorios de Spring Data, mappers, adapters
│   │   └── cacheredis/        config de Redis, cache de top-stock, decorator
│   └── entrypoints/
│       └── reactiveweb/       router, handlers, dtos, mappers, manejo de errores
├── config/                  BeanConfiguration
├── infra/terraform/         main.tf, variables.tf, outputs.tf
├── docker-compose.yml
├── Dockerfile
└── FranchiseApiApplication.java
```
