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
import ChessResources.Pieces.MovesGeneration;
import ChessResources.Pieces.PieceData;

import java.util.Comparator;
import java.util.Objects;

import static java.lang.Math.max;
import static java.lang.System.exit;

public class Evaluation {
    public static final int NEG_INF_SCORE = -90000;
    public static final int BIG_MOVE_SCORE = 10000;
    public static final int MAX_HALF_MOVES = 128;
    static final int[] MATERIAL_SCORE = new int[PieceData.TOTAL_PIECES];
    public static final ChessMove INVALID_MOVES = null;

    //region POSITIONAL_SCORES
    static final double [] PAWN_POSITIONAL_MODIFIER = {
           9,   9,   9,   9,   9,   9,   9,   9,
           3,   3,   3,   4,   4,   3,   3,   3,
           2,   2,   2,   3,   3,   2,   2,   2,
         1.5, 1.5, 1.5,   2,   2, 1.5, 1.5, 1.5,
        1.25,1.25, 1.5,   2,   2, 1.5,1.25,1.25,
           1,   1,   1,1.25,1.25,   1,   1,   1,
           1,   1,   1, .25, .25,   1,   1,   1,
           1,   1,   1,   1,   1,   1,   1,   1
    };

    static final double [] KNIGHT_POSITIONAL_MODIFIER = {
       .25,  .5,  .5,  .5,  .5,  .5, .5, .25,
        .5,   1,   1,   1,   1,   1,  1,  .5,
        .5,   1,1.25,   2,   2,1.25,  1,  .5,
        .5,   1, 1.5,   3,   3, 1.5,  1,  .5,
        .5,   1, 1.5,   3,   3, 1.5,  1,  .5,
        .5,   1,1.25,1.25,1.25,1.25,  1,  .5,
        .5,   1,   1,   1,   1,   1,  1,  .5,
       .25, .25,  .5,  .5,  .5,  .5,.25,  .9
    };

    static final double [] BISHOP_POSITIONAL_MODIFIER = {
        1,   1,  1,  1,  1,  1,  1,  1,
        1,   1,  1,  1,  1,  1,  1,  1,
        1,   1,  1,1.5,1.5,  1,  1,  1,
        1,   1,1.5,  3,  3, 1.5, 1,  1,
        1,   1,1.5,  3,  3, 1.5, 1,  1,
        1, 1.5,  1,  1,  1,  1,1.5,  1,
        1,   3,  1,  1,  1,  1,  3,  1,
        1,   1,.25,  1,  1,.25,  1,  1
    };
    static final double [] ROOK_POSITIONAL_MODIFIER = {
        5,   5,   5, 5, 5,   5,  5,  5,
        5,   5,   5, 5, 5,   5,  5,  5,
        1,   1, 1.5, 2, 2, 1.5,  1,  1,
        1,   1, 1.5, 2, 2, 1.5,  1,  1,
        1,   1, 1.5, 2, 2, 1.5,  1,  1,
        1,   1, 1.5, 2, 2, 1.5,  1,  1,
        1,   1, 1.5, 2, 2, 1.5,  1,  1,
        1,   1, 1.5, 2, 2, 1.5,  1,  1
    };

    static final double [] QUEEN_POSITIONAL_MODIFIER = {
        5,   5,   5, 5, 5,   5,  5,  5,
        5,   5,   5, 5, 5,   5,  5,  5,
        1,   1, 1.5, 3, 3, 1.5,  1,  1,
        1,   1,   2, 1, 1,   2,  1,  0,
        1,   1,   2, 1, 1,   2,  1,  1,
        1,   1, 1.5, 2, 2, 1.5,  1,  1,
        1,   3, 1.5, 2, 2, 1.5,  3,  1,
        1,   1, 1.5, 2, 2, 1.5,  1,  1
    };

