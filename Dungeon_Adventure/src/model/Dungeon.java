/*
 * TCSS 360 - Dungeon Adventure
 * Kevin Munoz-Rivera
 */
package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Stores a grid of rooms connected by randomly generated maze passages.
 * Places special rooms and pillars, validates connectivity, and displays the full map.
 *
 * @author Kevin Munoz-Rivera
 * @version 2026-11-01
 */
public class Dungeon implements Serializable {

    /** Version identifier for serialized dungeons. */
    private static final long serialVersionUID = 1L;

    /** Number of rows in the default dungeon. */
    private static final int HEIGHT = 5;

    /** Number of columns in the default dungeon. */
    private static final int WIDTH = 5;

    /** Number of text lines used to draw one room. */
    private static final int ROOM_DRAWING_HEIGHT = 3;

    /** Random number generator used for special-room placement. */
    private static final Random RANDOM = new Random();

    /** Row of the entrance. */
    private int myEntranceRow;

    /** Column of the entrance. */
    private int myEntranceColumn;

    /** Rooms indexed by row and then column. */
    private final Room[][] myRooms;

    // TODO: Add surrounding-room lookup for vision integration.

    /**
     * Creates a five-by-five grid and connects its rooms using randomized DFS.
     * Assigns special rooms before carving passages between them.
     * Retries generation until every room is reachable.
     */
    public Dungeon() {
        myRooms = new Room[HEIGHT][WIDTH];
        do {
            createRooms();
            placeSpecialRooms();
            final MazeGenerator generator = new MazeGenerator(myRooms);
            generator.generate();
        } while (!isTraversable());
    }

    /**
     * Creates a distinct room in every cell before generating passages.
     */
    private void createRooms() {
        for (int row = 0; row < myRooms.length; row++) {
            for (int column = 0; column < myRooms[row].length; column++) {
                myRooms[row][column] = new Room();
            }
        }
    }

    /**
     * Returns the room at the specified position.
     *
     * @param theRow the zero-based row
     * @param theColumn the zero-based column
     * @return the room at that position
     * @throws IllegalArgumentException if either coordinate is outside the grid
     */
    public Room getRoom(final int theRow, final int theColumn) {
        if (theRow < 0 || theRow >= myRooms.length
                || theColumn < 0 || theColumn >= myRooms[0].length) {
            throw new IllegalArgumentException("Room coordinates are outside the dungeon.");
        }
        return myRooms[theRow][theColumn];
    }


    /**
     * Reveals the current room and the eight
     * surrounding rooms when using a vision potion.
     *
     * @param theRow the hero's current row
     * @param theColumn the hero's current column
     */
    public void revealSurroundingRooms(final int theRow,
                                       final int theColumn) {

        // Make sure the starting position is valid.
        getRoom(theRow, theColumn);

        for (int row = theRow - 1; row <= theRow + 1; row++) {
            for (int column = theColumn - 1;
                 column <= theColumn + 1; column++) {

                // Check that the room is inside the dungeon.
                if (row >= 0 && row < myRooms.length
                        && column >= 0
                        && column < myRooms[row].length) {

                    myRooms[row][column].reveal();
                }
            }
        }
    }

    /**
     * Uses a vision potion to reveal nearby rooms.
     *
     * @param theHero the hero using the potion
     * @param theRow the hero's current row
     * @param theColumn the hero's current column
     * @return true if a potion was used
     */
    public boolean useVisionPotion(final Hero theHero,
                                   final int theRow,
                                   final int theColumn) {

        if (theHero == null) {
            throw new IllegalArgumentException("Hero cannot be null.");
        }

        // Validate coordinates before consuming the potion.
        getRoom(theRow, theColumn);

        if (theHero.getInventory().useVisionPotion()) {
            revealSurroundingRooms(theRow, theColumn);
            return true;
        }

        return false;
    }


