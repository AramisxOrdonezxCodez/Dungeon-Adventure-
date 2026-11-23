package test;


import data.SaveService;
import model.Dungeon;
import model.GameState;
import model.Pillar;
import model.Warrior;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests saving and loading game progress
 */
public class SaveServiceTest {
    @TempDir
    Path myTempDirectory;

    private SaveService mySaveService;
    private GameState myGameState;

    /**
     * Creates a new game and save service before each test.
     */
    @BeforeEach
    void setUp() {
        mySaveService = new SaveService(myTempDirectory);

        final Warrior hero = new Warrior("Test Hero");
        final Dungeon dungeon = new Dungeon();

        myGameState = new GameState(hero, dungeon);
        myGameState.startGame(hero, dungeon);
    }

    /**
     * Checks that saving creates a save file.
     */
    @Test
    void testSaveGame() throws IOException {
        mySaveService.save(myGameState, 1);

        assertTrue(mySaveService.isOccupied(1));
    }

    /**
     * Checks that loading restores the game.
     */
    @Test
    void testLoadGame()
            throws IOException, ClassNotFoundException {

        myGameState.getHero().setHitPoints(80);
        myGameState.addPillar(Pillar.ABSTRACTION);

        mySaveService.save(myGameState, 1);

        final GameState loaded = mySaveService.load(1);

        assertNotNull(loaded);
        assertEquals("Test Hero", loaded.getHero().getName());
        assertEquals(80, loaded.getHero().getHitPoints());
        assertEquals(1, loaded.getCollectedPillars().size());
        assertTrue(loaded.getCollectedPillars()
                .contains(Pillar.ABSTRACTION));

        assertEquals(myGameState.getHeroRow(),
                loaded.getHeroRow());
        assertEquals(myGameState.getHeroColumn(),
                loaded.getHeroColumn());
    }

    /**
     * Checks that a new save slot starts empty.
     */
    @Test
    void testEmptySlot() {
        assertFalse(mySaveService.isOccupied(1));
    }

    /**
     * Checks whether saving marks a slot as occupied.
     */
    @Test
    void testOccupiedSlot() throws IOException {
        mySaveService.save(myGameState, 2);

        assertTrue(mySaveService.isOccupied(2));
        assertFalse(mySaveService.isOccupied(3));
    }

    /**
     * Checks that invalid save slots are rejected.
     */
    @Test
    void testInvalidSlots() {
        assertThrows(IllegalArgumentException.class,
                () -> mySaveService.save(myGameState, 0));

        assertThrows(IllegalArgumentException.class,
                () -> mySaveService.save(myGameState, 11));

        assertThrows(IllegalArgumentException.class,
                () -> mySaveService.load(-1));
    }

    /**
     * Checks that loading an empty slot fails.
     */
    @Test
    void testLoadEmptySlot() {
        assertThrows(IOException.class,
                () -> mySaveService.load(5));
    }

    /**
     * Checks that the game cannot be saved
     * when it is not in exploration mode.
     */
    @Test
    void testCannotSaveBeforeGameStarts() {
        final GameState newState = new GameState(
                new Warrior("New Hero"), new Dungeon());

        assertThrows(IllegalStateException.class,
                () -> mySaveService.save(newState, 1));
    }

    /**
     * Checks that null game states are rejected.
     */
    @Test
    void testSaveNullGameState() {
        assertThrows(IllegalArgumentException.class,
                () -> mySaveService.save(null, 1));
    }

    /**
     * Checks that the save service lists all 10 slots.
     */
    @Test
    void testListSlots() throws IOException {
        mySaveService.save(myGameState, 1);

        final List<String> slots = mySaveService.listSlots();

        assertEquals(10, slots.size());
        assertEquals("Slot 1: Saved Game", slots.get(0));
        assertEquals("Slot 2: Empty", slots.get(1));
    }

    /**
     * Checks that saving again overwrites the old game.
     */
    @Test
    void testOverwriteSave()
            throws IOException, ClassNotFoundException {

        myGameState.getHero().setHitPoints(80);
        mySaveService.save(myGameState, 1);

        myGameState.getHero().setHitPoints(50);
        mySaveService.save(myGameState, 1);

        final GameState loaded = mySaveService.load(1);

        assertEquals(50, loaded.getHero().getHitPoints());
    }
}
