/*
 * TCSS 360 - Dungeon Adventure
 * Zachary Adams
 */
package model;

import java.util.Random;

/**
 * A foolish samurai warrior who seeks to return to the past.
 *
 * @author Zachary Adams
 * @version 2026-11-08
 */
public class Warrior extends Hero {

    /** Version identifier for serialized warriors. */
    private static final long serialVersionUID = 1L;

    /** Starting and maximum hit points. */
    private static final int HIT_POINTS = 125;

    /** Attack speed. */
    private static final int ATTACK_SPEED = 4;

    /** Probability that a normal attack lands. */
    private static final double CHANCE_TO_HIT = 0.8;

    /** Minimum damage of a normal attack. */
    private static final int MIN_DAMAGE = 35;

    /** Maximum damage of a normal attack. */
    private static final int MAX_DAMAGE = 60;

    /** Probability of blocking an attack. */
    private static final double CHANCE_TO_BLOCK = 0.2;

    /** Probability that a Crushing Blow lands. */
    private static final double CRUSHING_BLOW_CHANCE = 0.4;

    /** Minimum damage of a Crushing Blow. */
    private static final int MIN_CRUSHING_DAMAGE = 75;

    /** Maximum damage of a Crushing Blow. */
    private static final int MAX_CRUSHING_DAMAGE = 175;

    /** Random number generator shared by all warriors. */
    private static final Random RANDOM = new Random();

    /**
     * Constructs a warrior at full health with an empty inventory.
     *
     * @param theName the display name
     */
    public Warrior(final String theName) {
        super(theName, HIT_POINTS, ATTACK_SPEED, CHANCE_TO_HIT,
              MIN_DAMAGE, MAX_DAMAGE, CHANCE_TO_BLOCK);
    }

    /**
     * Attempts a Crushing Blow, which deals heavy damage if it lands.
     *
     * @param theOpponent the character being attacked
     * @return a message describing what happened
     */
    @Override
    public String specialSkill(final DungeonCharacter theOpponent) {
        if (RANDOM.nextDouble() >= CRUSHING_BLOW_CHANCE) {
            return getName() + " attempts a Crushing Blow on "
                    + theOpponent.getName() + " but misses.";
        }
        final int damage = RANDOM.nextInt(MAX_CRUSHING_DAMAGE - MIN_CRUSHING_DAMAGE + 1)
                + MIN_CRUSHING_DAMAGE;
        theOpponent.takeDamage(damage);
        return getName() + " lands a Crushing Blow on " + theOpponent.getName()
                + " for " + damage + " damage.";
    }
}
