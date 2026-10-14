package ChessResources.ChessHistoryTracker.BoardStateChanges;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * BoardStateChange is an event, not state: nothing in ChessBoard stores it. You only need this
 * codec if you want a move journal. On load, the board re-emits "spawn" events itself (see ChessBoardCodec).
 */
public final class BoardStateChangeCodec {
    private BoardStateChangeCodec() {}

    public static JsonObject write(BoardStateChange c) {
        JsonObject o = new JsonObject();
        o.addProperty("piece", c.getPiece());
        o.addProperty("from", c.getSpaceId());
        o.addProperty("to", c.getSpaceIdArriveAt());
        o.addProperty("captured", c.getPieceCaptured());
        return o;
    }

    public static BoardStateChange read(JsonObject o) {
        return new BoardStateChange(o.get("piece").getAsShort(), o.get("from").getAsInt(),
                o.get("to").getAsInt(), o.get("captured").getAsShort());
    }

    public static JsonArray writeAll(List<BoardStateChange> list) {
        JsonArray a = new JsonArray();
        for (BoardStateChange c : list) a.add(write(c));
        return a;
    }

    public static List<BoardStateChange> readAll(JsonArray a) {
        List<BoardStateChange> out = new ArrayList<>();
        for (JsonElement e : a) out.add(read(e.getAsJsonObject()));
        return out;
    }
}