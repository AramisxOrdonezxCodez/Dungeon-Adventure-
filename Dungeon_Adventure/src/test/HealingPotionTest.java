package test;
import model.Hero;
import model.Item;
import model.Room;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HealingPotionTest {
    private Hero createTestHero() {
        return new Hero("Test Hero", 100, 4,
                0.8, 20, 40, 0.2);
    }

    @Test
    void testHealingPotionRestoresHealth() {
        final Hero hero = createTestHero();
        hero.setHitPoints(50);

        hero.getInventory().addItem(
                new Item("Healing Potion", "Restores health.")
        );

        assertTrue(hero.useHealingPotion());

        assertTrue(hero.getHitPoints() >= 55);
        assertTrue(hero.getHitPoints() <= 65);
    }

    @Test
    void testHealingPotionDoesNotExceedMaxHealth() {
        final Hero hero = createTestHero();
        hero.setHitPoints(98);

        hero.getInventory().addItem(
                new Item("Healing Potion", "Restores health.")
        );

        assertTrue(hero.useHealingPotion());
        assertEquals(100, hero.getHitPoints());
    }

    @Test
    void testHealingPotionRemovedAfterUse() {
        final Hero hero = createTestHero();

        hero.getInventory().addItem(
                new Item("Healing Potion", "Restores health.")
        );

        assertEquals(1, hero.getInventory().getItemCount());

        assertTrue(hero.useHealingPotion());

        assertEquals(0, hero.getInventory().getItemCount());
        assertFalse(hero.useHealingPotion());
    }

    @Test
    void testHealingPotionCollection() {
        final Room room = new Room();
        final Hero hero = createTestHero();

        // Room contents are randomized, so only test
        // collection when a healing potion is present.
        if (room.getHasHealingPotion()) {
            assertTrue(room.collectHealingPotion(hero));
            assertEquals(1, hero.getInventory().getItemCount());
            assertFalse(room.getHasHealingPotion());
            assertFalse(room.collectHealingPotion(hero));
        }
    }
}
