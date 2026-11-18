package FrontEndConnection.ApiRequests.ChangeBoardStateRequests;

import ChessLogic.Configurations.Configurations;
import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import FrontEndConnection.ApiRequests.ApiRequest;
import FrontEndConnection.FrontEndState;
import com.google.gson.JsonObject;

import static Common.JsonUtils.str;
import static FrontEndConnection.ApiRequests.ApiRequest.describe;
import static FrontEndConnection.ApiRequests.ApiRequest.failure;
import static FrontEndConnection.ApiRequests.ApiRequest.updateBoardRequest;

public final class NewGameRequest implements ApiRequest {
    @Override
    public JsonObject request(FrontEndState state, JsonObject req, String id) {
        String fen = str(req, "fen");
        if (fen == null) return failure(id, "Malformed FEN");

        fen = fen.trim().replaceAll("\\s+", " ");

        String problem = validateFen(fen);
        if (problem != null) return failure(id, problem);

        MinimalChessGame fresh;
        try {
            fresh = new MinimalChessGame(fen, new ChessBoard(), new Configurations(state.getGame().getConfigurations()));
        } catch (IllegalArgumentException e) {
            return failure(id, describe(e));
        } catch (RuntimeException e) {
            return failure(id, "Invalid FEN");
        }

        state.installGame(fresh);

        return updateBoardRequest(state, id, true);
    }

    /** Returns a human-readable problem, or null if the FEN is acceptable. */
    private static String validateFen(String fen) {
        String[] f = fen.split(" ");
        if (f.length != 6) return "FEN must have 6 fields";
        if (f[0].split("/", -1).length != 8) return "FEN placement must have 8 ranks";

        int whiteKings = 0, blackKings = 0;
        for (char c : f[0].toCharArray()) {
            if (c == 'K') whiteKings++;
            else if (c == 'k') blackKings++;
        }
        if (whiteKings == 0) return "White king is missing";
        if (whiteKings > 1) return "White has more than one king";
        if (blackKings == 0) return "Black king is missing";
        if (blackKings > 1) return "Black has more than one king";
        return null;
    }
}