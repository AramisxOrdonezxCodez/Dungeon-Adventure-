/*
 * TCSS 360 - Dungeon Adventure
 * Zachary Adams
 */
package model;

/**
 * A small, fast monster that deals light damage but heals often.
 *
 * @author Zachary Adams
 * @version 2026-11-08
 */
public class Gremlin extends Monster {

    /** Version identifier for serialized gremlins. */
    private static final long serialVersionUID = 1L;

    /** The stats shared by every gremlin. */
    private static final MonsterDefinition DEFINITION =
            new MonsterDefinition("Gremlin", "Gremlin", 70, 5, 0.8, 15, 30, 0.4, 20, 40);

    /**
     * Constructs a gremlin at full health.
     */
    public Gremlin() {
        super(DEFINITION);
    }
}
