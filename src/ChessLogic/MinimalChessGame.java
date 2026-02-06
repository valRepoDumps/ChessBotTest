package ChessLogic;

import ChessResources.ChessBoard.ChessBoard;
import ChessResources.ChessBoard.ChessBoardUI;
import ChessResources.ChessErrors.OutOfOldTurns;
import ChessResources.ChessHistoryTracker.BoardStateChanges.BoardStateChange;
import ChessResources.ChessHistoryTracker.BoardStateChanges.PropertiesStatsChange;
import ChessResources.ChessHistoryTracker.ChessHistoryTracker;
import ChessResources.ChessHistoryTracker.GameStateChanges;
import ChessResources.ChessListener.StateChangeListener;
import ChessResources.GetMovesLogic.ChessSpaces;
import ChessResources.Pieces.PieceData;
import ChessResources.Pieces.PieceDatas;
import ChessResources.GetMovesLogic.PossibleMoves;

import java.util.ArrayList;
import java.util.function.BiFunction;

public class MinimalChessGame<Board extends ChessBoard> {
    //region GAME_DATAS
    public final Board chessBoard;
    BiFunction<Integer, Boolean, Short> choosePromotionPiece;

    PossibleMoves[] possibleMoves;
    static final int BLACK_PM = 0;
    static final int WHITE_PM = 1;

    ChessHistoryTracker chessHistoryTracker = new ChessHistoryTracker();
    //endregion
    //region GAME_TRACKER
    //region GAME_PROPERTIES
    public boolean[] gameProperties = new boolean[5];

    public static int BLACK_CASTLE_QUEEN = 0;
    public static int BLACK_CASTLE_KING = 1;

    public static int WHITE_CASTLE_QUEEN = 2;
    public static int WHITE_CASTLE_KING = 3;

    public static int SIDE_TO_MOVE = 4;
    //endregion
    
    //region GAME_STATS
    public int[] gameStats = new int[5];

    public static int ENPASSANT_TARGET = 0; //spaceId format.
    public static final int HALF_MOVES_SICE_CAPTURE_OR_PAWN_MOVES = 1;
    public static final int TOTAL_MOVES_ELAPSED = 2;
    public static final int WHITE_KING_SPACEID = 3;
    public static final int BLACK_KING_SPACEID = 4;
    public static final int INVALID_ENPASSANT_TARGET = -1;

    private final ArrayList<StateChangeListener<PropertiesStatsChange>>
            propertiesStatsChangeListenerList = new ArrayList<>();
    //endregion
    //endregion

    //region SAVE_GAME_OPCODE
    public static final int NORMAL_MOVE = 0;
    public static final int PROMOTION = 1;
    public static final int ENPASSANT = 2;
    //endregion

    //region LAMBDAS
    public static final BiFunction<Integer, Boolean, Short> DEFAULT_PROMOTION_FUNC =
            (Integer spaceId, Boolean color) -> {
                if (color == PieceData.BLACK) return PieceData.BQUEEN;
                else return PieceData.WQUEEN;
            };

    public final StateChangeListener<BoardStateChange> BOARD_STATE_LOGGER =
            (BoardStateChange boardStateChange)->{
        this.chessHistoryTracker.pushBoardStateChange(boardStateChange);
        System.out.println("Board State Change: " + boardStateChange);
    };

    public final StateChangeListener<PropertiesStatsChange> PROPERTIES_STATS_LOGGER =
            (PropertiesStatsChange propertiesStatsChange)->{
        this.chessHistoryTracker.setGamePropertiesStats(propertiesStatsChange);
        System.out.println("Prop/Stats Change: " + propertiesStatsChange);
    };
    //endregion

