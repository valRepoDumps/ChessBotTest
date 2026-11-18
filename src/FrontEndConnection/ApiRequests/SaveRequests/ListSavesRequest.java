package FrontEndConnection.ApiRequests.SaveRequests;

import ChessResources.GameSaver.GameSaveManager;
import FrontEndConnection.ApiRequests.ApiRequest;
import FrontEndConnection.FrontEndState;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;

import static FrontEndConnection.ApiRequests.ApiRequest.base;
import static FrontEndConnection.ApiRequests.ApiRequest.describe;
import static FrontEndConnection.ApiRequests.ApiRequest.failure;

public final class ListSavesRequest implements ApiRequest {
    @Override
    public JsonObject request(FrontEndState state, JsonObject req, String id) {
        List<GameSaveManager.SaveMeta> metas;
        try {
            metas = state.getSaves().list();
        } catch (IOException e) {
            return failure(id, describe(e));
        }

        JsonArray arr = new JsonArray();
        for (GameSaveManager.SaveMeta m : metas) {
            arr.add(m.toJson());
        }
        JsonObject o = base("list_saves_response", id);
        o.addProperty("ok", true);
        o.add("saves", arr);
        return o;
    }
}