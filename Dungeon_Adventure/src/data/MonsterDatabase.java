/*
 * TCSS 360 - Dungeon Adventure
 * Zachary Adams
 */
package data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores monster stats in a SQLite database and loads them as
 * MonsterDefinition objects.
 *
 * @author Zachary Adams
 * @version 2026-11-15
 */
public class MonsterDatabase {

    /** Location of the database file, relative to the working directory. */
    private static final String DB_PATH = "data/monsters.db";

    /** JDBC connection URL for the database file. */
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;

    /** SQL that creates the monster table on first run. */
    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS monsters (
                type           TEXT    PRIMARY KEY NOT NULL,
                name           TEXT    NOT NULL,
                hit_points     INTEGER NOT NULL CHECK (hit_points > 0),
                attack_speed   INTEGER NOT NULL CHECK (attack_speed > 0),
                chance_to_hit  REAL    NOT NULL CHECK (chance_to_hit BETWEEN 0 AND 1),
                min_damage     INTEGER NOT NULL CHECK (min_damage >= 0),
                max_damage     INTEGER NOT NULL CHECK (max_damage >= min_damage),
                chance_to_heal REAL    NOT NULL CHECK (chance_to_heal BETWEEN 0 AND 1),
                min_heal       INTEGER NOT NULL CHECK (min_heal >= 0),
                max_heal       INTEGER NOT NULL CHECK (max_heal >= min_heal)
            )""";

    /** SQL that reads every monster in the table. */
    private static final String SELECT_ALL_SQL = """
            SELECT type, name, hit_points, attack_speed, chance_to_hit,
                   min_damage, max_damage, chance_to_heal, min_heal, max_heal
            FROM monsters""";

    /** SQL that counts the rows in the monster table. */
    private static final String COUNT_SQL = "SELECT COUNT(*) FROM monsters";

    /** SQL that adds one monster; each ? is filled in before running. */
    private static final String INSERT_SQL = """
            INSERT INTO monsters (type, name, hit_points, attack_speed, chance_to_hit,
                                  min_damage, max_damage, chance_to_heal, min_heal, max_heal)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";

    /** Monsters written to a new, empty database. */
    private static final MonsterDefinition[] DEFAULT_MONSTERS = {
        new MonsterDefinition("Ogre", "Ogre", 200, 2, 0.6, 30, 60, 0.1, 30, 60),
        new MonsterDefinition("Gremlin", "Gremlin", 70, 5, 0.8, 15, 30, 0.4, 20, 40),
        new MonsterDefinition("Skeleton", "Skeleton", 100, 3, 0.8, 30, 50, 0.3, 30, 50)
    };

    /**
     * Opens the monster database, creating the file and table if needed
     * and filling an empty table with the default monsters.
     *
     * @throws IllegalStateException if the database cannot be set up
     */
    public MonsterDatabase() {
        createTable();
        if (isEmpty()) {
            insertDefaults();
        }
    }

    /**
     * Reads every monster in the database.
     *
     * @return a map from each monster type to its definition
     * @throws IllegalStateException if the monsters cannot be read
     */
    public Map<String, MonsterDefinition> loadDefinitions() {
        final Map<String, MonsterDefinition> definitions = new HashMap<>();
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(SELECT_ALL_SQL)) {
            while (rows.next()) {
                final MonsterDefinition definition = new MonsterDefinition(
                        rows.getString("type"),
                        rows.getString("name"),
                        rows.getInt("hit_points"),
                        rows.getInt("attack_speed"),
                        rows.getDouble("chance_to_hit"),
                        rows.getInt("min_damage"),
                        rows.getInt("max_damage"),
                        rows.getDouble("chance_to_heal"),
                        rows.getInt("min_heal"),
                        rows.getInt("max_heal"));
                definitions.put(definition.getType(), definition);
            }
        } catch (final SQLException e) {
            throw new IllegalStateException("Could not load the monsters.", e);
        }
        return definitions;
    }

    /**
     * Opens a new connection to the database file.
     * The caller is responsible for closing it.
     *
     * @return an open connection
     * @throws SQLException if the database cannot be opened
     */
    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Creates the folder for the database file and the monster table,
     * if they do not already exist.
     *
     * @throws IllegalStateException if the folder or table cannot be created
     */
    private static void createTable() {
        try {
            Files.createDirectories(Path.of(DB_PATH).getParent());
        } catch (final IOException e) {
            throw new IllegalStateException("Could not create the database folder.", e);
        }
        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_TABLE_SQL);
        } catch (final SQLException e) {
            throw new IllegalStateException("Could not create the monster table.", e);
        }
    }

    /**
     * Reports whether the monster table has no rows.
     *
     * @return true if the table is empty; false otherwise
     * @throws IllegalStateException if the table cannot be read
     */
    private static boolean isEmpty() {
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(COUNT_SQL)) {
            result.next();
            return result.getInt(1) == 0;
        } catch (final SQLException e) {
            throw new IllegalStateException("Could not count the monsters.", e);
        }
    }

    /**
     * Writes the default monsters to the table in a single transaction,
     * so either all of them are saved or none are.
     *
     * @throws IllegalStateException if the monsters cannot be saved
     */
    private static void insertDefaults() {
        try (Connection connection = connect();
             PreparedStatement insert = connection.prepareStatement(INSERT_SQL)) {
            connection.setAutoCommit(false);
            for (final MonsterDefinition monster : DEFAULT_MONSTERS) {
                insert.setString(1, monster.getType());
                insert.setString(2, monster.getName());
                insert.setInt(3, monster.getHitPoints());
                insert.setInt(4, monster.getAttackSpeed());
                insert.setDouble(5, monster.getChanceToHit());
                insert.setInt(6, monster.getMinDamage());
                insert.setInt(7, monster.getMaxDamage());
                insert.setDouble(8, monster.getChanceToHeal());
                insert.setInt(9, monster.getMinHeal());
                insert.setInt(10, monster.getMaxHeal());
                insert.addBatch();
            }
            insert.executeBatch();
            connection.commit();
        } catch (final SQLException e) {
            throw new IllegalStateException("Could not save the default monsters.", e);
        }
    }
}
