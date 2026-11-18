package FrontEndConnection.ApiRequests.EngineRequests;

import Engine.EngineDetector;
import FrontEndConnection.ApiRequests.ApiRequest;
import FrontEndConnection.FrontEndState;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import static FrontEndConnection.ApiRequests.ApiRequest.base;

public final class ListEnginesRequest implements ApiRequest {
    @Override
    public JsonObject request(FrontEndState state, JsonObject req, String id) {
        JsonArray engines = new JsonArray();

        // the built-in engine is always available and is the default one
        JsonObject builtIn = new JsonObject();
        builtIn.addProperty("name", "MyEngine");
        builtIn.addProperty("author", "Built-in");
        builtIn.addProperty("builtin", true);
        engines.add(builtIn);

        for (EngineDetector.EngineInfo engineInfo : EngineDetector.detect(EngineDetector.ENGINES_FOLDER)) {
            engines.add(engineInfo.toJson());
        }
        JsonObject o = base("list_engines_response", id);
        o.addProperty("ok", true);
        o.add("engines", engines);
        return o;
    }
}