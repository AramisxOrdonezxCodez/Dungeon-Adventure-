/*
 * TCSS 360 - Dungeon Adventure
 * Zachary Adams
 */
package controller;

import data.HeroFactory;
import data.MonsterDatabase;
import data.MonsterFactory;
import data.SaveService;
import java.io.IOException;
import java.util.Scanner;
import model.Direction;
import model.Dungeon;
import model.GamePhase;
import model.GameState;
import model.Hero;
import model.HeroType;
import model.Monster;
import model.Pillar;
import model.Room;
import view.GameView;

/**
 * Runs Dungeon Adventure: reads the player's commands, updates the game
 * state, and reports what happened through the view.
 *
 * @author Zachary Adams
 * @version 2026-11-18
 */
public class DungeonAdventure {

    /** Lowest save slot number. */
    private static final int FIRST_SLOT = 1;

    /** Highest save slot number. */
    private static final int LAST_SLOT = 10;

    /** Short introduction shown when the game starts. */
    private static final String INTRO = """
            Find the four Pillars of OO (Abstraction, Encapsulation,
            Inheritance, Polymorphism) and escape through the exit.""";

    /** Commands available while exploring, with the map legend. */
    private static final String HELP = """
            Commands:
              w, a, s, d   move up, left, down, or right
              h            drink a healing potion
              v            drink a vision potion
              m            show the map
              i            show your hero and inventory
              save         save the game
              load         load a saved game
              help         show these commands
              q            quit to the main menu
            Map: i = entrance, O = exit, A/E/I/P = pillar, H = healing potion,
                 V = vision potion, X = pit, M = multiple items, ? = unexplored""";

    /** The game being played, or null before the first game starts. */
    private GameState myState;

    /** Displays the game. */
    private final GameView myView;

    /** Reads the player's input from the console. */
    private final Scanner myInput;

    /** Creates monsters from the monster database. */
    private final MonsterFactory myFactory;

    /** Saves and loads games. */
    private final SaveService mySaveService;

    /** Whether the current game has changed since it was last saved or loaded. */
    private boolean myHasUnsavedProgress;

    /**
     * Constructs the game with a console view, the monster database, and save slots.
     */
    public DungeonAdventure() {
        myView = new GameView();
        myInput = new Scanner(System.in);
        myFactory = new MonsterFactory(new MonsterDatabase());
        mySaveService = new SaveService();
        myHasUnsavedProgress = false;
    }

    /**
     * Starts Dungeon Adventure.
     *
     * @param theArgs command-line arguments (not used)
     */
    public static void main(final String[] theArgs) {
        new DungeonAdventure().run();
    }

    /**
     * Shows the main menu and runs games until the player quits.
     */
    public void run() {
        myView.displayMessage(INTRO);
        boolean running = true;
        while (running) {
            myView.displayMessage("");
            myView.displayMainMenu();
            final String choice = readLine();
            if (choice == null || "3".equals(choice)) {
                running = false;
            } else if ("1".equals(choice)) {
                startNewGame();
            } else if ("2".equals(choice)) {
                load();
            } else {
                myView.displayMessage("Please choose 1, 2, or 3.");
            }
            if (running && myState != null && myState.getPhase() == GamePhase.EXPLORATION) {
                playGame();
            }
        }
        myView.displayMessage("Thanks for playing!");
    }

    /**
     * Asks the player for a hero and starts a new game in a new dungeon.
     * Does nothing if input ends before the hero is chosen.
     */
    public void startNewGame() {
        final String name = promptName();
        final HeroType type = name == null ? null : promptHeroType();
        if (type != null) {
            final Hero hero = HeroFactory.createHero(type, name);
            final Dungeon dungeon = new Dungeon();
            populateMonsters(dungeon);
            myState = new GameState(hero, dungeon);
            myState.startGame(hero, dungeon);
            myHasUnsavedProgress = true;
            myView.displayMessage("\n" + hero.getName() + " the "
                    + hero.getClass().getSimpleName() + " enters the dungeon.");
            myView.displayMessage(HELP);
            myView.displayDungeon(dungeon);
        }
    }

    /**
     * Asks for the hero's name until a non-blank name is entered.
     *
     * @return the name, or null if input ended
     */
    private String promptName() {
        String name = prompt("\nName your hero:");
        while (name != null && name.isBlank()) {
            name = prompt("Your hero needs a name:");
        }
        return name;
    }

