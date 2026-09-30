package ChessResources.Pieces;

import javax.swing.*;

public interface PieceConsts {
    //region PIECE_DATA_CONSTS
    //endregion

    //region PIECES_CONSTS
    PieceData BPAWN_DATA = new PieceData(
            PieceData.BPAWN, PieceData.BLACK, "black_pawn",
            new ImageIcon("resources/ChessBoard/ChessPieces/black_pawn.png"));

    PieceData WPAWN_DATA = new PieceData(
            PieceData.WPAWN, PieceData.WHITE, "white_pawn",
            new ImageIcon("resources/ChessBoard/ChessPieces/white_pawn.png"));

    PieceData BROOK_DATA = new PieceData(
            PieceData.BROOK, PieceData.BLACK, "black_rook",
            new ImageIcon("resources/ChessBoard/ChessPieces/black_rook.png"));

    PieceData WROOK_DATA = new PieceData(
            PieceData.WROOK, PieceData.WHITE, "white_rook",
            new ImageIcon("resources/ChessBoard/ChessPieces/white_rook.png"));

    PieceData BBISHOP_DATA = new PieceData(
            PieceData.BBISHOP, PieceData.BLACK, "black_bishop",
            new ImageIcon("resources/ChessBoard/ChessPieces/black_bishop.png"));

    PieceData WBISHOP_DATA = new PieceData(
            PieceData.WBISHOP, PieceData.WHITE, "white_bishop",
            new ImageIcon("resources/ChessBoard/ChessPieces/white_bishop.png"));

    PieceData BQUEEN_DATA = new PieceData(
            PieceData.BQUEEN, PieceData.BLACK, "black_queen",
            new ImageIcon("resources/ChessBoard/ChessPieces/black_queen.png"));

    PieceData WQUEEN_DATA = new PieceData(
            PieceData.WQUEEN, PieceData.WHITE, "white_queen",
            new ImageIcon("resources/ChessBoard/ChessPieces/white_queen.png"));

    PieceData BKING_DATA = new PieceData(
            PieceData.BKING, PieceData.BLACK, "black_king",
            new ImageIcon("resources/ChessBoard/ChessPieces/black_king.png"));
    PieceData WKING_DATA = new PieceData(
            PieceData.WKING, PieceData.WHITE, "white_king",
            new ImageIcon("resources/ChessBoard/ChessPieces/white_king.png"));

    PieceData BKNIGHT_DATA = new PieceData(
            PieceData.BKNIGHT, PieceData.BLACK, "black_knight",
            new ImageIcon("resources/ChessBoard/ChessPieces/black_knight.png"));
    PieceData WKNIGHT_DATA = new PieceData(
            PieceData.WKNIGHT, PieceData.WHITE, "white_knight",
            new ImageIcon("resources/ChessBoard/ChessPieces/white_knight.png"));

    PieceData NO_PIECE = null;
    //endregion
}
