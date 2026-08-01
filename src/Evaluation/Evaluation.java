package Evaluation;
import ChessLogic.ChessGame;
import ChessLogic.Configurations.Configurations;
import ChessLogic.MinimalChessGame;
import ChessResources.BitMasks;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.ChessHistoryTracker.BoardStateChanges.BoardStateChange;
import ChessResources.ChessListener.StateChangeListener;
import ChessResources.GetMovesLogic.ChessMove;
import ChessResources.GetMovesLogic.PossibleMoves;
import ChessResources.Pieces.PieceData;

import java.util.Objects;

import static java.lang.System.exit;

public class Evaluation {
    public static final int NEG_INF_SCORE = -90000;
    static final int[] MATERIAL_SCORE = new int[PieceData.TOTAL_PIECES];
    public static final ChessMove INVALID_MOVES = null;
    static final int [] PAWN_POSITIONAL_SCORE = {
        90, 90, 90, 90, 90, 90, 90, 90,
        30, 30, 30, 40, 40, 30, 30, 30,
        20, 20, 20, 30, 30, 20, 20, 20,
        10, 10, 10, 20, 20, 10, 10, 10,
        5,   5, 10, 20, 20, 10, 5 ,  5,
        0,   0,  0,  5,  5,  0,  0,  0,
        0,   0,  0,-10,-10,  0,  0,  0,
        0,   0,  0,  0,  0,  0,  0,  0
    };

    static final int [] KNIGHT_POSITIONAL_SCORE = {
        -10, -5, -5, -5, -5, -5, -5, -10,
        -5,   0,  0,  0,  0,  0,  0, -5,
        -5,   0,  5, 20, 20,  5,  0, -5,
        -5,   0, 10, 30, 30, 10,  0, -5,
        -5,   0, 10, 30, 30, 10,  0,  -5,
        -5,   0,  5,  5,  5,  5,  0,  -5,
        -5,   0,  0,  0,  0,  0,  0,  -5,
        -10,-10, -5, -5, -5, -5,-10,  -5
    };

    static final int [] BISHOP_POSITIONAL_SCORE = {
            0,   0,  0,  0, 0 , 0 , 0 , 0 ,
            0,   0,  0,  0,  0,  0,  0, 0 ,
            0,   0,  0, 10, 10,  0,  0, 0 ,
            0,   0, 10, 30, 30, 10,  0, 0 ,
            0,   0, 10, 30, 30, 10,  0,  0,
            0,  10,  0,  0,  0,  0, 10,  0,
            0,  30,  0,  0,  0,  0, 30,  0,
            0,   0,-10,  0,  0,-10,  0,  0
    };
    static final int [] ROOK_POSITIONAL_SCORE = {
            50, 50, 50, 50, 50, 50, 50, 50,
            50, 50, 50, 50, 50, 50, 50, 50,
            0,   0, 10, 20, 20, 10,  0, 0 ,
            0,   0, 10, 20, 20, 10,  0, 0 ,
            0,   0, 10, 20, 20, 10,  0,  0,
            0,   0, 10, 20, 20, 10,  0,  0,
            0,   0, 10, 20, 20, 10,  0,  0,
            0,   0, 10, 20, 20, 10,  0,  0
    };

    static final int [] QUEEN_POSITIONAL_SCORE = {
            50, 50, 50, 50, 50, 50, 50, 50,
            50, 50, 50, 50, 50, 50, 50, 50,
            0,   0, 10, 30, 30, 10,  0, 0 ,
            0,   0, 20, 50, 50, 20,  0, 0 ,
            0,   0, 20, 50, 50, 20,  0,  0,
            0,   0, 10, 20, 20, 10,  0,  0,
            0,  30, 10, 20, 20, 10, 30,  0,
            0,   0,-10, 20, 20,-10,  0,  0
    };

