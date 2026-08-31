package com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.router;

import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.handler.BranchHandler;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.handler.FranchiseHandler;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ApiRouter {

    @Bean
    public RouterFunction<ServerResponse> routes(FranchiseHandler franchiseHandler,
                                                  BranchHandler branchHandler,
                                                  ProductHandler productHandler) {
        return RouterFunctions.route()
                .POST("/api/franchises", franchiseHandler::crearFranquicia)
                .PUT("/api/franchises/{id}/name", franchiseHandler::actualizarNombreFranquicia)
                .POST("/api/franchises/{id}/branches", franchiseHandler::agregarSucursal)
                .PUT("/api/branches/{id}/name", branchHandler::actualizarNombreSucursal)
                .POST("/api/branches/{id}/products", branchHandler::agregarProducto)
                .DELETE("/api/products/{id}", productHandler::eliminarProducto)
                .PATCH("/api/products/{id}/stock", productHandler::actualizarStock)
                .PUT("/api/products/{id}/name", productHandler::actualizarNombre)
                .GET("/api/franchises/{id}/top-stock", productHandler::obtenerMayorStockPorFranquicia)
                .build();
    }
}
