package FrontEndConnection.ApiRequests.ChangeBoardStateRequests;

import ChessResources.Pieces.PieceData;
import FrontEndConnection.ApiRequests.ApiRequest;
import FrontEndConnection.FrontEndState;
import com.google.gson.JsonObject;

import java.io.IOException;

import static Common.JsonUtils.require;
import static FrontEndConnection.ApiRequests.ApiRequest.failure;
import static FrontEndConnection.ApiRequests.ApiRequest.updateBoardRequest;

public final class ComputerMoveRequest implements ApiRequest {
    @Override
    public JsonObject request(FrontEndState state, JsonObject req, String id) throws IOException {
        switch (require(req, "computer_side")) {
            case "b" -> state.adjustEvaluator(true, PieceData.BLACK);
            case "w" -> state.adjustEvaluator(true, PieceData.WHITE);
            default  -> { return failure(id, "No computer side selected"); }
        }
        return updateBoardRequest(state, id);   // unchanged board if it was not the computer's turn
    }
}