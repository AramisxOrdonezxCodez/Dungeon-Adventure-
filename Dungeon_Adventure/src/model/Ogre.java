/*
 * TCSS 360 - Dungeon Adventure
 * Zachary Adams
 */
package model;

/**
 * A slow, heavily armored monster that hits hard but rarely heals.
 *
 * @author Zachary Adams
 * @version 2026-11-08
 */
public class Ogre extends Monster {

    /** Version identifier for serialized ogres. */
    private static final long serialVersionUID = 1L;

    /** The stats shared by every ogre. */
    private static final MonsterDefinition DEFINITION =
            new MonsterDefinition("Ogre", "Ogre", 200, 2, 0.6, 30, 60, 0.1, 30, 60);

    /**
     * Constructs an ogre at full health.
     */
    public Ogre() {
        super(DEFINITION);
    }
}
