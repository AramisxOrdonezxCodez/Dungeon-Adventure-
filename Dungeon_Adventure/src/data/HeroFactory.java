/*
 * TCSS 360 - Dungeon Adventure
 * Zachary Adams
 */
package data;

import model.Hero;
import model.HeroType;
import model.Priestess;
import model.Thief;
import model.Warrior;

/**
 * Creates heroes from the hero type the player selects.
 *
 * @author Zachary Adams
 * @version 2026-11-15
 */
public final class HeroFactory {

    /**
     * Prevents instantiation of this utility class.
     */
    private HeroFactory() {
        throw new IllegalStateException("HeroFactory should not be instantiated.");
    }

    /**
     * Creates a new hero of the given type at full health.
     *
     * @param theType the kind of hero to create
     * @param theName the display name of the hero
     * @return a new hero of the requested type
     * @throws IllegalArgumentException if the type is null
     */
    public static Hero createHero(final HeroType theType, final String theName) {
        if (theType == null) {
            throw new IllegalArgumentException("Hero type must not be null.");
        }
        return switch (theType) {
            case WARRIOR -> new Warrior(theName);
            case PRIESTESS -> new Priestess(theName);
            case THIEF -> new Thief(theName);
        };
    }
}
