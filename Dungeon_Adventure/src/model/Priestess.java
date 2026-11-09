/*
 * TCSS 360 - Dungeon Adventure
 * Zachary Adams
 */
package model;

import java.util.Random;

/**
 * A maiden who's giving up waiting for her prince.
 *
 * @author Zachary Adams
 * @version 2026-11-08
 */
public class Priestess extends Hero {

    /** Version identifier for serialized priestesses. */
    private static final long serialVersionUID = 1L;

    /** Starting and maximum hit points. */
    private static final int HIT_POINTS = 75;

    /** Attack speed. */
    private static final int ATTACK_SPEED = 5;

    /** Probability that a normal attack lands. */
    private static final double CHANCE_TO_HIT = 0.7;

    /** Minimum damage of a normal attack. */
    private static final int MIN_DAMAGE = 25;

    /** Maximum damage of a normal attack. */
    private static final int MAX_DAMAGE = 45;

    /** Probability of blocking an attack. */
    private static final double CHANCE_TO_BLOCK = 0.3;

    /** Minimum hit points restored by the heal skill. */
    private static final int MIN_SKILL_HEAL = 25;

    /** Maximum hit points restored by the heal skill. */
    private static final int MAX_SKILL_HEAL = 45;

    /** Random number generator shared by all priestesses. */
    private static final Random RANDOM = new Random();

    /**
     * Constructs a priestess at full health with an empty inventory.
     *
     * @param theName the display name
     */
    public Priestess(final String theName) {
        super(theName, HIT_POINTS, ATTACK_SPEED, CHANCE_TO_HIT,
              MIN_DAMAGE, MAX_DAMAGE, CHANCE_TO_BLOCK);
    }

    /**
     * Heals the priestess by a random amount, stopping at her maximum
     * hit points. The opponent is not affected.
     *
     * @param theOpponent the character being fought
     * @return a message describing what happened
     */
    @Override
    public String specialSkill(final DungeonCharacter theOpponent) {
        final int before = getHitPoints();
        heal(RANDOM.nextInt(MAX_SKILL_HEAL - MIN_SKILL_HEAL + 1) + MIN_SKILL_HEAL);
        return getName() + " heals for " + (getHitPoints() - before) + " hit points.";
    }
}
