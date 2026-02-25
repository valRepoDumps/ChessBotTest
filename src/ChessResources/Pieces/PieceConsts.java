package ChessResources.Pieces;

import javax.swing.*;

public interface PieceConsts {
    //region PIECE_DATA_CONSTS
    //endregion

    //region PIECES_CONSTS
    public static final PieceData BPAWN_DATA = new MovingPieceData(
            PieceData.BPAWN, PieceData.BLACK, "black_pawn",
            new ImageIcon("resources/ChessBoard/ChessPieces/black_pawn.png"),
            MovingPieceData.BPAWN_FUNC);

    public static final PieceData WPAWN_DATA = new MovingPieceData(
            PieceData.WPAWN, PieceData.WHITE, "white_pawn",
            new ImageIcon("resources/ChessBoard/ChessPieces/white_pawn.png"),
            MovingPieceData.WPAWN_FUNC);

    public static final PieceData BROOK_DATA = new MovingPieceData(
            PieceData.BROOK, PieceData.BLACK, "black_rook",
            new ImageIcon("resources/ChessBoard/ChessPieces/black_rook.png"),
            MovingPieceData.ROOK_FUNC);

    public static final PieceData WROOK_DATA = new MovingPieceData(
            PieceData.WROOK, PieceData.WHITE, "white_rook",
            new ImageIcon("resources/ChessBoard/ChessPieces/white_rook.png"),
            MovingPieceData.ROOK_FUNC);

    public static final PieceData BBISHOP_DATA = new MovingPieceData(
            PieceData.BBISHOP, PieceData.BLACK, "black_bishop",
            new ImageIcon("resources/ChessBoard/ChessPieces/black_bishop.png"),
            MovingPieceData.BISHOP_FUNC);

    public static final PieceData WBISHOP_DATA = new MovingPieceData(
            PieceData.WBISHOP, PieceData.WHITE, "white_bishop",
            new ImageIcon("resources/ChessBoard/ChessPieces/white_bishop.png"),
            MovingPieceData.BISHOP_FUNC);

    public static final PieceData BQUEEN_DATA = new MovingPieceData(
            PieceData.BQUEEN, PieceData.BLACK, "black_queen",
            new ImageIcon("resources/ChessBoard/ChessPieces/black_queen.png"),
            MovingPieceData.QUEEN_FUNC);

    public static final PieceData WQUEEN_DATA = new MovingPieceData(
            PieceData.WQUEEN, PieceData.WHITE, "white_queen",
            new ImageIcon("resources/ChessBoard/ChessPieces/white_queen.png"),
            MovingPieceData.QUEEN_FUNC);

    public static final PieceData BKING_DATA = new MovingPieceData(
            PieceData.BKING, PieceData.BLACK, "black_king",
            new ImageIcon("resources/ChessBoard/ChessPieces/black_king.png"),
            MovingPieceData.BKING_FUNC);
    public static final PieceData WKING_DATA = new MovingPieceData(
            PieceData.WKING, PieceData.WHITE, "white_king",
            new ImageIcon("resources/ChessBoard/ChessPieces/white_king.png"),
            MovingPieceData.WKING_FUNC);

    public static final PieceData BKNIGHT_DATA = new MovingPieceData(
            PieceData.BKNIGHT, PieceData.BLACK, "black_knight",
            new ImageIcon("resources/ChessBoard/ChessPieces/black_knight.png"),
            MovingPieceData.KNIGHT_FUNC);
    public static final PieceData WKNIGHT_DATA = new MovingPieceData(
            PieceData.WKNIGHT, PieceData.WHITE, "white_knight",
            new ImageIcon("resources/ChessBoard/ChessPieces/white_knight.png"),
            MovingPieceData.KNIGHT_FUNC);

    public static final PieceData NO_PIECE = null;
    //endregion
}