    static final int [] KING_POSITIONAL_SCORE = {
            0,   0,  0,  0,  0,  0,  0,  0,
            0,   0,  0,  0,  0,  0,  0,  0,
            0,   0,  0,  0,  0,  0,  0,  0,
            0,   0,  0,  0,  0,  0,  0,  0,
            0,   0,  0,  0,  0,  0,  0,  0,
            0,   0,  0,  0,  0,  0,  0,  0,
            0,   0,  0,  0,  0,  0,  0,  0,
            0,  50, 50,  0,  0,  0, 50,  0
    };

    static final int [] MIRRORED_IDX = {
        56, 57, 58, 59, 60, 61, 62, 63,
        48, 49, 50, 51, 52, 53, 54, 55,
        40, 41, 42, 43, 44, 45, 46, 47,
        32, 33, 34, 35, 36, 37, 38, 39,
        24, 25, 26, 27, 28, 29, 30, 31,
        16, 17, 18, 19, 20, 21, 22, 23,
         8,  9, 10, 11, 12, 13, 14, 15,
         0,  1,  2,  3,  4,  5,  6,  7
    };

    static final int[][] POSITIONAL_SCORES = new int[PieceData.PIECES_DIFF][];

    private final StateChangeListener<MinimalChessGame> EVALUATOR =
            this::negaMaxSearch;

    private final StateChangeListener<MinimalChessGame> EVALUATOR_BLACK_PLAYS =
            (MinimalChessGame game)->{
                if (game.getCurrentColorToMove() == PieceData.BLACK && game.sideWon() == MinimalChessGame.INDETERMINATE) {
                    blackMoveEvaluator(game);
                }
            };
    static {
        MATERIAL_SCORE[PieceData.WPAWN] = 100;
        MATERIAL_SCORE[PieceData.WBISHOP] = 350;
        MATERIAL_SCORE[PieceData.WKNIGHT] = 300;
        MATERIAL_SCORE[PieceData.WROOK] = 500;
        MATERIAL_SCORE[PieceData.WQUEEN] = 900;
        MATERIAL_SCORE[PieceData.WKING] = 30000;

        MATERIAL_SCORE[PieceData.BPAWN] = -100;
        MATERIAL_SCORE[PieceData.BBISHOP] = -350;
        MATERIAL_SCORE[PieceData.BKNIGHT] = -300;
        MATERIAL_SCORE[PieceData.BROOK] = -500;
        MATERIAL_SCORE[PieceData.BQUEEN] = -900;
        MATERIAL_SCORE[PieceData.BKING] = -30000;
        
        POSITIONAL_SCORES[PieceData.PAWN] = PAWN_POSITIONAL_SCORE;
        POSITIONAL_SCORES[PieceData.KNIGHT] = KNIGHT_POSITIONAL_SCORE;
        POSITIONAL_SCORES[PieceData.BISHOP] = BISHOP_POSITIONAL_SCORE;
        POSITIONAL_SCORES[PieceData.ROOK] = ROOK_POSITIONAL_SCORE;
        POSITIONAL_SCORES[PieceData.QUEEN] = QUEEN_POSITIONAL_SCORE;
        POSITIONAL_SCORES[PieceData.KING] = KING_POSITIONAL_SCORE;
    }

    public ChessMove bestMove;
    public int depth;

    public Evaluation(int depth){
        this.depth = depth;
    }
    static int getMaterialScore(short pieceData, boolean sideToMove) {
        return sideToMove == PieceData.WHITE
                ? MATERIAL_SCORE[pieceData]
                : - MATERIAL_SCORE[pieceData];
    }

    static int getPositionalScore(short pieceData, int sqr, boolean sideToMove){
        int idx = PieceData.getColor(pieceData) == PieceData.WHITE
                ? sqr : MIRRORED_IDX[sqr];
        int pieceValue = PieceData.getColor(pieceData) == PieceData.WHITE
                ? POSITIONAL_SCORES[PieceData.getType(pieceData)][idx]
                : -POSITIONAL_SCORES[PieceData.getType(pieceData)][idx];

        return sideToMove == PieceData.WHITE
                ? pieceValue
                : -pieceValue;
    }

