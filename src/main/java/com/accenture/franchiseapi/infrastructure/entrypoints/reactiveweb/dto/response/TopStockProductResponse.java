package com.accenture.franchiseapi.infrastructure.entrypoints.reactiveweb.dto.response;

public record TopStockProductResponse(String sucursalId, String nombreSucursal, String productoId, String nombreProducto, int stock) {
}
