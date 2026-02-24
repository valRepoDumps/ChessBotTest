package ChessResources.Pieces;

import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.ChessSpaces;
import ChessResources.HelperFuncs.BoardScan.BoardScan;
import ChessResources.HelperFuncs.BoardScan.ScanResult;
import ChessResources.HelperFuncs.PieceFunc;

import javax.swing.*;
import java.util.UUID;

import static ChessResources.PreCalc.SLIDING_MOVES;

public class SlidingPieceData extends PieceData {

    //region PRE_CONSTRUCTOR
    public static final int NO_RANGE_LIMIT = ChessBoard.BOARD_SIZE;

    protected PieceFunc pieceFunc;

    //region PIECE FUNCS

    //region DIR_LIST
    public static final short[] BPAWN_DIR =
            new short[]{ChessBoard.SOUTH, ChessBoard.SOUTH_EAST, ChessBoard.SOUTH_WEST};
    public static final short[] WPAWN_DIR =
            new short[]{ChessBoard.NORTH, ChessBoard.NORTH_EAST, ChessBoard.NORTH_WEST};

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
    //endregion

    //endregion

    //endregion

    //region CONSTRUCTOR
    SlidingPieceData(short pieceId)
    {
        super(pieceId);
        switch(pieceId)
        {
            case BPAWN:
                this.pieceFunc = BPAWN_FUNC;
                break;
            case WPAWN:
                this.pieceFunc = WPAWN_FUNC;
                break;
            case BROOK: case WROOK:
                this.pieceFunc = ROOK_FUNC;
                break;
            case BBISHOP: case WBISHOP:
                this.pieceFunc = BISHOP_FUNC;
                break;
            case BQUEEN: case WQUEEN:
                this.pieceFunc = QUEEN_FUNC;
                break;
        }
    }

    @SuppressWarnings("unused")
    SlidingPieceData(short pieceId, boolean color, int value, String name, ImageIcon graphic,
                     PieceFunc func)
    {
        super(pieceId, color, value, name, graphic);
        this.pieceFunc = func;
    }

    SlidingPieceData(UUID uuid, short pieceId, boolean color, int value, String name, ImageIcon graphic,
                     PieceFunc func)
    {
        super(uuid, pieceId, color, value, name, graphic);
        this.pieceFunc = func;
    }

    public SlidingPieceData(SlidingPieceData piece)
    {
        super(piece);
    }

    //endregion

    @Override
    public SlidingPieceData clone(){
        return new SlidingPieceData(uuid, pieceId, color, value, name, graphic, pieceFunc);
    }

    @Override
    public SlidingPieceData getUniqueClone(){
        return new SlidingPieceData(pieceId, color, value, name, graphic, pieceFunc);
    }

    public void getPossibleMoves(MinimalChessGame<? extends ChessBoard>game,
                                                           int spaceId, ChessSpaces spaces){
        pieceFunc.apply(game, spaceId, spaces);
    }

    private static final void getSlidingMoves(
            int spaceId, short[] directions, int range,
            MinimalChessGame<? extends ChessBoard> game, ChessSpaces spaces){
        BoardScan.rayScanMultiDirectional(game, SLIDING_MOVES[spaceId],
                range, directions, spaces, true);
    }

}

