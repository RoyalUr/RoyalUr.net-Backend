package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.Logging;
import net.sothatsit.royalurserver.network.Client;
import net.sothatsit.royalurserver.network.incoming.PacketIn;
import net.sothatsit.royalurserver.network.incoming.PacketInMove;
import net.sothatsit.royalurserver.network.outgoing.*;
import net.sothatsit.royalurserver.scheduler.Scheduler;
import net.sothatsit.royalurserver.util.Checks;
import net.sothatsit.royalurserver.util.Time;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Game {

    public static final SecureRandom RANDOM = new SecureRandom();
    private static final long GAME_TIMEOUT_MS = 5 * 60 * 1000;

    private final Scheduler scheduler;
    private final Logger logger;
    public final GameID id;

    public final Client lightClient;
    public final Client darkClient;
    public final Set<Client> spectators;

    public final PlayerState light;
    public final PlayerState dark;
    public final Board board;

    private GameState state;
    private Player currentPlayer;
    private DiceRoll roll;
    private List<Move> potentialMoves;

    private final List<GameListener> listeners = new ArrayList<>();

    public Game(GameID id, Client lightClient, Client darkClient) {
        Checks.ensureNonNull(lightClient, "lightClient");
        Checks.ensureNonNull(darkClient, "darkClient");

        this.id = id;
        this.scheduler = new Scheduler("game " + id, 100, TimeUnit.MILLISECONDS);
        this.scheduler.start();
        this.logger = Logging.getLogger("game " + id);

        this.lightClient = lightClient;
        this.darkClient = darkClient;
        this.spectators = Collections.newSetFromMap(new IdentityHashMap<>());

        this.light = new PlayerState(Player.LIGHT, lightClient.getName());
        this.dark = new PlayerState(Player.DARK, darkClient.getName());
        this.board = new Board();

        this.state = GameState.ROLL;
        this.currentPlayer = Player.LIGHT;

        logger.info("starting game");

        scheduler.scheduleRepeating("game timeout", () -> {
            if (lightClient.isConnected() || darkClient.isConnected())
                return;

            Time disconnectTime = Time.latest(
                    lightClient.getDisconnectTime(),
                    darkClient.getDisconnectTime()
            );
            if(disconnectTime.getMillisSince() > GAME_TIMEOUT_MS) {
                stop("Opponent left the game");
            }
        }, 5000, TimeUnit.MILLISECONDS);
    }

    public void stop(String reason) {
        broadcast(new PacketOutGameEnd(reason));
        scheduler.stop();
        state = GameState.DONE;
        logger.info("stopping game due to: " + reason);
    }

    public void addGameListener(GameListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeGameListener(GameListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
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

    public PlayerState getWinner() {
        if (!isWon())
            throw new IllegalStateException("Game is not won");
        return getState(currentPlayer);
    }

    public PlayerState getLoser() {
        if (!isWon())
            throw new IllegalStateException("Game is not won");
        return getState(currentPlayer.getOtherPlayer());
    }

    /**
     * Measures the total advancement of all pieces of {@code player}.
     * @param player The player to measure the total advancement of.
     * @return The total advancement of all pieces.
     */
    public int measureAdvancementMetric(Player player) {
        PlayerState playerState = getState(player);
        List<Location> path = Location.getPath(player);

        int scoredPieceAdvancement = path.size() - 1;
        int advancement = scoredPieceAdvancement * playerState.getScore();
        for (int index = 1; index < path.size() - 1; ++index) {
            Location location = path.get(index);
            if (board.getOwner(location) == player) {
                advancement += index;
            }
        }
        return advancement;
    }

    public boolean isInactive() {
        // TODO : Should check the last time they were connected to this game.
        //        They could have changed games and this won't pick that up.
        return lightClient.isTimedOut() && darkClient.isTimedOut();
    }

    public boolean isPlayer(Client client) {
        return client == lightClient || client == darkClient;
    }

    private Player getPlayer(Client client) {
        if (client == lightClient)
            return Player.LIGHT;
        if (client == darkClient)
            return Player.DARK;
        return Player.SPECTATOR;
    }

    private Client getClient(Player player) {
        switch (player) {
            case DARK: return darkClient;
            case LIGHT: return lightClient;
            default: throw new IllegalArgumentException("Unknown player " + player);
        }
    }

    private PlayerState getState(Player player) {
        return (player == Player.LIGHT ? light : dark);
    }

    private void broadcast(PacketOut packet) {
        lightClient.trySend(packet);
        darkClient.trySend(packet);
        for (Client client : spectators) {
            client.trySend(packet);
        }
    }

    private void sendGamePacket(Client client) {
        client.send(new PacketOutGame(
                id,
                getPlayer(client),
                light.name,
                dark.name,
                lightClient.isConnected(),
                darkClient.isConnected()
        ));
    }

    private PacketOutState createStatePacket() {
        if (state != GameState.ROLL) {
            return new PacketOutState(light, dark, board, isWon(), currentPlayer, roll, potentialMoves.size() > 0);
        } else {
            return new PacketOutState(light, dark, board, isWon(), currentPlayer);
        }
    }

    public void onJoin(Client client) {
        Checks.ensureNonNull(client, "client");
        sendGamePacket(client);
        client.send(createStatePacket());

        if (getPlayer(client) == Player.SPECTATOR) {
            spectators.add(client);
        }
    }

    public void onReconnect(Client client) {
        this.onJoin(client);

        if (client == lightClient) {
            broadcast(new PacketOutPlayerStatus(Player.LIGHT, true));
        } else if (client == darkClient) {
            broadcast(new PacketOutPlayerStatus(Player.DARK, true));
        }
    }

    public void onDisconnect(Client client) {
        Checks.ensureNonNull(client, "client");

        if (client == lightClient) {
            broadcast(new PacketOutPlayerStatus(Player.LIGHT, false));
        } else if (client == darkClient) {
            broadcast(new PacketOutPlayerStatus(Player.DARK, false));
        } else {
            spectators.remove(client);
        }
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
                String reason = (roll.getValue() == 0 ? "Rolled a zero" : "All moves are blocked");
                broadcast(new PacketOutMessage("No moves", reason));
            }, 1500, TimeUnit.MILLISECONDS);

            scheduler.scheduleIn("state after no available moves", () -> {
                Game.this.state = GameState.ROLL;
                Game.this.currentPlayer = currentPlayer.getOtherPlayer();

                broadcast(createStatePacket());
            }, 4000, TimeUnit.MILLISECONDS);
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

        if(!move.to.isRosette()) {
            this.currentPlayer = state.player.getOtherPlayer();
        }

        // Win state
        if(state.isMaxScore()) {
            this.state = GameState.DONE;
            this.currentPlayer = state.player;
            this.logger.info("Winner winner chicken dinner " + currentPlayer);
            reportGameWin();
        } else {
            this.state = GameState.ROLL;
            this.roll = null;
        }

        broadcast(new PacketOutMove(move));
        broadcast(createStatePacket());
    }

    private void reportGameWin() {
        for (GameListener listener : listeners) {
            try {
                listener.onGameWin(this);
            } catch (Exception e) {
                new RuntimeException("Error in GameListener " + listener, e).printStackTrace();
            }
        }
    }

    private void handlePacket(Client client, PacketIn packet) {
        try {
            switch (packet.type) {
                case ROLL:
                    onRoll(client);
                    break;
                case MOVE:
                    onMove(client, (PacketInMove) packet);
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
        scheduler.schedule("onMessage from " + client, () -> this.handlePacket(client, packet));
    }
}
