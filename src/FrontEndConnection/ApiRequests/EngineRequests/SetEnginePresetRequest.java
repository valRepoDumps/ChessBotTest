package FrontEndConnection.ApiRequests.EngineRequests;

import Engine.Engine;
import Engine.EngineProperty.CompositeEngineProperty;
import FrontEndConnection.ApiRequests.ApiRequest;
import FrontEndConnection.FrontEndState;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static Common.JsonUtils.require;
import static FrontEndConnection.ApiRequests.ApiRequest.base;

/** {"type":"set_engine_preset","preset":"elo","values":[true,1500]} - one value per option, in order */
public final class SetEnginePresetRequest implements ApiRequest {
    @Override
    public JsonObject request(FrontEndState state, JsonObject req, String id) throws IOException {
        Engine engine = state.currentEngine();
        String label = require(req, "preset");

        CompositeEngineProperty preset = CompositeEngineProperty.PRESETS.stream()
                .filter(p -> p.getLabel().equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown preset: " + label));

        List<String> values = new ArrayList<>();
        for (var v : req.getAsJsonArray("values")) values.add(v.getAsString());
        preset.applyText(engine, values);

        JsonObject o = base("set_engine_preset_response", id);
        o.addProperty("ok", true);
        o.add("preset", preset.toJson(engine));
        return o;
    }
}