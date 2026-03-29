package ChessResources.GetMovesLogic;

import ChessResources.ChessBoard.ChessBoard;
import ChessResources.Pieces.PieceData;

import java.util.Objects;

public class ChessMove {

    public static final int PROMOTION_SHIFT = 7;
    public static final int CAPTURE_SHIFT = 14;
    public static final int ENPASSANT_SHIFT = 15;
    public static final int CASTLING_SHIFT = 16;
    public static final int BLACK_CASTLE_QUEEN_SIDE_SHIFT = CASTLING_SHIFT;
    public static final int WHITE_CASTLE_QUEEN_SIDE_SHIFT = CASTLING_SHIFT + 1;
    public static final int BLACK_CASTLE_KING_SIDE_SHIFT = CASTLING_SHIFT + 2;
    public static final int WHITE_CASTLE_KING_SIDE_SHIFT = CASTLING_SHIFT + 3;

    public static final long DOUBLE_PAWN_PUSH = 0b111111L;
    public static final long PROMOTION = 0b11111L<<PROMOTION_SHIFT;
    public static final long CAPTURE = 1L<<CAPTURE_SHIFT;
    public static final long EN_PASSANT = 1L<<ENPASSANT_SHIFT;

    public static final long PROMOTION_WROOK = PieceData.WROOK<<PROMOTION_SHIFT;
    public static final long PROMOTION_WBISHOP = PieceData.WBISHOP<<PROMOTION_SHIFT;
    public static final long PROMOTION_WKNIGHT = PieceData.WKNIGHT<<PROMOTION_SHIFT;
    public static final long PROMOTION_WQUEEN = PieceData.WQUEEN<<PROMOTION_SHIFT;

    public static final long PROMOTION_BROOK = PieceData.BROOK<<PROMOTION_SHIFT;
    public static final long PROMOTION_BBISHOP = PieceData.BBISHOP<<PROMOTION_SHIFT;
    public static final long PROMOTION_BKNIGHT = PieceData.BKNIGHT<<PROMOTION_SHIFT;
    public static final long PROMOTION_BQUEEN = PieceData.BQUEEN<<PROMOTION_SHIFT;

    public static final long CASTLING = 0b1111L << CASTLING_SHIFT;
    public static final long BLACK_CASTLE_QUEEN_SIDE = 1L<<BLACK_CASTLE_QUEEN_SIDE_SHIFT;
    public static final long WHITE_CASTLE_QUEEN_SIDE = 1L<<WHITE_CASTLE_QUEEN_SIDE_SHIFT;
    public static final long BLACK_CASTLE_KING_SIDE = 1L<<BLACK_CASTLE_KING_SIDE_SHIFT;
    public static final long WHITE_CASTLE_KING_SIDE = 1L<<WHITE_CASTLE_KING_SIDE_SHIFT;

    int spaceIdToMove;
    int spaceIdArriveAt;
    int spaceIdCaptureAt;

    short pieceId;

    long flags = 0L;
    public ChessMove(int spaceIdToMove, int spaceIdArriveAt,
                     int spaceIdCaptureAt, short pieceId,
                     long flags){
        this.spaceIdToMove = spaceIdToMove;
        this.spaceIdArriveAt =spaceIdArriveAt;
        this.spaceIdCaptureAt = spaceIdCaptureAt;
        this.pieceId = pieceId;

        this.flags = flags;
    }

    public ChessMove(int spaceIdToMove, int spaceIdArriveAt,
                     int spaceIdCaptureAt, short pieceId){
        this(spaceIdToMove, spaceIdArriveAt, spaceIdCaptureAt, pieceId, 0L);
    }

    public ChessMove(int spaceIdToMove, int spaceIdArriveAt, short pieceId, long flags){
        this(spaceIdToMove, spaceIdArriveAt, spaceIdArriveAt, pieceId, flags);
    }

    public ChessMove(int spaceIdToMove, int spaceIdArriveAt, short pieceId){
        this(spaceIdToMove, spaceIdArriveAt, spaceIdArriveAt, pieceId, 0L);
    }

    //region SETTERS

    public ChessMove setFlags(long flags){
        this.flags = flags;
        return this;
    }
    //endregion

    //region GETTERS

    public short getPieceId() {
        return pieceId;
    }

    public int getSpaceIdArriveAt() {
        return spaceIdArriveAt;
    }

    public int getSpaceIdToMove() {
        return spaceIdToMove;
    }

    public int getSpaceIdCaptureAt() {
        return spaceIdCaptureAt;
    }

    public boolean isCapture() {
        return (flags & CAPTURE) != 0;
    }

    public boolean isCastling() {
        return (flags & CASTLING) != 0;
    }

    public boolean isWhiteCastleQueen(){
        return (flags & WHITE_CASTLE_QUEEN_SIDE) != 0;
    }
    public boolean isWhiteCastleKing(){
        return (flags & WHITE_CASTLE_KING_SIDE) != 0;
    }

    public boolean isBlackCastleQueen(){
        return (flags & BLACK_CASTLE_QUEEN_SIDE) != 0;
    }
    public boolean isBlackCastleKing(){
        return (flags & BLACK_CASTLE_KING_SIDE) != 0;
    }
    public boolean isDoublePawnPush() {
        return (flags & DOUBLE_PAWN_PUSH) != 0;
        //can never get an id == 0 in double pawn push, so this is still correct.
    }

    public int getDoublePawnPushId(){
        return (int) (flags & DOUBLE_PAWN_PUSH);
    }
    public boolean isEnPassant() {
        return (flags & EN_PASSANT) != 0;
    }

    public boolean isPromotion() {
        return (flags & PROMOTION) != 0;
    }
    public short getPromotionPieceId(){return (short) ((flags & PROMOTION)>>PROMOTION_SHIFT);}
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChessMove chessMove = (ChessMove) o;
        return spaceIdToMove == chessMove.spaceIdToMove
                && spaceIdArriveAt == chessMove.spaceIdArriveAt
                && spaceIdCaptureAt == chessMove.spaceIdCaptureAt
                && pieceId == chessMove.pieceId
                && flags == chessMove.flags;
    }

    @Override
    public int hashCode() {
        return Objects.hash(spaceIdToMove, spaceIdArriveAt, spaceIdCaptureAt, pieceId);
    }

    public String toString(){
        StringBuffer str = new StringBuffer(PieceData.getName(pieceId));
        str.append(":" + spaceIdToMove + " -> " + spaceIdArriveAt + " (" + spaceIdCaptureAt +")");
        if (isCapture()){
            str.append(" Captures");
        }
        if (isEnPassant()){
            str.append(" EnPassant");
        }
        if (isCastling()){
            str.append(" Castling");
        }
        if (isDoublePawnPush()){
            str.append(" Double Pawn Push");
        }
        if (isPromotion()){
            str.append(" Promotion");
        }
        return str.toString();
    }
    //endregion
}
