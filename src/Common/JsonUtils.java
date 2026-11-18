package Common;

import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.ChessMove;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.util.List;

public class JsonUtils {
    public static String str(JsonObject o, String field) {
        return (o.has(field) && !o.get(field).isJsonNull()) ? o.get(field).getAsString() : null;
    }

    public static String require(JsonObject o, String field) {
        String v = str(o, field);
        if (v == null) throw new IllegalArgumentException("Missing field: " + field);
        return v;
    }

    public static JsonArray movesToJsonArray(List<ChessMove> moves) {
        JsonArray arr = new JsonArray();
        for (ChessMove move : moves) {
            JsonObject m = new JsonObject();
            m.addProperty("from", ChessBoard.convertSpaceIdToSquareNotation(move.getSpaceIdToMove()));
            m.addProperty("to", ChessBoard.convertSpaceIdToSquareNotation(move.getSpaceIdArriveAt()));

            int captureId = move.getSpaceIdCaptureAt();
            if (captureId < 0 || captureId > 63) m.add("captured", JsonNull.INSTANCE);
            else m.addProperty("captured", ChessBoard.convertSpaceIdToSquareNotation(captureId));
            arr.add(m);
        }
        return arr;
    }
}
