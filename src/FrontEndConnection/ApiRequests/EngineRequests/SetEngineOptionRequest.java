package FrontEndConnection.ApiRequests.EngineRequests;

import Engine.Engine;
import FrontEndConnection.ApiRequests.ApiRequest;
import FrontEndConnection.FrontEndState;
import com.google.gson.JsonObject;

import java.io.IOException;

import static Common.JsonUtils.require;
import static Common.JsonUtils.str;
import static FrontEndConnection.ApiRequests.ApiRequest.base;

/** {"type":"set_engine_option","property":"Hash","value":"256"}  (value omitted for buttons) */
public final class SetEngineOptionRequest implements ApiRequest {
    @Override
    public JsonObject request(FrontEndState state, JsonObject req, String id) throws IOException {
        Engine engine = state.currentEngine();
        String name = require(req, "property");
        engine.setOption(name, str(req, "value"));   // parsed + validated by the property

        JsonObject o = base("set_engine_option_response", id);
        o.addProperty("ok", true);
        o.add("property", engine.getTypedProperty(name).toJson());
        return o;
    }
}