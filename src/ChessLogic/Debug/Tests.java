package ChessLogic.Debug;

import ChessLogic.Configurations.Configurations;
import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.ChessMove;
import ChessResources.GetMovesLogic.PossibleMoves;

import java.util.*;

public class Tests {
    public static int TOTAL_MOVES = 0;
    public static int TOTAL_CAPTURE_MOVES = 1;
    public static int TOTAL_ENPASSANT_MOVES = 2;
    public static int TOTAL_CASTLING_MOVES = 3;
    public static int TOTAL_PROMOTION_MOVES = 4;
    public static int TOTAL_DOUBLE_PAWN_PUSHES = 5;
    public static int MILISEC_ELAPSED = 6;
    public static int RETURN_LEN = 7;


    public static final List<Map.Entry<String, ArrayList<int[]>>> TEST_ANSWERS = List.of(
            Map.entry(
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                    new ArrayList<>(List.of(
                            new int[]{20, 0, 0, 0, 0},
                            new int[]{400, 0, 0, 0, 0},
                            new int[]{8902, 34, 0, 0, 0},
                            new int[]{197281, 1576, 0, 0, 0},
                            new int[]{4865609, 82719, 258, 0, 0}
                    ))
            )
    );

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

    public static ArrayList<int[]> perftDriver(String fen, int maxDepths){
        ArrayList<int[]> ans = new ArrayList<>();

        for (int i = 1; i <= maxDepths; ++i) {
            MinimalChessGame game = new MinimalChessGame(
                    fen,
                    new ChessBoard(),
                    new Configurations(false, false, true));

            long start = System.nanoTime();
            int[] returnVals = Tests.moveGenerationTest(i, game);
            long end = System.nanoTime();
            double durationMs = (end - start) / 1_000_000.0;
            returnVals[MILISEC_ELAPSED] = (int)durationMs;
            ans.add(returnVals);
        }

        return ans;
    }

    public static void printPerft(ArrayList<int[]> perftAns, boolean includeTime){
        System.out.printf("%-6s %-12s %-10s %-10s %-10s %-10s %-10s%n",
                "Depth", "Nodes", "Captures", "EnPassant", "Castles", "Promotions", "Time(ms)");
        System.out.println("-".repeat(72));
        for (int depth = 1; depth <= perftAns.size(); ++depth){

            var returnVals = perftAns.get(depth-1);
            int time = 0;
            if (includeTime) time = returnVals[Tests.MILISEC_ELAPSED];
            System.out.printf("%-6d %-12d %-10d %-10d %-10d %-10d %-10d%n",
                    depth,
                    returnVals[Tests.TOTAL_MOVES],
                    returnVals[Tests.TOTAL_CAPTURE_MOVES],
                    returnVals[Tests.TOTAL_ENPASSANT_MOVES],
                    returnVals[Tests.TOTAL_CASTLING_MOVES],
                    returnVals[Tests.TOTAL_PROMOTION_MOVES],
                    time);
        }
    }

    public static boolean perftTest(){
        for (var test : TEST_ANSWERS){
            var answers = perftDriver(test.getKey(), test.getValue().size());
            for (int i = 0; i < test.getValue().size(); ++i){
                for (int j = TOTAL_MOVES; j <= TOTAL_PROMOTION_MOVES; ++j){
                    if (test.getValue().get(i)[j] != answers.get(i)[j]){
                        System.out.println("FAILED TEST AT POSITION: ");
                        System.out.println(test.getKey());

                        System.out.println("TEST ANSWER: ");
                        printPerft(test.getValue(), false);
                        System.out.println("USER ANSWER: ");
                        printPerft(answers, true);
                        return false;
                    }
                }
            }
        }
        System.out.println("ALL TESTS PASSED!");
        return true;
    }
}
