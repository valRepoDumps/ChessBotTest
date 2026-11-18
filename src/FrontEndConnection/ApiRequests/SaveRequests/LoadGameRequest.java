package FrontEndConnection.ApiRequests.SaveRequests;

import ChessLogic.MinimalChessGame;
import ChessResources.GameSaver.GameSaveManager;
import FrontEndConnection.ApiRequests.ApiRequest;
import FrontEndConnection.FrontEndState;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

import static Common.JsonUtils.require;
import static FrontEndConnection.ApiRequests.ApiRequest.describe;
import static FrontEndConnection.ApiRequests.ApiRequest.failure;
import static FrontEndConnection.ApiRequests.ApiRequest.updateBoardRequest;

public final class LoadGameRequest implements ApiRequest {
    @Override
    public JsonObject request(FrontEndState state, JsonObject req, String id) {
        String saveId = require(req, "save_id");
        GameSaveManager saves = state.getSaves();

        MinimalChessGame loaded;
        try {
            saves.readMeta(saveId);
            loaded = saves.load(saveId);   // throws before anything is swapped, so a bad save changes nothing
        } catch (NoSuchFileException e) {
            return failure(id, "Save not found: " + saveId);
        } catch (IOException e) {
            return failure(id, describe(e));
        }

        state.installGame(loaded);

        return updateBoardRequest(state, id, true);
    }
}