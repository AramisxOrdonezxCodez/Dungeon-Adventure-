/*
 * TCSS 360 - Dungeon Adventure
 * Kevin Munoz-Rivera
 */
package model;

/**
 * Represents the current stage of the game, from the main menu to the final outcome.
 *
 * @author Kevin Munoz-Rivera
 * @version 2026-11-08
 */
public enum GamePhase {

    /** The player is at the main menu. */
    MAIN_MENU,

    /** The player is choosing a hero for the adventure. */
    HERO_SELECTION,

    /** The player is exploring the dungeon. */
    EXPLORATION,

    /** The player is engaged in combat with a monster. */
    COMBAT,

    /** The player has won the game. */
    VICTORY,

    /** The player has lost the game. */
    DEFEAT
}
