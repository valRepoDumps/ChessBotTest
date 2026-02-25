package ChessResources.Pieces;

import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.ChessSpaces;
import ChessResources.HelperFuncs.BoardScan.BoardScan;
import ChessResources.HelperFuncs.BoardScan.ScanResult;
import ChessResources.HelperFuncs.PieceFunc;
import ChessResources.PreCalc;

import javax.swing.*;

import static ChessResources.PreCalc.SLIDING_MOVES;
import static ChessResources.PreCalc.getPreGenerateKingMoves;

public class MovingPieceData extends PieceData {

    //region PRE_CONSTRUCTOR
    public static final int NO_RANGE_LIMIT = ChessBoard.BOARD_SIZE;

    protected PieceFunc pieceFunc;

    //region PIECE FUNCS

    //region DIR_LIST
    public static final short[] BPAWN_DIR =
            new short[]{ChessBoard.SOUTH, ChessBoard.SOUTH_EAST, ChessBoard.SOUTH_WEST};
    public static final short[] BPAWN_CAPTURE_DIR = new short[]{ChessBoard.SOUTH_WEST, ChessBoard.SOUTH_EAST};

    public static final short[] WPAWN_DIR =
            new short[]{ChessBoard.NORTH, ChessBoard.NORTH_EAST, ChessBoard.NORTH_WEST};
    public static final short[] WPAWN_CAPTURE_DIR = new short[]{ChessBoard.NORTH_WEST, ChessBoard.NORTH_EAST};

    public static final short[] ROOK_DIR =
            new short[]{ChessBoard.NORTH, ChessBoard.SOUTH, ChessBoard.WEST, ChessBoard.EAST};
    public static final short[] BISHOP_DIR =
            new short[]{ChessBoard.NORTH_WEST, ChessBoard.NORTH_EAST, ChessBoard.SOUTH_WEST, ChessBoard.SOUTH_EAST};

    public static final short[] QUEEN_DIR =
            new short[]{ChessBoard.NORTH, ChessBoard.SOUTH, ChessBoard.WEST, ChessBoard.EAST,
            ChessBoard.NORTH_WEST, ChessBoard.NORTH_EAST, ChessBoard.SOUTH_WEST, ChessBoard.SOUTH_EAST};
    //endregion

    //region PIECE_FUNCS
    public static final PieceFunc ROOK_FUNC =
            (game, spaceId, out) -> {
                getSlidingMoves(spaceId, ROOK_DIR, NO_RANGE_LIMIT,game, out);
            };
    public static final PieceFunc BISHOP_FUNC =
            (game, spaceId, out) -> {
                getSlidingMoves(spaceId, BISHOP_DIR, NO_RANGE_LIMIT, game, out);
            };
    public static final PieceFunc QUEEN_FUNC =
            (game, spaceId, out) -> {
                getSlidingMoves(spaceId, QUEEN_DIR, NO_RANGE_LIMIT, game, out);
            };

    public static final PieceFunc BPAWN_FUNC =
            (game, spaceId, out) -> {
                int row = ChessBoard.getRow(spaceId);
                int range = (row == 1 ? 2 : 1);
                int EPTarget = game.getEnpassantTarget();

                ChessSpaces tmpSpace = new ChessSpaces();
                int[][] rays = SLIDING_MOVES[spaceId];


                for (short dir : BPAWN_DIR){
                    int[] ray = rays[dir];
                    tmpSpace.clear();
                    if (dir != ChessBoard.SOUTH) {
                        ScanResult sr = BoardScan.rayScan(game, ray, 1, tmpSpace);
                        if (tmpSpace.containSpace(EPTarget)){
                            //scanned space empty, but empty space is enpassant target.
                            out.addMoves(game.getEnpassantTarget());
                        }
                        else if (ScanResult.isValid(sr) && game.isEnemyPieceAt(sr.getSpaceId())){
                            //scanned a piece, is enemy
                            out.addMoves(sr.getSpaceId());
                        }
                    }
                    else {
                        BoardScan.rayScan(game, ray, range, out);
                    }
                }
            };

    public static final PieceFunc WPAWN_FUNC =
            (game, spaceId, out) -> {
                int row = ChessBoard.getRow(spaceId);
                int range = (row == ChessBoard.BOARD_SIZE-2 ? 2 : 1);

                int EPTarget = game.getEnpassantTarget();
                ChessSpaces tmpSpace = new ChessSpaces();
                int[][] rays = SLIDING_MOVES[spaceId];

                for (short dir : WPAWN_DIR){
                    int[] ray = rays[dir];
                    tmpSpace.clear();
                    if (dir != ChessBoard.NORTH) {
                        ScanResult sr = BoardScan.rayScan(game, ray, 1, tmpSpace);
                        if (tmpSpace.containSpace(EPTarget)){
                            //scanned space empty, but empty space is enpassant target.
                            out.addMoves(game.getEnpassantTarget());
                        }
                        else if (ScanResult.isValid(sr) && game.isEnemyPieceAt(sr.getSpaceId())){
                            //scanned a piece, is enemy
                            out.addMoves(sr.getSpaceId());
                        }
                    }
                    else {
                        BoardScan.rayScan(game, ray, range, out);
                    }
                }
            };

    //region KNIGHT_FUNCS
    public static final PieceFunc KNIGHT_FUNC =
            (game,  spaceId,  spaces) ->{
                ScanResult[] sqrs = BoardScan.jumpScan(game, PreCalc.getPreGenerateKnightMoves(spaceId), spaces);
                for (ScanResult sq : sqrs){
                    if (ScanResult.isValid(sq) && game.isEnemyPieceAt(sq.getSpaceId())){
                        spaces.addMoves(sq.getSpaceId());
                    }
                }
            };

    //endregion

