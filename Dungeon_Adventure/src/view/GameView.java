package view;

import model.Dungeon;
import model.Hero;

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

    public void displayMessage(final String theMessage) {
        if (theMessage == null) {
            throw new IllegalArgumentException(
                    "Message cant be null.");
        }

        System.out.println(theMessage);
    }
}
