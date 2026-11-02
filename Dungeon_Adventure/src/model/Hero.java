/*
 * TCSS 360 - Dungeon Adventure
 * Zachary Adams
 */
package model;

import java.util.Random;

/**
 * The player's character. Heroes may block incoming attacks
 * and carry an inventory of items such as healing potions.
 *
 * @author Zachary Adams
 * @version 2026-11-01
 */
public class Hero extends DungeonCharacter {

    /** Item name that identifies a healing potion in the inventory. */
    public static final String HEALING_POTION_NAME = "Healing Potion";

    /** Version identifier for serialized heroes. */
    private static final long serialVersionUID = 1L;

    /** Minimum hit points restored by a healing potion. */
    private static final int MIN_POTION_HEAL = 5;

    /** Maximum hit points restored by a healing potion. */
    private static final int MAX_POTION_HEAL = 15;

    /** Random number generator shared by all heroes. */
    private static final Random RANDOM = new Random();

    /** Probability from 0.0 to 1.0 that the hero blocks an attack. */
    private final double myChanceToBlock;

    /** Items the hero is carrying. */
    private final Inventory myInventory;

    /**
     * Constructs a hero at full health with an empty inventory.
     *
     * @param theName the display name
     * @param theHitPoints the starting and maximum hit points
     * @param theAttackSpeed the attack speed
     * @param theChanceToHit the probability that an attack lands
     * @param theMinDamage the minimum attack damage
     * @param theMaxDamage the maximum attack damage
     * @param theChanceToBlock the probability of blocking an attack
     */
    public Hero(final String theName,
                final int theHitPoints,
                final int theAttackSpeed,
                final double theChanceToHit,
                final int theMinDamage,
                final int theMaxDamage,
                final double theChanceToBlock) {
        super(theName, theHitPoints, theAttackSpeed, theChanceToHit,
              theMinDamage, theMaxDamage);
        myChanceToBlock = theChanceToBlock;
        myInventory = new Inventory();
    }

    /**
     * Rolls to block the attack. If the block fails, hit points
     * are reduced by the given amount, stopping at zero.
     *
     * @param theAmount the damage to take
     */
    @Override
    public void takeDamage(final int theAmount) {
        if (RANDOM.nextDouble() >= myChanceToBlock) {
            super.takeDamage(theAmount);
        }
    }

    /**
     * Drinks a healing potion from the inventory, if there is one,
     * restoring a random amount of hit points.
     *
     * @return true if a potion was used; false if none were carried
     */
    public boolean useHealingPotion() {
        boolean result = false;
        for (final Item item : myInventory.getItems()) {
            if (HEALING_POTION_NAME.equals(item.getName())) {
                myInventory.removeItem(item);
                heal(RANDOM.nextInt(MAX_POTION_HEAL - MIN_POTION_HEAL + 1)
                        + MIN_POTION_HEAL);
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Returns the hero's inventory.
     *
     * @return the inventory
     */
    public Inventory getInventory() {
        return myInventory;
    }

    /**
     * Returns the probability that the hero blocks an attack.
     *
     * @return the chance to block, from 0.0 to 1.0
     */
    public double getChanceToBlock() {
        return myChanceToBlock;
    }

    /**
     * Returns the hero's name, hit points, and inventory.
     *
     * @return a description of the hero
     */
    @Override
    public String toString() {
        return getName() + "\nHit Points: " + getHitPoints() + "/"
                + getMaxHitPoints() + "\n" + myInventory;
    }
}
