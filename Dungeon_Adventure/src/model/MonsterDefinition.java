/*
 * TCSS 360 - Dungeon Adventure
 * Zachary Adams
 */
package model;

/**
 * Holds the stats that describe one kind of monster.
 *
 * @author Zachary Adams
 * @version 2026-11-01
 */
public class MonsterDefinition {

    /** The kind of monster, such as "Ogre" or "Gremlin". */
    private final String myType;

    /** The display name of the monster. */
    private final String myName;

    /** The starting hit points of the monster. */
    private final int myHitPoints;

    /** How quickly the monster attacks relative to its opponent. */
    private final int myAttackSpeed;

    /** Probability from 0.0 to 1.0 that an attack lands. */
    private final double myChanceToHit;

    /** Minimum damage dealt by a successful attack. */
    private final int myMinDamage;

    /** Maximum damage dealt by a successful attack. */
    private final int myMaxDamage;

    /** Probability from 0.0 to 1.0 that the monster heals after being hit. */
    private final double myChanceToHeal;

    /** Minimum hit points restored by a heal. */
    private final int myMinHeal;

    /** Maximum hit points restored by a heal. */
    private final int myMaxHeal;

    /**
     * Constructs a monster definition with the given stats.
     *
     * @param theType the kind of monster
     * @param theName the display name of the monster
     * @param theHitPoints the starting hit points
     * @param theAttackSpeed the attack speed
     * @param theChanceToHit the probability that an attack lands
     * @param theMinDamage the minimum attack damage
     * @param theMaxDamage the maximum attack damage
     * @param theChanceToHeal the probability that the monster heals
     * @param theMinHeal the minimum heal amount
     * @param theMaxHeal the maximum heal amount
     */
    public MonsterDefinition(final String theType,
                             final String theName,
                             final int theHitPoints,
                             final int theAttackSpeed,
                             final double theChanceToHit,
                             final int theMinDamage,
                             final int theMaxDamage,
                             final double theChanceToHeal,
                             final int theMinHeal,
                             final int theMaxHeal) {
        myType = theType;
        myName = theName;
        myHitPoints = theHitPoints;
        myAttackSpeed = theAttackSpeed;
        myChanceToHit = theChanceToHit;
        myMinDamage = theMinDamage;
        myMaxDamage = theMaxDamage;
        myChanceToHeal = theChanceToHeal;
        myMinHeal = theMinHeal;
        myMaxHeal = theMaxHeal;
    }

    /**
     * Returns the kind of monster.
     *
     * @return the monster type
     */
    public String getType() {
        return myType;
    }

    /**
     * Returns the display name of the monster.
     *
     * @return the monster name
     */
    public String getName() {
        return myName;
    }

    /**
     * Returns the starting hit points.
     *
     * @return the hit points
     */
    public int getHitPoints() {
        return myHitPoints;
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
}
