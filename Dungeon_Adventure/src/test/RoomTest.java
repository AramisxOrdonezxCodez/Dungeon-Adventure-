/*
 * TCSS 360 - Dungeon Adventure
 * Kevin Munoz-Rivera
 */
package test;

import static org.junit.jupiter.api.Assertions.*;

import data.MonsterDefinition;
import model.Direction;
import model.Hero;
import model.Item;
import model.Monster;
import model.Pillar;
import model.Priestess;
import model.Room;
import model.Thief;
import model.Warrior;
import org.junit.jupiter.api.Test;

/**
 * Tests room contents, collection, doors, special rooms, and invalid interactions.
 *
 * @author Kevin Munoz-Rivera
 * @version 2026-11-22
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

    /** Checks that all three hero types take the full pit damage. */
    @Test
    void testPitDamageForHeroTypes() {
        final Room room = createPitRoom();
        final Hero[] heroes = {new Warrior("Warrior"), new Priestess("Priestess"),
            new Thief("Thief")};
        final int damage = room.getMyPitDamage();
        assertTrue(damage >= 1 && damage <= 20);
        for (final Hero hero : heroes) {
            final int healthBefore = hero.getHitPoints();
            room.applyPit(hero);
            assertEquals(healthBefore - damage, hero.getHitPoints());
        }
        assertTrue(room.getMyHasPit());
        assertEquals(damage, room.getMyPitDamage());
    }

    /** Checks that pit damage never calls the combat method that allows blocking. */
    @Test
    void testPitDamageCannotBeBlocked() {
        final Room room = createPitRoom();
        final Warrior hero = new Warrior("Test Warrior") {
            /**
             * Fails if pit damage is sent through the combat damage method.
             *
             * @param theAmount the attempted combat damage
             */
            @Override
            public void takeDamage(final int theAmount) {
                fail("Pit damage must bypass the combat blocking method.");
            }
        };
        final int healthBefore = hero.getHitPoints();
        room.applyPit(hero);
        assertEquals(healthBefore - room.getMyPitDamage(), hero.getHitPoints());
    }

    /** Checks that lethal pit damage stops at zero hit points for every hero type. */
    @Test
    void testPitDamageCannotMakeHealthNegative() {
        final Room room = createPitRoom();
        final Hero[] heroes = {new Warrior("Warrior"), new Priestess("Priestess"),
            new Thief("Thief")};
        for (final Hero hero : heroes) {
            hero.setHitPoints(1);
            room.applyPit(hero);
            assertEquals(0, hero.getHitPoints());
            room.applyPit(hero);
            assertEquals(0, hero.getHitPoints());
        }
    }

    /** Checks that a room without a pit leaves each hero's health unchanged. */
    @Test
    void testNoPitDoesNotDamageHero() {
        final Room room = new Room();
        room.setMyEntrance(true);
        room.setMyEntrance(false);
        final Hero[] heroes = {new Warrior("Warrior"), new Priestess("Priestess"),
            new Thief("Thief")};
        assertFalse(room.getMyHasPit());
        for (final Hero hero : heroes) {
            hero.setHitPoints(50);
            room.applyPit(hero);
            assertEquals(50, hero.getHitPoints());
        }
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

    /** Checks that each pillar is returned once and removed from the room drawing. */
    @Test
    void testCollectPillar() {
        for (final Pillar pillar : Pillar.values()) {
            final Room room = new Room();
            room.setMyEntrance(true);
            room.setMyEntrance(false);
            room.setPillar(pillar);
            assertEquals("***\n*" + pillar.getSymbol() + "*\n***", room.toString());
            assertSame(pillar, room.collectPillar());
            assertNull(room.getPillar());
            assertNull(room.collectPillar());
            assertEquals("***\n* *\n***", room.toString());
        }
    }

    /** Checks that collecting from a room with no pillar returns null. */
    @Test
    void testCollectMissingPillar() {
        final Room room = new Room();
        final String before = room.toString();
        assertNull(room.collectPillar());
        assertEquals(before, room.toString());
    }

    /** Checks that pillar collection preserves the monster, doors, and other contents. */
    @Test
    void testCollectPillarPreservesOtherContents() {
        final Room room = new Room();
        final Monster monster = createMonster();
        final boolean healingPotion = room.getHasHealingPotion();
        final boolean visionPotion = room.getMyHasVisionPotion();
        final boolean pit = room.getMyHasPit();
        final int damage = room.getMyPitDamage();
        room.setMonster(monster);
        room.addDoor(Direction.NORTH);
        room.reveal();
        room.setPillar(Pillar.ABSTRACTION);
        assertSame(Pillar.ABSTRACTION, room.collectPillar());
        assertSame(monster, room.getMonster());
        assertTrue(room.hasDoor(Direction.NORTH));
        assertTrue(room.isVisible());
        assertEquals(healingPotion, room.getHasHealingPotion());
        assertEquals(visionPotion, room.getMyHasVisionPotion());
        assertEquals(pit, room.getMyHasPit());
        assertEquals(damage, room.getMyPitDamage());
    }

    /** Checks every door orientation and that adding the same door twice changes nothing. */
    @Test
    void testDoorDirectionsAndDuplicates() {
        final Direction[] directions = {Direction.NORTH, Direction.SOUTH,
            Direction.EAST, Direction.WEST};
        final String[] drawings = {"*-*\n* *\n***", "***\n* *\n*-*",
            "***\n* |\n***", "***\n| *\n***"};
        for (int index = 0; index < directions.length; index++) {
            final Room room = createEmptyRoom();
            room.addDoor(directions[index]);
            room.addDoor(directions[index]);
            assertEquals(drawings[index], room.toString());
            for (final Direction direction : directions) {
                assertEquals(direction == directions[index], room.hasDoor(direction));
            }
        }
        final Room room = createEmptyRoom();
        for (final Direction direction : directions) {
            room.addDoor(direction);
        }
        assertEquals("*-*\n| |\n*-*", room.toString());
    }

    /** Checks that null directions are rejected without changing existing doors. */
    @Test
    void testNullDirectionsPreserveRoom() {
        final Room room = createEmptyRoom();
        room.addDoor(Direction.WEST);
        final String before = room.toString();
        assertThrows(IllegalArgumentException.class, () -> room.addDoor(null));
        assertThrows(IllegalArgumentException.class, () -> room.hasDoor(null));
        assertEquals(before, room.toString());
        assertTrue(room.hasDoor(Direction.WEST));
    }

    /** Checks the reverse entrance/exit restriction without losing the exit role. */
    @Test
    void testExitCannotBeEntrance() {
        final Room room = new Room();
        room.setExit(true);
        assertThrows(IllegalStateException.class, () -> room.setMyEntrance(true));
        assertTrue(room.getIsExit());
        assertFalse(room.getIsEntrance());
        assertEquals("***\n*O*\n***", room.toString());
    }

    /** Checks that special rooms clear contents but retain doors and visibility. */
    @Test
    void testSpecialRoomsPreserveDoorsAndVisibility() {
        for (int role = 0; role < 2; role++) {
            final Room room = createPitRoom();
            room.setMonster(createMonster());
            room.setPillar(Pillar.INHERITANCE);
            room.addDoor(Direction.SOUTH);
            room.reveal();
            if (role == 0) {
                room.setMyEntrance(true);
                room.setMyEntrance(false);
            } else {
                room.setExit(true);
                room.setExit(false);
            }
            assertNull(room.getMonster());
            assertNull(room.getPillar());
            assertFalse(room.getHasHealingPotion());
            assertFalse(room.getMyHasVisionPotion());
            assertFalse(room.getMyHasPit());
            assertEquals(0, room.getMyPitDamage());
            assertFalse(room.getIsEntrance());
            assertFalse(room.getIsExit());
            assertTrue(room.hasDoor(Direction.SOUTH));
            assertTrue(room.isVisible());
            assertEquals("***\n* *\n*-*", room.toString());
        }
    }

    /** Checks pillar rejection in both special roles and permits clearing null contents. */
    @Test
    void testSpecialRoomsRejectPillars() {
        final Room entrance = new Room();
        entrance.setMyEntrance(true);
        final Room exit = new Room();
        exit.setExit(true);
        final Room[] rooms = {entrance, exit};
        for (final Room room : rooms) {
            for (final Pillar pillar : Pillar.values()) {
                assertThrows(IllegalStateException.class, () -> room.setPillar(pillar));
                assertNull(room.getPillar());
            }
            assertDoesNotThrow(() -> room.setPillar(null));
            assertDoesNotThrow(() -> room.setMonster(null));
            assertNull(room.collectPillar());
        }
    }

    /** Checks that absent potions do not change existing inventory items or health. */
    @Test
    void testCollectAbsentPotions() {
        final Room room = createEmptyRoom();
        final Hero hero = new Warrior("Collector");
        final Item existing = new Item("Existing item", "Keep this item.");
        hero.getInventory().addItem(existing);
        hero.setHitPoints(50);
        assertFalse(room.collectHealingPotion(hero));
        assertFalse(room.collectVisionPotion(hero));
        assertEquals(1, hero.getInventory().getItemCount());
        assertSame(existing, hero.getInventory().getItems().get(0));
        assertEquals(50, hero.getHitPoints());
    }

    /** Checks a healing potion is transferred exactly once without applying its effect. */
    @Test
    void testHealingPotionCollectedOnlyOnce() {
        final Room room = createPotionRoom(true);
        final Hero hero = new Warrior("Collector");
        hero.setHitPoints(50);
        final boolean vision = room.getMyHasVisionPotion();
        final boolean pit = room.getMyHasPit();
        final int damage = room.getMyPitDamage();
        final Monster monster = createMonster();
        room.setMonster(monster);
        room.setPillar(Pillar.ABSTRACTION);
        assertTrue(room.collectHealingPotion(hero));
        assertFalse(room.collectHealingPotion(hero));
        assertFalse(room.getHasHealingPotion());
        assertEquals(1, hero.getInventory().getItemCount());
        assertEquals("Healing Potion", hero.getInventory().getItems().get(0).getName());
        assertEquals(50, hero.getHitPoints());
        assertEquals(vision, room.getMyHasVisionPotion());
        assertEquals(pit, room.getMyHasPit());
        assertEquals(damage, room.getMyPitDamage());
        assertSame(monster, room.getMonster());
        assertEquals(Pillar.ABSTRACTION, room.getPillar());
    }

    /** Checks a vision potion is transferred once and collection does not reveal the room. */
    @Test
    void testVisionPotionCollectedOnlyOnce() {
        final Room room = createPotionRoom(false);
        final Hero hero = new Warrior("Collector");
        final boolean healing = room.getHasHealingPotion();
        final boolean pit = room.getMyHasPit();
        final int damage = room.getMyPitDamage();
        assertFalse(room.isVisible());
        assertTrue(room.collectVisionPotion(hero));
        assertFalse(room.collectVisionPotion(hero));
        assertFalse(room.getMyHasVisionPotion());
        assertEquals(1, hero.getInventory().getItemCount());
        assertEquals("Vision Potion", hero.getInventory().getItems().get(0).getName());
        assertFalse(room.isVisible());
        assertEquals(healing, room.getHasHealingPotion());
        assertEquals(pit, room.getMyHasPit());
        assertEquals(damage, room.getMyPitDamage());
    }

    /** Checks null heroes are rejected before removing either available potion. */
    @Test
    void testNullCollectorsPreservePotions() {
        final Room healingRoom = createPotionRoom(true);
        final Room visionRoom = createPotionRoom(false);
        assertThrows(IllegalArgumentException.class,
            () -> healingRoom.collectHealingPotion(null));
        assertThrows(IllegalArgumentException.class,
            () -> visionRoom.collectVisionPotion(null));
        assertTrue(healingRoom.getHasHealingPotion());
        assertTrue(visionRoom.getMyHasVisionPotion());
        final Room emptyRoom = createEmptyRoom();
        assertThrows(IllegalArgumentException.class,
            () -> emptyRoom.collectHealingPotion(null));
        assertThrows(IllegalArgumentException.class,
            () -> emptyRoom.collectVisionPotion(null));
    }

    /** Checks repeated rendering and revealing preserve the existing room contents. */
    @Test
    void testRenderingAndRevealPreserveContents() {
        final Room room = createPitRoom();
        room.setPillar(Pillar.POLYMORPHISM);
        final Monster monster = createMonster();
        room.setMonster(monster);
        final int damage = room.getMyPitDamage();
        final boolean healing = room.getHasHealingPotion();
        final boolean vision = room.getMyHasVisionPotion();
        assertEquals("***\n*M*\n***", room.toString());
        assertFalse(room.isVisible());
        room.reveal();
        room.reveal();
        assertEquals("***\n*M*\n***", room.toString());
        assertTrue(room.isVisible());
        assertEquals(damage, room.getMyPitDamage());
        assertTrue(room.getMyHasPit());
        assertEquals(healing, room.getHasHealingPotion());
        assertEquals(vision, room.getMyHasVisionPotion());
        assertEquals(Pillar.POLYMORPHISM, room.getPillar());
        assertSame(monster, room.getMonster());
    }

    /**
     * Creates an ordinary empty room using the public entrance operation.
     *
     * @return an empty, unrevealed room without doors
     */
    private Room createEmptyRoom() {
        final Room room = new Room();
        room.setMyEntrance(true);
        room.setMyEntrance(false);
        return room;
    }

    /**
     * Finds a generated room with the requested potion, failing if none is generated.
     *
     * @param theHealing true to find healing; false to find vision
     * @return a room containing the requested potion
     * @throws AssertionError if no matching room appears within the attempt limit
     */
    private Room createPotionRoom(final boolean theHealing) {
        final int maximumAttempts = 1000;
        for (int attempt = 0; attempt < maximumAttempts; attempt++) {
            final Room room = new Room();
            if (theHealing && room.getHasHealingPotion()
                    || !theHealing && room.getMyHasVisionPotion()) {
                return room;
            }
        }
        throw new AssertionError("No requested potion generated in " + maximumAttempts
                + " rooms.");
    }
}
