package FrontEndConnection.ApiRequests.EngineRequests;

import Engine.Engine;
import Engine.EngineProperty.CompositeEngineProperty;
import Engine.EngineProperty.EngineProperty;
import FrontEndConnection.ApiRequests.ApiRequest;
import FrontEndConnection.FrontEndState;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import static FrontEndConnection.ApiRequests.ApiRequest.base;

/** Always returns the presets; the raw option list only when the request has "advanced": true. */
public final class ListEngineOptionsRequest implements ApiRequest {
    @Override
    public JsonObject request(FrontEndState state, JsonObject req, String id) {
        Engine engine = state.currentEngine();

        JsonArray presets = new JsonArray();
        for (CompositeEngineProperty preset : CompositeEngineProperty.PRESETS) {
            presets.add(preset.toJson(engine));
        }

        JsonObject o = base("list_engine_options_response", id);
        o.addProperty("ok", true);
        o.add("preset_options", presets);

        if (req.has("advanced") && req.get("advanced").getAsBoolean()) {
            JsonArray advanced = new JsonArray();
            for (EngineProperty<?> option : engine.getOptions()) {
                advanced.add(option.toJson());
            }
            o.add("advanced_options", advanced);
        }
        return o;
    }
}