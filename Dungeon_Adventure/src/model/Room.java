/*
 * TCSS 360 - Dungeon Adventure
 * Kevin Munoz-Rivera
 */
package model;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Random;

/**
 * Represents a dungeon room with doors and randomly generated contents.
 * Potion presence, pit presence, and pit damage are determined at construction.
 * Stores a monster supplied by the game; item collection and effects remain separate.
 *
 * @author Kevin Munoz-Rivera
 * @version 2026-11-08
 */
public class Room implements Serializable {

    /** Version identifier for serialized rooms. */
    private static final long serialVersionUID = 1L;

    /** The roll that causes a room feature to be present. */
    private static final int POSSIBLE_MODS = 1;

    /** Number of equally likely rolls, giving each feature a ten percent chance. */
    private static final int CHANCE_OUTCOMES = 10;

    /** Minimum damage assigned to a pit. */
    private static final int MIN_PIT_DAMAGE = 1;

    /** Maximum damage assigned to a pit. */
    private static final int MAX_PIT_DAMAGE = 20;

    /** Error message for a missing door direction. */
    private static final String NULL_DIRECTION_MESSAGE = "Direction must not be null.";

    /** Random number generator shared by rooms. */
    private static final Random RANDOM = new Random();

    /** Directions in which this room has doors. */
    private final EnumSet<Direction> myDoors;

    /** Whether this room contains a pit. */
    private boolean myHasPit;

    /** Whether this room contains a healing potion. */
    private boolean myHasHealingPotion;

    /** Whether this room contains a vision potion. */
    private boolean myHasVisionPotion;

    /** Fixed pit damage for this room, or zero when there is no pit. */
    private int myPitDamage;

    /** Pillar stored in this room, or null when no pillar is present. */
    private Pillar myPillar;

    /** Monster in this room, or null when no monster is present. */
    private Monster myMonster;

    /** Whether this room has been revealed to the player. */
    private boolean myIsVisible;

    // TODO: Connect monster placement when the team's spawning rules are ready.
    // TODO: Add methods to remove collected potions and pillars from Room.
    // TODO: Connect Room's contents and pit damage to teammates' code.
    //       Teammates' classes handle item effects and changes to the hero.

    /** Whether this room is the dungeon entrance. */
    private boolean myIsEntrance;

    /** Whether this room is the dungeon exit. */
    private boolean myIsExit;

    /**
     * Constructs a room without doors and independently generates its contents.
     * The room initially has no pillar and is neither an entrance nor an exit.
     * The room is initially unrevealed.
     */
    public Room() {
        myDoors = EnumSet.noneOf(Direction.class);
        myIsEntrance = false;
        myIsExit = false;
        setUpMods();
    }

    /**
     * Independently generates potion and pit presence and assigns fixed pit damage.
     */
    private void setUpMods() {
        myHasHealingPotion = generateMods();
        myHasVisionPotion = generateMods();
        myHasPit = generateMods();
        myPitDamage = 0;
        if (myHasPit) {
            myPitDamage = RANDOM.nextInt(MAX_PIT_DAMAGE - MIN_PIT_DAMAGE + 1)
                    + MIN_PIT_DAMAGE;
        }
    }

    /**
     * Rolls for a single room feature with a ten percent chance of success.
     *
     * @return true if the feature should be present; false otherwise
     */
    private boolean generateMods() {
        final int chance = RANDOM.nextInt(CHANCE_OUTCOMES) + 1;
        return chance == POSSIBLE_MODS;
    }

    /**
     * Reports whether this room contains a healing potion.
     *
     * @return true if a healing potion is present; false otherwise
     */
    public boolean getHasHealingPotion() {
        return myHasHealingPotion;
    }

    /**
     * Reports whether this room contains a vision potion.
     *
     * @return true if a vision potion is present; false otherwise
     */
    public boolean getMyHasVisionPotion() {
        return myHasVisionPotion;
    }

    /**
     * Reports whether this room contains a pit.
     *
     * @return true if a pit is present; false otherwise
     */
    public boolean getMyHasPit() {
        return myHasPit;
    }

    /**
     * Returns the damage assigned to this room's pit at construction.
     * This method does not apply damage to a hero.
     *
     * @return damage from 1 through 20, or zero when no pit is present
     */
    public int getMyPitDamage() {
        return myPitDamage;
    }

    /**
     * Sets whether this room is the entrance.
     * Marking this role clears contents but preserves doors.
     * Removing the designation does not regenerate contents.
     * Rejects marking an existing exit as an entrance without changing its state.
     *
     * @param theEntrance true to mark the entrance; false to remove that designation
     * @throws IllegalStateException if marking an exit room as the entrance
     */
    public void setMyEntrance(final boolean theEntrance) {
        if (theEntrance && myIsExit) {
            throw new IllegalStateException("An exit room cannot also be the entrance.");
        }
        myIsEntrance = theEntrance;
        if (theEntrance) {
            clearContents();
        }
    }

    /**
     * Adds a door in the specified direction.
     * Adding an existing door leaves the set unchanged.
     *
     * @param theDirection the direction of the door
     * @throws IllegalArgumentException if the direction is null
     */
    public void addDoor(final Direction theDirection) {
        if (theDirection == null) {
            throw new IllegalArgumentException(NULL_DIRECTION_MESSAGE);
        }
        myDoors.add(theDirection);
    }