    /**
     * Places the entrance, exit, and each pillar in distinct randomly chosen rooms.
     * Called before passage generation; selecting and removing positions prevents overlap.
     */
    private void placeSpecialRooms() {
        final int width = myRooms[0].length;
        final List<Integer> positions = new ArrayList<>();
        for (int position = 0; position < myRooms.length * width; position++) {
            positions.add(position);
        }
        final int entrance = positions.remove(RANDOM.nextInt(positions.size()));
        myEntranceRow = entrance / width;
        myEntranceColumn = entrance % width;
        getEntrance().setMyEntrance(true);

        final int exit = positions.remove(RANDOM.nextInt(positions.size()));
        myRooms[exit / width][exit % width].setExit(true);
        for (final Pillar pillar : Pillar.values()) {
            final int position = positions.remove(RANDOM.nextInt(positions.size()));
            myRooms[position / width][position % width].setPillar(pillar);
        }
    }

    /**
     * Returns the dungeon entrance room.
     *
     * @return the entrance room
     */
    public Room getEntrance() {
        return myRooms[myEntranceRow][myEntranceColumn];
    }

    /**
     * Checks actual door connections after generation using a fresh traversal.
     * Requiring every room to be reachable also checks the exit and all pillars.
     * Does not change player visibility or room contents.
     *
     * @return true if all rooms are reachable from the entrance; false otherwise
     */
    public boolean isTraversable() {
        final MazeGenerator traversal = new MazeGenerator(myRooms);
        traversal.visitPassages(myEntranceRow, myEntranceColumn);
        return traversal.allRoomsVisited();
    }

