package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PetTypeTests {

    @Test
    void testPetTypeProperties() {
        PetType type = new PetType();
        type.setId(1);
        type.setName("Hamster");

        assertEquals(1, type.getId());
        assertEquals("Hamster", type.getName());
    }
}
