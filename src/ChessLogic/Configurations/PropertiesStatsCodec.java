package ChessLogic.Configurations;

import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GameSaver.JsonUtil;
import com.google.gson.JsonObject;

public final class PropertiesStatsCodec {
    private PropertiesStatsCodec() {}

    public static JsonObject write(PropertiesStats p) {
        JsonObject o = new JsonObject();
        o.addProperty("raw", JsonUtil.hex(p.toRaw()));
        // readable mirror for humans / diffs; ignored on read
        o.addProperty("sideToMove", p.getSideToMove() == ChessBoard.WHITE ? "white" : "black");
        o.addProperty("castlingFlag", p.getCastlingFlag());
        o.addProperty("enPassantTarget", p.getEnPassantTarget());
        o.addProperty("halfMoves", p.getHalfMovesSinceCaptureOrPawnMove());
        o.addProperty("totalMoves", p.getTotalMovesElapsed());
        return o;
    }

    public static PropertiesStats read(JsonObject o) {
        return new PropertiesStats(JsonUtil.unhex(o.get("raw").getAsString()));
    }
}
