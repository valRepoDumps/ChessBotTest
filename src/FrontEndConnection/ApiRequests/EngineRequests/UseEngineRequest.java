package FrontEndConnection.ApiRequests.EngineRequests;

import ChessLogic.Evaluation.Evaluation;
import Engine.ForeignEngine;
import FrontEndConnection.ApiRequests.ApiRequest;
import FrontEndConnection.FrontEndState;
import com.google.gson.JsonObject;

import static Common.JsonUtils.require;
import static FrontEndConnection.ApiRequests.ApiRequest.failure;
import static FrontEndConnection.ApiRequests.ApiRequest.updateBoardRequest;

public final class UseEngineRequest implements ApiRequest {
    @Override
    public JsonObject request(FrontEndState state, JsonObject req, String id) {
        JsonObject engineJson = req.getAsJsonObject("engine");
        boolean builtin = engineJson.has("builtin") && engineJson.get("builtin").getAsBoolean();
        Evaluation evaluation = state.getEvaluation();

        try {
            if (builtin) evaluation.useDefaultEngine();
            else evaluation.addNewEngine(new ForeignEngine(require(engineJson, "path")));
            evaluation.evaluateGame();
        } catch (Exception e) {
            return failure(id, e.getMessage());
        }

        return updateBoardRequest(state, id);
    }
}