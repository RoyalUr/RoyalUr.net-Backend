package net.sothatsit.royalurserver.game;

/**
 * An interface that allows other classes to listen to game events.
 */
public interface GameListener {

    void onGameWin(Game game);
}
