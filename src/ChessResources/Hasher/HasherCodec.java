package ChessResources.Hasher;

import ChessLogic.MinimalChessGame;
import ChessResources.GameSaver.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class HasherCodec {
    private HasherCodec() {}

    public static JsonArray writeHash(HashContainer h) {
        return JsonUtil.hexArray(h.getHash());
    }

    public static HashContainer readHash(JsonArray a) {
        return new HashContainer(JsonUtil.unhexArray(a));
    }

    /** Saves only the two seeds: ZobristHasher rebuilds identical tables from them.[cite: 1] */
    public static JsonObject writeGenerator(HashGenerator g) {
        JsonObject o = new JsonObject();
        o.addProperty("seed1", JsonUtil.hex(g.getSeed1()));
        o.addProperty("seed2", JsonUtil.hex(g.getSeed2()));
        return o;
    }

    /** owner is the live game the hashes belong to (HashGenerator keeps a back-reference to it).[cite: 1] */
    public static HashGenerator readGenerator(JsonObject o, MinimalChessGame owner) {
        return new HashGenerator(owner,
                JsonUtil.unhex(o.get("seed1").getAsString()),
                JsonUtil.unhex(o.get("seed2").getAsString()));
    }
}