    //region Call??
    public MinimalChessGame(Board chessBoard, BiFunction<Integer, Boolean, Short> choosePromotionPiece)
    {
        this.chessBoard = chessBoard;
        this.choosePromotionPiece = choosePromotionPiece;

        this.chessBoard.addMoveListener(BOARD_STATE_LOGGER);
        this.addPropertiesStatsChangeListener(PROPERTIES_STATS_LOGGER);
    }
    public MinimalChessGame(String fen, Board chessBoard, BiFunction<Integer, Boolean, Short> choosePromotionPiece)
    {
        String[] args = fen.trim().split(" ");
        this.chessBoard = chessBoard;
        this.choosePromotionPiece = choosePromotionPiece;

        this.chessBoard.addMoveListener(BOARD_STATE_LOGGER);
        addPropertiesStatsChangeListener(PROPERTIES_STATS_LOGGER);

        possibleMoves = new PossibleMoves[]{new PossibleMoves(this, PieceData.BLACK),
                new PossibleMoves(this, PieceData.WHITE)};

        //fen translator.

        if (args.length != 6) {
            throw new IllegalArgumentException("Invalid FEN: Must include 6 arguments.");
        }

        //region ASSIGN_SIDE_TO_MOVE
        if (args[1].equals("w")) gameProperties[SIDE_TO_MOVE] = PieceData.WHITE;
        else if (args[1].equals("b")) gameProperties[SIDE_TO_MOVE] = PieceData.BLACK;
        else
            throw new IllegalArgumentException("Invalid FEN: Side to Move is invalid (" + args[1] + ").");
        //endregion

        //region ASSIGN_CASTLING_RIGHTS
        gameProperties[BLACK_CASTLE_KING]  = args[2].contains("k");
        gameProperties[BLACK_CASTLE_QUEEN] = args[2].contains("q");
        gameProperties[WHITE_CASTLE_KING]  = args[2].contains("K");
        gameProperties[WHITE_CASTLE_QUEEN] = args[2].contains("Q");
        //endregion

        //region ASSIGN_EN_PASSANT_TARGET
        if (args[3].length() == 1 && args[3].equals("-")) {
            gameStats[ENPASSANT_TARGET] = -1;
        } else if (args[3].length() == 2 && Character.isAlphabetic(args[3].charAt(0))
                && Character.isDigit(args[3].charAt(1))) {
            gameStats[ENPASSANT_TARGET] = ChessBoardUI.convertSquareNotationToSpaceId(
                    args[3].charAt(0), args[3].charAt(1));
        } else
            throw new IllegalArgumentException("Invalid files and ranks input (" + args[3] + ").");
        //endregion

        gameStats[HALF_MOVES_SICE_CAPTURE_OR_PAWN_MOVES] = Integer.parseInt(args[4]);
        gameStats[TOTAL_MOVES_ELAPSED] = Integer.parseInt(args[5]);

        //region ASSIGN_KINGS_LOCATION
        gameStats[WHITE_KING_SPACEID] = chessBoard.findPiece(PieceDatas.WKING_DATA.pieceId);
        gameStats[BLACK_KING_SPACEID] = chessBoard.findPiece(PieceDatas.BKING_DATA.pieceId);
        //endregion

        possibleMoves[gameProperties[SIDE_TO_MOVE] == PieceData.WHITE? BLACK_PM:WHITE_PM].generateMoves();
        possibleMoves[gameProperties[SIDE_TO_MOVE] == PieceData.WHITE? WHITE_PM:BLACK_PM].generateMoves();

    }
    //endregion

