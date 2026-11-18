package FrontEndConnection.ApiRequests.MiscRequests;

import ChessLogic.Debug.DebugMode;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.ChessMove;
import Common.JsonUtils;
import FrontEndConnection.ApiRequests.ApiRequest;
import FrontEndConnection.FrontEndState;
import com.google.gson.JsonObject;

import java.util.List;

import static Common.JsonUtils.movesToJsonArray;
import static Common.JsonUtils.require;
import static FrontEndConnection.ApiRequests.ApiRequest.base;

public final class GetValidMovesRequest implements ApiRequest {
    @Override
    public JsonObject request(FrontEndState state, JsonObject req, String id) {
        int spaceId = ChessBoard.convertSquareNotationToSpaceId(require(req, "square"));
        DebugMode.debugPrint(state, "get valid moves request: from=" + spaceId);

        List<ChessMove> moves = state.getGame().getPossibleMoves().canMoveToFrom(spaceId);
        JsonObject o = base("get_valid_moves_response", id);
        o.add("moves", JsonUtils.movesToJsonArray(moves));
        return o;
    }
}