package ChessResources.Pieces;

import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.ChessSpaces;
import ChessResources.PreCalc;

import javax.swing.*;

public class PieceData implements PieceConsts{

    public static final boolean BLACK = false;
    public static final boolean WHITE = true;


    //public static final short EMPTY_SPACE = 0;
    public final static short PIECES_DIFF = 6;

    public static final short PAWN = 0;
    public static final short KNIGHT = 1;
    public static final short BISHOP = 2;
    public static final short ROOK = 3;
    public static final short QUEEN = 4;
    public static final short KING = 5;

    public static final short BPAWN = PAWN;
    public static final short BKNIGHT = KNIGHT;
    public static final short BBISHOP = BISHOP;
    public static final short BROOK = ROOK;
    public static final short BQUEEN = QUEEN;
    public static final short BKING = KING;

    public static final short WPAWN = PAWN + PIECES_DIFF;
    public static final short WKNIGHT = KNIGHT + PIECES_DIFF;
    public static final short WBISHOP = BISHOP + PIECES_DIFF;
    public static final short WROOK = ROOK + PIECES_DIFF;
    public static final short WQUEEN = QUEEN + PIECES_DIFF;
    public static final short WKING = KING + PIECES_DIFF;

    public static final short INVALID_PIECES = 12;
    public static final short MAX_PIECES = 12;

    protected short pieceId;
    protected boolean color;
    protected String name;
    protected ImageIcon graphic;

    public static final int TOTAL_PIECES = 12;

    public static PieceData getUniqueClone(PieceData pieceData)
    {
        if (pieceData == null) return null;
        return pieceData.getUniqueClone();
    }

    public static PieceData getClone(PieceData pieceData){
        if (pieceData == null) return null;
        return  pieceData.clone();
    }

    public PieceData(short pieceId, boolean color, String name, ImageIcon graphic) {

        this.pieceId = pieceId;
        this.color = color;
        this.name = name;
        this.graphic = graphic;
    }

    public PieceData(PieceData piece)
    {
        this(piece.getPieceId(),
                piece.getColor(),
                piece.getName(), piece.getGraphic());
    }

    public static short convertArrayIdxToPieceId(short i) {
        return i;
    }

    private boolean getColor() {
        return color;
    }

    public PieceData(short pieceId)
    {
        this.pieceId = pieceId;
        switch(pieceId) {
            case BPAWN: this.color = BLACK;this.name = "black_pawn";break;
            case BKNIGHT: this.color = BLACK;this.name = "black_knight";break;
            case BBISHOP: this.color = BLACK;this.name = "black_bishop"; break;
            case BROOK: this.color = BLACK; this.name = "black_rook"; break;
            case BQUEEN: this.color = BLACK; this.name = "black_queen"; break;
            case BKING: this.color = BLACK; this.name = "black_king"; break;

            case WPAWN: this.color = WHITE; this.name = "white_pawn"; break;
            case WKNIGHT: this.color = WHITE; this.name = "white_knight"; break;
            case WBISHOP: this.color = WHITE; this.name = "white_bishop"; break;
            case WROOK: this.color = WHITE; this.name = "white_rook"; break;
            case WQUEEN: this.color = WHITE; this.name = "white_queen"; break;
            case WKING: this.color = WHITE; this.name = "white_king"; break;
            default:
        }
        this.graphic = new ImageIcon("resources/ChessBoard/ChessPieces/" + this.name + ".png");
    }

    public PieceData clone(){
        return new PieceData(this);
    }
    public PieceData getUniqueClone(){
        return new PieceData(this);
    }

    public short getPieceId() {
        return pieceId;
    }

    public String getName() {
        return name;
    }

    public ImageIcon getGraphic() {
        return graphic;
    }

    public static ImageIcon getGraphic(int pieceId){
        return PreCalc.PIECE_ID_TO_PIECE_DATA_MAP[pieceId].getGraphic();
    }

    public static int getOppositeColor(int pieceId){
        if (getColor(pieceId) == BLACK) return pieceId | PIECES_DIFF;
        else return pieceId ^ PIECES_DIFF;
    }
    public static boolean getColor(int pieceId){
        return (pieceId/PIECES_DIFF) == 0 ? BLACK:WHITE;
    }
    //endregion

    public static boolean isValidPieceId(int pieceId){

        return pieceId < PieceData.MAX_PIECES && PreCalc.PIECE_ID_TO_PIECE_DATA_MAP[pieceId] != PieceConsts.NO_PIECE;
    }

    public static String getName(int pieceId){
        return PreCalc.PIECE_ID_TO_PIECE_DATA_MAP[pieceId].getName();
    }

    public static PieceData makePiece(int pieceId)
    {
        return switch (pieceId) {
            case PieceData.BPAWN -> getUniqueClone(BPAWN_DATA);
            case PieceData.WPAWN -> getUniqueClone(WPAWN_DATA);
            case PieceData.BROOK -> getUniqueClone(BROOK_DATA);
            case PieceData.WROOK -> getUniqueClone(WROOK_DATA);
            case PieceData.BKNIGHT -> getUniqueClone(BKNIGHT_DATA);
            case PieceData.WKNIGHT -> getUniqueClone(WKNIGHT_DATA);
            case PieceData.BBISHOP -> getUniqueClone(BBISHOP_DATA);
            case PieceData.WBISHOP -> getUniqueClone(WBISHOP_DATA);
            case PieceData.BQUEEN -> getUniqueClone(BQUEEN_DATA);
            case PieceData.WQUEEN -> getUniqueClone(WQUEEN_DATA);
            case PieceData.BKING -> getUniqueClone(BKING_DATA);
            case PieceData.WKING -> getUniqueClone(WKING_DATA);
            default -> NO_PIECE;
        };
    }

    public static int convertPieceIdToArrayIdx(short pieceId){
        return pieceId;
    }

    public static short getType(short pieceId){
        if (pieceId < PIECES_DIFF) return pieceId;
        else return (short) (pieceId - PIECES_DIFF);
    }
}

