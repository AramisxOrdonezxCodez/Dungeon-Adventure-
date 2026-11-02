/*
 * TCSS 360 - Dungeon Adventure
 * Zachary Adams
 */
package model;

import java.util.Random;

/**
 * A dungeon character built from a MonsterDefinition.
 * Monsters may heal after surviving damage.
 *
 * @author Zachary Adams
 * @version 2026-11-01
 */
public class Monster extends DungeonCharacter {

    /** Version identifier for serialized monsters. */
    private static final long serialVersionUID = 1L;

    /** Random number generator shared by all monsters. */
    private static final Random RANDOM = new Random();

    /** Probability from 0.0 to 1.0 that the monster heals after being hit. */
    private final double myChanceToHeal;

    /** Minimum hit points restored by a heal. */
    private final int myMinHeal;

    /** Maximum hit points restored by a heal. */
    private final int myMaxHeal;

    /**
     * Constructs a monster at full health using the stats in a definition.
     *
     * @param theDefinition the stats for this kind of monster
     */
    public Monster(final MonsterDefinition theDefinition) {
        super(theDefinition.getName(),
              theDefinition.getHitPoints(),
              theDefinition.getAttackSpeed(),
              theDefinition.getChanceToHit(),
              theDefinition.getMinDamage(),
              theDefinition.getMaxDamage());
        myChanceToHeal = theDefinition.getChanceToHeal();
        myMinHeal = theDefinition.getMinHeal();
        myMaxHeal = theDefinition.getMaxHeal();
    }

    /**
     * Reduces hit points by the given amount, then tries to heal
     * if the monster survived.
     *
     * @param theAmount the damage to take
     */
    @Override
    public void takeDamage(final int theAmount) {
        super.takeDamage(theAmount);
        if (isAlive()) {
            tryHeal();
        }
    }

    /**
     * Returns the probability that the monster heals.
     *
     * @return the chance to heal, from 0.0 to 1.0
     */
    public double getChanceToHeal() {
        return myChanceToHeal;
    }

    /**
     * Returns the minimum heal amount.
     *
     * @return the minimum heal
     */
    public int getMinHeal() {
        return myMinHeal;
    }

    /**
     * Returns the maximum heal amount.
     *
     * @return the maximum heal
     */
    public int getMaxHeal() {
        return myMaxHeal;
    }

    /**
     * Rolls against the chance to heal. On success, restores a random
     * amount of hit points between the minimum and maximum heal.
     *
     * @return true if the monster healed; false otherwise
     */
    private boolean tryHeal() {
        boolean result = true;
        if (RANDOM.nextDouble() >= myChanceToHeal) {
            result = false;
        } else {
            heal(RANDOM.nextInt(myMaxHeal - myMinHeal + 1) + myMinHeal);
        }
        return result;
    }
}
