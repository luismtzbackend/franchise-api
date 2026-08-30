package com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.dto.response;

public record ProductResponse(String id, String nombre, String sucursalId, int stock) {
}
