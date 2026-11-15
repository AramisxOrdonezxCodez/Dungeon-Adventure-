package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.EnumSet;


public class Inventory implements Serializable  {
    private static final long serialVersionUID = 1L;

    private final List<Item> myItems;
    private final EnumSet<Pillar> myPillars;

    public Inventory() {

        myItems = new ArrayList<>();
        myPillars = EnumSet.noneOf(Pillar.class);
    }

    /**
     * Adds a pillar to the inventory.
     *
     * @param thePillar the pillar being collected
     */
    public void addPillar(final Pillar thePillar) {
        if (thePillar != null) {
            myPillars.add(thePillar);
        }
    }

    /**
     * Checks if the inventory contains all four pillars.
     *
     * @return true if all pillars are collected,
     *         false otherwise
     */
    public boolean hasAllPillars() {
        return myPillars.size() == Pillar.values().length;
    }


    /**
     * Returns the number of pillars collected.
     *
     * @return the number of unique pillars
     */
    public int getPillarCount() {
        return myPillars.size();
    }

    /**
     * Returns a copy of the collected pillars.
     *
     * @return the pillars currently in the inventory
     */
    public EnumSet<Pillar> getPillars() {
        return EnumSet.copyOf(myPillars);
    }


    // Adds an item to the inventory.
    public void addItem(final Item theItem) {
        if (theItem != null) {
            myItems.add(theItem);
        }
    }

    // Removes an item from the inventory.
    public boolean removeItem(final Item theItem) {
        return myItems.remove(theItem);
    }


    /**
     * Uses a vision potion from the inventory.
     *
     * @return true if a vision potion was used,
     *         false if none were available
     */
    public boolean useVisionPotion() {
        for (final Item item : getItems()) {
            if ("Vision Potion".equals(item.getName())) {
                return removeItem(item);
            }
        }

        return false;
    }


    // Returns a copy of the inventory items.
    public List<Item> getItems() {
        return new ArrayList<>(myItems);
    }

    // Returns the number of items in the inventory.
    public int getItemCount() {
        return myItems.size();
    }

    // Checks whether the inventory is empty.
    public boolean isEmpty() {
        return myItems.isEmpty();
    }

    @Override
    public String toString() {
        if (myItems.isEmpty()) {
            return "Inventory is empty.";
        }

        StringBuilder result = new StringBuilder("Inventory:\n");

        for (Item item : myItems) {
            result.append("- ").append(item).append("\n");
        }

        return result.toString();
    }
}
