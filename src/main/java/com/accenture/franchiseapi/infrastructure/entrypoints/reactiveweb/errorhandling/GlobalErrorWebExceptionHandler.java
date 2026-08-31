package com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.errorhandling;

import com.accenture.franchiseapi.domain.model.exceptions.EntityNotFound;
import com.accenture.franchiseapi.domain.model.exceptions.InvalidStock;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = resolverEstado(ex);
        String mensaje = status == HttpStatus.INTERNAL_SERVER_ERROR
                ? "Ha ocurrido un error inesperado"
                : ex.getMessage();

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes = ("{\"error\":\"" + escaparJson(mensaje) + "\"}").getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private HttpStatus resolverEstado(Throwable ex) {
        if (ex instanceof EntityNotFound) {
            return HttpStatus.NOT_FOUND;
        }
        if (ex instanceof InvalidStock || ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String escaparJson(String mensaje) {
        return mensaje == null ? "" : mensaje.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