    public static int evaluateGame(MinimalChessGame game){
        int currScore = 0;
        short piece;
        int sqr;
        //System.out.println("Start evaluation");
        for (short pieceId = PieceData.BPAWN; pieceId < PieceData.TOTAL_PIECES; ++pieceId){
            long bb = game.getBoard().getBitBoard(pieceId);
            while (bb != 0){
                piece = pieceId;
                sqr = Long.numberOfTrailingZeros(bb);
                bb = BitMasks.unSetBit(bb, sqr);
                currScore += getMaterialScore(piece, game.getGameProperties().getSideToMove());
                currScore += getPositionalScore(piece, sqr, game.getGameProperties().getSideToMove());
                //System.out.println(currScore + " " + PieceData.getName(piece) + " " + sqr + " " + game.getGameProperties().getSideToMove());
            }
        }
        //System.out.println("End evaluation");
        return currScore;
    }

    public void registerGenericEvaluator(MinimalChessGame game){
        game.addTurnEndListener(EVALUATOR);
    }

    public void registerBlackEvaluator(MinimalChessGame game){
        game.addTurnEndListener(EVALUATOR_BLACK_PLAYS);
    }

    private void blackMoveEvaluator(MinimalChessGame game){
        System.out.println("Black Eval: " + evaluateGame(game));
        negaMaxSearch(game);
        game.movePiece(bestMove);
        if (game.getCurrentColorToMove() == PieceData.BLACK){
            System.out.println("NO MOVES FOUND??");
        }
        System.out.println("White Eval: " + evaluateGame(game));
    }

    private void negaMaxSearch(MinimalChessGame game){
        bestMove = INVALID_MOVES;
        if (game == null){
            System.out.println("GAME IS NULL\n");
        }
        assert game != null;
        System.out.println("starting new eval rounds");
        MinimalChessGame testGame = game.cloneMinimalGame();
        testGame.changeConfigurations(new Configurations(false, false, false));
        negaMaxSearch(testGame, depth, NEG_INF_SCORE, -NEG_INF_SCORE, true);

//        System.out.println("Best move: " + bestMove.toString());
//        System.out.println(evaluateGame(testGame));
//        System.out.println(evaluateGame(game));
//        testGame.getBoard().printBoard();
//        game.getBoard().printBoard();
    }

    int negaMaxSearch(MinimalChessGame game, int depth, int alpha, int beta, boolean isRootCall){
        //System.out.println(depth + " " + alpha +" " + beta + " " + isRootCall);
        if (game.sideWon() != MinimalChessGame.INDETERMINATE){
            if (game.sideWon() == MinimalChessGame.DRAW) return 0;
            return NEG_INF_SCORE/2 - depth; // side to move has been checkmated
        }


        if (depth == 0) {
            return evaluateGame(game);
        }
        String beforeMoves = game.getBoard().toString();

        int oldAlpha = alpha;
        ChessMove currBestMove = INVALID_MOVES;

        PossibleMoves currPossibleMoves = game.getCurrentPossibleMoves().getClone();
        for (int i = 0; i < currPossibleMoves.currLen; ++i){
            ChessMove move = currPossibleMoves.getMoves()[i];
            System.out.println("Evaluating: " + move);
            System.out.println(game.getBoard().toString());
            String beforeMoves2 = game.getBoard().toString();
            game.movePiece(move);
            System.out.println("Moved.");
            System.out.println(game.getBoard().toString());
            String afterMove = game.getBoard().toString();
            int score = -negaMaxSearch(game, depth-1, -beta, -alpha, false);
            try {
                game.undoTurn();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            if (!Objects.equals(beforeMoves, game.getBoard().toString())){
                System.out.println("Inaccurate Undo: " + i + " " + depth);
                System.out.println(beforeMoves);
                System.out.println(beforeMoves2);
                System.out.println(afterMove);
                System.out.println(game.getBoard().toString());

                exit(0);
            }
            if (score >= beta) return beta; //beta cuttoff.
            //found a better move.
            if (score > alpha) {
                alpha = score;
                if (isRootCall) {
                    currBestMove = move;
                    System.out.println("Update best moves: ");
                    System.out.println(move);
                }
            }
        }
        if (alpha != oldAlpha) {
            bestMove = currBestMove;
        }
        return alpha;
    }
}