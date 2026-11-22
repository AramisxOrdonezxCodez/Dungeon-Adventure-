/*
 * TCSS 360 - Dungeon Adventure
 * Kevin Munoz-Rivera
 */
package test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EnumSet;
import model.Direction;
import model.Dungeon;
import model.GamePhase;
import model.GameState;
import model.Pillar;
import model.Room;
import model.Warrior;
import org.junit.jupiter.api.Test;

/**
 * Checks game progress, entrance placement, and movement rules.
 *
 * @author Kevin Munoz-Rivera
 * @version 2026-11-22
 */
public class GameStateTest {

    /** Checks the initial entrance and resets progress when starting another game. */
    @Test
    void testStartAndRestart() {
        final Dungeon first = new Dungeon();
        final GameState state = new GameState(new Warrior("First"), first);
        assertEquals(GamePhase.MAIN_MENU, state.getPhase());
        assertSame(first.getEntrance(), state.getCurrentRoom());
        assertTrue(state.getCurrentRoom().isVisible());
        assertFalse(state.canSave());
        state.addPillar(Pillar.ABSTRACTION);
        state.setHeroPosition(0, 0);
        final Dungeon second = new Dungeon();
        final Warrior hero = new Warrior("Second");
        state.startGame(hero, second);
        assertSame(hero, state.getHero());
        assertSame(second, state.getDungeon());
        assertSame(second.getEntrance(), state.getCurrentRoom());
        assertTrue(state.getCurrentRoom().isVisible());
        assertEquals(GamePhase.EXPLORATION, state.getPhase());
        assertTrue(state.getCollectedPillars().isEmpty());
    }

    /** Checks all four distinct pillars and protects the stored collection. */
    @Test
    void testCollectedPillars() {
        final GameState state = new GameState(new Warrior("Hero"), new Dungeon());
        assertFalse(state.hasAllPillars());
        state.getCollectedPillars().add(Pillar.ABSTRACTION);
        assertTrue(state.getCollectedPillars().isEmpty());
        state.addPillar(Pillar.ABSTRACTION);
        state.addPillar(Pillar.ABSTRACTION);
        assertEquals(1, state.getCollectedPillars().size());
        assertFalse(state.hasAllPillars());
        state.addPillar(Pillar.ENCAPSULATION);
        state.addPillar(Pillar.INHERITANCE);
        assertFalse(state.hasAllPillars());
        state.addPillar(Pillar.POLYMORPHISM);
        assertTrue(state.hasAllPillars());
        state.getCollectedPillars().clear();
        assertTrue(state.hasAllPillars());
    }

    /** Checks every generated room and direction against actual door connections. */
    @Test
    void testMovementThroughDoors() {
        final Dungeon dungeon = new Dungeon();
        final Warrior hero = new Warrior("Hero");
        final GameState state = new GameState(hero, dungeon);
        state.startGame(hero, dungeon);
        final Direction[] directions = {Direction.NORTH, Direction.SOUTH,
            Direction.EAST, Direction.WEST};
        final Direction[] opposite = {Direction.SOUTH, Direction.NORTH,
            Direction.WEST, Direction.EAST};
        final int[] rowChanges = {-1, 1, 0, 0};
        final int[] columnChanges = {0, 0, 1, -1};
        for (int row = 0; row < dungeon.getRowCount(); row++) {
            for (int column = 0; column < dungeon.getColumnCount(); column++) {
                for (int index = 0; index < directions.length; index++) {
                    state.setHeroPosition(row, column);
                    final int nextRow = row + rowChanges[index];
                    final int nextColumn = column + columnChanges[index];
                    boolean expected = false;
                    if (nextRow >= 0 && nextRow < dungeon.getRowCount()
                            && nextColumn >= 0 && nextColumn < dungeon.getColumnCount()) {
                        expected = dungeon.getRoom(row, column).hasDoor(directions[index])
                            && dungeon.getRoom(nextRow, nextColumn).hasDoor(opposite[index]);
                    }
                    assertEquals(expected, state.moveHero(directions[index]));
                    if (expected) {
                        assertEquals(nextRow, state.getHeroRow());
                        assertEquals(nextColumn, state.getHeroColumn());
                        assertTrue(state.getCurrentRoom().isVisible());
                    } else {
                        assertEquals(row, state.getHeroRow());
                        assertEquals(column, state.getHeroColumn());
                    }
                }
            }
        }
    }

