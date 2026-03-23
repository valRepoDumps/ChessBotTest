package ChessLogic.Debug;

import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.ChessMove;
import ChessResources.GetMovesLogic.ChessSpaces;
import ChessResources.GetMovesLogic.PossibleMoves;

import java.util.*;

public class Tests {
    public static int TOTAL_MOVES = 0;
    public static int TOTAL_CASTLING_MOVES = 1;
    public static int TOTAL_CAPTURE_MOVES = 2;
    public static int TOTAL_ENPASSANT_MOVES = 3;
    public static int TOTAL_PROMOTION_MOVES = 4;
    public static int TOTAL_DOUBLE_PAWN_PUSHES = 5;
    public static int RETURN_LEN = 6;

    public static int[] moveGenerationTest(int depth, MinimalChessGame game) {
        int[] returnVals = new int[RETURN_LEN];

        moveGenerationTest(depth, game, returnVals);
        return returnVals;
    }

    public static
    int moveGenerationTest(int depth, MinimalChessGame game, int[] returnVals){
        if (depth == 0) {
            return 1;
        }
        int numPos = 0;

        PossibleMoves currPossibleMoves = game.getCurrentPossibleMoves().getClone();
        assertNoMoveDuplicates(game, currPossibleMoves);
        for (int i = 0; i < currPossibleMoves.currLen; ++i){
            ChessMove move = currPossibleMoves.getMoves()[i];
            game.movePiece(move);

            if (depth == 1) {
                returnVals[TOTAL_MOVES]++;
                if (move.isDoublePawnPush()) {
                    returnVals[TOTAL_DOUBLE_PAWN_PUSHES]++;
                }

                if (move.isCastling()) {
                    returnVals[TOTAL_CASTLING_MOVES]++;
                }

                if (move.isCapture()) {
                    returnVals[TOTAL_CAPTURE_MOVES]++;
                }

                if (move.isEnPassant()) {
                    returnVals[TOTAL_ENPASSANT_MOVES]++;
                }

                if (move.isPromotion()) {
                    returnVals[TOTAL_PROMOTION_MOVES]++;
                }
            }
            numPos  += moveGenerationTest(depth - 1, game, returnVals);
            try {
                game.undoTurn();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        return numPos;
    }

    public static boolean assertNoMoveDuplicates(MinimalChessGame game,
                                                 PossibleMoves pm){
        HashMap<ChessMove, ChessMove> map = new HashMap<>();
        for (int i = 0; i < pm.currLen; ++i){
            ChessMove moves = pm.getMoves()[i];
            if (map.getOrDefault(moves, null) == null){
                map.put(moves, moves);
            }else{
                System.out.println("REPEATS FOUND!");
                System.out.println(moves);
                if (moves.isPromotion()) System.out.println("Promotion " + moves.getDoublePawnPushId());

                System.out.println(map.get(moves));
                game.getBoard().printBoard();
                return false;
            }
        }
        return true;
    }
}
