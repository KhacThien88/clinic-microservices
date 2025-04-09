package org.springframework.samples.petclinic.visits.model;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class VisitTest {

    @Test
    void testVisitGettersAndSetters() {
        Visit visit = new Visit();
        Date now = new Date();
        visit.setId(1);
        visit.setDate(now);
        visit.setDescription("Routine Checkup");
        visit.setPetId(42);

        assertEquals(1, visit.getId());
        assertEquals(now, visit.getDate());
        assertEquals("Routine Checkup", visit.getDescription());
        assertEquals(42, visit.getPetId());
    }

    @Test
    void testVisitBuilder() {
        Date now = new Date();
        Visit visit = Visit.VisitBuilder.aVisit()
                .id(2)
                .date(now)
                .description("Dental cleaning")
                .petId(101)
                .build();

        assertEquals(2, visit.getId());
        assertEquals(now, visit.getDate());
        assertEquals("Dental cleaning", visit.getDescription());
        assertEquals(101, visit.getPetId());
    }

    @Test
    void testDefaultDateNotNull() {
        Visit visit = new Visit();
        assertNotNull(visit.getDate(), "Default date should not be null");
    }
    @Test
    void testVisitBuilderWithNullValues() {
        Visit visit = Visit.VisitBuilder.aVisit()
                .id(null)
                .date(null)
                .description(null)
                .petId(0)
                .build();

        assertNull(visit.getId());
        assertNull(visit.getDate());
        assertNull(visit.getDescription());
        assertEquals(0, visit.getPetId());
    }
    @Test
    void testUpdateVisitDescription() {
        Visit visit = new Visit();
        visit.setPetId(500);
        visit.setDescription("Initial check");
        visit.setDate(new Date());

        visit = visitRepository.save(visit);

        visit.setDescription("Updated description");
        visit = visitRepository.save(visit);

        Visit updated = visitRepository.findById(visit.getId()).orElseThrow();
        assertEquals("Updated description", updated.getDescription());
    }

}
