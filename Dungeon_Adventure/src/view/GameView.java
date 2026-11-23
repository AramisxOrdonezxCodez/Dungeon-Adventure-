package view;

import model.Dungeon;
import model.Hero;
import java.util.List;

public class GameView {
    public void displayMainMenu() {

        System.out.println(" DUNGEON ADVENTURE");

        System.out.println("1. New Game");
        System.out.println("2. Load Game");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");
    }

    public void displayHeroSelection() {
        System.out.println("\nSelect your hero:");
        System.out.println("1. Warrior");
        System.out.println("2. Priestess");
        System.out.println("3. Thief");
        System.out.print("Enter your choice: ");
    }

    public void displayDungeon(final Dungeon theDungeon) {
        if (theDungeon == null) {
            throw new IllegalArgumentException(
                    "Dungeon cant be null.");
        }

        System.out.println(theDungeon.toVisibleString());
    }

    public void displayHeroStats(final Hero theHero) {
        if (theHero == null) {
            throw new IllegalArgumentException(
                    "Hero cant be null.");
        }

        System.out.println(theHero);
    }

    /**
     * Displays the items stored in the hero's inventory.
     *
     * @param theHero the hero whose inventory is displayed
     */
    public void displayInventory(final Hero theHero) {
        if (theHero == null) {
            throw new IllegalArgumentException(
                    "Hero cannot be null.");
        }

        System.out.println("\n INVENTORY ");

        if (theHero.getInventory().isEmpty()) {
            System.out.println("No items collected.");
        } else {
            System.out.println(theHero.getInventory());
        }
    }

    /**
     * Displays the available actions during combat
     */
    public void displayCombatMenu() {
        System.out.println("\n MORTAL KOMBAT ");
        System.out.println("1. Attack");
        System.out.println("2. Special Skill");
        System.out.println("3. Healing Potion");
        System.out.print("Choose an action: ");
    }


    /**
     * Displays the available save slots
     *
     * @param theSlots the list of save slot information
     */
    public void displaySaveSlots(final List<String> theSlots) {
        if (theSlots == null) {
            throw new IllegalArgumentException(
                    "Save slots cannot be null.");
        }

        System.out.println("\nSAVE SLOTS");

        for (final String slot : theSlots) {
            System.out.println(slot);
        }
    }


    /**
     * Displays a message when the player wins the game
     */
    public void displayVictory() {

        System.out.println(" VICTORIOUS");

        System.out.println("All da four Pillars of OO were collected");
        System.out.println("You barely escaped the dungeon!");
    }

    /**
     * Displays a message when the player loses the game.
     */
    public void displayDefeat() {

        System.out.println(" LOSER");

        System.out.println("Your hero has failed");
        System.out.println("Hold on to this L real quick");
    }




    public void displayMessage(final String theMessage) {
        if (theMessage == null) {
            throw new IllegalArgumentException(
                    "Message cant be null.");
        }

        System.out.println(theMessage);
    }
}