    //region HELPERS
    public boolean movePiece(int spaceIdToMove, int spaceIdArriveAt)
    {
        StateChangeListener.<PropertiesStatsChange>notifyListeners(propertiesStatsChangeListenerList,
                new PropertiesStatsChange(gameProperties, gameStats)); //announce new rules added.

        int promotionSpaceId = -1; //should doubled as check to see if promotion is possible.
        int opCode = NORMAL_MOVE;
        PieceData piece = chessBoard.getPiece(spaceIdToMove);
        PieceData secondPiece = null;

        if (possibleMoves[gameProperties[SIDE_TO_MOVE] == PieceData.WHITE? WHITE_PM : BLACK_PM]
                .possibleMoves.containsKey(spaceIdToMove) &&
                possibleMoves[gameProperties[SIDE_TO_MOVE] == PieceData.WHITE? WHITE_PM : BLACK_PM]
                        .possibleMoves.get(spaceIdToMove).containSpace(spaceIdArriveAt)
        ) {
            int spaceIdCaptureAt = spaceIdArriveAt;

            gameStats[ENPASSANT_TARGET] = INVALID_ENPASSANT_TARGET;
            if (piece.pieceId == PieceData.BPAWN || piece.pieceId == PieceData.WPAWN)
            {
                //region EN_PASSANT_LOGIC
                if (chessBoard.isEmptySpaceAt(spaceIdArriveAt) && (
                        ChessBoard.isImmediateWest(spaceIdToMove, spaceIdArriveAt) ||
                                ChessBoard.isImmediateEast(spaceIdToMove, spaceIdArriveAt)))
                {//if the spaceIdArriveAt is east or west of pawn, must be diagonal move. With no piece -> enpassant

                    short northId = chessBoard.getPieceIdAt(spaceIdArriveAt +
                            ChessBoard.directionOffsets[ChessBoard.NORTH]);
//                    short southId = chessBoard.getPieceIdAt(spaceIdArriveAt +
//                            ChessBoard.directionOffsets[ChessBoard.SOUTH]);

                    if (northId == PieceData.BPAWN || northId == PieceData.WPAWN)
                    {
                        spaceIdCaptureAt = spaceIdArriveAt + ChessBoard.directionOffsets[ChessBoard.NORTH];
                    }
                    else //assumes once pawn do enpassant, must exist a valid capture piece.
                    {
                        spaceIdCaptureAt = spaceIdArriveAt + ChessBoard.directionOffsets[ChessBoard.SOUTH];
                    }
                    opCode = ENPASSANT;
                    secondPiece = chessBoard.getPiece(spaceIdCaptureAt);
                }
                //en passant logic
                if (Math.abs(ChessBoard.getRow(spaceIdToMove) - ChessBoard.getRow(spaceIdArriveAt)) == 2) //pawn moved 2 space
                {
                    if (piece.pieceId == PieceData.BPAWN)
                    {
                        this.gameStats[ENPASSANT_TARGET] = spaceIdArriveAt + ChessBoard.directionOffsets[ChessBoard.NORTH];
                        //no need to check column and row validity, always exist North in case pawn moved 2 space in standard board.
                    }
                    else //case white pawn
                    {
                        this.gameStats[ENPASSANT_TARGET] = spaceIdArriveAt + ChessBoard.directionOffsets[ChessBoard.SOUTH];
                        //same as above.
                    }
                }
                //endregion

                //region PROMOTION_LOGIC
                if (piece.pieceId == PieceData.BPAWN && ChessBoard.getRow(spaceIdArriveAt) == ChessBoard.BOARD_SIZE-1)
                    //check pawn color, do not assume pawn dont go backward or start at like row 0 or 8.
                {
                    promotionSpaceId = spaceIdArriveAt;
                    opCode = PROMOTION;
                }
                else if (piece.pieceId == PieceData.WPAWN && ChessBoard.getRow(spaceIdArriveAt) == 0)
                {
                    promotionSpaceId = spaceIdArriveAt;
                    opCode = PROMOTION;
                }
                //endregion
            }

            //region CASTLING_LOGIC
            if (piece.pieceId == PieceData.WKING)
            {
                if (ChessBoard.getCol(spaceIdToMove) - ChessBoard.getCol(spaceIdArriveAt) > 1)
                {
                    int rookSpaceId = ChessBoard.BOARD_SIZE*(ChessBoard.BOARD_SIZE-1);
                    int rookMoveId = ChessBoard.getEastSpaceId(spaceIdArriveAt, 1); //rook should be to the east of new king.
                    assert(gameProperties[WHITE_CASTLE_QUEEN]); //should be true
                    assert(chessBoard.boardSquares[rookSpaceId].equals(PieceDatas.WROOK_DATA));
                    chessBoard.movePieceCapture(rookSpaceId, rookMoveId, rookMoveId);
                }
                else if (ChessBoard.getCol(spaceIdToMove) - ChessBoard.getCol(spaceIdArriveAt) < -1)
                {
                    int rookSpaceId = ChessBoard.BOARD_SIZE* ChessBoard.BOARD_SIZE-1;
                    int rookMoveId = ChessBoard.getWestSpaceId(spaceIdArriveAt, 1); //rook should be to the west of new king.
                    assert(gameProperties[WHITE_CASTLE_KING]); //should be true
                    assert(chessBoard.boardSquares[rookSpaceId].equals(PieceDatas.WROOK_DATA));
                    chessBoard.movePieceCapture(rookSpaceId, rookMoveId, rookMoveId);
                }

                gameProperties[WHITE_CASTLE_QUEEN] = false;
                gameProperties[WHITE_CASTLE_KING] = false;
            }
            else if (piece.pieceId == PieceData.BKING)
            {
                if (ChessBoard.getCol(spaceIdToMove) - ChessBoard.getCol(spaceIdArriveAt) > 1)
                {
                    int rookSpaceId = 0;
                    int rookMoveId = ChessBoard.getEastSpaceId(spaceIdArriveAt, 1); //rook should be to the east of new king.
                    assert(gameProperties[BLACK_CASTLE_QUEEN]); //should be true
                    assert(chessBoard.boardSquares[rookSpaceId].equals(PieceDatas.BROOK_DATA));
                    chessBoard.movePieceCapture(rookSpaceId, rookMoveId, rookMoveId);
                }
                else if (ChessBoard.getCol(spaceIdToMove) - ChessBoard.getCol(spaceIdArriveAt) < -1)
                {
                    int rookSpaceId = ChessBoard.BOARD_SIZE-1;
                    int rookMoveId = ChessBoard.getWestSpaceId(spaceIdArriveAt, 1); //rook should be to the west of new king.
                    assert(gameProperties[BLACK_CASTLE_KING]); //should be true
                    assert(chessBoard.boardSquares[rookSpaceId].equals(PieceDatas.BROOK_DATA));
                    chessBoard.movePieceCapture(rookSpaceId, rookMoveId, rookMoveId);
                }
                gameProperties[BLACK_CASTLE_QUEEN] = false;
                gameProperties[BLACK_CASTLE_KING] = false;
            }
            else if (piece.pieceId == PieceData.WROOK) //these rook funcs wont work if i ever want to implement chess960.
            {
                if (gameProperties[WHITE_CASTLE_KING] && ChessBoard.getCol(spaceIdToMove) == 7)
                {
                    gameProperties[WHITE_CASTLE_KING] = false;
                }
                else if (gameProperties[WHITE_CASTLE_QUEEN] && ChessBoard.getCol(spaceIdToMove) == 0)
                {
                    gameProperties[WHITE_CASTLE_QUEEN] = false;
                }
            }
            else if (piece.pieceId == PieceData.BROOK)
            {
                if (gameProperties[BLACK_CASTLE_KING] && ChessBoard.getCol(spaceIdToMove) == 7)
                {
                    gameProperties[BLACK_CASTLE_KING] = false;
                }
                else if (gameProperties[BLACK_CASTLE_QUEEN] && ChessBoard.getCol(spaceIdToMove) == 0)
                {
                    gameProperties[BLACK_CASTLE_QUEEN] = false;
                }
            }
            //endregion

            //region WINNING_LOGIC
            if (chessBoard.getPieceIdAt(spaceIdArriveAt) == PieceData.BKING)
            {
                System.out.println("WHITE WIN!!!");
                endGame(ChessBoard.BLACK);
            }
            else if (chessBoard.getPieceIdAt(spaceIdArriveAt) == PieceData.WKING)
            {
                System.out.println("BLACK WIN!!!");
                endGame((ChessBoard.WHITE));
            }
            //endregion

            //region ACTUALLY_MOVING_THE_PIECE
            chessBoard.movePieceCapture(spaceIdToMove, spaceIdArriveAt, spaceIdCaptureAt);
            if (opCode == PROMOTION) //make no further checks, shouldnt be invalid.
            {
                secondPiece = PieceDatas.getCopyOfPiece(choosePromotionPiece.apply(spaceIdArriveAt, piece.color));
                promotionHandler(spaceIdArriveAt, piece.color, secondPiece);
            }
            //endregion

            //region SAVE_GAME_P&P
            finishTurn();
            //endregion

            //region UPDATING_GAME_PROPERTIES_AND STATS
            gameProperties[SIDE_TO_MOVE] = !gameProperties[SIDE_TO_MOVE];//change move sid
            //endregion

            //region GENERATE_POSSIBLE_MOVES
            //generate possible moves after changing side.
            // Doing it this way ensure when a new moves is generated, new piece location information is taken into account
            possibleMoves[gameProperties[SIDE_TO_MOVE] == PieceData.WHITE? BLACK_PM:WHITE_PM].clearPossibleMoves();
            possibleMoves[gameProperties[SIDE_TO_MOVE] == PieceData.WHITE? BLACK_PM:WHITE_PM].generateMoves();

            possibleMoves[gameProperties[SIDE_TO_MOVE] == PieceData.WHITE? WHITE_PM:BLACK_PM].clearPossibleMoves();
            possibleMoves[gameProperties[SIDE_TO_MOVE] == PieceData.WHITE? WHITE_PM:BLACK_PM].generateMoves();
            return true;
            //endregion
        }
        return false;
    }