    /** Checks movement restrictions and saving for each phase and a defeated hero. */
    @Test
    void testPhasesAndDefeat() {
        final Dungeon dungeon = new Dungeon();
        final Warrior hero = new Warrior("Hero");
        final GameState state = new GameState(hero, dungeon);
        final Room entrance = state.getCurrentRoom();
        for (final GamePhase phase : GamePhase.values()) {
            state.setPhase(phase);
            assertEquals(phase == GamePhase.EXPLORATION, state.canSave());
            if (phase != GamePhase.EXPLORATION) {
                for (final Direction direction : Direction.values()) {
                    assertFalse(state.moveHero(direction));
                    assertSame(entrance, state.getCurrentRoom());
                }
            }
        }
        state.setPhase(GamePhase.EXPLORATION);
        hero.setHitPoints(0);
        assertFalse(state.canSave());
        for (final Direction direction : Direction.values()) {
            assertFalse(state.moveHero(direction));
            assertSame(entrance, state.getCurrentRoom());
        }
    }

    /** Checks invalid inputs and prevents an outward door from leaving the grid. */
    @Test
    void testInvalidRequests() {
        final Dungeon dungeon = new Dungeon();
        final Warrior hero = new Warrior("Hero");
        final GameState state = new GameState(hero, dungeon);
        assertThrows(IllegalArgumentException.class, () -> new GameState(null, dungeon));
        assertThrows(IllegalArgumentException.class, () -> new GameState(hero, null));
        assertThrows(IllegalArgumentException.class, () -> state.startGame(null, dungeon));
        assertThrows(IllegalArgumentException.class, () -> state.startGame(hero, null));
        assertEquals(GamePhase.MAIN_MENU, state.getPhase());
        assertThrows(IllegalArgumentException.class, () -> state.moveHero(null));
        assertThrows(IllegalArgumentException.class, () -> state.addPillar(null));
        assertThrows(IllegalArgumentException.class, () -> state.setPhase(null));
        state.startGame(hero, dungeon);
        state.setHeroPosition(0, 0);
        assertThrows(IllegalArgumentException.class, () -> state.setHeroPosition(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> state.setHeroPosition(0, -1));
        assertThrows(IllegalArgumentException.class,
            () -> state.setHeroPosition(dungeon.getRowCount(), 0));
        assertThrows(IllegalArgumentException.class,
            () -> state.setHeroPosition(0, dungeon.getColumnCount()));
        dungeon.getRoom(0, 0).addDoor(Direction.NORTH);
        assertFalse(state.moveHero(Direction.NORTH));
        assertEquals(0, state.getHeroRow());
        assertEquals(0, state.getHeroColumn());
    }

    /** Checks that rejected restarts preserve existing progress and object references. */
    @Test
    void testInvalidRestartPreservesProgress() {
        final Dungeon dungeon = new Dungeon();
        final Warrior hero = new Warrior("Original");
        final GameState state = new GameState(hero, dungeon);
        state.startGame(hero, dungeon);
        state.setHeroPosition(3, 2);
        state.setPhase(GamePhase.COMBAT);
        state.addPillar(Pillar.INHERITANCE);
        final String map = dungeon.toVisibleString();
        assertThrows(IllegalArgumentException.class,
                () -> state.startGame(null, new Dungeon()));
        assertThrows(IllegalArgumentException.class,
                () -> state.startGame(new Warrior("Replacement"), null));
        assertSame(hero, state.getHero());
        assertSame(dungeon, state.getDungeon());
        assertEquals(3, state.getHeroRow());
        assertEquals(2, state.getHeroColumn());
        assertEquals(GamePhase.COMBAT, state.getPhase());
        assertEquals(EnumSet.of(Pillar.INHERITANCE), state.getCollectedPillars());
        assertEquals(map, dungeon.toVisibleString());
    }

    /** Checks that invalid coordinates never partially update a valid position. */
    @Test
    void testInvalidCoordinatesPreserveBothCoordinates() {
        final Dungeon dungeon = new Dungeon();
        final GameState state = new GameState(new Warrior("Hero"), dungeon);
        state.setHeroPosition(3, 2);
        final String before = dungeon.toVisibleString();
        final int[][] invalid = {{-1, 1}, {1, -1}, {5, 1}, {1, 5},
            {Integer.MIN_VALUE, 1}, {1, Integer.MAX_VALUE}};
        for (final int[] position : invalid) {
            assertThrows(IllegalArgumentException.class,
                    () -> state.setHeroPosition(position[0], position[1]));
            assertEquals(3, state.getHeroRow());
            assertEquals(2, state.getHeroColumn());
            assertSame(dungeon.getRoom(3, 2), state.getCurrentRoom());
            assertEquals(before, dungeon.toVisibleString());
        }
    }

    /** Checks that rejected null actions retain the current phase, pillars, and map. */
    @Test
    void testNullActionsPreserveProgress() {
        final Dungeon dungeon = new Dungeon();
        final GameState state = new GameState(new Warrior("Hero"), dungeon);
        state.setPhase(GamePhase.COMBAT);
        state.addPillar(Pillar.ENCAPSULATION);
        final Room current = state.getCurrentRoom();
        final String before = dungeon.toVisibleString();
        assertThrows(IllegalArgumentException.class, () -> state.setPhase(null));
        assertThrows(IllegalArgumentException.class, () -> state.addPillar(null));
        assertThrows(IllegalArgumentException.class, () -> state.moveHero(null));
        assertEquals(GamePhase.COMBAT, state.getPhase());
        assertEquals(EnumSet.of(Pillar.ENCAPSULATION), state.getCollectedPillars());
        assertSame(current, state.getCurrentRoom());
        assertEquals(before, dungeon.toVisibleString());
    }

    /** Checks that a door without its matching return door cannot be used. */
    @Test
    void testOneWayDoorDoesNotAllowMovement() {
        final Dungeon dungeon = new Dungeon();
        final Warrior hero = new Warrior("Hero");
        final GameState state = new GameState(hero, dungeon);
        state.startGame(hero, dungeon);
        final Direction[] directions = {Direction.SOUTH, Direction.EAST};
        final Direction[] opposite = {Direction.NORTH, Direction.WEST};
        final int[] rowChanges = {1, 0};
        final int[] columnChanges = {0, 1};
        for (int row = 0; row < dungeon.getRowCount() - 1; row++) {
            for (int column = 0; column < dungeon.getColumnCount() - 1; column++) {
                for (int index = 0; index < directions.length; index++) {
                    final Room current = dungeon.getRoom(row, column);
                    final Room next = dungeon.getRoom(row + rowChanges[index],
                            column + columnChanges[index]);
                    if (!current.hasDoor(directions[index])
                            && !next.hasDoor(opposite[index])) {
                        current.addDoor(directions[index]);
                        state.setHeroPosition(row, column);
                        final String before = dungeon.toVisibleString();
                        assertFalse(state.moveHero(directions[index]));
                        assertSame(current, state.getCurrentRoom());
                        assertEquals(row, state.getHeroRow());
                        assertEquals(column, state.getHeroColumn());
                        assertEquals(before, dungeon.toVisibleString());
                        return;
                    }
                }
            }
        }
        fail("A tree maze must have a closed internal connection to test");
    }

    /** Checks movement reveals its destination and leaves room events to gameplay. */
    @Test
    void testMovementDoesNotProcessRoomEvents() {
        final Dungeon dungeon = new Dungeon();
        final Warrior hero = new Warrior("Hero");
        final GameState state = new GameState(hero, dungeon);
        state.startGame(hero, dungeon);
        final Room entrance = state.getCurrentRoom();
        final String fullMap = dungeon.toString();
        final int health = hero.getHitPoints();
        for (final Direction direction : Direction.values()) {
            if (entrance.hasDoor(direction)) {
                assertTrue(state.moveHero(direction));
                final Room destination = state.getCurrentRoom();
                assertNotSame(entrance, destination);
                assertEquals(health, hero.getHitPoints());
                assertTrue(hero.getInventory().isEmpty());
                assertTrue(state.getCollectedPillars().isEmpty());
                assertEquals(GamePhase.EXPLORATION, state.getPhase());
                assertEquals(fullMap, dungeon.toString());
                for (int row = 0; row < dungeon.getRowCount(); row++) {
                    for (int column = 0; column < dungeon.getColumnCount(); column++) {
                        final Room room = dungeon.getRoom(row, column);
                        assertEquals(room == entrance || room == destination,
                                room.isVisible());
                    }
                }
                return;
            }
        }
        fail("The entrance must have a passage");
    }

    /** Checks returned pillar sets remain independent snapshots when progress changes. */
    @Test
    void testPillarSnapshotsAndRestart() {
        final Dungeon dungeon = new Dungeon();
        final Warrior hero = new Warrior("Hero");
        final GameState state = new GameState(hero, dungeon);
        state.addPillar(Pillar.ABSTRACTION);
        final EnumSet<Pillar> snapshot = state.getCollectedPillars();
        state.addPillar(Pillar.INHERITANCE);
        assertEquals(EnumSet.of(Pillar.ABSTRACTION), snapshot);
        for (final Pillar pillar : Pillar.values()) {
            state.addPillar(pillar);
        }
        assertTrue(state.hasAllPillars());
        state.startGame(new Warrior("New Hero"), new Dungeon());
        assertFalse(state.hasAllPillars());
        assertTrue(state.getCollectedPillars().isEmpty());
        assertEquals(EnumSet.of(Pillar.ABSTRACTION), snapshot);
    }

    /**
     * Checks serialization preserves progress and restores independent objects.
     * Uses in-memory bytes and does not create or overwrite saved-game files.
     *
     * @throws IOException if serialization fails
     * @throws ClassNotFoundException if the saved classes cannot be restored
     */
    @Test
    void testSerializationPreservesProgress() throws IOException, ClassNotFoundException {
        final Dungeon dungeon = new Dungeon();
        final Warrior hero = new Warrior("Saved Hero");
        final GameState state = new GameState(hero, dungeon);
        state.startGame(hero, dungeon);
        state.setHeroPosition(3, 2);
        state.getCurrentRoom().reveal();
        state.addPillar(Pillar.POLYMORPHISM);
        hero.setHitPoints(40);
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(state);
        }
        final GameState restored;
        try (ObjectInputStream input = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (GameState) input.readObject();
        }
        assertNotSame(state, restored);
        assertNotSame(hero, restored.getHero());
        assertNotSame(dungeon, restored.getDungeon());
        assertEquals(40, restored.getHero().getHitPoints());
        assertEquals(3, restored.getHeroRow());
        assertEquals(2, restored.getHeroColumn());
        assertSame(restored.getDungeon().getRoom(3, 2), restored.getCurrentRoom());
        assertEquals(state.getPhase(), restored.getPhase());
        assertTrue(restored.canSave());
        assertEquals(state.getCollectedPillars(), restored.getCollectedPillars());
        assertEquals(dungeon.toString(), restored.getDungeon().toString());
        assertEquals(dungeon.toVisibleString(), restored.getDungeon().toVisibleString());
        restored.addPillar(Pillar.ABSTRACTION);
        restored.getHero().setHitPoints(1);
        assertEquals(EnumSet.of(Pillar.POLYMORPHISM), state.getCollectedPillars());
        assertEquals(40, hero.getHitPoints());
    }
}
