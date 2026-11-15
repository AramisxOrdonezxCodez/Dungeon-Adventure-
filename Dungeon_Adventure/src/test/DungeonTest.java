/*
 * TCSS 360 - Dungeon Adventure
 * Kevin Munoz-Rivera
 */
package test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import model.Dungeon;
import model.Gremlin;
import model.Item;
import model.Monster;
import model.Ogre;
import model.Room;
import model.Skeleton;
import model.Warrior;
import org.junit.jupiter.api.Test;

/**
 * Basic tests for the dungeon's rooms and entrance.
 *
 * @author Kevin Munoz-Rivera
 * @version 2026-11-14
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

    /** Checks that all 23 ordinary rooms have their own allowed monster type. */
    @Test
    void testMonsterPlacement() {
        final Dungeon dungeon = new Dungeon();
        final Set<Monster> monsters = new HashSet<>();
        int specialRooms = 0;
        int pillarRooms = 0;
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                final Room room = dungeon.getRoom(row, column);
                final Monster monster = room.getMonster();
                if (room.getIsEntrance() || room.getIsExit()) {
                    assertNull(monster);
                    specialRooms++;
                } else {
                    assertNotNull(monster);
                    assertTrue(monster instanceof Ogre || monster instanceof Gremlin
                            || monster instanceof Skeleton);
                    assertTrue(monsters.add(monster), "Rooms must not share a monster");
                    assertEquals(monster.getMaxHitPoints(), monster.getHitPoints());
                }
                if (room.getPillar() != null) {
                    assertNotNull(monster);
                    pillarRooms++;
                }
            }
        }
        assertEquals(2, specialRooms);
        assertEquals(4, pillarRooms);
        assertEquals(23, monsters.size());
        assertTrue(dungeon.isTraversable());
    }

    /** Checks that the player map hides rooms while the required full map shows them. */
    @Test
    void testHiddenAndFullMaps() {
        final Dungeon dungeon = new Dungeon();
        final String[] hidden = dungeon.toVisibleString().split("\n", -1);
        final String[] full = dungeon.toString().split("\n", -1);
        assertEquals(15, hidden.length);
        assertEquals(15, full.length);
        for (final String line : hidden) {
            assertEquals("???????????????", line);
        }
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                final Room room = dungeon.getRoom(row, column);
                final String[] roomLines = room.toString().split("\n");
                for (int line = 0; line < 3; line++) {
                    assertEquals(roomLines[line],
                            full[row * 3 + line].substring(column * 3, column * 3 + 3));
                }
                assertFalse(room.isVisible());
            }
        }
    }

    /** Checks potion use updates only nearby rooms in center, corner, and edge maps. */
    @Test
    void testVisionPotionUpdatesVisibleMap() {
        final int[][] positions = {{2, 2}, {0, 0}, {0, 2}};
        for (final int[] position : positions) {
            final Dungeon dungeon = new Dungeon();
            final Warrior hero = new Warrior("Test Warrior");
            hero.getInventory().addItem(new Item("Vision Potion", "Reveals nearby rooms."));
            hero.getInventory().addItem(new Item("Vision Potion", "Reveals nearby rooms."));
            assertTrue(dungeon.useVisionPotion(hero, position[0], position[1]));
            assertEquals(1, hero.getInventory().getItemCount());
            assertVisibleArea(dungeon, position[0], position[1]);
        }
    }

    /** Checks that lacking a potion leaves the player's map unchanged. */
    @Test
    void testNoVisionPotionLeavesMapHidden() {
        final Dungeon dungeon = new Dungeon();
        final Warrior hero = new Warrior("Test Warrior");
        final String before = dungeon.toVisibleString();
        assertFalse(dungeon.useVisionPotion(hero, 2, 2));
        assertEquals(before, dungeon.toVisibleString());
    }

    /** Checks invalid requests do not consume a potion or reveal rooms. */
    @Test
    void testInvalidVisionRequestPreservesPotion() {
        final Dungeon dungeon = new Dungeon();
        final Warrior hero = new Warrior("Test Warrior");
        hero.getInventory().addItem(new Item("Vision Potion", "Reveals nearby rooms."));
        final String before = dungeon.toVisibleString();
        assertThrows(IllegalArgumentException.class,
                () -> dungeon.useVisionPotion(hero, -1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> dungeon.useVisionPotion(null, 2, 2));
        assertEquals(1, hero.getInventory().getItemCount());
        assertEquals(before, dungeon.toVisibleString());
    }

    /**
     * Checks actual displayed room blocks against the expected revealed area.
     *
     * @param theDungeon the dungeon being displayed
     * @param theRow the row where the potion was used
     * @param theColumn the column where the potion was used
     */
    private void assertVisibleArea(final Dungeon theDungeon, final int theRow,
                                   final int theColumn) {
        final String[] lines = theDungeon.toVisibleString().split("\n", -1);
        assertEquals(15, lines.length);
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                final Room room = theDungeon.getRoom(row, column);
                final boolean nearby = Math.abs(row - theRow) <= 1
                        && Math.abs(column - theColumn) <= 1;
                assertEquals(nearby, room.isVisible());
                String expected = "???\n???\n???";
                if (nearby) {
                    expected = room.toString();
                }
                final String[] roomLines = expected.split("\n");
                for (int line = 0; line < 3; line++) {
                    assertEquals(15, lines[row * 3 + line].length());
                    assertEquals(roomLines[line],
                            lines[row * 3 + line].substring(column * 3, column * 3 + 3));
                }
            }
        }
    }
}
