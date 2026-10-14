package ChessResources.GameSaver;

import com.google.gson.JsonArray;

/** 64-bit values are written as hex strings: JSON numbers are doubles in most parsers and lose bits above 2^53. */
public final class JsonUtil {
    private JsonUtil() {}

    public static String hex(long v) { return Long.toHexString(v); }
    public static long unhex(String s) { return Long.parseUnsignedLong(s, 16); }

    public static JsonArray hexArray(long[] a) {
        JsonArray out = new JsonArray();
        for (long v : a) out.add(hex(v));
        return out;
    }

    public static long[] unhexArray(JsonArray a) {
        long[] out = new long[a.size()];
        for (int i = 0; i < out.length; i++) out[i] = unhex(a.get(i).getAsString());
        return out;
    }
}
