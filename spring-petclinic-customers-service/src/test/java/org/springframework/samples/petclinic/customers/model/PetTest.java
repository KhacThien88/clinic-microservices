package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class PetTests {

    @Test
    void testPetBasicProperties() {
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Tom");
        Date date = new Date();
        pet.setBirthDate(date);

        PetType type = new PetType();
        type.setId(1);
        type.setName("Dog");
        pet.setType(type);

        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        pet.setOwner(owner);

        assertEquals(1, pet.getId());
        assertEquals("Tom", pet.getName());
        assertEquals(date, pet.getBirthDate());
        assertEquals("Dog", pet.getType().getName());
        assertEquals("John", pet.getOwner().getFirstName());
    }

    @Test
    void testPetToString() {
        Pet pet = new Pet();
        pet.setId(2);
        pet.setName("Jerry");
        pet.setBirthDate(new Date());

        PetType type = new PetType();
        type.setName("Cat");
        pet.setType(type);

        Owner owner = new Owner();
        owner.setFirstName("Alice");
        owner.setLastName("Smith");
        pet.setOwner(owner);

        String str = pet.toString();
        assertTrue(str.contains("Jerry"));
        assertTrue(str.contains("Cat"));
        assertTrue(str.contains("Alice"));
        assertTrue(str.contains("Smith"));
    }

    @Test
    void testEqualsAndHashCode() {
        PetType type = new PetType();
        type.setId(1);
        type.setName("Dog");

        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");

        Date date = new Date();

        Pet pet1 = new Pet();
        pet1.setId(1);
        pet1.setName("Tom");
        pet1.setBirthDate(date);
        pet1.setType(type);
        pet1.setOwner(owner);

        Pet pet2 = new Pet();
        pet2.setId(1);
        pet2.setName("Tom");
        pet2.setBirthDate(date);
        pet2.setType(type);
        pet2.setOwner(owner);

        assertEquals(pet1, pet2);
        assertEquals(pet1.hashCode(), pet2.hashCode());
    }

    @Test
    void testNotEqualsDifferentClass() {
        Pet pet = new Pet();
        assertNotEquals(pet, "NotAPet");
    }

    @Test
    void testNotEqualsNull() {
        Pet pet = new Pet();
        assertNotEquals(pet, null);
    }
}
