/*
 * TCSS 360 - Dungeon Adventure
 * Kevin Munoz-Rivera
 */
package test;

import static org.junit.jupiter.api.Assertions.*;

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
 * @version 2026-11-14
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
}