    //region KING FUNCS
    public static PieceFunc WKING_FUNC =
            (game, spaceId, spaces) ->{
                int[] moves = PreCalc.getPreGenerateKingMoves(spaceId);
                for (int i = 0; i < moves.length; ++i){
                    if (!ChessBoard.isValidSpaceId(moves[i])){
                        continue;
                    }

                    if ((i == PreCalc.QUEEN_SIDE_CASTLE)) {
                        if (game.gameProperties[MinimalChessGame.WHITE_CASTLE_QUEEN]){
                            //meaning neither rook nor king has moved
                            ChessSpaces tmp = new ChessSpaces();

                            ScanResult sr = BoardScan.rayScan(game,
                                    PreCalc.SLIDING_MOVES[spaceId][ChessBoard.WEST],
                                    MovingPieceData.NO_RANGE_LIMIT, tmp);
                            if (ScanResult.isValid(sr) && sr.getPieceId() == PieceData.WROOK){
                                for (int sq : tmp.getChessMoves()){
                                    if (!game.spaceNotUnderThreatAndEmpty(sq)) return;
                                }
                                spaces.addMoves(moves[i]);}
                        }
                    }
                    else if ((i == PreCalc.KING_SIDE_CASTLE)) {
                        if (game.gameProperties[MinimalChessGame.WHITE_CASTLE_KING]){
                            //meaning neither rook nor king has moved
                            ChessSpaces tmp = new ChessSpaces();

                            ScanResult sr = BoardScan.rayScan(game,
                                    PreCalc.SLIDING_MOVES[spaceId][ChessBoard.EAST],
                                    MovingPieceData.NO_RANGE_LIMIT, tmp);

                            if (ScanResult.isValid(sr) && sr.getPieceId() == PieceData.WROOK){
                                for (int sq : tmp.getChessMoves()){
                                    if (!game.spaceNotUnderThreatAndEmpty(sq)) return;
                                }
                                spaces.addMoves(moves[i]);}
                        }
                    }
                    else if (game.spaceNotUnderThreatAndEmpty(moves[i])){
                        spaces.addMoves(moves[i]);
                    }
                    else if (game.isEnemyPieceAt(moves[i]) && game.spaceNotUnderThreat(moves[i])){

                        spaces.addMoves(moves[i]);
                    }
                }
            };

    public static PieceFunc BKING_FUNC =
            (game, spaceId, spaces) ->{
                int[] moves = getPreGenerateKingMoves(spaceId);
                for (int i = 0; i < moves.length; ++i){
                    if (!ChessBoard.isValidSpaceId(moves[i])){
                        continue;
                    }
                    if ((i == PreCalc.QUEEN_SIDE_CASTLE)) {
                        if (game.gameProperties[MinimalChessGame.BLACK_CASTLE_QUEEN]){
                            //meaning neither rook nor king has moved
                            ChessSpaces tmp = new ChessSpaces();

                            ScanResult sr = BoardScan.rayScan(game,
                                    PreCalc.SLIDING_MOVES[spaceId][ChessBoard.WEST],
                                    MovingPieceData.NO_RANGE_LIMIT, tmp);
                            if (ScanResult.isValid(sr) && sr.getPieceId() == PieceData.BROOK){
                                for (int sq : tmp.getChessMoves()){
                                    if (!game.spaceNotUnderThreatAndEmpty(sq)) return;
                                }
                                spaces.addMoves(moves[i]);}
                        }
                    }
                    else if ((i == PreCalc.KING_SIDE_CASTLE)) {
                        if (game.gameProperties[MinimalChessGame.BLACK_CASTLE_KING]){
                            //meaning neither rook nor king has moved
                            ChessSpaces tmp = new ChessSpaces();

                            ScanResult sr = BoardScan.rayScan(game,
                                    PreCalc.SLIDING_MOVES[spaceId][ChessBoard.EAST],
                                    MovingPieceData.NO_RANGE_LIMIT, tmp);

                            if (ScanResult.isValid(sr) && sr.getPieceId() == PieceData.BROOK){
                                for (int sq : tmp.getChessMoves()){
                                    if (!game.spaceNotUnderThreatAndEmpty(sq)) return;
                                }
                                spaces.addMoves(moves[i]);}
                        }
                    }
                    else if (game.spaceNotUnderThreatAndEmpty(moves[i])){
                        spaces.addMoves(moves[i]);
                    }
                    else if (game.isEnemyPieceAt(moves[i]) && game.spaceNotUnderThreat(moves[i])){

                        spaces.addMoves(moves[i]);
                    }
                }
            };


    //endregion

    //endregion

    //endregion

    //endregion

    //region CONSTRUCTOR

    @SuppressWarnings("unused")
    MovingPieceData(short pieceId, boolean color, String name, ImageIcon graphic,
                    PieceFunc func)
    {
        super(pieceId, color, name, graphic);
        this.pieceFunc = func;
    }


    public MovingPieceData(MovingPieceData piece)
    {
        super(piece);
    }

    //endregion

    @Override
    public MovingPieceData clone(){
        return new MovingPieceData(pieceId, color, name, graphic, pieceFunc);
    }

    @Override
    public MovingPieceData getUniqueClone(){
        return new MovingPieceData(pieceId, color, name, graphic, pieceFunc);
    }

    public void getPossibleMoves(MinimalChessGame<? extends ChessBoard>game,
                                                           int spaceId, ChessSpaces spaces){
        pieceFunc.apply(game, spaceId, spaces);
    }

    private static void getSlidingMoves(
            int spaceId, short[] directions, int range,
            MinimalChessGame<? extends ChessBoard> game, ChessSpaces spaces){
        BoardScan.rayScanMultiDirectional(game, SLIDING_MOVES[spaceId],
                range, directions, spaces, true);
    }

}

