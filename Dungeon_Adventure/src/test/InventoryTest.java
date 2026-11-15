package test;


import model.Inventory;
import model.Pillar;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryTest {
    /**
     * Checks that the inventory starts with no pillars.
     */
    @Test
    void testInventoryStartsEmpty() {
        final Inventory inventory = new Inventory();

        assertEquals(0, inventory.getPillarCount());
        assertFalse(inventory.hasAllPillars());
        assertTrue(inventory.getPillars().isEmpty());
    }

    /**
     * Checks that pillars can be added to the inventory
     */
    @Test
    void testAddPillar() {
        final Inventory inventory = new Inventory();

        inventory.addPillar(Pillar.ABSTRACTION);
        inventory.addPillar(Pillar.INHERITANCE);

        assertEquals(2, inventory.getPillarCount());
        assertTrue(inventory.getPillars().contains(Pillar.ABSTRACTION));
        assertTrue(inventory.getPillars().contains(Pillar.INHERITANCE));
    }

    /**
     * Checks that the same pillar cannot be counted twice
     */
    @Test
    void testDuplicatePillars() {
        final Inventory inventory = new Inventory();

        inventory.addPillar(Pillar.ABSTRACTION);
        inventory.addPillar(Pillar.ABSTRACTION);

        assertEquals(1, inventory.getPillarCount());
    }

    /**
     * Checks that missing pillars return false
     */
    @Test
    void testMissingPillars() {
        final Inventory inventory = new Inventory();

        inventory.addPillar(Pillar.ABSTRACTION);
        inventory.addPillar(Pillar.ENCAPSULATION);
        inventory.addPillar(Pillar.INHERITANCE);

        assertFalse(inventory.hasAllPillars());
    }

    /**
     * Checks that collecting all four pillars returns true
     */
    @Test
    void testAllPillarsCollected() {
        final Inventory inventory = new Inventory();

        inventory.addPillar(Pillar.ABSTRACTION);
        inventory.addPillar(Pillar.ENCAPSULATION);
        inventory.addPillar(Pillar.INHERITANCE);
        inventory.addPillar(Pillar.POLYMORPHISM);

        assertEquals(4, inventory.getPillarCount());
        assertTrue(inventory.hasAllPillars());
    }

    /**
     * Checks that getPillars returns a copy aand
     * not the original inventory collection
     */
    @Test
    void testGetPillarsReturnsCopy() {
        final Inventory inventory = new Inventory();

        inventory.addPillar(Pillar.ABSTRACTION);

        final EnumSet<Pillar> pillars = inventory.getPillars();

        pillars.add(Pillar.INHERITANCE);

        assertEquals(1, inventory.getPillarCount());
        assertFalse(inventory.getPillars().contains(Pillar.INHERITANCE));
    }

    /**
     * Checks that null cannot be added as a pillar
     */
    @Test
    void testAddNullPillar() {
        final Inventory inventory = new Inventory();

        inventory.addPillar(null);

        assertEquals(0, inventory.getPillarCount());
    }
}
