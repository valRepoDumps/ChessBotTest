package ChessResources.Pieces.PieceDatas;

import ChessResources.ChessBoard;

public class PieceDatas
{
    //region PIECES_CONSTS
    public static final PieceData MOVED_BPAWN_DATA = new ConditionalSlidingPieceData(PieceData.BPAWN,
            new short[]{ChessBoard.SOUTH}, 1, false, 2);
    public static final PieceData MOVED_WPAWN_DATA = new ConditionalSlidingPieceData(PieceData.WPAWN,
            new short[]{ChessBoard.NORTH}, 1, false, 2);

    public static final PieceData BPAWN_DATA = new ConditionalSlidingPieceData(PieceData.BPAWN,
            new short[]{ChessBoard.SOUTH}, 1, true, 2);
    public static final PieceData WPAWN_DATA = new ConditionalSlidingPieceData(PieceData.WPAWN,
            new short[]{ChessBoard.NORTH}, 1, true, 2);
    public static final PieceData BROOK_DATA = new SlidingPieceData(PieceData.BROOK,
            new short[] {ChessBoard.NORTH, ChessBoard.SOUTH,
            ChessBoard.WEST, ChessBoard.EAST}, SlidingPieceData.NO_RANGE_LIMIT);
    public static final PieceData WROOK_DATA = new SlidingPieceData(PieceData.WROOK,
            new short[] {ChessBoard.NORTH, ChessBoard.SOUTH,
                    ChessBoard.WEST, ChessBoard.EAST}, SlidingPieceData.NO_RANGE_LIMIT);
    public static final PieceData BBISHOP_DATA = new SlidingPieceData(PieceData.BBISHOP,
            new short[] {ChessBoard.NORTH_WEST, ChessBoard.NORTH_EAST,
                    ChessBoard.SOUTH_WEST, ChessBoard.SOUTH_EAST}, SlidingPieceData.NO_RANGE_LIMIT);
    public static final PieceData WBISHOP_DATA = new SlidingPieceData(PieceData.WBISHOP,
            new short[] {ChessBoard.NORTH_WEST, ChessBoard.NORTH_EAST,
                    ChessBoard.SOUTH_WEST, ChessBoard.SOUTH_EAST}, SlidingPieceData.NO_RANGE_LIMIT);
    public static final PieceData BQUEEN_DATA = new SlidingPieceData(PieceData.BQUEEN,
            new short[] {ChessBoard.NORTH, ChessBoard.SOUTH,
                    ChessBoard.WEST, ChessBoard.EAST, ChessBoard.NORTH_WEST, ChessBoard.NORTH_EAST,
                    ChessBoard.SOUTH_WEST, ChessBoard.SOUTH_EAST}, SlidingPieceData.NO_RANGE_LIMIT);
    public static final PieceData WQUEEN_DATA = new SlidingPieceData(PieceData.WQUEEN,
            new short[] {ChessBoard.NORTH, ChessBoard.SOUTH,
                    ChessBoard.WEST, ChessBoard.EAST, ChessBoard.NORTH_WEST, ChessBoard.NORTH_EAST,
                    ChessBoard.SOUTH_WEST, ChessBoard.SOUTH_EAST}, SlidingPieceData.NO_RANGE_LIMIT);
    public static final PieceData BKING_DATA = new SlidingPieceData(PieceData.BKING,
            new short[] {ChessBoard.NORTH, ChessBoard.SOUTH,
                    ChessBoard.WEST, ChessBoard.EAST, ChessBoard.NORTH_WEST, ChessBoard.NORTH_EAST,
                    ChessBoard.SOUTH_WEST, ChessBoard.SOUTH_EAST}, 1);
    public static final PieceData WKING_DATA = new SlidingPieceData(PieceData.WKING,
            new short[] {ChessBoard.NORTH, ChessBoard.SOUTH,
                    ChessBoard.WEST, ChessBoard.EAST, ChessBoard.NORTH_WEST, ChessBoard.NORTH_EAST,
                    ChessBoard.SOUTH_WEST, ChessBoard.SOUTH_EAST}, 1);

    public static final PieceData BKNIGHT_DATA = new PieceData(PieceData.BKNIGHT);
    public static final PieceData WKNIGHT_DATA = new PieceData(PieceData.WKNIGHT);

    public static final PieceData NO_PIECE = null;
    public final static int PIECES_DIFF = 16;
    //endregion

    public PieceDatas()
    {
    }

    public static PieceData makePiece(short pieceId)
    {
        return switch (pieceId) {
            case PieceData.BPAWN, PieceData.WPAWN -> new ConditionalSlidingPieceData(pieceId);
            case PieceData.BROOK, PieceData.WROOK, PieceData.BBISHOP, PieceData.WBISHOP, PieceData.BQUEEN,
                 PieceData.WQUEEN, PieceData.BKING, PieceData.WKING -> new SlidingPieceData(pieceId);
            default -> null;
        };
    }

    public static ConditionalSlidingPieceData makePawn(short pieceId, boolean pawnMoved)
    {
        return new ConditionalSlidingPieceData(pieceId, pawnMoved);
    }
}