package net.sothatsit.royalurserver.management;

import net.sothatsit.royalurserver.game.SavedGame;

import javax.annotation.Nonnull;

/**
 * An entry in the game repository. This can take the form of either
 * an actual game, or a reservation for a game ID.
 */
public interface GameRepositoryEntry {

    boolean isGame();

    @Nonnull SavedGame getGame();

    boolean isReservation();

    @Nonnull
    GameReservation getReservation();

    /**
     * Create an entry for the game repository that corresponds to an actual game.
     * @param game The actual game.
     * @return An entry for the game repository corresponding to the given game.
     */
    static @Nonnull GameRepositoryEntry create(@Nonnull SavedGame game) {
        return new GameEntry(game);
    }

    /**
     * Create an entry for the game repository that corresponds to a game reservation.
     * @param reservation The game reservation.
     * @return An entry for the game repository corresponding to the given reservation.
     */
    static @Nonnull GameRepositoryEntry create(@Nonnull GameReservation reservation) {
        return new ReservationEntry(reservation);
    }

    /**
     * A game entry in the game repository.
     */
    class GameEntry implements GameRepositoryEntry {

        private final @Nonnull SavedGame game;

        public GameEntry(@Nonnull SavedGame game) {
            this.game = game;
        }

        @Override
        public boolean isGame() {
            return true;
        }

        @Override
        public @Nonnull SavedGame getGame() {
            return game;
        }

        @Override
        public boolean isReservation() {
            return false;
        }

        @Override
        public @Nonnull GameReservation getReservation() {
            throw new IllegalStateException("This entry contains a game, not a reservation");
        }
    }

    /**
     * A reservation entry in the game repository.
     */
    class ReservationEntry implements GameRepositoryEntry {

        private final @Nonnull GameReservation reservation;

        public ReservationEntry(@Nonnull GameReservation reservation) {
            this.reservation = reservation;
        }

        @Override
        public boolean isGame() {
            return false;
        }

        @Override
        public @Nonnull SavedGame getGame() {
            throw new IllegalStateException("This entry contains a reservation, not a game");
        }

        @Override
        public boolean isReservation() {
            return true;
        }

        @Override
        public @Nonnull GameReservation getReservation() {
            return reservation;
        }
    }
}
