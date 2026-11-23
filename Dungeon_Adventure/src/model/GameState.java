/*
 * TCSS 360 - Dungeon Adventure
 * Kevin Munoz-Rivera
 */
package model;

import java.io.Serializable;
import java.util.EnumSet;

/**
 * Holds the hero, dungeon, current position, collected pillars, and game phase.
 * Validates movement and stores progress while gameplay handles room events.
 * Gameplay decides when to move, fight, collect items, and change phases.
 *
 * @author Kevin Munoz-Rivera
 * @version 2026-11-22
 */
public class GameState implements Serializable {

    /** Version of the saved game format. */
    private static final int SAVE_FORMAT_VERSION = 1;

    /** Version identifier for serialized Game state. */
    private static final long serialVersionUID = 1L;

    /** Message for a missing hero argument. */
    private static final String NULL_HERO_MESSAGE = "Hero cannot be null.";

    /** Message for a missing dungeon argument. */
    private static final String NULL_DUNGEON_MESSAGE = "Dungeon cannot be null.";

    /** Message when no dungeon is available. */
    private static final String MISSING_DUNGEON_MESSAGE = "Dungeon has not been set.";

    /** The player's selected hero. */
    private Hero myHero;

    /** The dungeon for the current game. */
    private Dungeon myDungeon;

    /** The hero's current row in the dungeon. */
    private int myHeroRow;

    /** The hero's current column in the dungeon. */
    private int myHeroColumn;

    /** The current stage of the game. */
    private GamePhase myPhase;

    /** The distinct pillars collected by the player. */
    private EnumSet<Pillar> myCollectedPillars;

    /**
     * Stores the selected hero and dungeon at the entrance in the main-menu phase.
     *
     * @param theHero the selected hero
     * @param theDungeon the dungeon to use
     * @throws IllegalArgumentException if the hero or dungeon is null
     */
    public GameState(final Hero theHero, final Dungeon theDungeon) {
        initializeGame(theHero, theDungeon, GamePhase.MAIN_MENU);
    }

    /**
     * Starts exploration at the supplied dungeon's entrance.
     * Reveals the entrance and clears previously collected pillars.
     *
     * @param theHero the selected hero
     * @param theDungeon the dungeon to explore
     * @throws IllegalArgumentException if the hero or dungeon is null
     */
    public void startGame(final Hero theHero, final Dungeon theDungeon) {
        initializeGame(theHero, theDungeon, GamePhase.EXPLORATION);
    }

    /**
     * Validates the supplied objects and initializes shared game information.
     * The caller chooses the initial phase for construction or starting exploration.
     *
     * @param theHero the selected hero
     * @param theDungeon the dungeon to use
     * @param thePhase the initial phase supplied internally
     * @throws IllegalArgumentException if the hero or dungeon is null
     */
    private void initializeGame(final Hero theHero, final Dungeon theDungeon,
                                final GamePhase thePhase) {
        if (theHero == null) {
            throw new IllegalArgumentException(NULL_HERO_MESSAGE);
        }
        if (theDungeon == null) {
            throw new IllegalArgumentException(NULL_DUNGEON_MESSAGE);
        }
        myHero = theHero;
        myDungeon = theDungeon;
        myPhase = thePhase;
        myCollectedPillars = EnumSet.noneOf(Pillar.class);
        placeHeroAtEntrance();
    }

    /**
     * Places the hero at the entrance and reveals that room.
     *
     * @throws IllegalStateException if the dungeon does not contain its entrance
     */
    private void placeHeroAtEntrance() {
        final Room entrance = myDungeon.getEntrance();
        for (int row = 0; row < myDungeon.getRowCount(); row++) {
            for (int column = 0; column < myDungeon.getColumnCount(); column++) {
                if (myDungeon.getRoom(row, column) == entrance) {
                    setHeroPosition(row, column);
                    entrance.reveal();
                    return;
                }
            }
        }
        throw new IllegalStateException("Dungeon entrance was not found.");
    }

    /**
     * Moves one room through matching doors during exploration and reveals it.
     * Gameplay handles the destination's items, pits, monsters, and victory checks.
     * An unsuccessful move leaves the position and visibility unchanged.
     *
     * @param theDirection the requested direction
     * @return true if the hero moved; false if movement was not allowed
     * @throws IllegalArgumentException if the direction is null
     */
    public boolean moveHero(final Direction theDirection) {
        if (theDirection == null) {
            throw new IllegalArgumentException("Direction cannot be null.");
        }
        boolean moved = false;
        if (isExploring() && getCurrentRoom().hasDoor(theDirection)) {
            int row = myHeroRow;
            int column = myHeroColumn;
            final Direction opposite;
            switch (theDirection) {
                case NORTH:
                    row--;
                    opposite = Direction.SOUTH;
                    break;
                case SOUTH:
                    row++;
                    opposite = Direction.NORTH;
                    break;
                case EAST:
                    column++;
                    opposite = Direction.WEST;
                    break;
                case WEST:
                    column--;
                    opposite = Direction.EAST;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported direction.");
            }
            if (canEnterRoom(row, column, opposite)) {
                setHeroPosition(row, column);
                getCurrentRoom().reveal();
                moved = true;
            }
        }
        return moved;
    }

