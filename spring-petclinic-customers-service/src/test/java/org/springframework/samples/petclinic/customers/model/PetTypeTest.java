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
    void testEqualsWithSameId() {
        PetType type1 = new PetType();
        type1.setId(1);
        
        PetType type2 = new PetType();
        type2.setId(1);
        
        assertEquals(type1, type2, "PetTypes with same ID should be equal");
    }

    @Test
    void testNotEqualsWithDifferentIds() {
        PetType type1 = new PetType();
        type1.setId(1);
        
        PetType type2 = new PetType();
        type2.setId(2);
        
        assertNotEquals(type1, type2, "PetTypes with different IDs should not be equal");
    }

    @Test
    void testHashCodeConsistency() {
        PetType type = new PetType();
        type.setId(1);
        type.setName("Hamster");
        
        int initialHashCode = type.hashCode();
        assertEquals(initialHashCode, type.hashCode(), "Hash code should remain consistent");
    }

    @Test
    void testToStringContainsName() {
        PetType type = new PetType();
        type.setName("Hamster");
        
        String toStringResult = type.toString();
        assertTrue(toStringResult.contains("Hamster"), 
            "toString() should include the pet type name");
    }

    @Test
    void testEqualityWithDifferentClass() {
        PetType type = new PetType();
        Object otherObject = new Object();
        
        assertNotEquals(type, otherObject, 
            "PetType should not be equal to different class");
    }
}
