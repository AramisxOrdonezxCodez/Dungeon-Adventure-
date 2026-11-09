/*
 * TCSS 360 - Dungeon Adventure
 * Zachary Adams
 */
package model;

/**
 * A balanced monster with moderate damage and a fair chance to heal.
 *
 * @author Zachary Adams
 * @version 2026-11-08
 */
public class Skeleton extends Monster {

    /** Version identifier for serialized skeletons. */
    private static final long serialVersionUID = 1L;

    /** The stats shared by every skeleton. */
    private static final MonsterDefinition DEFINITION =
            new MonsterDefinition("Skeleton", "Skeleton", 100, 3, 0.8, 30, 50, 0.3, 30, 50);

    /**
     * Constructs a skeleton at full health.
     */
    public Skeleton() {
        super(DEFINITION);
    }
}
