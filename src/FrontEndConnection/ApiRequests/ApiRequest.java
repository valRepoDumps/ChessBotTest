package FrontEndConnection.ApiRequests;

import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.ChessMove;
import FrontEndConnection.FrontEndState;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.util.List;

public interface ApiRequest {
    /** Any exception thrown here is turned into a "failure" response by the dispatcher. */
    JsonObject request(FrontEndState state, JsonObject req, String id) throws Exception;

    /** Rejection in the shape the frontend expects for that request type. */
    static JsonObject failure(String id, String reason) {
        JsonObject o = base("failure", id);
        o.addProperty("ok", false);
        o.addProperty("reason", reason);
        return o;
    }

    static JsonObject base(String type, String id) {
        JsonObject o = new JsonObject();
        o.addProperty("type", type);
        if (id != null) o.addProperty("id", id);
        return o;
    }

    static JsonObject updateBoardRequest(FrontEndState state, String id) {
        return updateBoardRequest(state, id, false);
    }

    static JsonObject updateBoardRequest(FrontEndState state, String id, boolean isNewGame) {
        MinimalChessGame game = state.getGame();
        JsonObject o = base("update_board", id);
        o.addProperty("ok", true);
        o.addProperty("fen", game.toFen());
        o.addProperty("is_new_game", isNewGame);

        String result;
        switch (game.sideWon()) {
            case MinimalChessGame.DRAW -> result = "draw";
            case MinimalChessGame.BLACK_WON -> result = "b";
            case MinimalChessGame.WHITE_WON -> result = "w";
            default -> result = "indeterminate";
        }
        o.addProperty("game_over", result);
        return o;
    }

    static String describe(Exception e) {
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }
}