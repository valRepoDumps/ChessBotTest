package FrontEndConnection.ApiRequests.SaveRequests;

import ChessResources.GameSaver.GameSaveManager;
import FrontEndConnection.ApiRequests.ApiRequest;
import FrontEndConnection.FrontEndState;
import com.google.gson.JsonObject;

import java.io.IOException;

import static Common.JsonUtils.str;
import static FrontEndConnection.ApiRequests.ApiRequest.base;
import static FrontEndConnection.ApiRequests.ApiRequest.describe;
import static FrontEndConnection.ApiRequests.ApiRequest.failure;

public final class SaveGameRequest implements ApiRequest {
    @Override
    public JsonObject request(FrontEndState state, JsonObject req, String id) {
        String name = str(req, "name");
        if (name == null || name.isBlank()) name = "Untitled";

        GameSaveManager.SaveMeta meta;
        try {
            meta = state.getSaves().save(state.getGame(), state.nextSaveId(), name.trim());
        } catch (IOException e) {
            return failure(id, describe(e));
        }

        JsonObject o = base("save_game_response", id);
        o.addProperty("ok", true);
        o.addProperty("save_id", meta.saveId());
        o.addProperty("name", meta.saveName());
        o.addProperty("saved_at", meta.isoSeconds());
        return o;
    }
}