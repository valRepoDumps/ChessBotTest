package FrontEndConnection.ApiRequests.SaveRequests;

import FrontEndConnection.ApiRequests.ApiRequest;
import FrontEndConnection.FrontEndState;
import com.google.gson.JsonObject;

import static Common.JsonUtils.require;
import static FrontEndConnection.ApiRequests.ApiRequest.failure;

public final class DeleteSaveRequest implements ApiRequest {
    private final ListSavesRequest listSaves = new ListSavesRequest();

    @Override
    public JsonObject request(FrontEndState state, JsonObject req, String id) {
        String saveId = require(req, "save_id");
        try {
            state.getSaves().delete(saveId);
        } catch (Exception e) {
            return failure(id, e.getMessage());
        }

        return listSaves.request(state, req, id);
    }
}