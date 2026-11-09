/*
 * TCSS 360 - Dungeon Adventure
 * Kevin Munoz-Rivera
 */
package test;

import static org.junit.jupiter.api.Assertions.*;

import model.Dungeon;
import model.Room;
import org.junit.jupiter.api.Test;

/**
 * Basic tests for the dungeon's rooms and entrance.
 *
 * @author Kevin Munoz-Rivera
 * @version 2026-11-08
 */
public class DungeonTest {

    /** Checks that the default dungeon contains a room in every position. */
    @Test
    void testRoomsExist() {
        final Dungeon dungeon = new Dungeon();
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                assertNotNull(dungeon.getRoom(row, column));
            }
        }
    }

    /** Checks that the dungeon supplies a designated entrance room. */
    @Test
    void testEntrance() {
        final Dungeon dungeon = new Dungeon();
        final Room entrance = dungeon.getEntrance();
        assertNotNull(entrance);
        assertTrue(entrance.getIsEntrance());
        assertFalse(entrance.getIsExit());
    }

    /** Checks that an out-of-bounds room request is rejected. */
    @Test
    void testInvalidRoomPosition() {
        final Dungeon dungeon = new Dungeon();
        assertThrows(IllegalArgumentException.class, () -> dungeon.getRoom(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> dungeon.getRoom(0, 5));
    }
}
