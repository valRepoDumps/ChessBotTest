package ChessLogic;

import ChessLogic.Configurations.ConfigurationsCodec;
import ChessLogic.Configurations.PropertiesStatsCodec;
import ChessResources.ChessBoard.ChessBoardCodec;
import ChessResources.ChessHistoryTracker.ChessHistoryTrackerCodec;
import ChessResources.GetMovesLogic.PossibleMoves;
import ChessResources.Hasher.HashContainer;
import ChessResources.Hasher.HashGenerator;
import ChessResources.Hasher.HasherCodec;
import com.google.gson.*;

/** In package ChessLogic so it can reach MinimalChessGame's package-private/protected fields. */
public final class MinimalChessGameCodec {
    public static final int VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private MinimalChessGameCodec() {}

    public static String toJson(MinimalChessGame g) { return GSON.toJson(write(g)); }

    public static MinimalChessGame fromJson(String json) {
        return read(JsonParser.parseString(json).getAsJsonObject());
    }

    // ---------- write ----------

    public static JsonObject write(MinimalChessGame g) {
        JsonObject root = writeState(g);
        root.addProperty("version", VERSION);
        root.add("hashGenerator", HasherCodec.writeGenerator(g.hashGenerator));
        root.add("history",
                ChessHistoryTrackerCodec.write(
                        g.chessHistoryTracker,
                        MinimalChessGameCodec::writeState));

        return root;
    }

    /** Exactly what setGame() copies, nothing derived. Used for the live game and for each history snapshot. */
    private static JsonObject writeState(MinimalChessGame g) {
        JsonObject o = new JsonObject();
        o.add("configurations", ConfigurationsCodec.write(g.getConfigurations()));
        o.add("board", ChessBoardCodec.write(g.getBoard()));
        o.add("properties", PropertiesStatsCodec.write(g.getGameProperties()));
        o.add("hash", HasherCodec.writeHash(g.hs));
        o.addProperty("currEnPassantTarget", g.getEnpassantTarget());
        return o;
    }

    // ---------- read ----------

    public static MinimalChessGame read(JsonObject root) {
        int v = root.get("version").getAsInt();
        if (v != VERSION) throw new IllegalStateException("Unsupported save version " + v);

        MinimalChessGame g = new MinimalChessGame();
        // one shared generator, back-referencing the live game, exactly like the constructor + setGame() do
        HashGenerator gen = HasherCodec.readGenerator(root.getAsJsonObject("hashGenerator"), g);
        fill(g, root, gen);

        // integrity check: recomputed hash must equal the saved one (catches wrong seeds / changed hashing code)
        HashContainer saved = g.hs;
        g.calculateHash();
        if (!saved.equals(g.hs))
            throw new IllegalStateException("Position hash mismatch: saved " + saved + ", recomputed " + g.hs);

        ChessHistoryTrackerCodec.readInto(g.chessHistoryTracker, root.getAsJsonArray("history"), s -> {
            MinimalChessGame snap = new MinimalChessGame();
            fill(snap, s, gen);
            return snap;
        });

        // mirror endGame(): a finished game has no legal moves exposed
        if (g.sideWon() != MinimalChessGame.INDETERMINATE && g.configurations.isAllowGameEnd())
            g.possibleMoves = new PossibleMoves(g);

        return g;
    }

    private static void fill(MinimalChessGame g, JsonObject o, HashGenerator gen) {
        g.configurations = ConfigurationsCodec.read(o.getAsJsonObject("configurations"));
        g.hashGenerator = gen;
        g.chessBoard = ChessBoardCodec.read(o.getAsJsonObject("board"));
        g.gameProperties = PropertiesStatsCodec.read(o.getAsJsonObject("properties"));
        g.hs = HasherCodec.readHash(o.getAsJsonArray("hash"));
        g.currEnPassantTarget = o.get("currEnPassantTarget").getAsInt();

        g.updateConfigurations();          // pushes debugMode down to the board
        g.possibleMoves = new PossibleMoves(g);
        g.generateBitMasks();              // derived board masks
        g.generatePossibleMoves();         // derived move list
        g.tryEndGame(); //dont need to save endGameCode, use tryEndGame to get it.
    }
}