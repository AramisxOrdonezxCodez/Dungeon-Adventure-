package data;

import model.GameState;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
public class SaveService {

    // Maximum number of save slots
    private static final int SLOT_COUNT = 10;

    // Folder where saved games are stored
    private static final String SAVE_DIR = "saves";

    private final Path mySaveDirectory;


    // Creates a new SaveService

    public SaveService() {

        this(Paths.get(SAVE_DIR));
    }

    public SaveService(final Path theSaveDirectory) {
        if (theSaveDirectory == null) {
            throw new IllegalArgumentException(
                    "Save directory cannot be null.");
        }

        mySaveDirectory = theSaveDirectory;
    }

    /**
     * Saves the current game state to the selected slot
     * @param theGameState the game state to save
     * @param theSlot the selected save slot (1-10)
     * @throws IOException if the game cannot be saved
     */
    public void save(final GameState theGameState,
                     final int theSlot) throws IOException {

        validateSlot(theSlot);

        if (theGameState == null) {
            throw new IllegalArgumentException(
                    "Game state cannot be null.");
        }

        if (!theGameState.canSave()) {
            throw new IllegalStateException(
                    "The game cannot be saved right now.");
        }

        final Path savePath = getSavePath(theSlot);

        Files.createDirectories(savePath.getParent());

        final Path tempPath = Files.createTempFile(
                savePath.getParent(), "slot_" + theSlot + "_", ".tmp");

        try {
            try (ObjectOutputStream output =
                         new ObjectOutputStream(
                                 new FileOutputStream(tempPath.toFile()))) {

                output.writeObject(theGameState);
            }

            try {
                Files.move(tempPath, savePath,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(tempPath, savePath,
                        StandardCopyOption.REPLACE_EXISTING);
            }

        } finally {
            Files.deleteIfExists(tempPath);
        }
    }

    /**
     * Loads a previously saved game from the selected slot
     * @param theSlot the selected save slot (1-10)
     * @return the loaded game state
     * @throws IOException if the save file cannot be read
     * @throws ClassNotFoundException if the saved class is unavailable
     */
    public GameState load(final int theSlot)
            throws IOException, ClassNotFoundException {

        validateSlot(theSlot);

        final Path savePath = getSavePath(theSlot);

        if (!Files.isRegularFile(savePath)) {
            throw new IOException(
                    "No saved game found in slot " + theSlot);
        }

        try (ObjectInputStream input =
                     new ObjectInputStream(
                             new FileInputStream(savePath.toFile()))) {

            final Object loadedObject = input.readObject();

            if (!(loadedObject instanceof GameState)) {
                throw new IOException(
                        "Invalid save file in slot " + theSlot);
            }

            return (GameState) loadedObject;
        }
    }

    /**
     * Checks whether a save slot contains a saved game.
     * @param theSlot the selected save slot
     * @return true if a save file exists
     */
    public boolean isOccupied(final int theSlot) {

        validateSlot(theSlot);

        return Files.isRegularFile(getSavePath(theSlot));
    }

    /**
     * Returns information about all 10 save slots.
     * @return a list showing which slots are occupied
     */
    public List<String> listSlots() {

        final List<String> slots = new ArrayList<>();

        for (int slot = 1; slot <= SLOT_COUNT; slot++) {

            if (isOccupied(slot)) {
                slots.add("Slot " + slot + ": Saved Game");
            } else {
                slots.add("Slot " + slot + ": Empty");
            }
        }

        return slots;
    }

    /**
     * Checks whether the selected slot is valid.
     * @param theSlot the slot number to validate
     */
    private void validateSlot(final int theSlot) {

        if (theSlot < 1 || theSlot > SLOT_COUNT) {
            throw new IllegalArgumentException(
                    "Save slot must be between 1 and 10.");
        }
    }

    /**
     * Returns the file path for the selected save slot.
     * @param theSlot the selected slot number
     * @return path to the save file
     */
    private Path getSavePath(final int theSlot) {
        return mySaveDirectory.resolve("slot_" + theSlot + ".dat");
    }
}
