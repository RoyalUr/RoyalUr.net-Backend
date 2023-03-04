package net.sothatsit.royalurserver.management;

import net.royalur.Game;
import net.royalur.model.Move;
import net.royalur.model.Player;
import net.royalur.model.PlayerState;
import net.royalur.rules.simple.SimplePiece;
import net.sothatsit.royalurserver.Logging;
import net.sothatsit.royalurserver.game.GameID;
import net.sothatsit.royalurserver.game.RoyalUrNetDiceRoll;
import net.sothatsit.royalurserver.game.SavedGame;
import net.sothatsit.royalurserver.network.Client;
import net.sothatsit.royalurserver.network.incoming.PacketIn;
import net.sothatsit.royalurserver.network.incoming.PacketInGameMove;
import net.sothatsit.royalurserver.network.incoming.PacketInGameRoll;
import net.sothatsit.royalurserver.network.outgoing.*;
import net.sothatsit.royalurserver.util.Checks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * A game with connected clients.
 */
public class ManagedGame {

    public final SavedGame savedGame;
    public final Client lightClient;
    public final Client darkClient;
    public final List<Client> spectators;

    private final Logger logger;

    public ManagedGame(SavedGame savedGame, Client lightClient, Client darkClient, List<Client> spectators) {
        this.savedGame = savedGame;
        this.lightClient = lightClient;
        this.darkClient = darkClient;
        this.spectators = new ArrayList<>(spectators);

        this.logger = Logging.getGameLogger(savedGame.id);
    }

    public ManagedGame(SavedGame savedGame, Client lightClient, Client darkClient) {
        this(savedGame, lightClient, darkClient, Collections.emptyList());
    }

    public GameID getID() {
        return savedGame.id;
    }

    public boolean isPlayer(Client client) {
        return client == lightClient || client == darkClient;
    }

    public @Nullable Player getPlayer(Client client) {
        if (client == lightClient)
            return Player.LIGHT;
        if (client == darkClient)
            return Player.DARK;

        // Spectator.
        return null;
    }

    public @Nonnull PlayerState getPlayerState(Client client) {
        if (client == lightClient) {
            return savedGame.game.getLightPlayer();
        } else if (client == darkClient) {
            return savedGame.game.getDarkPlayer();
        }
        throw new IllegalStateException("The client is not a player in the game: " + client.getSessionID());
    }

    public @Nonnull PlayerState getPlayerState(Player player) {
        return switch (player) {
            case LIGHT -> savedGame.game.getLightPlayer();
            case DARK -> savedGame.game.getDarkPlayer();
            default -> throw new IllegalStateException("Unknown player: " + player);
        };
    }

    public boolean isInactive() {
        // TODO : Should check the last time they were connected to this game.
        //        They could have changed games and this won't pick that up.
        return lightClient.isTimedOut() && darkClient.isTimedOut();
    }

    private void sendGameMetadataPacket(Client client) {
        client.send(new PacketOutGameMetadata(
                getID(),
                getPlayer(client),
                Objects.requireNonNullElse(lightClient.getName(), "Light"),
                Objects.requireNonNullElse(darkClient.getName(), "Dark"),
                lightClient.isConnected(),
                darkClient.isConnected()
        ));
    }

    private PacketOutGameState createGameStatePacket() {
        return new PacketOutGameState(getID(), savedGame.game);
    }

    public void onJoin(@Nonnull Client client) {
        Checks.ensureNonNull(client, "client");
        sendGameMetadataPacket(client);
        client.send(createGameStatePacket());

        if (getPlayer(client) == null) {
            spectators.add(client);
        }
    }

    public void onReconnect(@Nonnull Client client) {
        onJoin(client);

        if (client == lightClient) {
            broadcast(new PacketOutGamePlayerStatus(getID(), Player.LIGHT, true));
        } else if (client == darkClient) {
            broadcast(new PacketOutGamePlayerStatus(getID(), Player.DARK, true));
        }
    }

