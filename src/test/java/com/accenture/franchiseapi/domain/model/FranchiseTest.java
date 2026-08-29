package com.accenture.franchiseapi.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FranchiseTest {

    @Test
    void shouldBuildValidFranchise() {
        Franchise franchise = Franchise.builder()
                .id("f-1")
                .name("Franquicia Central")
                .build();

        assertEquals("f-1", franchise.getId());
        assertEquals("Franquicia Central", franchise.getName());
    }

    @Test
    void shouldFailWhenNameIsNull() {
        Franchise.Builder builder = Franchise.builder().id("f-1");

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        Franchise.Builder builder = Franchise.builder().id("f-1").name("   ");

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void shouldRenameByCreatingNewInstance() {
        Franchise original = Franchise.builder().id("f-1").name("Nombre viejo").build();

        Franchise renamed = original.rename("Nombre nuevo");

        assertEquals("Nombre nuevo", renamed.getName());
        assertEquals("Nombre viejo", original.getName());
        assertEquals(original.getId(), renamed.getId());
    }
}
