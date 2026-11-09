/*
 * TCSS 360 - Dungeon Adventure
 * Kevin Munoz-Rivera
 */
package test;

import static org.junit.jupiter.api.Assertions.*;

import model.Direction;
import model.Hero;
import model.Monster;
import model.MonsterDefinition;
import model.Pillar;
import model.Room;
import org.junit.jupiter.api.Test;

/**
 * Basic tests for room doors and special rooms.
 *
 * @author Kevin Munoz-Rivera
 * @version 2026-11-08
 */
public class RoomTest {

    /** Checks that adding a north door opens only the north direction. */
    @Test
    void testAddDoor() {
        final Room room = new Room();
        room.addDoor(Direction.NORTH);
        assertTrue(room.hasDoor(Direction.NORTH));
        assertFalse(room.hasDoor(Direction.SOUTH));
        assertFalse(room.hasDoor(Direction.EAST));
        assertFalse(room.hasDoor(Direction.WEST));
    }

    /** Checks that the entrance is empty of items and pits. */
    @Test
    void testEntranceIsEmpty() {
        final Room room = new Room();
        room.setPillar(Pillar.ABSTRACTION);
        room.setMyEntrance(true);
        assertTrue(room.getIsEntrance());
        assertFalse(room.getIsExit());
        assertFalse(room.getHasHealingPotion());
        assertFalse(room.getMyHasVisionPotion());
        assertFalse(room.getMyHasPit());
        assertEquals(0, room.getMyPitDamage());
        assertNull(room.getPillar());
    }

    /** Checks that an entrance cannot also become an exit. */
    @Test
    void testEntranceCannotBeExit() {
        final Room room = new Room();
        room.setMyEntrance(true);
        assertThrows(IllegalStateException.class, () -> room.setExit(true));
        assertTrue(room.getIsEntrance());
        assertFalse(room.getIsExit());
    }

    /** Checks the entrance drawing shown in the assignment. */
    @Test
    void testEntranceDrawing() {
        final Room room = new Room();
        room.setMyEntrance(true);
        room.addDoor(Direction.EAST);
        room.addDoor(Direction.SOUTH);
        assertEquals("***\n*i|\n*-*", room.toString());
    }

    /** Checks that a room stores and removes the supplied monster. */
    @Test
    void testMonsterStorage() {
        final Room room = new Room();
        final Monster monster = createMonster();
        assertNull(room.getMonster());
        room.setMonster(monster);
        assertSame(monster, room.getMonster());
        assertSame(monster, room.removeMonster());
        assertNull(room.getMonster());
        assertNull(room.removeMonster());
        room.setMonster(monster);
        room.setMonster(null);
        assertNull(room.getMonster());
    }

    /** Checks that marking an entrance clears its monster and rejects another one. */
    @Test
    void testEntranceCannotContainMonster() {
        final Room room = new Room();
        final Monster monster = createMonster();
        room.setMonster(monster);
        room.setMyEntrance(true);
        assertNull(room.getMonster());
        assertThrows(IllegalStateException.class, () -> room.setMonster(monster));
        assertNull(room.getMonster());
    }

    /** Checks that marking an exit clears its monster and rejects another one. */
    @Test
    void testExitCannotContainMonster() {
        final Room room = new Room();
        final Monster monster = createMonster();
        room.setMonster(monster);
        room.setExit(true);
        assertNull(room.getMonster());
        assertThrows(IllegalStateException.class, () -> room.setMonster(monster));
        assertNull(room.getMonster());
    }

    /**
     * Creates a monster with fixed test values using the team's classes.
     *
     * @return a monster for a room test
     */
    private Monster createMonster() {
        final MonsterDefinition definition = new MonsterDefinition(
                "Test", "Test Monster", 100, 1, 1.0, 1, 1, 0.0, 1, 1);
        return new Monster(definition);
    }

    /** Checks that even a hero with guaranteed blocking takes full pit damage. */
    @Test
    void testPitDamageCannotBeBlocked() {
        final Room room = createPitRoom();
        final Hero hero = new Hero("Test Hero", 100, 1, 1.0, 1, 1, 1.0);
        final int damage = room.getMyPitDamage();
        assertTrue(damage >= 1 && damage <= 20);
        hero.takeDamage(damage);
        assertEquals(100, hero.getHitPoints());
        room.applyPit(hero);
        assertEquals(100 - damage, hero.getHitPoints());
        assertTrue(room.getMyHasPit());
        assertEquals(damage, room.getMyPitDamage());
    }

    /** Checks that lethal pit damage stops at zero hit points. */
    @Test
    void testPitDamageCannotMakeHealthNegative() {
        final Room room = createPitRoom();
        final Hero hero = new Hero("Test Hero", 1, 1, 1.0, 1, 1, 1.0);
        room.applyPit(hero);
        assertEquals(0, hero.getHitPoints());
        room.applyPit(hero);
        assertEquals(0, hero.getHitPoints());
    }

    /** Checks that a room without a pit leaves the hero's health unchanged. */
    @Test
    void testNoPitDoesNotDamageHero() {
        final Room room = new Room();
        room.setMyEntrance(true);
        room.setMyEntrance(false);
        final Hero hero = new Hero("Test Hero", 100, 1, 1.0, 1, 1, 1.0);
        hero.setHitPoints(50);
        assertFalse(room.getMyHasPit());
        room.applyPit(hero);
        assertEquals(50, hero.getHitPoints());
    }

    /** Checks that a missing hero is rejected. */
    @Test
    void testPitRejectsNullHero() {
        final Room room = new Room();
        assertThrows(IllegalArgumentException.class, () -> room.applyPit(null));
    }

    /**
     * Finds a naturally generated pit room without changing private fields.
     * The attempt limit prevents an infinite loop if pit generation is broken.
     *
     * @return a room containing a pit
     * @throws AssertionError if no pit is generated within the attempt limit
     */
    private Room createPitRoom() {
        final int maximumAttempts = 1000;
        for (int attempt = 0; attempt < maximumAttempts; attempt++) {
            final Room room = new Room();
            if (room.getMyHasPit()) {
                return room;
            }
        }
        throw new AssertionError("No pit generated in " + maximumAttempts + " rooms.");
    }
}