    /**
     * Asks which kind of hero to play until a valid choice is entered.
     * The view lists the heroes in the same order as HeroType.
     *
     * @return the chosen hero type, or null if input ended
     */
    private HeroType promptHeroType() {
        final HeroType[] types = HeroType.values();
        myView.displayHeroSelection();
        HeroType type = null;
        String choice = readLine();
        while (choice != null && type == null) {
            final int index = parseNumber(choice) - 1;
            if (index >= 0 && index < types.length) {
                type = types[index];
            } else {
                choice = prompt("Choose 1-" + types.length + ":");
            }
        }
        return type;
    }

    /**
     * Replaces every monster the dungeon placed with one built from the
     * monster database, so the database controls monster stats.
     *
     * @param theDungeon the newly created dungeon
     */
    private void populateMonsters(final Dungeon theDungeon) {
        for (int row = 0; row < theDungeon.getRowCount(); row++) {
            for (int column = 0; column < theDungeon.getColumnCount(); column++) {
                final Room room = theDungeon.getRoom(row, column);
                if (room.getMonster() != null) {
                    room.setMonster(myFactory.createRandomMonster());
                }
            }
        }
    }

    /**
     * Reads and handles commands while the player is exploring.
     */
    private void playGame() {
        while (myState.getPhase() == GamePhase.EXPLORATION) {
            final String input = prompt("\nWhat now? (help for commands)");
            if (input == null) {
                myState.setPhase(GamePhase.MAIN_MENU);
            } else {
                handleCommand(input);
            }
        }
    }

    /**
     * Carries out one exploration command.
     *
     * @param theInput the command the player typed
     */
    private void handleCommand(final String theInput) {
        switch (theInput.toLowerCase()) {
            case "w", "up" -> move(Direction.NORTH);
            case "s", "down" -> move(Direction.SOUTH);
            case "a", "left" -> move(Direction.WEST);
            case "d", "right" -> move(Direction.EAST);
            case "h", "heal" -> useHealingPotion();
            case "v", "vision" -> useVisionPotion();
            case "m", "map" -> myView.displayDungeon(myState.getDungeon());
            case "i", "inventory" -> showHeroStatus();
            case "save" -> save();
            case "load" -> load();
            case "help", "?" -> myView.displayMessage(HELP);
            case "q", "quit" -> quitToMenu();
            default -> myView.displayMessage("Unknown command. Type help for commands.");
        }
    }

    /**
     * Moves the hero through a door and handles whatever is in the new room.
     *
     * @param theDirection the direction to move
     */
    private void move(final Direction theDirection) {
        if (myState.moveHero(theDirection)) {
            myHasUnsavedProgress = true;
            myView.displayMessage("You move " + theDirection.name().toLowerCase() + ".");
            processRoomEvents(myState.getCurrentRoom());
            checkVictoryOrDefeat();
            if (myState.getPhase() == GamePhase.EXPLORATION) {
                myView.displayDungeon(myState.getDungeon());
            }
        } else {
            myView.displayMessage("There is no door to the "
                    + theDirection.name().toLowerCase() + ".");
        }
    }

    /**
     * Handles the room the hero just entered: pit first, then any monster,
     * then items and pillars, which the hero only collects if still alive.
     *
     * @param theRoom the room the hero entered
     */
    private void processRoomEvents(final Room theRoom) {
        final Hero hero = myState.getHero();
        if (theRoom.getMyHasPit()) {
            theRoom.applyPit(hero);
            myView.displayMessage("You fall into a pit and take " + theRoom.getMyPitDamage()
                    + " damage! (" + hero.getHitPoints() + "/" + hero.getMaxHitPoints()
                    + " HP)");
        }
        final Monster monster = theRoom.getMonster();
        if (hero.isAlive() && monster != null) {
            runCombat(monster);
        }
        if (hero.isAlive()) {
            if (theRoom.collectHealingPotion(hero)) {
                myView.displayMessage("You found a healing potion.");
            }
            if (theRoom.collectVisionPotion(hero)) {
                myView.displayMessage("You found a vision potion.");
            }
            final Pillar pillar = theRoom.collectPillar();
            if (pillar != null) {
                myState.addPillar(pillar);
                hero.getInventory().addPillar(pillar);
                myView.displayMessage("You found the Pillar of " + pillar + "! ("
                        + myState.getCollectedPillars().size() + "/"
                        + Pillar.values().length + ")");
            }
        }
    }

