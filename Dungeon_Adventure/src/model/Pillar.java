/*
 * TCSS 360 - Dungeon Adventure
 * Kevin Munoz-Rivera
 */
package model;

/**
 * Represents the four pillars of object-oriented programming collected in the dungeon.
 * Each pillar has a character used to display it on the dungeon map.
 *
 * @author Kevin Munoz-Rivera
 * @version 2026-10-29
 */
public enum Pillar {
    /** The abstraction pillar, represented by A. */
    ABSTRACTION('A'),

    /** The encapsulation pillar, represented by E. */
    ENCAPSULATION('E'),

    /** The inheritance pillar, represented by I. */
    INHERITANCE('I'),

    /** The polymorphism pillar, represented by P. */
    POLYMORPHISM('P');

    /** The character used to display this pillar on the dungeon map. */
    private final char mySymbol;

    /**
     * Creates a pillar with its display symbol.
     *
     * @param theSymbol the character representing this pillar
     */
    Pillar(final char theSymbol) {
        mySymbol = theSymbol;
    }

    /**
     * Returns this pillar's display symbol.
     *
     * @return the character representing this pillar
     */
    public char getSymbol() {
        return mySymbol;
    }
}
