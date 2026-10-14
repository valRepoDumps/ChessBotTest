package ChessLogic.Configurations;

import com.google.gson.JsonObject;

public final class ConfigurationsCodec {
    private ConfigurationsCodec() {}

    public static JsonObject write(Configurations c) {
        JsonObject o = new JsonObject();
        o.addProperty("enableBoardGraphic", c.isEnableBoardGraphic());
        o.addProperty("debugMode", c.isDebugMode());
        o.addProperty("allowGameEnd", c.isAllowGameEnd());
        return o;
    }

    public static Configurations read(JsonObject o) {
        return new Configurations(o.get("enableBoardGraphic").getAsBoolean(),
                o.get("debugMode").getAsBoolean(),
                o.get("allowGameEnd").getAsBoolean());
    }
}
