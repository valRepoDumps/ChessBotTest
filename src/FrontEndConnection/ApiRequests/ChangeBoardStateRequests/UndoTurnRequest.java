package FrontEndConnection.ApiRequests.ChangeBoardStateRequests;

import FrontEndConnection.ApiRequests.ApiRequest;
import FrontEndConnection.FrontEndState;
import com.google.gson.JsonObject;

import static Common.JsonUtils.require;
import static FrontEndConnection.ApiRequests.ApiRequest.failure;
import static FrontEndConnection.ApiRequests.ApiRequest.updateBoardRequest;

public final class UndoTurnRequest implements ApiRequest {
    @Override
    public JsonObject request(FrontEndState state, JsonObject req, String id) {
        String turnsUndo = require(req, "turns");
        int turnsToUndo;
        try {
            turnsToUndo = Integer.parseInt(turnsUndo);
        } catch (NumberFormatException e) {
            return failure(id, "Invalid number of turns to undo: " + turnsUndo);
        }

        try {
            state.getGame().undoTurn(turnsToUndo);
        } catch (Exception e) {
            return failure(id, e.getMessage());
        }
        return updateBoardRequest(state, id);
    }
}