    /**
     * Draws the entire dungeon by joining the three-line drawings of its rooms.
     * Includes unrevealed rooms for the required full-map display.
     *
     * @return the complete dungeon drawing without a trailing newline
     */
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        for (int row = 0; row < myRooms.length; row++) {
            appendRoomRow(result, row);
        }
        return result.toString();
    }

    /**
     * Appends one grid row as three text lines, preserving each room's drawing.
     *
     * @param theResult the destination for the drawing
     * @param theRow the grid row to append
     */
    private void appendRoomRow(final StringBuilder theResult, final int theRow) {
        final List<String[]> roomTextRows = new ArrayList<>();
        for (int column = 0; column < myRooms[theRow].length; column++) {
            roomTextRows.add(myRooms[theRow][column].toString().split("\n"));
        }
        for (int line = 0; line < ROOM_DRAWING_HEIGHT; line++) {
            if (theResult.length() > 0) {
                theResult.append('\n');
            }
            for (final String[] roomLines : roomTextRows) {
                theResult.append(roomLines[line]);
            }
        }
    }

    /**
     * Connects a fresh grid of rooms using randomized depth-first search.
     * Also supports a separate traversal of existing doors to validate connectivity.
     *
     * @author Kevin Munoz-Rivera
     * @version 2026-11-01
     */
    private static final class MazeGenerator {

        /** Random number generator used to choose neighboring directions. */
        private static final Random RANDOM = new Random();

        /** Fresh rectangular room grid supplied by Dungeon. */
        private final Room[][] myRooms;

        /** Tracks generation visits separately from player visibility. */
        private final boolean[][] myVisited;

        /**
         * Prepares a fresh visited grid for generation or validation.
         * The grid must be nonempty and rectangular with distinct initialized rooms.
         * Generation requires doorless rooms; validation follows existing doors.
         *
         * @param theRooms the fresh grid to connect
         */
        private MazeGenerator(final Room[][] theRooms) {
            myRooms = theRooms;
            myVisited = new boolean[theRooms.length][theRooms[0].length];
        }

        /**
         * Generates passages starting at the top-left room.
         * Called once for this fresh grid; the starting cell need not be the game entrance.
         */
        private void generate() {
            buildMaze(0, 0);
        }

        /**
         * Visits a room and recursively explores its neighbors in random order.
         * Returning from a recursive call backtracks to explore remaining neighbors.
         *
         * @param theRow the current room's row
         * @param theColumn the current room's column
         */
        private void buildMaze(final int theRow, final int theColumn) {
            myVisited[theRow][theColumn] = true;
            final List<Direction> neighbors = getNeighbors(theRow, theColumn);

            while (!neighbors.isEmpty()) {
                final int index = RANDOM.nextInt(neighbors.size());
                final Direction direction = neighbors.remove(index);
                final int nextRow = getNextRow(theRow, direction);
                final int nextColumn = getNextColumn(theColumn, direction);

                if (!myVisited[nextRow][nextColumn]) {
                    myRooms[theRow][theColumn].addDoor(direction);
                    myRooms[nextRow][nextColumn].addDoor(getOpposite(direction));
                    buildMaze(nextRow, nextColumn);
                }
            }
        }

        /**
         * Marks rooms reachable through existing matching doors using depth-first search.
         * Unlike generation, this traversal never creates a passage.
         *
         * @param theRow the current room row
         * @param theColumn the current room column
         */
        private void visitPassages(final int theRow, final int theColumn) {
            myVisited[theRow][theColumn] = true;
            for (final Direction direction : getNeighbors(theRow, theColumn)) {
                final int nextRow = getNextRow(theRow, direction);
                final int nextColumn = getNextColumn(theColumn, direction);
                if (!myVisited[nextRow][nextColumn]
                        && myRooms[theRow][theColumn].hasDoor(direction)
                        && myRooms[nextRow][nextColumn].hasDoor(getOpposite(direction))) {
                    visitPassages(nextRow, nextColumn);
                }
            }
        }

        /**
         * Reports whether the traversal reached every cell in the grid.
         *
         * @return true if every room was visited; false otherwise
         */
        private boolean allRoomsVisited() {
            boolean allVisited = true;
            for (int row = 0; row < myVisited.length; row++) {
                for (int column = 0; column < myVisited[row].length; column++) {
                    if (!myVisited[row][column]) {
                        allVisited = false;
                    }
                }
            }
            return allVisited;
        }

        /**
         * Lists directions to neighboring cells within the grid boundaries.
         * Visited cells are checked during traversal because recursion can visit them first.
         *
         * @param theRow the current row
         * @param theColumn the current column
         * @return the directions that lead to cells inside the grid
         */
        private List<Direction> getNeighbors(final int theRow, final int theColumn) {
            final List<Direction> neighbors = new ArrayList<>();
            if (theRow > 0) {
                neighbors.add(Direction.NORTH);
            }
            if (theRow < myRooms.length - 1) {
                neighbors.add(Direction.SOUTH);
            }
            if (theColumn < myRooms[0].length - 1) {
                neighbors.add(Direction.EAST);
            }
            if (theColumn > 0) {
                neighbors.add(Direction.WEST);
            }
            return neighbors;
        }

        /**
         * Calculates the row reached by moving one cell in a direction.
         *
         * @param theRow the current row
         * @param theDirection the movement direction
         * @return the destination row
         */
        private int getNextRow(final int theRow, final Direction theDirection) {
            int row = theRow;
            if (theDirection == Direction.NORTH) {
                row--;
            } else if (theDirection == Direction.SOUTH) {
                row++;
            }
            return row;
        }

        /**
         * Calculates the column reached by moving one cell in a direction.
         *
         * @param theColumn the current column
         * @param theDirection the movement direction
         * @return the destination column
         */
        private int getNextColumn(final int theColumn, final Direction theDirection) {
            int column = theColumn;
            if (theDirection == Direction.EAST) {
                column++;
            } else if (theDirection == Direction.WEST) {
                column--;
            }
            return column;
        }

        /**
         * Finds the matching direction for the neighboring room's door.
         *
         * @param theDirection the non-null direction of the original door
         * @return the opposite direction
         */
        private Direction getOpposite(final Direction theDirection) {
            final Direction opposite;
            switch (theDirection) {
                case NORTH:
                    opposite = Direction.SOUTH;
                    break;
                case SOUTH:
                    opposite = Direction.NORTH;
                    break;
                case EAST:
                    opposite = Direction.WEST;
                    break;
                case WEST:
                    opposite = Direction.EAST;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported direction.");
            }
            return opposite;
        }
    }
}
