package ChessResources.ChessHistoryTracker;

import ChessLogic.MinimalChessGame;
import ChessResources.ChessListener.StateChangeListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps a game as "original FEN + UCI moves", ready to send to an engine:
 *
 *     position fen <original fen> moves e2e4 e7e5 g1f3
 *
 * Attach it to a MinimalChessGame at any time. On attach it builds the original FEN and every
 * move played so far, then adds one move after each end-of-turn notification.
 *
 * Moves are worked out by comparing consecutive positions in game.chessHistoryTracker, so
 * nothing in MinimalChessGame needs to change. That also makes undo safe: undoTurn() does not
 * notify listeners, so every call sync()s against the game's history first and drops any
 * moves that were undone or replaced.
 *
 * Threading: use it from the thread that drives the game, then hand the resulting String to
 * your engine thread (toPositionCommand() returns an immutable snapshot).
 *
 * Usage:
 *     UciPositionTracker tracker = new UciPositionTracker(game);
 *     engine.send(tracker.toPositionCommand());
 *     engine.send("go movetime 1000");
 */
public class UciPositionTracker {

    /** One reference per position: snapshots.get(i) is the game state after i moves. */
    private final List<MinimalChessGame> snapshots = new ArrayList<>();
    private final List<String> moves = new ArrayList<>();
    private String originalFen;

    private MinimalChessGame game;

    // Method reference, so this works with whatever the single method of StateChangeListener is called.
    // Kept in a field because removeTurnEndListener() looks it up by reference.
    private final StateChangeListener<MinimalChessGame> turnEndListener = this::onTurnEnd;

    public UciPositionTracker() {}

    public UciPositionTracker(MinimalChessGame game) {
        attach(game);
    }

    //region ATTACH / DETACH

    /** Starts tracking this game. Builds the move list from the game's history right away. */
    public void attach(MinimalChessGame game) {
        if (game == null) throw new IllegalArgumentException("game cannot be null");
        detach();

        this.game = game;
        snapshots.clear();
        moves.clear();
        originalFen = null;

        game.addTurnEndListener(turnEndListener);   // also notifies immediately
        sync();                                     // harmless if the notification already did the work
    }

    /** Stops tracking. The moves collected so far stay readable but no longer update. */
    public void detach() {
        if (game != null) {
            game.removeTurnEndListener(turnEndListener);
            game = null;
        }
    }

    //endregion

    //region OUTPUT

    /** FEN of the position the game started from. */
    public String getOriginalFen() {
        sync();
        return originalFen;
    }

    /** UCI moves played since the original position, e.g. [e2e4, e7e5, e1g1, e7e8q]. */
    public List<String> getMoves() {
        sync();
        return List.copyOf(moves);
    }

    /** "e2e4 e7e5 g1f3" (empty string if no move has been played). */
    public String getMovesString() {
        sync();
        return String.join(" ", moves);
    }

    /** The full command for the engine, e.g. "position fen rnbq... w KQkq - 0 1 moves e2e4 e7e5". */
    public String toPositionCommand() {
        sync();
        if (originalFen == null) throw new IllegalStateException("Nothing tracked yet: call attach(game) first.");
        return moves.isEmpty()
                ? "position fen " + originalFen
                : "position fen " + originalFen + " moves " + String.join(" ", moves);
    }

    @Override
    public String toString() {
        return originalFen == null ? "(not attached)" : toPositionCommand();
    }

    //endregion

    //region SYNC

    private void onTurnEnd(MinimalChessGame ignored) {
        sync();
    }

    /**
     * Brings moves in line with game.chessHistoryTracker.
     * history[0] is the original position and every move adds one more entry,
     * so the number of moves played is totalTurns() - 1.
     */
    private void sync() {
        if (game == null) return;
        ChessHistoryTracker<MinimalChessGame> history = game.chessHistoryTracker;

        int played = history.totalTurns() - 1;
        if (played < 0) return;

        if (snapshots.isEmpty()) {
            MinimalChessGame start = history.getTurn(0);
            snapshots.add(start);
            originalFen = start.toFen();
        }

        // Keep only the moves whose position is still in the history. Comparing references is exact:
        // after "undo, then play something else" the replaced entries are different objects.
        int keep = 0;
        int limit = Math.min(moves.size(), played);
        while (keep < limit && history.getTurn(keep + 1) == snapshots.get(keep + 1)) keep++;
        while (moves.size() > keep) {
            moves.removeLast();
            snapshots.removeLast();
        }

        // Add every move we haven't recorded: all of them on attach, one per turn afterwards.
        for (int i = keep; i < played; i++) {
            MinimalChessGame after = history.getTurn(i + 1);
            moves.add(uciMoveBetween(history.getTurn(i).toFen(), after.toFen()));
            snapshots.add(after);
        }
    }

    //endregion

    //region MOVE FROM TWO POSITIONS

    /**
     * Works out the UCI move that turns one position into the next, using only the FEN text.
     *
     * Looks at the squares that changed and keeps the ones holding the mover's pieces:
     *   - normal move / capture / en passant: one square emptied, one square filled
     *   - promotion: the pawn's square emptied, the new piece's square filled
     *   - castling: two squares emptied and two filled (king and rook), so the king is chosen
     */
    static String uciMoveBetween(String fenBefore, String fenAfter) {
        String[] beforeFields = fenBefore.trim().split("\\s+");
        char[] before = expandRanks(beforeFields[0]);
        char[] after = expandRanks(fenAfter.trim().split("\\s+")[0]);
        boolean whiteMoved = beforeFields[1].equals("w");

        int from = -1;
        int to = -1;
        for (int sq = 0; sq < 64; sq++) {
            if (before[sq] == after[sq]) continue;
            if (isSide(before[sq], whiteMoved) && (from < 0 || isKing(before[sq]))) from = sq;
            if (isSide(after[sq], whiteMoved) && (to < 0 || isKing(after[sq]))) to = sq;
        }
        if (from < 0 || to < 0) {
            throw new IllegalStateException(
                    "Could not work out the move between \"" + fenBefore + "\" and \"" + fenAfter + "\"");
        }

        StringBuilder uci = new StringBuilder(squareName(from)).append(squareName(to));
        char moved = before[from];
        if ((moved == 'P' || moved == 'p') && after[to] != moved) {
            uci.append(Character.toLowerCase(after[to]));   // promotion piece, always lowercase in UCI
        }
        return uci.toString();
    }

    /** FEN piece placement to 64 chars, a8 first. '.' means empty. */
    private static char[] expandRanks(String placement) {
        char[] squares = new char[64];
        int i = 0;
        for (char c : placement.toCharArray()) {
            if (c == '/') continue;
            if (Character.isDigit(c)) {
                for (int n = c - '0'; n > 0; n--) squares[i++] = '.';
            } else {
                squares[i++] = c;
            }
        }
        return squares;
    }

    private static boolean isSide(char piece, boolean white) {
        return piece != '.' && Character.isUpperCase(piece) == white;
    }

    private static boolean isKing(char piece) {
        return piece == 'K' || piece == 'k';
    }

    private static String squareName(int index) {
        return "" + (char) ('a' + index % 8) + (char) ('8' - index / 8);
    }

    //endregion
}