package net.sothatsit.royalurserver.game;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameIDTest {

    @Test
    public void testCycle() {
        Random random = new Random(567);
        for (int index = 0; index < 100; ++index) {
            GameID gameID = GameID.random(random);
            GameID parsedGameID = GameID.fromString(gameID.toString());
            assertEquals(gameID, parsedGameID);
        }
    }
}
