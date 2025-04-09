package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VetTest {

    @Test
    void testAddAndGetSpecialties() {
        Vet vet = new Vet();
        Specialty s1 = new Specialty();
        s1.setName("surgery");

        Specialty s2 = new Specialty();
        s2.setName("dentistry");

        vet.addSpecialty(s1);
        vet.addSpecialty(s2);

        List<Specialty> specialties = vet.getSpecialties();

        assertEquals(2, specialties.size());
        assertTrue(specialties.stream().anyMatch(s -> s.getName().equals("surgery")));
        assertTrue(specialties.stream().anyMatch(s -> s.getName().equals("dentistry")));
    }

    @Test
    void testGetNrOfSpecialties() {
        Vet vet = new Vet();
        assertEquals(0, vet.getNrOfSpecialties());

        Specialty specialty = new Specialty();
        specialty.setName("radiology");

        vet.addSpecialty(specialty);
        assertEquals(1, vet.getNrOfSpecialties());
    }

    @Test
    void testSettersAndGetters() {
        Vet vet = new Vet();

        vet.setId(101);
        vet.setFirstName("John");
        vet.setLastName("Doe");

        assertEquals(101, vet.getId());
        assertEquals("John", vet.getFirstName());
        assertEquals("Doe", vet.getLastName());
    }
}
