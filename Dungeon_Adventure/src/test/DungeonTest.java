/*
 * TCSS 360 - Dungeon Adventure
 * Kevin Munoz-Rivera
 */
package test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import model.Direction;
import model.Dungeon;
import model.Gremlin;
import model.Item;
import model.Monster;
import model.Ogre;
import model.Pillar;
import model.Room;
import model.Skeleton;
import model.Warrior;
import org.junit.jupiter.api.Test;

/**
 * Tests dungeon generation, room integration, visibility, and invalid requests.
 *
 * @author Kevin Munoz-Rivera
 * @version 2026-11-22
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

    /** Checks unique rooms and the exact special-room rules across generated dungeons. */
    @Test
    void testSpecialRoomPlacementAndDistinctRooms() {
        final int generations = 10;
        for (int generation = 0; generation < generations; generation++) {
            final Dungeon dungeon = new Dungeon();
            assertEquals(5, dungeon.getRowCount());
            assertEquals(5, dungeon.getColumnCount());
            final Set<Room> rooms = new HashSet<>();
            final EnumSet<Pillar> pillars = EnumSet.noneOf(Pillar.class);
            int entrances = 0;
            int exits = 0;
            for (int row = 0; row < dungeon.getRowCount(); row++) {
                for (int column = 0; column < dungeon.getColumnCount(); column++) {
                    final Room room = dungeon.getRoom(row, column);
                    assertTrue(rooms.add(room), "Every position needs a separate Room");
                    assertFalse(room.isVisible());
                    if (room.getIsEntrance()) {
                        entrances++;
                        assertSame(dungeon.getEntrance(), room);
                        assertFalse(room.getIsExit());
                    }
                    if (room.getIsExit()) {
                        exits++;
                    }
                    if (room.getIsEntrance() || room.getIsExit()) {
                        assertNull(room.getPillar());
                        assertNull(room.getMonster());
                        assertFalse(room.getHasHealingPotion());
                        assertFalse(room.getMyHasVisionPotion());
                        assertFalse(room.getMyHasPit());
                        assertEquals(0, room.getMyPitDamage());
                    } else if (room.getPillar() != null) {
                        assertTrue(pillars.add(room.getPillar()), "Pillars must be unique");
                    }
                }
            }
            assertEquals(1, entrances);
            assertEquals(1, exits);
            assertEquals(EnumSet.allOf(Pillar.class), pillars);
            assertEquals(25, rooms.size());
        }
    }

    /** Checks connectivity independently of isTraversable and requires maze dead ends. */
    @Test
    void testMazeConnectionsAndDeadEnds() {
        final int generations = 10;
        for (int generation = 0; generation < generations; generation++) {
            assertMazeConnections(new Dungeon());
        }
    }

    /**
     * Uses an independent breadth-first traversal and checks every door encountered.
     *
     * @param theDungeon the generated dungeon to inspect
     */
    private void assertMazeConnections(final Dungeon theDungeon) {
        final Direction[] directions = {Direction.NORTH, Direction.SOUTH,
            Direction.EAST, Direction.WEST};
        final Direction[] opposite = {Direction.SOUTH, Direction.NORTH,
            Direction.WEST, Direction.EAST};
        final int[] rowChanges = {-1, 1, 0, 0};
        final int[] columnChanges = {0, 0, 1, -1};
        final Set<Room> reached = new HashSet<>();
        final ArrayDeque<int[]> pending = new ArrayDeque<>();
        for (int row = 0; row < theDungeon.getRowCount(); row++) {
            for (int column = 0; column < theDungeon.getColumnCount(); column++) {
                if (theDungeon.getRoom(row, column) == theDungeon.getEntrance()) {
                    reached.add(theDungeon.getEntrance());
                    pending.add(new int[] {row, column});
                }
            }
        }
        int doorCount = 0;
        int deadEnds = 0;
        while (!pending.isEmpty()) {
            final int[] position = pending.remove();
            final Room room = theDungeon.getRoom(position[0], position[1]);
            int roomDoors = 0;
            for (int index = 0; index < directions.length; index++) {
                if (room.hasDoor(directions[index])) {
                    roomDoors++;
                    final int row = position[0] + rowChanges[index];
                    final int column = position[1] + columnChanges[index];
                    assertTrue(row >= 0 && row < theDungeon.getRowCount(),
                            "A door must not leave the dungeon rows");
                    assertTrue(column >= 0 && column < theDungeon.getColumnCount(),
                            "A door must not leave the dungeon columns");
                    final Room neighbor = theDungeon.getRoom(row, column);
                    assertTrue(neighbor.hasDoor(opposite[index]),
                            "A passage must have matching doors");
                    if (reached.add(neighbor)) {
                        pending.add(new int[] {row, column});
                    }
                }
            }
            doorCount += roomDoors;
            if (roomDoors == 1) {
                deadEnds++;
            }
        }
        final int roomCount = theDungeon.getRowCount() * theDungeon.getColumnCount();
        assertEquals(roomCount, reached.size(), "Every room must be reachable");
        assertEquals(2 * (roomCount - 1), doorCount, "DFS creates a tree of passages");
        assertTrue(deadEnds >= 2, "The generated maze must contain dead ends");
        assertTrue(theDungeon.isTraversable());
    }

    /** Checks every invalid boundary before any potion consumption or map changes. */
    @Test
    void testAllCoordinateBoundaries() {
        final Dungeon dungeon = new Dungeon();
        final Warrior hero = new Warrior("Boundary Hero");
        final Item potion = new Item("Vision Potion", "Keep until a valid request.");
        hero.getInventory().addItem(potion);
        final String before = dungeon.toVisibleString();
        final int[][] invalid = {{-1, 0}, {0, -1}, {5, 0}, {0, 5},
            {Integer.MIN_VALUE, 0}, {0, Integer.MAX_VALUE}};
        for (final int[] position : invalid) {
            assertThrows(IllegalArgumentException.class,
                    () -> dungeon.getRoom(position[0], position[1]));
            assertThrows(IllegalArgumentException.class,
                    () -> dungeon.revealSurroundingRooms(position[0], position[1]));
            assertThrows(IllegalArgumentException.class,
                    () -> dungeon.useVisionPotion(hero, position[0], position[1]));
            assertEquals(before, dungeon.toVisibleString());
            assertEquals(1, hero.getInventory().getItemCount());
            assertSame(potion, hero.getInventory().getItems().get(0));
        }
    }

    /** Checks exact vision coverage at every position, including all edges and corners. */
    @Test
    void testVisionAtEveryPosition() {
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                final Dungeon dungeon = new Dungeon();
                dungeon.revealSurroundingRooms(row, column);
                assertVisibleArea(dungeon, row, column);
            }
        }
    }

    /** Checks overlapping reveals preserve discoveries and leave distant rooms hidden. */
    @Test
    void testRepeatedAndOverlappingReveals() {
        final Dungeon dungeon = new Dungeon();
        dungeon.revealSurroundingRooms(0, 0);
        final String first = dungeon.toVisibleString();
        dungeon.revealSurroundingRooms(0, 0);
        assertEquals(first, dungeon.toVisibleString());
        dungeon.revealSurroundingRooms(2, 2);
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                final boolean expected = row <= 1 && column <= 1
                        || row >= 1 && row <= 3 && column >= 1 && column <= 3;
                assertEquals(expected, dungeon.getRoom(row, column).isVisible());
            }
        }
    }

    /** Checks that exhausted vision potions preserve other inventory items and visibility. */
    @Test
    void testVisionPotionExhaustionPreservesOtherItems() {
        final Dungeon dungeon = new Dungeon();
        final Warrior hero = new Warrior("Potion Hero");
        final Item healing = new Item("Healing Potion", "Keep this potion.");
        hero.getInventory().addItem(healing);
        hero.getInventory().addItem(new Item("Vision Potion", "Reveal nearby rooms."));
        assertTrue(dungeon.useVisionPotion(hero, 0, 0));
        final String before = dungeon.toVisibleString();
        assertFalse(dungeon.useVisionPotion(hero, 4, 4));
        assertEquals(before, dungeon.toVisibleString());
        assertEquals(1, hero.getInventory().getItemCount());
        assertSame(healing, hero.getInventory().getItems().get(0));
    }

    /** Checks traversal and map operations never regenerate rooms or their contents. */
    @Test
    void testTraversalAndVisibilityPreserveContents() {
        final Dungeon dungeon = new Dungeon();
        final String full = dungeon.toString();
        final Room[][] rooms = new Room[5][5];
        final Monster[][] monsters = new Monster[5][5];
        final int[][] damage = new int[5][5];
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                rooms[row][column] = dungeon.getRoom(row, column);
                monsters[row][column] = rooms[row][column].getMonster();
                damage[row][column] = rooms[row][column].getMyPitDamage();
            }
        }
        final String hidden = dungeon.toVisibleString();
        assertTrue(dungeon.isTraversable());
        assertTrue(dungeon.isTraversable());
        assertEquals(hidden, dungeon.toVisibleString());
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                dungeon.revealSurroundingRooms(row, column);
                assertSame(rooms[row][column], dungeon.getRoom(row, column));
                assertSame(monsters[row][column], rooms[row][column].getMonster());
                assertEquals(damage[row][column], rooms[row][column].getMyPitDamage());
            }
        }
        assertEquals(full, dungeon.toString());
        assertEquals(full, dungeon.toVisibleString());
    }
}