    public void onDisconnect(@Nonnull Client client) {
        Checks.ensureNonNull(client, "client");

        if (client == lightClient) {
            broadcast(new PacketOutGamePlayerStatus(getID(), Player.LIGHT, false));
        } else if (client == darkClient) {
            broadcast(new PacketOutGamePlayerStatus(getID(), Player.DARK, false));
        } else {
            spectators.remove(client);
        }
    }

    private void onRollPacket(Client client, PacketInGameRoll packet) {
        Player player = getPlayer(client);
        if (player == null) {
            client.error("You are not a player in the game");
            throw new IllegalStateException("Spectator sent a roll packet");
        }

        Game<SimplePiece, PlayerState, RoyalUrNetDiceRoll> game = savedGame.game;
        if (!game.isWaitingForRoll()) {
            client.error("Unexpected dice roll");
            throw new IllegalStateException("Unexpected dice roll");
        }

        if (player != game.getTurnPlayer().player) {
            client.error("It is not your turn to roll the dice");
            throw new IllegalStateException(client + " tried to roll when it was not their turn");
        }

        // Roll the dice!
        game.rollDice();
        broadcast(createGameStatePacket());
    }

    private void onMovePacket(Client client, PacketInGameMove packet) {
        Player player = getPlayer(client);
        if (player == null) {
            client.error("You are not a player in the game");
            throw new IllegalStateException("Spectator sent a move packet");
        }

        Game<SimplePiece, PlayerState, RoyalUrNetDiceRoll> game = savedGame.game;
        if (!game.isWaitingForMove()) {
            client.error("Unexpected move");
            throw new IllegalStateException("Unexpected move");
        }

        if (player != game.getTurnPlayer().player) {
            client.error("It is not your turn to move");
            throw new IllegalStateException(client + " tried to move when it was not their turn");
        }

        // Find the move.
        List<Move<SimplePiece>> moves = game.findAvailableMoves();
        Move<SimplePiece> matchingMove = null;

        // TODO : Make this explicit in the protocol instead of this hacky method.
        if (packet.from == null) {
            for (Move<SimplePiece> move : moves) {
                if (move.isIntroducingPiece()) {
                    matchingMove = move;
                    break;
                }
            }
        } else {
            for (Move<SimplePiece> move : moves) {
                if (!move.isIntroducingPiece() && move.getSource().equals(packet.from)) {
                    matchingMove = move;
                    break;
                }
            }
        }
        if (matchingMove == null) {
            client.error("Illegal move");
            throw new IllegalStateException(client + " tried to make an illegal move");
        }

        // Perform the move.
        game.makeMove(matchingMove);

        // Update the clients.
        broadcast(new PacketOutGameMove(savedGame.id, matchingMove));
        broadcast(createGameStatePacket());
    }

    public void onPacket(Client client, PacketIn packet) {
        try {
            switch (packet.type) {
                case ROLL -> onRollPacket(client, (PacketInGameRoll) packet);
                case MOVE -> onMovePacket(client, (PacketInGameMove) packet);
                default -> {
                    client.error("Unexpected packet " + packet);
                    logger.warning("Unexpected packet " + packet + " from " + client);
                }
            }
        } catch (Exception exception) {
            // Try report the error to the client, before propagating it up the call chain
            try {
                client.error("The game server has hit an error");
            } catch (Exception reportException){
                String errorMessage = "Exception handling packet of type " + packet.type + " from " + client;
                RuntimeException propagateError = new RuntimeException(errorMessage);
                propagateError.addSuppressed(reportException);
                throw propagateError;
            }
        }
    }

    public void stop(@Nonnull String reason) {
        broadcast(new PacketOutGameEnd(savedGame.id, reason));
        logger.info("Stopping game due to: " + reason);
    }

    private void broadcast(@Nonnull PacketOut packet) {
        lightClient.trySend(packet);
        darkClient.trySend(packet);
        for (Client client : spectators) {
            client.trySend(packet);
        }
    }
}