    public boolean spaceNotUnderThreat(int spaceId, boolean color)
    {
        //ensure spaceId valid.
        for (ChessSpaces moves : possibleMoves[color == PieceData.WHITE? BLACK_PM : WHITE_PM].possibleMoves.values())
        {
            if (moves.containSpace(spaceId))
            {
                return false;
            }
        }
        return true;
    }

    public boolean spaceNotUnderThreatAndEmpty(int spaceId, boolean color)
    {
        return spaceNotUnderThreat(spaceId, color) && chessBoard.isEmptySpaceAt(spaceId);
    }

    public boolean kingNotUnderFutureThreat(int spaceIdToMove, int spaceIdArriveAt, boolean color){
        boolean ans;
        movePiece(spaceIdToMove, spaceIdArriveAt);
        //int kingCurrentSpaceId = chessBoard.
        return true;
    }

    public boolean spaceNotUnderThreatAndNoAllies(int spaceId, boolean color)
    {
        return spaceNotUnderThreat(spaceId, color) && !chessBoard.isAlliedPieceAt(spaceId, color);
    }

    private void promotionHandler(int spaceId, boolean color, PieceData piece)
    {
        chessBoard.spawnPieceAt(spaceId, piece);
    }

    private void finishTurn()
    {
        System.out.println("Finish turn");
        chessHistoryTracker.pushTurn();
        gameStats[HALF_MOVES_SICE_CAPTURE_OR_PAWN_MOVES]++;

//        StateChangeListener.<PropertiesStatsChange>notifyListeners(propertiesStatsChangeListenerList,
//                new PropertiesStatsChange(gameProperties, gameStats));
    }

