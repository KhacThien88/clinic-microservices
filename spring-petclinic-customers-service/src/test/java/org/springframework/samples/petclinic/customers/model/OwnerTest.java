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
    @Test
    void petManagementWorkflow() {
        Owner owner = new Owner();
        Pet pet = new Pet();
        pet.setName("Fluffy");
        
        owner.addPet(pet);
        
        assertEquals(1, owner.getPets().size());
        assertEquals(owner, pet.getOwner());
    }

    @Test
    void petSortingByName() {
        Owner owner = new Owner();
        
        Pet pet1 = new Pet();
        pet1.setName("Zorro");
        Pet pet2 = new Pet();
        pet2.setName("Alpha");
        
        owner.addPet(pet1);
        owner.addPet(pet2);
        
        List<Pet> pets = owner.getPets();
        assertEquals("Alpha", pets.get(0).getName());
        assertEquals("Zorro", pets.get(1).getName());
    }

    @Test
    void unmodifiablePetList() {
        Owner owner = new Owner();
        assertThrows(UnsupportedOperationException.class, () -> {
            owner.getPets().add(new Pet());
        });
    }

    @Test
    void fieldValidationThroughSetters() {
        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        owner.setAddress("123 Street");
        owner.setCity("Metropolis");
        owner.setTelephone("1234567890");
        
        assertEquals("John", owner.getFirstName());
        assertEquals("Doe", owner.getLastName());
        assertEquals("123 Street", owner.getAddress());
        assertEquals("Metropolis", owner.getCity());
        assertEquals("1234567890", owner.getTelephone());
    }
    @Test
    void testPetsInitialization() {
        Owner owner = new Owner();
        assertTrue(owner.getPets().isEmpty(), "Should initialize with empty pets list");
    
        owner.addPet(new Pet());
        assertEquals(1, owner.getPets().size(), "Should add pet to collection");
    }

    @Test
    void testIdAutoGeneration() {
        Owner owner = new Owner();
        assertNull(owner.getId(), "ID should be null before persistence");
    }

    @Test
    void testTelephoneValidation() {
        Owner owner = new Owner();
        assertThrows(ValidationException.class, () -> 
        owner.setTelephone("invalid-phone"));
    }
}