    /**
     * Fights a monster until one side falls. Each round, the faster side
     * may act more than once: a side gets one turn per multiple of the
     * other side's attack speed, and always at least one.
     *
     * @param theMonster the monster to fight
     */
    private void runCombat(final Monster theMonster) {
        final Hero hero = myState.getHero();
        myState.setPhase(GamePhase.COMBAT);
        myView.displayMessage("The " + theMonster.getName() + " attacks!");
        final int heroTurns = Math.max(1,
                hero.getAttackSpeed() / theMonster.getAttackSpeed());
        final int monsterTurns = Math.max(1,
                theMonster.getAttackSpeed() / hero.getAttackSpeed());
        while (hero.isAlive() && theMonster.isAlive()) {
            for (int turn = 0; turn < heroTurns && theMonster.isAlive(); turn++) {
                showCombatStatus(theMonster);
                takeCombatTurn(theMonster);
            }
            for (int turn = 0; turn < monsterTurns && theMonster.isAlive()
                    && hero.isAlive(); turn++) {
                myView.displayMessage(theMonster.attack(hero));
            }
        }
        if (!theMonster.isAlive()) {
            myState.getCurrentRoom().removeMonster();
            myView.displayMessage("You defeated the " + theMonster.getName() + "!");
        }
        myState.setPhase(GamePhase.EXPLORATION);
    }

    /**
     * Asks the player for one combat action and carries it out.
     * Drinking a potion uses the turn; if there is none, the player chooses again.
     * If input ends, the hero attacks.
     *
     * @param theMonster the monster being fought
     */
    private void takeCombatTurn(final Monster theMonster) {
        final Hero hero = myState.getHero();
        boolean turnTaken = false;
        while (!turnTaken) {
            final String choice = prompt("1) Attack  2) Special skill  3) Healing potion");
            if (choice == null || "1".equals(choice)) {
                myView.displayMessage(hero.attack(theMonster));
                turnTaken = true;
            } else if ("2".equals(choice)) {
                myView.displayMessage(hero.specialSkill(theMonster));
                turnTaken = true;
            } else if ("3".equals(choice)) {
                turnTaken = drinkHealingPotion();
            } else {
                myView.displayMessage("Please choose 1, 2, or 3.");
            }
        }
    }

    /**
     * Drinks a healing potion during exploration.
     */
    private void useHealingPotion() {
        if (drinkHealingPotion()) {
            myHasUnsavedProgress = true;
        }
    }

    /**
     * Drinks a healing potion if the hero has one and reports the result.
     *
     * @return true if a potion was drunk; false otherwise
     */
    private boolean drinkHealingPotion() {
        final Hero hero = myState.getHero();
        final int before = hero.getHitPoints();
        final boolean used = hero.useHealingPotion();
        if (used) {
            myView.displayMessage("You drink a healing potion and recover "
                    + (hero.getHitPoints() - before) + " HP. (" + hero.getHitPoints()
                    + "/" + hero.getMaxHitPoints() + " HP)");
        } else {
            myView.displayMessage("You have no healing potions.");
        }
        return used;
    }

    /**
     * Drinks a vision potion, revealing the rooms around the hero.
     */
    private void useVisionPotion() {
        if (myState.getDungeon().useVisionPotion(myState.getHero(),
                myState.getHeroRow(), myState.getHeroColumn())) {
            myHasUnsavedProgress = true;
            myView.displayMessage("Your vision expands to the surrounding rooms.");
            myView.displayDungeon(myState.getDungeon());
        } else {
            myView.displayMessage("You have no vision potions.");
        }
    }

    /**
     * Saves the current game to a slot the player chooses.
     */
    private void save() {
        final int slot = promptSlot("Save to which slot?");
        if (slot != 0 && (!mySaveService.isOccupied(slot)
                || confirm("Slot " + slot + " already has a save. Overwrite it?"))) {
            try {
                mySaveService.save(myState, slot);
                myHasUnsavedProgress = false;
                myView.displayMessage("Game saved to slot " + slot + ".");
            } catch (final IOException | IllegalStateException e) {
                myView.displayMessage("Could not save: " + e.getMessage());
            }
        }
    }