    private void endGame(boolean sideWon)
    {

        System.out.println(chessHistoryTracker);
        System.exit(0);

    }
    //endregion
    public void undoTurn() throws OutOfOldTurns, NullPointerException
    {
        if (chessHistoryTracker == null) throw new NullPointerException("chessHistoryTracker is null!");

        if(chessHistoryTracker.isEmpty()) throw new OutOfOldTurns("");

        GameStateChanges gameStateChanges = chessHistoryTracker.popTurn();
        chessHistoryTracker.clearCurrentTurn();

        this.gameStats = gameStateChanges.getGameStats();
        this.gameProperties = gameStateChanges.getGameProperties();

        chessBoard.undoBoardState(gameStateChanges.getBoardStateChanges()); //throws nullpointerexcep

        //region GENERATE_POSSIBLE_MOVES
        //generate possible moves after changing side.
        // Doing it this way ensure when a new moves is generated, new piece location information is taken into account
        possibleMoves[gameProperties[SIDE_TO_MOVE] == PieceData.WHITE? BLACK_PM:WHITE_PM].clearPossibleMoves();
        possibleMoves[gameProperties[SIDE_TO_MOVE] == PieceData.WHITE? BLACK_PM:WHITE_PM].generateMoves();

        possibleMoves[gameProperties[SIDE_TO_MOVE] == PieceData.WHITE? WHITE_PM:BLACK_PM].clearPossibleMoves();
        possibleMoves[gameProperties[SIDE_TO_MOVE] == PieceData.WHITE? WHITE_PM:BLACK_PM].generateMoves();
        //endregion

    }

    //region LISTENERS
    public void addMoveListener(StateChangeListener<BoardStateChange> stateChangeListener)
    {
        chessBoard.addMoveListener(stateChangeListener);
    }

    public void addPropertiesStatsChangeListener(StateChangeListener<PropertiesStatsChange> listener){
        propertiesStatsChangeListenerList.add(listener);
    }
    //endregion

}
