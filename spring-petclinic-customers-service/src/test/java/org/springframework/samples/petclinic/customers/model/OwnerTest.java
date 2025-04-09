package org.springframework.samples.petclinic.customers.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OwnerTests {

    private Owner owner;

    @BeforeEach
    void setup() {
        owner = new Owner();
        owner.setFirstName("Nguyen");
        owner.setLastName("Van A");
        owner.setAddress("123 Le Loi");
        owner.setCity("Da Nang");
        owner.setTelephone("0123456789");
    }

    @Test
    void testOwnerBasicProperties() {
        assertEquals("Nguyen", owner.getFirstName());
        assertEquals("Van A", owner.getLastName());
        assertEquals("123 Le Loi", owner.getAddress());
        assertEquals("Da Nang", owner.getCity());
        assertEquals("0123456789", owner.getTelephone());
    }

    @Test
    void testAddPet() {
        Pet pet = new Pet();
        pet.setName("Tom");
        pet.setBirthDate(new Date());

        PetType type = new PetType();
        type.setId(1);
        type.setName("Dog");

        pet.setType(type);

        owner.addPet(pet);

        List<Pet> pets = owner.getPets();
        assertEquals(1, pets.size());
        assertEquals("Tom", pets.get(0).getName());
        assertEquals(owner, pets.get(0).getOwner());
    }

    @Test
    void testGetPetsInternalEmptyInit() {
        assertNotNull(owner.getPets());
        assertTrue(owner.getPets().isEmpty());
    }

    @Test
    void testToString() {
        owner.setFirstName("An");
        owner.setLastName("Nguyen");
        owner.setAddress("456 Tran Hung Dao");
        owner.setCity("Ha Noi");
        owner.setTelephone("0987654321");

        String toString = owner.toString();

        assertTrue(toString.contains("An"));
        assertTrue(toString.contains("Nguyen"));
        assertTrue(toString.contains("456 Tran Hung Dao"));
        assertTrue(toString.contains("Ha Noi"));
        assertTrue(toString.contains("0987654321"));
    }
}
