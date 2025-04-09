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
    @Test
    void testPetTypeProperties1() {
        PetType type = new PetType();
        type.setId(2);
        type.setName("Hamsters");

        assertEquals(2, type.getId());
        assertEquals("Hamsters", type.getName());
    }
    @Test
    void testPetTypeProperties2() {
        PetType type = new PetType();
        type.setId(3);
        type.setName("Hamsterss");

        assertEquals(3, type.getId());
        assertEquals("Hamsterss", type.getName());
    }
    @Test
    void testHashCodeConsistency() {
        PetType type = new PetType();
        type.setId(4);
        type.setName("Hamstersssssssssssssssssss");
        
        int initialHashCode = type.hashCode();
        assertEquals(initialHashCode, type.hashCode(), "Hash code should remain consistent");
    }

    @Test
    void testToStringContainsName() {
        PetType type = new PetType();
        type.setName("Hamsterssssss");
        
        String toStringResult = type.toString();
        assertTrue(toStringResult.contains("Hamster"), 
            "toString() should include the pet type name");
    }
}
