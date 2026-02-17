package ChessResources.Pieces;

import javax.swing.*;
import java.util.UUID;

public abstract class PieceData {
    //region PIECE_DATA_CONSTS
    public static final boolean BLACK = false;
    public static final boolean WHITE = true;


    //public static final short EMPTY_SPACE = 0;
    public static final short BPAWN = 1;
    public static final short BKNIGHT = 2;
    public static final short BBISHOP = 3;
    public static final short BROOK = 4;
    public static final short BQUEEN = 5;
    public static final short BKING = 6;

    public static final short WPAWN = 17;
    public static final short WKNIGHT = 18;
    public static final short WBISHOP = 19;
    public static final short WROOK = 20;
    public static final short WQUEEN = 21;
    public static final short WKING = 22;

    public static final short INVALID_PIECES = 27;
    public final static int PIECES_DIFF = 16;
    //endregion

    protected UUID uuid;
    protected short pieceId;
    protected boolean color;
    protected int value;
    protected String name;
    protected ImageIcon graphic;

    public PieceData(UUID uuid, short pieceId, boolean color, int value, String name, ImageIcon graphic) {
        this.uuid = uuid;
        this.pieceId = pieceId;
        this.color = color;
        this.value = value;
        this.name = name;
        this.graphic = graphic;
    }

    public PieceData(short pieceId, boolean color, int value, String name, ImageIcon graphic) {
        this(UUID.randomUUID(), pieceId, color,value,name, graphic);
    }

    public PieceData(PieceData piece)
    {
        this(piece.getUuid(), piece.getPieceId(),
                piece.getColor(), piece.getValue(),
                piece.getName(), piece.getGraphic());
    }

    public PieceData(short pieceId)
    {
        this.uuid = UUID.randomUUID();
        this.pieceId = pieceId;
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

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    public boolean equals(PieceData pd){
        return uuid.equals(pd.uuid);
    }

    public abstract PieceData clone();

    //region GETTERS
    public UUID getUuid(){return  uuid;}
    public boolean getColor(){
        return (pieceId&PIECES_DIFF) == 0 ? BLACK:WHITE;
    }
    public short getPieceId() {
        return pieceId;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public ImageIcon getGraphic() {
        return graphic;
    }
    //endregion
}

