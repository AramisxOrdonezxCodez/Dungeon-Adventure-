/*
 * TCSS 360 - Dungeon Adventure
 * Zachary Adams
 */
package model;

import java.io.Serializable;
import java.util.Random;

/**
 * Base class for every character in the dungeon.
 * Heroes and monsters extend this class.
 *
 * @author Zachary Adams
 * @version 2026-11-01
 */
public abstract class DungeonCharacter implements Serializable {

    /** Version identifier for serialized characters. */
    private static final long serialVersionUID = 1L;

    /** Error message for a missing opponent. */
    private static final String NULL_OPPONENT_MESSAGE = "Opponent must not be null.";

    /** Random number generator shared by all characters. */
    private static final Random RANDOM = new Random();

    /** The display name of this character. */
    private String myName;

    /** Current hit points, from 0 to myMaxHitPoints. */
    private int myHitPoints;

    /** Hit points this character starts with and can heal up to. */
    private final int myMaxHitPoints;

    /** How quickly this character attacks relative to its opponent. */
    private final int myAttackSpeed;

    /** Probability from 0.0 to 1.0 that an attack lands. */
    private final double myChanceToHit;

    /** Minimum damage dealt by a successful attack. */
    private final int myMinDamage;

    /** Maximum damage dealt by a successful attack. */
    private final int myMaxDamage;

    /**
     * Constructs a character at full health.
     * The starting hit points also become the character's maximum hit points.
     *
     * @param theName the display name
     * @param theHitPoints the starting and maximum hit points
     * @param theAttackSpeed the attack speed
     * @param theChanceToHit the probability that an attack lands
     * @param theMinDamage the minimum attack damage
     * @param theMaxDamage the maximum attack damage
     */
    public DungeonCharacter(final String theName,
                            final int theHitPoints,
                            final int theAttackSpeed,
                            final double theChanceToHit,
                            final int theMinDamage,
                            final int theMaxDamage) {
        myName = theName;
        myHitPoints = theHitPoints;
        myMaxHitPoints = theHitPoints;
        myAttackSpeed = theAttackSpeed;
        myChanceToHit = theChanceToHit;
        myMinDamage = theMinDamage;
        myMaxDamage = theMaxDamage;
    }

    /**
     * Attempts one attack on an opponent. If the attack lands, the opponent
     * takes a random amount of damage between the minimum and maximum damage.
     *
     * @param theOpponent the character being attacked
     * @return a message describing what happened
     * @throws IllegalArgumentException if the opponent is null
     */
    public String attack(final DungeonCharacter theOpponent) {
        final String result;
        if (theOpponent == null) {
            throw new IllegalArgumentException(NULL_OPPONENT_MESSAGE);
        }
        if (RANDOM.nextDouble() >= myChanceToHit) {
            result = myName + " attacks " + theOpponent.getName() + " but misses.";
        } else {
            final int damage = RANDOM.nextInt(myMaxDamage - myMinDamage + 1) + myMinDamage;
            theOpponent.takeDamage(damage);
            result = myName + " hits " + theOpponent.getName() + " for " + damage
                    + " damage.";
        }
        return result;
    }

    /**
     * Reduces hit points by the given amount, stopping at zero.
     *
     * @param theAmount the damage to take
     */
    public void takeDamage(final int theAmount) {
        setHitPoints(myHitPoints - theAmount);
    }

    /**
     * Increases hit points by the given amount, stopping at the maximum.
     *
     * @param theAmount the hit points to restore
     */
    public void heal(final int theAmount) {
        setHitPoints(myHitPoints + theAmount);
    }

    /**
     * Reports whether this character still has hit points.
     *
     * @return true if hit points are above zero; false otherwise
     */
    public boolean isAlive() {
        return myHitPoints > 0;
    }

    /**
     * Returns the display name.
     *
     * @return the name
     */
    public String getName() {
        return myName;
    }

    /**
     * Changes the display name.
     *
     * @param theName the new name
     * @throws IllegalArgumentException if the name is null or blank
     */
    public void setName(final String theName) {
        myName = theName;
    }

    /**
     * Returns the current hit points.
     *
     * @return the hit points
     */
    public int getHitPoints() {
        return myHitPoints;
    }

    /**
     * Sets the current hit points, keeping them between zero and the maximum.
     *
     * @param theHitPoints the new hit points
     */
    public void setHitPoints(final int theHitPoints) {
        myHitPoints = Math.max(0, Math.min(theHitPoints, myMaxHitPoints));
    }

    /**
     * Returns the maximum hit points.
     *
     * @return the maximum hit points
     */
    public int getMaxHitPoints() {
        return myMaxHitPoints;
    }

    /**
     * Returns the attack speed.
     *
     * @return the attack speed
     */
    public int getAttackSpeed() {
        return myAttackSpeed;
    }

    /**
     * Returns the probability that an attack lands.
     *
     * @return the chance to hit, from 0.0 to 1.0
     */
    public double getChanceToHit() {
        return myChanceToHit;
    }

    /**
     * Returns the minimum attack damage.
     *
     * @return the minimum damage
     */
    public int getMinDamage() {
        return myMinDamage;
    }

    /**
     * Returns the maximum attack damage.
     *
     * @return the maximum damage
     */
    public int getMaxDamage() {
        return myMaxDamage;
    }
}
