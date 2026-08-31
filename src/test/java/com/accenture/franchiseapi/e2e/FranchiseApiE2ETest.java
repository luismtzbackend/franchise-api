package com.accenture.franchiseapi.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FranchiseApiE2ETest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getFirstMappedPort());
    }

    @BeforeEach
    void setUp() {
    }

    @Test
    void debeCrearFranquiciaYValidarRespuesta() {
        webTestClient.post()
                .uri("/api/franchises")
                .bodyValue(new CreateFranchiseRequestDto("Franquicia Test E2E"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.nombre").isEqualTo("Franquicia Test E2E");
    }

    @Test
    void debeAgregarSucursalAFranquicia() {
        String franchiseId = crearFranquicia("Franquicia Test");

        webTestClient.post()
                .uri("/api/franchises/{id}/branches", franchiseId)
                .bodyValue(new AddBranchRequestDto("Sucursal Central"))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists("Location");
    }

    @Test
    void debeAgregarProductoASucursal() {
        String franchiseId = crearFranquicia("Franquicia Test");
        String branchId = crearSucursal(franchiseId, "Sucursal Test");

        webTestClient.post()
                .uri("/api/branches/{id}/products", branchId)
                .bodyValue(new AddProductRequestDto("Café Premium", 100))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.nombre").isEqualTo("Café Premium")
                .jsonPath("$.stock").isEqualTo(100);
    }

    @Test
    void debeActualizarStockProducto() {
        String franchiseId = crearFranquicia("Franquicia Test");
        String branchId = crearSucursal(franchiseId, "Sucursal Test");
        String productId = crearProducto(branchId, "Café", 100);

        webTestClient.patch()
                .uri("/api/products/{id}/stock", productId)
                .bodyValue(new UpdateStockRequestDto(250))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void debeActualizarNombreProducto() {
        String franchiseId = crearFranquicia("Franquicia Test");
        String branchId = crearSucursal(franchiseId, "Sucursal Test");
        String productId = crearProducto(branchId, "Café Viejo", 100);

        webTestClient.put()
                .uri("/api/products/{id}/name", productId)
                .bodyValue(new UpdateNameRequestDto("Café Nuevo"))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void debeEliminarProducto() {
        String franchiseId = crearFranquicia("Franquicia Test");
        String branchId = crearSucursal(franchiseId, "Sucursal Test");
        String productId = crearProducto(branchId, "Café", 100);

        webTestClient.delete()
                .uri("/api/products/{id}", productId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void debeObtenerProductoMayorStockPorFranquicia() {
        String franchiseId = crearFranquicia("Franquicia Test");
        String branchId1 = crearSucursal(franchiseId, "Sucursal 1");
        String branchId2 = crearSucursal(franchiseId, "Sucursal 2");

        crearProducto(branchId1, "Café", 100);
        crearProducto(branchId1, "Té", 50);
        crearProducto(branchId2, "Azúcar", 150);

        webTestClient.get()
                .uri("/api/franchises/{id}/top-stock", franchiseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].nombre").isEqualTo("Café")
                .jsonPath("$[1].nombre").isEqualTo("Azúcar");
    }

    private String crearFranquicia(String nombre) {
        return webTestClient.post()
                .uri("/api/franchises")
                .bodyValue(new CreateFranchiseRequestDto(nombre))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(FranchiseResponseDto.class)
                .returnResult()
                .getResponseBody()
                .id();
    }

    private String crearSucursal(String franchiseId, String nombre) {
        String location = webTestClient.post()
                .uri("/api/franchises/{id}/branches", franchiseId)
                .bodyValue(new AddBranchRequestDto(nombre))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult()
                .getResponseHeaders()
                .getLocation()
                .getPath();
        return location.substring(location.lastIndexOf('/') + 1);
    }

    private String crearProducto(String branchId, String nombre, int stock) {
        return webTestClient.post()
                .uri("/api/branches/{id}/products", branchId)
                .bodyValue(new AddProductRequestDto(nombre, stock))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponseDto.class)
                .returnResult()
                .getResponseBody()
                .id();
    }

    record CreateFranchiseRequestDto(String nombre) {}
    record AddBranchRequestDto(String nombre) {}
    record AddProductRequestDto(String nombre, int stock) {}
    record UpdateStockRequestDto(int stock) {}
    record UpdateNameRequestDto(String nombre) {}
    record FranchiseResponseDto(String id, String nombre) {}
    record ProductResponseDto(String id, String nombre) {}
}
