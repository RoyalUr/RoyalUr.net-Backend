package net.sothatsit.royalurserver.discord;

import net.sothatsit.royalurserver.RoyalUrNetIdentity;

/**
 * The Discord bot gets its own identity.
 */
public class DiscordBotIdentity extends RoyalUrNetIdentity {

    public static final String ID = "discord-bot";
    public static final String NAME = "Discord Bot";

    public DiscordBotIdentity() {
        super(ID, NAME);
    }
}
