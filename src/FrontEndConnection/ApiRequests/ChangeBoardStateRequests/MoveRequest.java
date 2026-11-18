package FrontEndConnection.ApiRequests.ChangeBoardStateRequests;

import ChessLogic.Debug.DebugMode;
import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.ChessMove;
import ChessResources.Pieces.PieceData;
import FrontEndConnection.ApiRequests.ApiRequest;
import FrontEndConnection.FrontEndState;
import com.google.gson.JsonObject;

import static Common.JsonUtils.require;
import static FrontEndConnection.ApiRequests.ApiRequest.failure;
import static FrontEndConnection.ApiRequests.ApiRequest.updateBoardRequest;

public final class MoveRequest implements ApiRequest {
    @Override
    public JsonObject request(FrontEndState state, JsonObject req, String id) {
        MinimalChessGame game = state.getGame();

        int from = ChessBoard.convertSquareNotationToSpaceId(require(req, "from"));
        int to = ChessBoard.convertSquareNotationToSpaceId(require(req, "to"));
        String promotion = require(req, "promotion");

        short promotedPieceId = switch (promotion) {
            case "Q" -> PieceData.QUEEN;
            case "R" -> PieceData.ROOK;
            case "B" -> PieceData.BISHOP;
            case "N" -> PieceData.KNIGHT;
            default -> PieceData.INVALID_PIECES;
        };
        DebugMode.debugPrint(state, "move request: from=" + from + ", to=" + to);

        ChessMove move = game.getPossibleMoves().getMove(from, to, promotedPieceId);

        if (move == null) {
            return failure(id,
                    game.sideWon() != MinimalChessGame.INDETERMINATE
                            ? "Game is over"
                            : "Illegal move");
        }
        if (!game.movePiece(move)) return failure(id, "Move could not be applied");

        return updateBoardRequest(state, id);
    }
}