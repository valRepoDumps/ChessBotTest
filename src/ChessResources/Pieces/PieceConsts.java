package ChessResources.Pieces;

import javax.swing.*;

public interface PieceConsts {
    //region PIECE_DATA_CONSTS
    //endregion

    //region PIECES_CONSTS
    public static final PieceData BPAWN_DATA = new PieceData(
            PieceData.BPAWN, PieceData.BLACK, "black_pawn",
            new ImageIcon("resources/ChessBoard/ChessPieces/black_pawn.png"));

    public static final PieceData WPAWN_DATA = new PieceData(
            PieceData.WPAWN, PieceData.WHITE, "white_pawn",
            new ImageIcon("resources/ChessBoard/ChessPieces/white_pawn.png"));

    public static final PieceData BROOK_DATA = new PieceData(
            PieceData.BROOK, PieceData.BLACK, "black_rook",
            new ImageIcon("resources/ChessBoard/ChessPieces/black_rook.png"));

    public static final PieceData WROOK_DATA = new PieceData(
            PieceData.WROOK, PieceData.WHITE, "white_rook",
            new ImageIcon("resources/ChessBoard/ChessPieces/white_rook.png"));

    public static final PieceData BBISHOP_DATA = new PieceData(
            PieceData.BBISHOP, PieceData.BLACK, "black_bishop",
            new ImageIcon("resources/ChessBoard/ChessPieces/black_bishop.png"));

    public static final PieceData WBISHOP_DATA = new PieceData(
            PieceData.WBISHOP, PieceData.WHITE, "white_bishop",
            new ImageIcon("resources/ChessBoard/ChessPieces/white_bishop.png"));

    public static final PieceData BQUEEN_DATA = new PieceData(
            PieceData.BQUEEN, PieceData.BLACK, "black_queen",
            new ImageIcon("resources/ChessBoard/ChessPieces/black_queen.png"));

    public static final PieceData WQUEEN_DATA = new PieceData(
            PieceData.WQUEEN, PieceData.WHITE, "white_queen",
            new ImageIcon("resources/ChessBoard/ChessPieces/white_queen.png"));

    public static final PieceData BKING_DATA = new PieceData(
            PieceData.BKING, PieceData.BLACK, "black_king",
            new ImageIcon("resources/ChessBoard/ChessPieces/black_king.png"));
    public static final PieceData WKING_DATA = new PieceData(
            PieceData.WKING, PieceData.WHITE, "white_king",
            new ImageIcon("resources/ChessBoard/ChessPieces/white_king.png"));

    public static final PieceData BKNIGHT_DATA = new PieceData(
            PieceData.BKNIGHT, PieceData.BLACK, "black_knight",
            new ImageIcon("resources/ChessBoard/ChessPieces/black_knight.png"));
    public static final PieceData WKNIGHT_DATA = new PieceData(
            PieceData.WKNIGHT, PieceData.WHITE, "white_knight",
            new ImageIcon("resources/ChessBoard/ChessPieces/white_knight.png"));

    public static final PieceData NO_PIECE = null;
    //endregion

    //region MISC_CONST
    public static final short[] INF_RANGE_DIAGONAL_MOVE_PIECE_WHITE = new short[]{PieceData.WBISHOP, PieceData.WQUEEN};
    public static final short[] INF_RANGE_DIAGONAL_MOVE_PIECE_BLACK = new short[]{PieceData.BBISHOP, PieceData.BQUEEN};

    public static final short[] INF_RANGE_STRAIGHT_MOVE_PIECE_WHITE = new short[]{PieceData.WROOK, PieceData.WQUEEN};
    public static final short[] INF_RANGE_STRAIGHT_MOVE_PIECE_BLACK = new short[]{PieceData.BROOK, PieceData.BQUEEN};

    //endregion
}
