package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.Logging;
import net.sothatsit.royalurserver.network.Client;
import net.sothatsit.royalurserver.network.incoming.PacketIn;
import net.sothatsit.royalurserver.network.incoming.PacketInMove;
import net.sothatsit.royalurserver.network.incoming.PacketInRoll;
import net.sothatsit.royalurserver.network.outgoing.*;
import net.sothatsit.royalurserver.scheduler.Scheduler;
import net.sothatsit.royalurserver.util.Checks;
import net.sothatsit.royalurserver.util.Time;

import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Game {

    public static final SecureRandom RANDOM = new SecureRandom();
    private static final long GAME_TIMEOUT_MS = 60 * 1000;

    private final Scheduler scheduler;
    private final Logger logger;
    public final GameID id;

    public final Client lightClient;
    public final Client darkClient;

    private final PlayerState light;
    private final PlayerState dark;
    private final Board board;

    private GameState state;
    private Player currentPlayer;
    private DiceRoll roll;
    private List<Move> potentialMoves;

    public Game(GameID id, Client lightClient, Client darkClient) {
        Checks.ensureNonNull(lightClient, "lightClient");
        Checks.ensureNonNull(darkClient, "darkClient");

        this.id = id;
        this.scheduler = new Scheduler("game " + id, 100, TimeUnit.MILLISECONDS);
        this.scheduler.start();
        this.logger = Logging.getLogger("game " + id);

        this.lightClient = lightClient;
        this.darkClient = darkClient;

        this.light = new PlayerState(Player.LIGHT, "Light");
        this.dark = new PlayerState(Player.DARK, "Dark");
        this.board = new Board();

        this.state = GameState.ROLL;
        this.currentPlayer = Player.LIGHT;

        logger.info("starting game");

        scheduler.scheduleRepeating("game timeout", () -> {
            Time disconnectTime = Time.now();

            if(!lightClient.isConnected()) {
                disconnectTime = lightClient.getDisconnectTime();
            }

            if(!darkClient.isConnected() && disconnectTime.isAfter(darkClient.getDisconnectTime())) {
                disconnectTime = darkClient.getDisconnectTime();
            }

            if(disconnectTime.getMillisSince() > GAME_TIMEOUT_MS) {
                stop();
            }
        }, 5000, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduler.stop();
        state = GameState.DONE;
    }

    public GameState getState() {
        return state;
    }

    public GameID getID() {
        return id;
    }

    public boolean isWon() {
        return state == GameState.DONE && (light.isMaxScore() || dark.isMaxScore());
    }

    public boolean isInactive() {
        // TODO : Should check the last time they were connected to this game.
        //        They could have changed games and this won't pick that up.
        return lightClient.isTimedOut() || darkClient.isTimedOut();
    }

    public boolean isPlayer(Client client) {
        return client == lightClient || client == darkClient;
    }

    private Player getPlayer(Client client) {
        if(client == lightClient) return Player.LIGHT;
        if(client == darkClient) return Player.DARK;

        throw new IllegalArgumentException(client + " is not a player of this game");
    }

    private PlayerState getState(Player player) {
        return (player == Player.LIGHT ? light : dark);
    }

    private void broadcast(PacketOut packet) {
        lightClient.send(packet);
        darkClient.send(packet);
    }

    private void sendGamePacket(Client client) {
        Player player = getPlayer(client);
        PlayerState state = getState(player.getOtherPlayer());

        client.send(PacketOutGame.create(id, player, state.name));
    }

    private PacketOut createStatePacket() {
        if (state != GameState.ROLL) {
            return PacketOutState.createRolled(
                    light, dark, board, isWon(), currentPlayer, roll, potentialMoves.size() > 0
            );
        } else {
            return PacketOutState.createAwaitingRoll(
                    light, dark, board, isWon(), currentPlayer
            );
        }
    }

    public void onJoin(Client client) {
        Checks.ensureNonNull(client, "client");

        sendGamePacket(client);
        client.send(createStatePacket());
    }

    public void onDisconnect(Client client) {
        Checks.ensureNonNull(client, "client");
    }

    public void onTimeout(Client client) {
        Checks.ensureNonNull(client, "client");
    }

    private void onRoll(Client client) {
        Player player = getPlayer(client);
        PlayerState state = getState(player);

        if(this.state != GameState.ROLL) {
            client.error("not awaiting player to roll");
            throw new IllegalStateException("not awaiting player to roll");
        }

        if(player != currentPlayer) {
            client.error("tried to roll when not the current player");
            throw new IllegalStateException(client + " tried to roll when not the current player");
        }

        this.roll = DiceRoll.roll(RANDOM);
        this.potentialMoves = Move.getMoves(board, state, roll.getValue());
        this.state = GameState.MOVE;

        broadcast(createStatePacket());

        if(potentialMoves.size() == 0) {
            scheduler.scheduleIn("no available moves", () -> {
                broadcast(PacketOutMessage.create("No moves"));
            }, 2500, TimeUnit.MILLISECONDS);

            scheduler.scheduleIn("state after no available moves", () -> {
                Game.this.state = GameState.ROLL;
                Game.this.currentPlayer = currentPlayer.getOtherPlayer();

                broadcast(createStatePacket());
            }, 5000, TimeUnit.MILLISECONDS);
        }
    }

    private Move findMoveFrom(Location from) {
        Checks.ensureNonNull(from, "from");

        for(Move move : potentialMoves) {
            if(move.from.equals(from))
                return move;
        }

        return null;
    }

    private void onMove(Client client, PacketInMove packet) {
        Player player = getPlayer(client);
        Player otherPlayer = player.getOtherPlayer();

        PlayerState state = getState(player);
        PlayerState otherState = getState(otherPlayer);

        if(this.state != GameState.MOVE) {
            client.error("not awaiting player to move");
            throw new IllegalStateException("not awaiting player to move");
        }

        if(player != currentPlayer) {
            client.error("tried to move when not the current player");
            throw new IllegalStateException(client + " tried to move when not the current player");
        }

        Move move = findMoveFrom(packet.from);

        if(move == null) {
            for(Move m : potentialMoves) {
                System.out.println("potential move: " + m);
            }

            client.error("tried to make an illegal move");
            throw new IllegalStateException(client + " tried to make an illegal move");
        }

        performMove(state, otherState, move);
    }

    private void performMove(PlayerState state, PlayerState otherState, Move move) {
        Player tileOccupant = board.getOwner(move.to);

        if(move.from.isStart(state.player)) {
            state.useTile();
        }

        if(tileOccupant != null) {
            otherState.addTile();
        }

        board.clearOwner(move.from);

        if(!move.to.isEnd(state.player)) {
            board.setOwner(move.to, state.player);
        } else {
            board.clearOwner(move.to);
            state.addScore();
        }

        if(!move.to.isLotus()) {
            this.currentPlayer = state.player.getOtherPlayer();
        }

        // Win state
        if(state.isMaxScore()) {
            this.state = GameState.DONE;
            this.currentPlayer = state.player;
            this.logger.info("Winner winner chicken dinner " + currentPlayer);
        } else {
            this.state = GameState.ROLL;
            this.roll = null;
        }

        broadcast(PacketOutMove.create(move));
        broadcast(createStatePacket());
    }

    private void handlePacket(Client client, PacketIn packet) {
        try {
            switch (packet.type) {
                case ROLL:
                    PacketInRoll.read(packet);

                    onRoll(client);
                    break;
                case MOVE:
                    PacketInMove move = PacketInMove.read(packet);

                    onMove(client, move);
                    break;
                default:
                    client.error("Unexpected packet " + packet);
                    logger.warning("Unexpected packet " + packet + " from " + client);
                    break;
            }
        } catch (Exception exception) {
            // Try report the error to the client, before propagating it up the call chain
            try {
                client.error("The game server has hit an error");
            } catch (Exception reportException){
                String errorMessage = "Exception handling packet of type " + packet.type + " from client " + client.id;
                RuntimeException propagateError = new RuntimeException(errorMessage);
                propagateError.addSuppressed(reportException);
                throw propagateError;
            }
        }
    }

    public void onMessage(Client client, PacketIn packet) {
        logger.info(getPlayer(client) + " -> " + packet);
        scheduler.schedule("onMessage from " + client, () -> this.handlePacket(client, packet));
    }
}
