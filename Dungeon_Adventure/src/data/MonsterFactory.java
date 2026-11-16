/*
 * TCSS 360 - Dungeon Adventure
 * Zachary Adams
 */
package data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import model.Gremlin;
import model.Monster;
import model.Ogre;
import model.Skeleton;

/**
 * Creates monsters using the definitions stored in the monster database.
 * Types with their own class become that class; any other type in the
 * database becomes a plain Monster.
 *
 * @author Zachary Adams
 * @version 2026-11-15
 */
public class MonsterFactory {

    /** Random number generator used to pick random monsters. */
    private static final Random RANDOM = new Random();

    /** Monster definitions loaded from the database, keyed by type. */
    private final Map<String, MonsterDefinition> myDefinitions;

    /**
     * Constructs a factory, loading every monster definition from the database once.
     *
     * @param theDatabase the database to load monsters from
     * @throws IllegalArgumentException if the database is null
     * @throws IllegalStateException if the database has no monsters
     */
    public MonsterFactory(final MonsterDatabase theDatabase) {
        if (theDatabase == null) {
            throw new IllegalArgumentException("Database must not be null.");
        }
        myDefinitions = Map.copyOf(theDatabase.loadDefinitions());
        if (myDefinitions.isEmpty()) {
            throw new IllegalStateException("The monster database has no monsters.");
        }
    }

    /**
     * Creates a new monster of the given type at full health.
     *
     * @param theType the kind of monster, such as "Ogre"
     * @return a new monster of that type
     * @throws IllegalArgumentException if the type is not in the database
     */
    public Monster createMonster(final String theType) {
        final MonsterDefinition definition = myDefinitions.get(theType);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown monster type: " + theType);
        }
        return switch (definition.getType()) {
            case "Ogre" -> new Ogre(definition);
            case "Gremlin" -> new Gremlin(definition);
            case "Skeleton" -> new Skeleton(definition);
            default -> new Monster(definition);
        };
    }

    /**
     * Creates a new monster of a random type, with each type in the
     * database equally likely.
     *
     * @return a new monster of a random type
     */
    public Monster createRandomMonster() {
        final List<String> types = new ArrayList<>(myDefinitions.keySet());
        return createMonster(types.get(RANDOM.nextInt(types.size())));
    }
}
