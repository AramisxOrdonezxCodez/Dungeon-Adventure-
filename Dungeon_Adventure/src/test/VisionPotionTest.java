package test;

import model.Dungeon;
import model.Hero;
import model.Item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VisionPotionTest {

    /**
     * Checks that using a vision potion removes
     * one potion from the inventory.
     */
    @Test
    void testVisionPotionIsConsumed() {
        final Dungeon dungeon = new Dungeon();
        final Hero hero = createTestHero();

        hero.getInventory().addItem(
                new Item("Vision Potion", "Reveals nearby rooms.")
        );

        assertEquals(1, hero.getInventory().getItemCount());

        assertTrue(dungeon.useVisionPotion(hero, 2, 2));

        assertEquals(0, hero.getInventory().getItemCount());
    }

    /**
     * Checks that using a vision potion reveals
     * the current room and surrounding rooms.
     */
    @Test
    void testVisionPotionRevealsRooms() {
        final Dungeon dungeon = new Dungeon();
        final Hero hero = createTestHero();

        hero.getInventory().addItem(
                new Item("Vision Potion", "Reveals nearby rooms.")
        );

        assertTrue(dungeon.useVisionPotion(hero, 2, 2));

        for (int row = 1; row <= 3; row++) {
            for (int col = 1; col <= 3; col++) {
                assertTrue(dungeon.getRoom(row, col).isVisible());
            }
        }
    }

    /**
     * Checks that using a vision potion without
     * having one returns false.
     */
    @Test
    void testVisionPotionNotAvailable() {
        final Dungeon dungeon = new Dungeon();
        final Hero hero = createTestHero();

        assertFalse(dungeon.useVisionPotion(hero, 2, 2));
    }

    /**
     * Checks that a vision potion works
     * at the corner of the dungeon.
     */
    @Test
    void testVisionPotionAtCorner() {
        final Dungeon dungeon = new Dungeon();
        final Hero hero = createTestHero();

        hero.getInventory().addItem(
                new Item("Vision Potion", "Reveals nearby rooms.")
        );

        assertTrue(dungeon.useVisionPotion(hero, 0, 0));

        assertTrue(dungeon.getRoom(0, 0).isVisible());
        assertTrue(dungeon.getRoom(0, 1).isVisible());
        assertTrue(dungeon.getRoom(1, 0).isVisible());
        assertTrue(dungeon.getRoom(1, 1).isVisible());

        assertFalse(dungeon.getRoom(2, 2).isVisible());
    }

    /**
     * Creates a hero for testing.
     */
    private Hero createTestHero() {
        return new Hero("Test Hero", 100, 4,
                0.8, 20, 40, 0.2);
    }
}



