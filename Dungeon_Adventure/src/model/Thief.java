/*
 * TCSS 360 - Dungeon Adventure
 * Zachary Adams
 */
package model;

import java.util.Random;

/**
 * A sly thief sporting a conspicuous cain.
 *
 * @author Zachary Adams
 * @version 2026-11-08
 */
public class Thief extends Hero {

    /** Version identifier for serialized thieves. */
    private static final long serialVersionUID = 1L;

    /** Starting and maximum hit points. */
    private static final int HIT_POINTS = 75;

    /** Attack speed. */
    private static final int ATTACK_SPEED = 6;

    /** Probability that a normal attack lands. */
    private static final double CHANCE_TO_HIT = 0.8;

    /** Minimum damage of a normal attack. */
    private static final int MIN_DAMAGE = 20;

    /** Maximum damage of a normal attack. */
    private static final int MAX_DAMAGE = 40;

    /** Probability of blocking an attack. */
    private static final double CHANCE_TO_BLOCK = 0.4;

    /** Probability that the Surprise Attack earns a bonus attack. */
    private static final double SURPRISE_CHANCE = 0.4;

    /** Probability that the thief is caught and does not attack. */
    private static final double CAUGHT_CHANCE = 0.2;

    /** Random number generator shared by all thieves. */
    private static final Random RANDOM = new Random();

    /**
     * Constructs a thief at full health with an empty inventory.
     *
     * @param theName the display name
     */
    public Thief(final String theName) {
        super(theName, HIT_POINTS, ATTACK_SPEED, CHANCE_TO_HIT,
              MIN_DAMAGE, MAX_DAMAGE, CHANCE_TO_BLOCK);
    }

    /**
     * Attempts a Surprise Attack. 40% of the time the thief attacks twice,
     * 20% of the time the thief is caught and does not attack, and
     * otherwise the thief makes one normal attack.
     *
     * @param theOpponent the character being attacked
     * @return a message describing what happened
     */
    @Override
    public String specialSkill(final DungeonCharacter theOpponent) {
        final double roll = RANDOM.nextDouble();
        if (roll < SURPRISE_CHANCE) {
            final String first = attack(theOpponent);
            if (!theOpponent.isAlive()) {
                return getName() + " surprises " + theOpponent.getName() + "! " + first;
            }
            return getName() + " surprises " + theOpponent.getName() + "! " + first
                    + " " + attack(theOpponent);
        }
        if (roll < SURPRISE_CHANCE + CAUGHT_CHANCE) {
            return getName() + " is caught trying to surprise "
                    + theOpponent.getName() + " and does not attack.";
        }
        return attack(theOpponent);
    }
}