    static final double [] KING_POSITIONAL_MODIFIER = {
        1,   1,  1,  1,  1,  1,  1,  1,
        1,   1,  1,  1,  1,  1,  1,  1,
        1,   1,  1,  1,  1,  1,  1,  1,
        1,   1,  1,  1,  1,  1,  1,  1,
        1,   1,  1,  1,  1,  1,  1,  1,
        1,   1,  1,  1,  1,  1,  1,  1,
        1,   1,  1,  1,  1,  1,  1,  1,
        1,   5,  1,  1,  1,  1,  5,  1
    };

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
         1,  1,  2,  3,  4,  5,  6,  7
    };

    static final int[][] POSITIONAL_SCORES = new int[PieceData.PIECES_DIFF][];
    static final double[][] POSITIONAL_MODIFIERS = new double[PieceData.PIECES_DIFF][];
    //endregion

    static final int[] PAWN_CAPTURE_SCORE = new int[PieceData.TOTAL_PIECES];
    static final int[] KNIGHT_CAPTURE_SCORE = new int[PieceData.TOTAL_PIECES];
    static final int[] BISHOP_CAPTURE_SCORE = new int[PieceData.TOTAL_PIECES];
    static final int[] ROOK_CAPTURE_SCORE = new int[PieceData.TOTAL_PIECES];
    static final int[] QUEEN_CAPTURE_SCORE = new int[PieceData.TOTAL_PIECES];
    static final int[] KING_CAPTURE_SCORE = new int[PieceData.TOTAL_PIECES];

    //[attacker][defender]
    static final int[][] CAPTURE_SCORES = new int[PieceData.TOTAL_PIECES][];

    private final StateChangeListener<MinimalChessGame> EVALUATOR_BLACK_PLAYS =
            (MinimalChessGame game)->{
                if (game.getCurrentColorToMove() == PieceData.BLACK && game.sideWon() == MinimalChessGame.INDETERMINATE) {
                    moveEvaluator(game);
                }
            };
    private final StateChangeListener<MinimalChessGame> EVALUATOR_WHITE_PLAYS =
            (MinimalChessGame game)->{
                if (game.getCurrentColorToMove() == PieceData.WHITE && game.sideWon() == MinimalChessGame.INDETERMINATE) {
                    moveEvaluator(game);
                }
            };

    //region STATIC
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

        POSITIONAL_MODIFIERS[PieceData.PAWN] = PAWN_POSITIONAL_MODIFIER;
        POSITIONAL_MODIFIERS[PieceData.KNIGHT] = KNIGHT_POSITIONAL_MODIFIER;
        POSITIONAL_MODIFIERS[PieceData.BISHOP] = BISHOP_POSITIONAL_MODIFIER;
        POSITIONAL_MODIFIERS[PieceData.ROOK] = ROOK_POSITIONAL_MODIFIER;
        POSITIONAL_MODIFIERS[PieceData.QUEEN] = QUEEN_POSITIONAL_MODIFIER;
        POSITIONAL_MODIFIERS[PieceData.KING] = KING_POSITIONAL_MODIFIER;

        PAWN_CAPTURE_SCORE[PieceData.BPAWN] = PAWN_CAPTURE_SCORE[PieceData.WPAWN] = 105;
        PAWN_CAPTURE_SCORE[PieceData.WKNIGHT] = PAWN_CAPTURE_SCORE[PieceData.BKNIGHT] = 205;
        PAWN_CAPTURE_SCORE[PieceData.WBISHOP] = PAWN_CAPTURE_SCORE[PieceData.BBISHOP] = 305;
        PAWN_CAPTURE_SCORE[PieceData.WROOK] = PAWN_CAPTURE_SCORE[PieceData.BROOK] = 405;
        PAWN_CAPTURE_SCORE[PieceData.WQUEEN] = PAWN_CAPTURE_SCORE[PieceData.BQUEEN] = 505;
        PAWN_CAPTURE_SCORE[PieceData.WKING] = PAWN_CAPTURE_SCORE[PieceData.BKING] = 605;

        KNIGHT_CAPTURE_SCORE[PieceData.BPAWN] = KNIGHT_CAPTURE_SCORE[PieceData.WPAWN] = 104;
        KNIGHT_CAPTURE_SCORE[PieceData.WKNIGHT] = KNIGHT_CAPTURE_SCORE[PieceData.BKNIGHT] = 204;
        KNIGHT_CAPTURE_SCORE[PieceData.WBISHOP] = KNIGHT_CAPTURE_SCORE[PieceData.BBISHOP] = 304;
        KNIGHT_CAPTURE_SCORE[PieceData.WROOK] = KNIGHT_CAPTURE_SCORE[PieceData.BROOK] = 404;
        KNIGHT_CAPTURE_SCORE[PieceData.WQUEEN] = KNIGHT_CAPTURE_SCORE[PieceData.BQUEEN] = 504;
        KNIGHT_CAPTURE_SCORE[PieceData.WKING] = KNIGHT_CAPTURE_SCORE[PieceData.BKING] = 604;

        BISHOP_CAPTURE_SCORE[PieceData.BPAWN] = BISHOP_CAPTURE_SCORE[PieceData.WPAWN] = 103;
        BISHOP_CAPTURE_SCORE[PieceData.WKNIGHT] = BISHOP_CAPTURE_SCORE[PieceData.BKNIGHT] = 203;
        BISHOP_CAPTURE_SCORE[PieceData.WBISHOP] = BISHOP_CAPTURE_SCORE[PieceData.BBISHOP] = 303;
        BISHOP_CAPTURE_SCORE[PieceData.WROOK] = BISHOP_CAPTURE_SCORE[PieceData.BROOK] = 403;
        BISHOP_CAPTURE_SCORE[PieceData.WQUEEN] = BISHOP_CAPTURE_SCORE[PieceData.BQUEEN] = 503;
        BISHOP_CAPTURE_SCORE[PieceData.WKING] = BISHOP_CAPTURE_SCORE[PieceData.BKING] = 603;

        ROOK_CAPTURE_SCORE[PieceData.BPAWN] = ROOK_CAPTURE_SCORE[PieceData.WPAWN] = 102;
        ROOK_CAPTURE_SCORE[PieceData.WKNIGHT] = ROOK_CAPTURE_SCORE[PieceData.BKNIGHT] = 202;
        ROOK_CAPTURE_SCORE[PieceData.WBISHOP] = ROOK_CAPTURE_SCORE[PieceData.BBISHOP] = 302;
        ROOK_CAPTURE_SCORE[PieceData.WROOK] = ROOK_CAPTURE_SCORE[PieceData.BROOK] = 402;
        ROOK_CAPTURE_SCORE[PieceData.WQUEEN] = ROOK_CAPTURE_SCORE[PieceData.BQUEEN] = 502;
        ROOK_CAPTURE_SCORE[PieceData.WKING] = ROOK_CAPTURE_SCORE[PieceData.BKING] = 602;

        QUEEN_CAPTURE_SCORE[PieceData.BPAWN] = QUEEN_CAPTURE_SCORE[PieceData.WPAWN] = 101;
        QUEEN_CAPTURE_SCORE[PieceData.WKNIGHT] = QUEEN_CAPTURE_SCORE[PieceData.BKNIGHT] = 201;
        QUEEN_CAPTURE_SCORE[PieceData.WBISHOP] = QUEEN_CAPTURE_SCORE[PieceData.BBISHOP] = 301;
        QUEEN_CAPTURE_SCORE[PieceData.WROOK] = QUEEN_CAPTURE_SCORE[PieceData.BROOK] = 401;
        QUEEN_CAPTURE_SCORE[PieceData.WQUEEN] = QUEEN_CAPTURE_SCORE[PieceData.BQUEEN] = 501;
        QUEEN_CAPTURE_SCORE[PieceData.WKING] = QUEEN_CAPTURE_SCORE[PieceData.BKING] = 601;

        KING_CAPTURE_SCORE[PieceData.BPAWN] = KING_CAPTURE_SCORE[PieceData.WPAWN] = 100;
        KING_CAPTURE_SCORE[PieceData.WKNIGHT] = KING_CAPTURE_SCORE[PieceData.BKNIGHT] = 200;
        KING_CAPTURE_SCORE[PieceData.WBISHOP] = KING_CAPTURE_SCORE[PieceData.BBISHOP] = 300;
        KING_CAPTURE_SCORE[PieceData.WROOK] = KING_CAPTURE_SCORE[PieceData.BROOK] = 400;
        KING_CAPTURE_SCORE[PieceData.WQUEEN] = KING_CAPTURE_SCORE[PieceData.BQUEEN] = 500;
        KING_CAPTURE_SCORE[PieceData.WKING] = KING_CAPTURE_SCORE[PieceData.BKING] = 600;

        CAPTURE_SCORES[PieceData.BPAWN] = PAWN_CAPTURE_SCORE;
        CAPTURE_SCORES[PieceData.BKNIGHT] = KNIGHT_CAPTURE_SCORE;
        CAPTURE_SCORES[PieceData.BBISHOP] = BISHOP_CAPTURE_SCORE;
        CAPTURE_SCORES[PieceData.BROOK] = ROOK_CAPTURE_SCORE;
        CAPTURE_SCORES[PieceData.BQUEEN] = QUEEN_CAPTURE_SCORE;
        CAPTURE_SCORES[PieceData.BKING] = KING_CAPTURE_SCORE;

        CAPTURE_SCORES[PieceData.WPAWN] = PAWN_CAPTURE_SCORE;
        CAPTURE_SCORES[PieceData.WKNIGHT] = KNIGHT_CAPTURE_SCORE;
        CAPTURE_SCORES[PieceData.WBISHOP] = BISHOP_CAPTURE_SCORE;
        CAPTURE_SCORES[PieceData.WROOK] = ROOK_CAPTURE_SCORE;
        CAPTURE_SCORES[PieceData.WQUEEN] = QUEEN_CAPTURE_SCORE;
        CAPTURE_SCORES[PieceData.WKING] = KING_CAPTURE_SCORE;
    }

    private MinimalChessGame game;
    private ChessMove[][] killerMoves;
    private int[][] historyMoves;
    private int[] principalVariationLen;
    private ChessMove[][] principalVariationTable;

    private boolean followPrincipalVariation, scorePrincipalVariation;
    //endregion
    public ChessMove bestMove;
    public int depth;
    public int nodes;
    public int singleHalfMoves;
    private final Comparator<ChessMove> sortFunc = Comparator.comparingInt(this::scoreMove).reversed();

    public Evaluation(int depth){
        this.depth = depth;
        resetEvaluation();
    }

    private void resetEvaluation(){
        nodes = 0;
        singleHalfMoves = 0;
        bestMove = INVALID_MOVES;
        killerMoves = new ChessMove[2][MAX_HALF_MOVES];
        historyMoves = new int[12][ChessBoard.TOTAL_SPACES];

        principalVariationLen = new int[MAX_HALF_MOVES];
        principalVariationTable = new ChessMove[MAX_HALF_MOVES][MAX_HALF_MOVES];
        followPrincipalVariation = false;
        scorePrincipalVariation = false;
    }

    static int getMaterialScore(short pieceData, boolean sideToMove) {
        return sideToMove == PieceData.WHITE
                ? MATERIAL_SCORE[pieceData]
                : - MATERIAL_SCORE[pieceData];
    }

    private int getPositionalScore(short pieceData, int sqr){
        int idx = PieceData.getColor(pieceData) == PieceData.WHITE
                ? sqr : MIRRORED_IDX[sqr];

        int pieceValue = (int) (PieceData.getColor(pieceData) == PieceData.WHITE
                        ? POSITIONAL_SCORES[PieceData.getType(pieceData)][idx] +
                        POSITIONAL_MODIFIERS[PieceData.getType(pieceData)][idx] *
                        max(1, BitMasks.countBit(MovesGeneration.getUniversalMoves(game, sqr, pieceData)))
                        : -POSITIONAL_SCORES[PieceData.getType(pieceData)][idx] +
                        POSITIONAL_MODIFIERS[PieceData.getType(pieceData)][idx] *
                        max(1, BitMasks.countBit(MovesGeneration.getUniversalMoves(game, sqr, pieceData))));

        return game.getCurrentColorToMove() == PieceData.WHITE
                ? pieceValue
                : -pieceValue;
    }

    private int getCaptureScore(ChessMove move){
        if (!move.isCapture()){
            if(killerMoves[0][singleHalfMoves] != null &&
                    killerMoves[0][singleHalfMoves].equals(move)){
                return (int) (BIG_MOVE_SCORE*.9);
            }else if (killerMoves[1][singleHalfMoves] != null &&
                    killerMoves[1][singleHalfMoves].equals(move)){
                return (int) (BIG_MOVE_SCORE*.8);
            }else {
                return historyMoves[move.getPieceId()][move.getSpaceIdArriveAt()];
            }
        }
        return CAPTURE_SCORES[move.getPieceId()][move.getCapturedPieceId()] + BIG_MOVE_SCORE;
    }

    private int scoreMove(ChessMove move){
        if (scorePrincipalVariation){
            if (principalVariationTable[0][singleHalfMoves] != null &&
                    principalVariationTable[0][singleHalfMoves].equals(move)){
                scorePrincipalVariation = false;
                return BIG_MOVE_SCORE*2;
            }
        }


        return getCaptureScore(move) +
                getPositionalScore(move.getPieceId(),
                        move.getSpaceIdArriveAt()) -
                getPositionalScore(move.getPieceId(),
                        move.getSpaceIdToMove());
    }

    public int evaluateGame(){
        int currScore = 0;
        short piece;
        int sqr;

        for (short pieceId = PieceData.BPAWN; pieceId < PieceData.TOTAL_PIECES; ++pieceId){
            long bb = game.getBoard().getBitBoard(pieceId);
            while (bb != 0){
                piece = pieceId;
                sqr = Long.numberOfTrailingZeros(bb);
                bb = BitMasks.unSetBit(bb, sqr);
                currScore += getMaterialScore(piece, game.getGameProperties().getSideToMove());
                currScore += getPositionalScore(piece, sqr);

            }
        }

        currScore -= game.countPiecesNearSpace(
                game.getKingSpaceId(!game.getCurrentColorToMove()),
                3,
                game.getCurrentColorToMove())*5;
        currScore += game.countPiecesNearSpace(
                game.getKingSpaceId(game.getCurrentColorToMove()),
                3,
                game.getCurrentColorToMove())*5;

        if (game.countPiecesOnCols(
                game.getKingSpaceId(game.getCurrentColorToMove()),
                1,
                game.getCurrentColorToMove()
            ) < 5
        ){
            currScore-=100; //
        }

        return currScore;
    }

    public void registerBlackEvaluator(MinimalChessGame game){
        game.addTurnEndListener(EVALUATOR_BLACK_PLAYS);
    }

    public void registerWhiteEvaluator(MinimalChessGame game){
        game.addTurnEndListener(EVALUATOR_WHITE_PLAYS);
    }

    public void registerGame(MinimalChessGame game){
        this.game = game;
    }

    private void moveEvaluator(MinimalChessGame game){
        resetEvaluation();

        Configurations oldConfig = game.getConfigurations();
        game.changeConfigurations(new Configurations(false, false, false));
        MinimalChessGame testGame = game.cloneMinimalGame();
        game.changeConfigurations(oldConfig);
        registerGame(testGame);
        for (int i = 1; i <= depth; ++i){
            followPrincipalVariation = true;
            negaMaxSearch(i, NEG_INF_SCORE, -NEG_INF_SCORE, true);
            System.out.println("Depth: " + i + " Nodes: " + nodes);
        }

        System.out.println("Before movement eval: " + evaluateGame());
        game.movePiece(principalVariationTable[0][0]);
        registerGame(game);
        System.out.println("Current Side Eval: " + evaluateGame());
        System.out.println("Nodes visited: " + nodes);
    }

    private void enablePrincipalVariationScoring(PossibleMoves currPossibleMoves){
        followPrincipalVariation = false;
        for (int i = 0; i < currPossibleMoves.currLen; ++i){
            ChessMove move = currPossibleMoves.getMoves()[i];

            if (principalVariationTable[0][singleHalfMoves] != null &&
                    principalVariationTable[0][singleHalfMoves].equals(move)){
                scorePrincipalVariation = true;
                followPrincipalVariation = true;

            }
        }
    }

    private int quiescenceSearch(int alpha, int beta){
        ++nodes;

        int eval = evaluateGame();

        if (eval >= beta){
            return beta;
        }

        if (eval > alpha){
            alpha = eval;
        }

        PossibleMoves currPossibleMoves = game.getCurrentPossibleMoves().getClone();
        currPossibleMoves.sortMoves(sortFunc);
        //System.out.println(game.getGameProperties().getHalfMovesSinceCaptureOrPawnMove());
        for (int i = 0; i < currPossibleMoves.currLen; ++i){
            ChessMove move = currPossibleMoves.getMoves()[i];

            if (!move.isCapture()) continue;
            game.movePiece(move);

            ++singleHalfMoves;
            int score = -quiescenceSearch(-beta, -alpha);
            --singleHalfMoves;

            try {
                game.undoTurn();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            if (score >= beta) return beta; //beta cuttoff.
            //found a better move.
            if (score > alpha) {
                alpha = score;
            }
        }

        return alpha;
    }

    int negaMaxSearch(int currDepth, int alpha, int beta, boolean isRootCall){
        principalVariationLen[singleHalfMoves] = singleHalfMoves;
        boolean foundPrincipalVariationMove = false;

        if (game.kingToMoveUnderThreat()){
            ++currDepth;
        }

        if (currDepth == 0) {
            return quiescenceSearch(alpha, beta);
        }

        if (singleHalfMoves > MAX_HALF_MOVES){
            return evaluateGame();
        }

        if (game.sideWon() != MinimalChessGame.INDETERMINATE){
            if (game.sideWon() == MinimalChessGame.DRAW) return 0;
            return NEG_INF_SCORE/2 - currDepth; // side to move has been checkmated
        }

        ++nodes;

        PossibleMoves currPossibleMoves = game.getCurrentPossibleMoves().getClone();

        if (followPrincipalVariation){
            enablePrincipalVariationScoring(currPossibleMoves);
        }

        currPossibleMoves.sortMoves(sortFunc);


        for (int i = 0; i < currPossibleMoves.currLen; ++i){
            ChessMove move = currPossibleMoves.getMoves()[i];

            game.movePiece(move);

            ++singleHalfMoves;

            int score;

            if (foundPrincipalVariationMove){
                score = -negaMaxSearch(currDepth-1, -alpha-1, -alpha, false);
                if ((score > alpha) && (score < beta)){
                    score = -negaMaxSearch(currDepth-1, -beta, -alpha, false);
                }
            }else{
                score = -negaMaxSearch(currDepth-1, -beta, -alpha, false);
            }

            --singleHalfMoves;
            try {
                game.undoTurn();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            if (score >= beta){
                if (!move.isCapture()) {
                    killerMoves[1][singleHalfMoves] = killerMoves[0][singleHalfMoves];
                    killerMoves[0][singleHalfMoves] = move;
                }
                return beta; //beta cuttoff.
            }
            //found a better move.
            if (score > alpha) {

                if (!move.isCapture()) {
                    historyMoves[move.getPieceId()][move.getSpaceIdArriveAt()] += depth;
                    //System.out.println(historyMoves[move.getPieceId()][move.getSpaceIdArriveAt()]);
                }
                alpha = score;

                foundPrincipalVariationMove = true;
                principalVariationTable[singleHalfMoves][singleHalfMoves] = move;

                //loooping over next principle variation.
                if (principalVariationLen[singleHalfMoves + 1] - (singleHalfMoves + 1) >= 0)
                    System.arraycopy(principalVariationTable[singleHalfMoves + 1],
                            singleHalfMoves + 1, principalVariationTable[singleHalfMoves],
                            singleHalfMoves + 1,
                            principalVariationLen[singleHalfMoves + 1] - (singleHalfMoves + 1));

                principalVariationLen[singleHalfMoves] = principalVariationLen[singleHalfMoves+1];
            }
        }
        return alpha;
    }
}