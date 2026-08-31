package com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.handler;

import com.accenture.franchiseapi.application.usecase.ActualizarNombreSucursal;
import com.accenture.franchiseapi.application.usecase.AgregarProducto;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.dto.request.AddProductRequest;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.dto.request.UpdateNameRequest;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.mapper.ProductMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class BranchHandler {

    private final ActualizarNombreSucursal actualizarNombreSucursal;
    private final AgregarProducto agregarProducto;
    private final ProductMapper productMapper;

    public BranchHandler(ActualizarNombreSucursal actualizarNombreSucursal,
                        AgregarProducto agregarProducto,
                        ProductMapper productMapper) {
        this.actualizarNombreSucursal = actualizarNombreSucursal;
        this.agregarProducto = agregarProducto;
        this.productMapper = productMapper;
    }

    public Mono<ServerResponse> actualizarNombreSucursal(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.bodyToMono(UpdateNameRequest.class)
                .flatMap(dto -> actualizarNombreSucursal.ejecutar(id, dto.nombre())
                        .then(ServerResponse.noContent().build()));
    }

    public Mono<ServerResponse> agregarProducto(ServerRequest request) {
        String branchId = request.pathVariable("id");
        return request.bodyToMono(AddProductRequest.class)
                .flatMap(dto -> agregarProducto.ejecutar(branchId, dto.nombre(), dto.stock())
                        .flatMap(product -> ServerResponse.created(
                                java.net.URI.create("/api/products/" + product.getId()))
                                .bodyValue(productMapper.toProductResponse(product))));
    }
}
