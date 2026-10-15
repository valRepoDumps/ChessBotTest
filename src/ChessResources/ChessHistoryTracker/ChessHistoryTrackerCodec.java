package ChessResources.ChessHistoryTracker;

import ChessLogic.MinimalChessGame;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Function;

/**
 * Only the ordered list of snapshots is saved. tableOfPositions and threeFoldRepitionFlag are rebuilt
 * by replaying pushTurn(), so they can never disagree with the history.
 */
public final class ChessHistoryTrackerCodec {
    private ChessHistoryTrackerCodec() {}

    public static <G extends MinimalChessGame> JsonArray write(ChessHistoryTracker<G> t,
                                                               Function<G, JsonObject> snapshotWriter) {
        JsonArray a = new JsonArray();
        for (int i = 0; i < t.totalTurns(); i++) a.add(snapshotWriter.apply(t.getTurn(i)));
        return a;
    }

    /** t must be empty. */
    public static <G extends MinimalChessGame> void readInto(ChessHistoryTracker<G> t, JsonArray a,
                                                             Function<JsonObject, G> snapshotReader) {
        for (JsonElement e : a) t.pushTurn(snapshotReader.apply(e.getAsJsonObject()));
    }
}