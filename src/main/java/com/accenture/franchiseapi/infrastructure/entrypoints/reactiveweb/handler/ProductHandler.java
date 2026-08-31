package com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.handler;

import com.accenture.franchiseapi.application.usecase.ActualizarNombreProducto;
import com.accenture.franchiseapi.application.usecase.ActualizarStockProducto;
import com.accenture.franchiseapi.application.usecase.EliminarProducto;
import com.accenture.franchiseapi.application.usecase.ObtenerMayorStockPorFranquicia;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.dto.request.UpdateNameRequest;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.dto.request.UpdateStockRequest;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.mapper.ProductMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ProductHandler {

    private final EliminarProducto eliminarProducto;
    private final ActualizarStockProducto actualizarStockProducto;
    private final ActualizarNombreProducto actualizarNombreProducto;
    private final ObtenerMayorStockPorFranquicia obtenerMayorStockPorFranquicia;
    private final ProductMapper productMapper;

    public ProductHandler(EliminarProducto eliminarProducto,
                         ActualizarStockProducto actualizarStockProducto,
                         ActualizarNombreProducto actualizarNombreProducto,
                         ObtenerMayorStockPorFranquicia obtenerMayorStockPorFranquicia,
                         ProductMapper productMapper) {
        this.eliminarProducto = eliminarProducto;
        this.actualizarStockProducto = actualizarStockProducto;
        this.actualizarNombreProducto = actualizarNombreProducto;
        this.obtenerMayorStockPorFranquicia = obtenerMayorStockPorFranquicia;
        this.productMapper = productMapper;
    }

    public Mono<ServerResponse> eliminarProducto(ServerRequest request) {
        String id = request.pathVariable("id");
        return eliminarProducto.ejecutar(id)
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> actualizarStock(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.bodyToMono(UpdateStockRequest.class)
                .flatMap(dto -> actualizarStockProducto.ejecutar(id, dto.stock())
                        .then(ServerResponse.noContent().build()));
    }

    public Mono<ServerResponse> actualizarNombre(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.bodyToMono(UpdateNameRequest.class)
                .flatMap(dto -> actualizarNombreProducto.ejecutar(id, dto.nombre())
                        .then(ServerResponse.noContent().build()));
    }

    public Mono<ServerResponse> obtenerMayorStockPorFranquicia(ServerRequest request) {
        String franchiseId = request.pathVariable("id");
        return obtenerMayorStockPorFranquicia.ejecutar(franchiseId)
                .map(productMapper::toProductResponse)
                .collectList()
                .flatMap(productos -> ServerResponse.ok().bodyValue(productos));
    }
}