    /**
     * Checks the destination bounds and its matching door.
     *
     * @param theRow the destination row
     * @param theColumn the destination column
     * @param theDoor the door leading back to the current room
     * @return true if the destination exists and has the matching door
     */
    private boolean canEnterRoom(final int theRow, final int theColumn,
                                 final Direction theDoor) {
        if (!isInsideDungeon(theRow, theColumn)) {
            return false;
        }
        return myDungeon.getRoom(theRow, theColumn).hasDoor(theDoor);
    }

    /**
     * Checks whether the hero is alive and currently exploring.
     *
     * @return true if movement and saving are permitted by the current state
     */
    private boolean isExploring() {
        return myPhase == GamePhase.EXPLORATION && myHero.isAlive();
    }

    /**
     * Checks coordinates without changing the hero's position.
     *
     * @param theRow the requested row
     * @param theColumn the requested column
     * @return true if both coordinates are inside the dungeon
     */
    private boolean isInsideDungeon(final int theRow, final int theColumn) {
        return theRow >= 0 && theRow < myDungeon.getRowCount()
                && theColumn >= 0 && theColumn < myDungeon.getColumnCount();
    }

    /**
     * Reports whether progress can be saved during exploration with a living hero.
     * This method does not write a save file.
     *
     * @return true if the hero is alive and the phase is exploration
     */
    public boolean canSave() {
        return isExploring();
    }

    /**
     * Returns the selected hero.
     *
     * @return the selected hero
     */
    public Hero getHero() {
        return myHero;
    }

    /**
     * Returns the current dungeon.
     *
     * @return the current dungeon
     */
    public Dungeon getDungeon() {
        return myDungeon;
    }

    /**
     * Returns the hero's current room after a game has started.
     *
     * @return the room at the stored row and column
     * @throws IllegalStateException if the dungeon has not been set
     */
    public Room getCurrentRoom() {
        if (myDungeon == null) {
            throw new IllegalStateException(MISSING_DUNGEON_MESSAGE);
        }
        return myDungeon.getRoom(myHeroRow, myHeroColumn);
    }

    /**
     * Returns the hero's current row.
     *
     * @return the zero-based row
     */
    public int getHeroRow() {
        return myHeroRow;
    }

    /**
     * Returns the hero's current column.
     *
     * @return the zero-based column
     */
    public int getHeroColumn() {
        return myHeroColumn;
    }

    /**
     * Updates the stored position after checking the coordinates.
     * This setter does not check doors or reveal rooms; normal movement uses moveHero.
     *
     * @param theRow the destination row
     * @param theColumn the destination column
     * @throws IllegalStateException if the dungeon has not been set
     * @throws IllegalArgumentException if either coordinate is outside the dungeon
     */
    public void setHeroPosition(final int theRow, final int theColumn) {
        if (myDungeon == null) {
            throw new IllegalStateException(MISSING_DUNGEON_MESSAGE);
        }

        if (!isInsideDungeon(theRow, theColumn)) {
            throw new IllegalArgumentException("The coordinates are out of bounds");
        }
        myHeroRow = theRow;
        myHeroColumn = theColumn;
    }

    /**
     * Returns the current game phase.
     *
     * @return the current phase
     */
    public GamePhase getPhase() {
        return myPhase;
    }

    /**
     * Updates the game phase selected by gameplay.
     *
     * @param thePhase the new phase
     * @throws IllegalArgumentException if the phase is null
     */
    public void setPhase(final GamePhase thePhase) {
        if (thePhase == null) {
            throw new IllegalArgumentException("Phase cannot be null.");
        }
        myPhase = thePhase;
    }

    /**
     * Records a pillar returned by Room.collectPillar without storing duplicates.
     *
     * @param thePillar the collected pillar
     * @throws IllegalArgumentException if the pillar is null
     */
    public void addPillar(final Pillar thePillar) {
        if (thePillar == null) {
            throw new IllegalArgumentException("Pillar cannot be null.");
        }

        myCollectedPillars.add(thePillar);
    }

    /**
     * Returns a copy of the player's collected pillars.
     *
     * @return a separate set containing the collected pillars
     */
    public EnumSet<Pillar> getCollectedPillars() {
        return EnumSet.copyOf(myCollectedPillars);
    }

    /**
     * Reports whether the player has collected all four pillars.
     * Gameplay also checks the exit and hero's health before declaring victory.
     *
     * @return true when all pillar types have been collected
     */
    public boolean hasAllPillars() {
        return myCollectedPillars.size() == Pillar.values().length;
    }
}
