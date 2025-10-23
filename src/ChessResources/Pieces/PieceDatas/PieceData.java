package ChessResources.Pieces.PieceDatas;

import javax.swing.*;

public class PieceData {
    //region PIECE_DATA_CONSTS
    public static final boolean BLACK = false;
    public static final boolean WHITE = true;


    public static final short EMPTY_SPACE = 0;
    public static final short BPAWN = 1;
    public static final short BKNIGHT = 2;
    public static final short BBISHOP = 3;
    public static final short BROOK = 5;
    public static final short BQUEEN = 9;
    public static final short BKING = 10;

    public static final short WPAWN = 17;
    public static final short WKNIGHT = 18;
    public static final short WBISHOP = 19;
    public static final short WROOK = 21;
    public static final short WQUEEN = 25;
    public static final short WKING = 26;

    public static final short INVALID_PIECES = 27;
    //endregion

    public short pieceId;
    public boolean color;
    public int value;
    public String name;
    public ImageIcon graphic;

    public PieceData(short pieceId, boolean color, int value, String name, ImageIcon graphic) {
        this.color = color;
        this.value = value;
        this.name = name;
        this.graphic = graphic;
    }

    public PieceData(boolean color, int value, String name) {
        this.color = color;
        this.value = value;
        this.name = name;
        this.graphic = new ImageIcon("resources/ChessBoard/ChessPieces/" + name + ".png");
    }

    public PieceData(short pieceId)
    {
        switch(pieceId) {
            case BPAWN: this.color = BLACK;this.value = 1;this.name = "black_pawn";break;
            case BKNIGHT: this.color = BLACK;this.value = 3;this.name = "black_knight";break;
            case BBISHOP: this.color = BLACK;this.value = 3;this.name = "black_bishop"; break;
            case BROOK: this.color = BLACK; this.value = 5; this.name = "black_rook"; break;
            case BQUEEN: this.color = BLACK; this.value = 9; this.name = "black_queen"; break;
            case BKING: this.color = BLACK; this.value = 1000; this.name = "black_king"; break;

            case WPAWN: this.color = WHITE; this.value = 1; this.name = "white_pawn"; break;
            case WKNIGHT: this.color = WHITE; this.value = 3; this.name = "white_knight"; break;
            case WBISHOP: this.color = WHITE; this.value = 3; this.name = "white_bishop"; break;
            case WROOK: this.color = WHITE; this.value = 5; this.name = "white_rook"; break;
            case WQUEEN: this.color = WHITE; this.value = 9; this.name = "white_queen"; break;
            case WKING: this.color = WHITE; this.value = 1000; this.name = "white_king"; break;
            default:
        }
        this.graphic = new ImageIcon("resources/ChessBoard/ChessPieces/" + this.name + ".png");
    }
}

