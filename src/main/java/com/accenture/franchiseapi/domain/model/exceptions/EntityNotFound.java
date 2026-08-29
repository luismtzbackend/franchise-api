package com.accenture.franchiseapi.domain.model.exceptions;

public class EntityNotFound extends RuntimeException {

    private final String entityType;
    private final String entityId;

    public EntityNotFound(String entityType, String entityId) {
        super("El " + entityType + " con ID '" + entityId + "' no existe");
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }
}
