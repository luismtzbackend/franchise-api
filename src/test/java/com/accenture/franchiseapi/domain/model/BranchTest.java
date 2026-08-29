package com.accenture.franchiseapi.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BranchTest {

    @Test
    void shouldBuildValidBranch() {
        Branch branch = Branch.builder()
                .id("b-1")
                .franchiseId("f-1")
                .name("Sucursal Norte")
                .build();

        assertEquals("b-1", branch.getId());
        assertEquals("f-1", branch.getFranchiseId());
        assertEquals("Sucursal Norte", branch.getName());
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        Branch.Builder builder = Branch.builder().id("b-1").franchiseId("f-1").name("");

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void shouldFailWhenFranchiseIdIsMissing() {
        Branch.Builder builder = Branch.builder().id("b-1").name("Sucursal Norte");

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void shouldRenameByCreatingNewInstance() {
        Branch original = Branch.builder().id("b-1").franchiseId("f-1").name("Vieja").build();

        Branch renamed = original.rename("Nueva");

        assertEquals("Nueva", renamed.getName());
        assertEquals("Vieja", original.getName());
        assertEquals(original.getFranchiseId(), renamed.getFranchiseId());
    }
}
