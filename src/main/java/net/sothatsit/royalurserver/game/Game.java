package net.sothatsit.royalurserver.game;

import net.sothatsit.royalurserver.Main;
import net.sothatsit.royalurserver.network.Client;
import net.sothatsit.royalurserver.network.incoming.PacketIn;
import net.sothatsit.royalurserver.network.incoming.PacketInMove;
import net.sothatsit.royalurserver.network.incoming.PacketInRoll;
import net.sothatsit.royalurserver.network.outgoing.*;
import net.sothatsit.royalurserver.scheduler.Scheduler;
import net.sothatsit.royalurserver.util.Checks;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Game {

    private final Scheduler scheduler;
    private final Logger logger;
    public final UUID id;

    public final Client lightClient;
    public final Client darkClient;

    private final PlayerState light;
    private final PlayerState dark;
    private final Board board;

    private GameState state;
    private Player currentPlayer;
    private DiceRoll roll;
    private List<Move> potentialMoves;

    public Game(Client lightClient, Client darkClient) {
        Checks.ensureNonNull(lightClient, "lightClient");
        Checks.ensureNonNull(darkClient, "darkClient");

        this.id = UUID.randomUUID();
        this.scheduler = new Scheduler("game " + getShortId(), 100, TimeUnit.MILLISECONDS);
        this.scheduler.start();
        this.logger = Main.getLogger("game " + getShortId());

        this.lightClient = lightClient;
        this.darkClient = darkClient;

        this.light = new PlayerState(Player.LIGHT, "Light");
        this.dark = new PlayerState(Player.DARK, "Dark");
        this.board = new Board();

        this.state = GameState.ROLL;
        this.currentPlayer = Player.LIGHT;

        logger.info("starting game");

        sendGamePacket(lightClient);
        sendGamePacket(darkClient);

        broadcast(createStatePacket());
    }

    public void stop() {
        scheduler.stop();
    }

    public String getShortId() {
        return id.toString().substring(0, 8);
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
        PlayerState state = getState(Player.getOtherPlayer(player));

        client.send(PacketOutGame.create(player, state.name));
    }

    private PacketOut createStatePacket() {
        if (state != GameState.ROLL) {
            return PacketOutState.createRolled(light, dark, board, currentPlayer, roll, potentialMoves.size() > 0);
        } else {
            return PacketOutState.createAwaitingRoll(light, dark, board, currentPlayer);
        }
    }

    public void onReconnect(Client client) {
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

        this.roll = DiceRoll.roll();
        this.potentialMoves = Move.getMoves(board, state, roll.getValue());
        this.state = GameState.MOVE;

        broadcast(createStatePacket());

        if(potentialMoves.size() == 0) {
            scheduler.scheduleIn("no available moves", () -> {
                broadcast(PacketOutMessage.create("No\r\nmoves"));
            }, 2500, TimeUnit.MILLISECONDS);

            scheduler.scheduleIn("state after no available moves", () -> {
                Game.this.state = GameState.ROLL;
                Game.this.currentPlayer = Player.getOtherPlayer(currentPlayer);

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
        Player otherPlayer = Player.getOtherPlayer(player);

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

        this.state = GameState.ROLL;
        this.roll = null;

        if(!move.to.isLotus()) {
            this.currentPlayer = Player.getOtherPlayer(state.player);
        }

        // Win state
        if(state.isMaxScore()) {
            this.state = GameState.DONE;
            this.currentPlayer = state.player;
            this.logger.info("Winner winner chicken dinner " + currentPlayer);

            scheduler.scheduleIn("winner", () -> {
                broadcast(PacketOutWin.create(state.player));
            }, 500, TimeUnit.MILLISECONDS);
        }

        broadcast(PacketOutMove.create(move));
        broadcast(createStatePacket());
    }

    public void onMessage(Client client, PacketIn packet) {
        logger.info(getPlayer(client) + " -> " + packet);

        scheduler.schedule("onMessage from " + client, () -> {
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
        });
    }
}
