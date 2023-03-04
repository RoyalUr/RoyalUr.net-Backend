package net.sothatsit.royalurserver;

import net.royalur.model.identity.NamedPlayer;

import javax.annotation.Nonnull;

/**
 * An identity of someone on RoyalUr.net.
 */
public class RoyalUrNetIdentity extends NamedPlayer {

    private final @Nonnull String id;

    /**
     * @param name The name of the player.
     */
    public RoyalUrNetIdentity(@Nonnull String id, @Nonnull String name) {
        super(name);
        this.id = id;
    }

    public String getID() {
        return id;
    }
}