    /**
     * Reports whether this room has a door in the specified direction.
     *
     * @param theDirection the direction to check
     * @return true if the door exists; false otherwise
     * @throws IllegalArgumentException if the direction is null
     */
    public boolean hasDoor(final Direction theDirection) {
        if (theDirection == null) {
            throw new IllegalArgumentException(NULL_DIRECTION_MESSAGE);
        }
        return myDoors.contains(theDirection);
    }

    /**
     * Reports whether this room is the entrance.
     *
     * @return true if this room is the entrance; false otherwise
     */
    public boolean getIsEntrance() {
        return myIsEntrance;
    }

    /**
     * Sets whether this room is the exit.
     * Marking this role clears contents but preserves doors.
     * Removing the designation does not regenerate contents.
     * Rejects marking an existing entrance as an exit without changing its state.
     *
     * @param theExit true to mark the exit; false to remove that designation
     * @throws IllegalStateException if marking an entrance room as the exit
     */
    public void setExit(final boolean theExit) {
        if (theExit && myIsEntrance) {
            throw new IllegalStateException("An entrance room cannot also be the exit.");
        }
        myIsExit = theExit;
        if (theExit) {
            clearContents();
        }
    }

    /**
     * Reports whether this room is the exit.
     *
     * @return true if this room is the exit; false otherwise
     */
    public boolean getIsExit() {
        return myIsExit;
    }

    /**
     * Returns the pillar currently stored in this room.
     *
     * @return the pillar, or null when no pillar is present
     */
    public Pillar getPillar() {
        return myPillar;
    }

    /**
     * Returns the monster currently in this room.
     *
     * @return the monster, or null when no monster is present
     */
    public Monster getMonster() {
        return myMonster;
    }

    /**
     * Stores a supplied monster, replacing any monster already in this room.
     *
     * @param theMonster the monster to store, or null to clear the room
     * @throws IllegalStateException if adding a monster to an entrance or exit
     */
    public void setMonster(final Monster theMonster) {
        if (theMonster != null && (myIsEntrance || myIsExit)) {
            throw new IllegalStateException(
                    "An entrance or exit cannot contain a monster.");
        }
        myMonster = theMonster;
    }

    /**
     * Removes the monster from this room without changing its health.
     *
     * @return the removed monster, or null if there was no monster
     */
    public Monster removeMonster() {
        final Monster monster = myMonster;
        myMonster = null;
        return monster;
    }

    /**
     * Marks this room as revealed to the player.
     * Dungeon and view code decide which rooms to reveal and display.
     */
    public void reveal() {
        myIsVisible = true;
    }

    /**
     * Reports whether this room has been revealed to the player.
     *
     * @return true if the room has been revealed; false otherwise
     */
    public boolean isVisible() {
        return myIsVisible;
    }

    /**
     * Stores the specified pillar in this room.
     *
     * @param thePillar the pillar to store, or null to remove the pillar
     * @throws IllegalStateException if placing a pillar in an entrance or exit
     */
    public void setPillar(final Pillar thePillar) {
        if (thePillar != null && (myIsEntrance || myIsExit)) {
            throw new IllegalStateException(
                    "An entrance or exit cannot contain a pillar.");
        }
        myPillar = thePillar;
    }

    /**
     * Removes all current room contents while preserving doors and room designations.
     */
    private void clearContents() {
        myMonster = null;
        myHasHealingPotion = false;
        myHasVisionPotion = false;
        myHasPit = false;
        myPitDamage = 0;
        myPillar = null;
    }

    /**
     * Selects the center symbol for this room.
     * Multiple contents use M; a single pillar uses its own display symbol.
     *
     * @return the entrance, exit, contents, or empty-room symbol
     */
    private char getContentSymbol() {
        int count = 0;
        char symbol = ' ';

        if (myHasHealingPotion) {
            count++;
            symbol = 'H';
        }
        if (myHasVisionPotion) {
            count++;
            symbol = 'V';
        }
        if (myHasPit) {
            count++;
            symbol = 'X';
        }
        if (myPillar != null) {
            count++;
            symbol = myPillar.getSymbol();
        }

        if (myIsEntrance) {
            symbol = 'i';
        } else if (myIsExit) {
            symbol = 'O';
        } else if (count > 1) {
            symbol = 'M';
        }
        return symbol;
    }

    /**
     * Builds a three-line drawing of the room's doors and contents.
     * Walls use stars, north/south doors use dashes, and east/west doors use bars.
     * This method does not change the room or generate new contents.
     *
     * @return three rows of three characters separated by newlines, with no final newline
     */
    @Override
    public String toString() {
        char north = '*';
        char south = '*';
        char east = '*';
        char west = '*';

        if (hasDoor(Direction.NORTH)) {
            north = '-';
        }
        if (hasDoor(Direction.SOUTH)) {
            south = '-';
        }
        if (hasDoor(Direction.EAST)) {
            east = '|';
        }
        if (hasDoor(Direction.WEST)) {
            west = '|';
        }

        final StringBuilder result = new StringBuilder();
        result.append('*').append(north).append('*').append('\n');
        result.append(west).append(getContentSymbol()).append(east).append('\n');
        result.append('*').append(south).append('*');
        return result.toString();
    }
}
