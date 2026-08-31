package com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.handler;

import com.accenture.franchiseapi.application.usecase.ActualizarNombreFranquicia;
import com.accenture.franchiseapi.application.usecase.AgregarSucursal;
import com.accenture.franchiseapi.application.usecase.CrearFranquicia;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.dto.request.AddBranchRequest;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.dto.request.CreateFranchiseRequest;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.dto.request.UpdateNameRequest;
import com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.mapper.FranchiseMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class FranchiseHandler {

    private final CrearFranquicia crearFranquicia;
    private final ActualizarNombreFranquicia actualizarNombreFranquicia;
    private final AgregarSucursal agregarSucursal;
    private final FranchiseMapper franchiseMapper;

    public FranchiseHandler(CrearFranquicia crearFranquicia,
                           ActualizarNombreFranquicia actualizarNombreFranquicia,
                           AgregarSucursal agregarSucursal,
                           FranchiseMapper franchiseMapper) {
        this.crearFranquicia = crearFranquicia;
        this.actualizarNombreFranquicia = actualizarNombreFranquicia;
        this.agregarSucursal = agregarSucursal;
        this.franchiseMapper = franchiseMapper;
    }

    public Mono<ServerResponse> crearFranquicia(ServerRequest request) {
        return request.bodyToMono(CreateFranchiseRequest.class)
                .flatMap(dto -> crearFranquicia.ejecutar(dto.nombre())
                        .flatMap(franchise -> ServerResponse.created(
                                java.net.URI.create("/api/franchises/" + franchise.getId()))
                                .bodyValue(franchiseMapper.toFranchiseResponse(franchise))));
    }

    public Mono<ServerResponse> actualizarNombreFranquicia(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.bodyToMono(UpdateNameRequest.class)
                .flatMap(dto -> actualizarNombreFranquicia.ejecutar(id, dto.nombre())
                        .then(ServerResponse.noContent().build()));
    }

    public Mono<ServerResponse> agregarSucursal(ServerRequest request) {
        String franchiseId = request.pathVariable("id");
        return request.bodyToMono(AddBranchRequest.class)
                .flatMap(dto -> agregarSucursal.ejecutar(franchiseId, dto.nombre())
                        .flatMap(branch -> ServerResponse.created(
                                java.net.URI.create("/api/branches/" + branch.getId()))
                                .build()));
    }
}
