package FrontEndConnection.ApiRequests.RenderRequests;

import FrontEndConnection.ApiRequests.ApiRequest;
import FrontEndConnection.FrontEndState;
import com.google.gson.JsonObject;

public class RenderPlayerRequest implements ApiRequest {
    @Override
    public JsonObject request(FrontEndState state, JsonObject req, String id) throws Exception{
        return new JsonObject();
    }
}