    /**
     * Loads a saved game from a slot the player chooses. During a game,
     * asks first if loading would lose unsaved progress.
     */
    private void load() {
        final boolean inGame = myState != null
                && myState.getPhase() == GamePhase.EXPLORATION;
        if (!inGame || !myHasUnsavedProgress
                || confirm("You have unsaved progress. Load anyway?")) {
            final int slot = promptSlot("Load which slot?");
            if (slot != 0) {
                try {
                    myState = mySaveService.load(slot);
                    myHasUnsavedProgress = false;
                    myView.displayMessage("Loaded slot " + slot + ".");
                    showHeroStatus();
                    myView.displayDungeon(myState.getDungeon());
                } catch (final IOException | ClassNotFoundException e) {
                    myView.displayMessage("Could not load: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Shows the save slots and asks for a slot number.
     *
     * @param thePrompt the question to ask
     * @return the chosen slot, or 0 if the player cancels
     */
    private int promptSlot(final String thePrompt) {
        myView.displayMessage(String.join("\n", mySaveService.listSlots()));
        int slot = -1;
        while (slot == -1) {
            final String choice = prompt(thePrompt + " (" + FIRST_SLOT + "-"
                    + LAST_SLOT + ", or 0 to cancel)");
            final int number = choice == null ? 0 : parseNumber(choice);
            if (number == 0 || number >= FIRST_SLOT && number <= LAST_SLOT) {
                slot = number;
            } else {
                myView.displayMessage("That is not a save slot.");
            }
        }
        return slot;
    }

    /**
     * Returns to the main menu, asking first if there is unsaved progress.
     */
    private void quitToMenu() {
        if (!myHasUnsavedProgress
                || confirm("You have unsaved progress. Quit anyway?")) {
            myState.setPhase(GamePhase.MAIN_MENU);
        }
    }

    /**
     * Ends the game if the hero has died, or if the hero reached the exit
     * holding every pillar. Reminds the player when they reach the exit early.
     * At the end of a game, shows the whole dungeon.
     */
    private void checkVictoryOrDefeat() {
        final Room room = myState.getCurrentRoom();
        if (!myState.getHero().isAlive()) {
            myState.setPhase(GamePhase.DEFEAT);
            myHasUnsavedProgress = false;
            myView.displayMessage("\nYou have fallen in the dungeon. Game over.");
            myView.displayMessage(myState.getDungeon().toString());
        } else if (room.getIsExit() && myState.hasAllPillars()) {
            myState.setPhase(GamePhase.VICTORY);
            myHasUnsavedProgress = false;
            myView.displayMessage("\nYou escape the dungeon with all four Pillars of OO. "
                    + "You win!");
            myView.displayMessage(myState.getDungeon().toString());
        } else if (room.getIsExit()) {
            myView.displayMessage("You found the exit, but you still need "
                    + (Pillar.values().length - myState.getCollectedPillars().size())
                    + " more pillar(s).");
        }
    }

    /**
     * Shows the hero's stats and the pillars collected so far.
     */
    private void showHeroStatus() {
        myView.displayHeroStats(myState.getHero());
        myView.displayMessage("Pillars: " + myState.getCollectedPillars().size() + "/"
                + Pillar.values().length + " " + myState.getCollectedPillars());
    }

    /**
     * Shows the hit points of the hero and the monster being fought.
     *
     * @param theMonster the monster being fought
     */
    private void showCombatStatus(final Monster theMonster) {
        final Hero hero = myState.getHero();
        myView.displayMessage("\n" + hero.getName() + ": " + hero.getHitPoints() + "/"
                + hero.getMaxHitPoints() + " HP   vs   " + theMonster.getName() + ": "
                + theMonster.getHitPoints() + "/" + theMonster.getMaxHitPoints() + " HP");
    }

    /**
     * Asks a yes-or-no question.
     *
     * @param theQuestion the question to ask
     * @return true if the player answered yes; false otherwise
     */
    private boolean confirm(final String theQuestion) {
        final String answer = prompt(theQuestion + " (y/n)");
        return answer != null && answer.toLowerCase().startsWith("y");
    }

    /**
     * Shows a prompt and reads the player's answer.
     *
     * @param thePrompt the text shown before reading
     * @return the trimmed line, or null if there is no more input
     */
    private String prompt(final String thePrompt) {
        myView.displayMessage(thePrompt);
        return readLine();
    }

    /**
     * Reads one line of the player's input.
     *
     * @return the trimmed line, or null if there is no more input
     */
    private String readLine() {
        String line = null;
        if (myInput.hasNextLine()) {
            line = myInput.nextLine().trim();
        }
        return line;
    }

    /**
     * Reads a whole number from the player's input.
     *
     * @param theInput the text to read
     * @return the number, or -1 if the text is not a number
     */
    private static int parseNumber(final String theInput) {
        int number = -1;
        try {
            number = Integer.parseInt(theInput.trim());
        } catch (final NumberFormatException e) {
            number = -1;
        }
        return number;
    }
}
