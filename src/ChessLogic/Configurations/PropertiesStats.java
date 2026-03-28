package ChessLogic.Configurations;

import ChessResources.ChessBoard.ChessBoard;

public class PropertiesStats {
    public static int SIDE_TO_MOVE_SHIFT = 8;
    public static int CASTLING_SHIFT = 9;
    public static int BLACK_CASTLE_QUEEN_SIDE_SHIFT = CASTLING_SHIFT;
    public static int WHITE_CASTLE_QUEEN_SIDE_SHIFT = CASTLING_SHIFT+1;
    public static int BLACK_CASTLE_KING_SIDE_SHIFT = CASTLING_SHIFT+2;
    public static int WHITE_CASTLE_KING_SIDE_SHIFT = CASTLING_SHIFT+3;

    public static int HALF_MOVES_SHIFT = 13;
    public static int TOTAL_MOVES_ELAPSED_SHIFT = 20;

    private long properties = 0L;
    public static final long ENPASSANT_TARGET = 0b1111111L;
    public static final long SIDE_TO_MOVE = 1L<<SIDE_TO_MOVE_SHIFT;

    public static final long CASTLING = 0b1111L << CASTLING_SHIFT;
    public static final long BLACK_CASTLE_QUEEN_SIDE = 1L<<BLACK_CASTLE_QUEEN_SIDE_SHIFT;
    public static final long WHITE_CASTLE_QUEEN_SIDE = 1L<<WHITE_CASTLE_QUEEN_SIDE_SHIFT;
    public static final long BLACK_CASTLE_KING_SIDE = 1L<<BLACK_CASTLE_KING_SIDE_SHIFT;
    public static final long WHITE_CASTLE_KING_SIDE = 1L<<WHITE_CASTLE_KING_SIDE_SHIFT;

    public static final long HALF_MOVES_SINCE_CAPTURE_OR_PAWN_MOVE = 0b11111L<<HALF_MOVES_SHIFT;
    public static final long TOTAL_MOVES_ELAPSED = 0b1111111111111111L<<TOTAL_MOVES_ELAPSED_SHIFT;

    public static final long WHITE_CASTLE = WHITE_CASTLE_QUEEN_SIDE | WHITE_CASTLE_KING_SIDE;
    public static final long BLACK_CASTLE = BLACK_CASTLE_KING_SIDE | BLACK_CASTLE_QUEEN_SIDE;

    public PropertiesStats(){
    }

    public PropertiesStats(PropertiesStats p){
        this(p.properties);
    }

    public PropertiesStats(long properties){
        this.properties = properties;
    }

    public PropertiesStats getCopy(){
        return new PropertiesStats(this.properties);
    }

    public boolean canCastle(){
        return (properties&CASTLING) != 0;
    }
    public boolean canBlackCastleQueen(){
        return (properties&BLACK_CASTLE_QUEEN_SIDE) != 0;
    }

    public boolean canBlackCastleKing(){
        return (properties&BLACK_CASTLE_KING_SIDE) != 0;
    }

    public boolean canWhiteCastleQueen(){
        return (properties&WHITE_CASTLE_QUEEN_SIDE) != 0;
    }

    public boolean canWhiteCastle(){
        return (properties&WHITE_CASTLE) != 0;
    }

    public boolean canBlackCastle(){
        return (properties&BLACK_CASTLE) != 0;
    }

    public boolean canWhiteCastleKing(){
        return (properties&WHITE_CASTLE_KING_SIDE) != 0;
    }

    public int getCastlingFlag(){
        return (int)((properties&CASTLING)>>CASTLING_SHIFT);
    }

    public boolean canEnPassant(){
        return (properties&ENPASSANT_TARGET) != ENPASSANT_TARGET;
    }

    public int getEnPassantTarget(){
        return (int)(properties&ENPASSANT_TARGET);
    }

    public boolean getSideToMove(){
        return ((properties & SIDE_TO_MOVE) == 0) ? ChessBoard.WHITE : ChessBoard.BLACK;
    }

    public int getHalfMovesSinceCaptureOrPawnMove(){
        return (int)((properties& HALF_MOVES_SINCE_CAPTURE_OR_PAWN_MOVE)>>>11);
    }

    public int getTotalMovesElapsed(){
        return (int)((properties&TOTAL_MOVES_ELAPSED)>>>TOTAL_MOVES_ELAPSED_SHIFT);
    }

    public void setEnPassantTarget(int square) {
        clearEnPassantTarget();          // clear bits
        properties |= (square & ENPASSANT_TARGET); // set new value
    }

    public void clearEnPassantTarget(){
        properties &= ~ENPASSANT_TARGET;
    }

    public static boolean isValidEnPassantTarget(int e){
        return 0 < e && e < 63;
    }
    public void flipSideToMove() {
        properties ^= SIDE_TO_MOVE;
    }

    public void revokeCastlingRight(long right) {
        properties &= ~right;
    }

    public void setCastlingRight(long right){
        revokeCastlingRight(CASTLING);
        properties |= right;
    }

    public void incrementHalfMoves() {
        int current = getHalfMovesSinceCaptureOrPawnMove();
        resetHalfMoves();
        properties |= ((long)(current + 1) << HALF_MOVES_SHIFT);
    }

    public void resetHalfMoves() {
        properties &= ~HALF_MOVES_SINCE_CAPTURE_OR_PAWN_MOVE;
    }

    public void setHalfMoves(int count){
        resetHalfMoves();
        properties |= ((long) count << HALF_MOVES_SHIFT);
    }

    public void setTotalMovesElapsed(int count){
        resetTotalMoves();
        properties |= ((long) count << TOTAL_MOVES_ELAPSED_SHIFT);
    }

    public void resetTotalMoves(){
        properties&=~TOTAL_MOVES_ELAPSED;
    }

    public void incrementTotalMoves() {
        int current = getTotalMovesElapsed();
        properties &= ~TOTAL_MOVES_ELAPSED;
        properties |= ((long)(current + 1) << 17);
    }

    @Override
    public String toString(){
        return getEnPassantTarget() + " " +
                getSideToMove() + " " +
                getCastlingFlag() + " " +
                getHalfMovesSinceCaptureOrPawnMove() + " " +
                getTotalMovesElapsed();
    }
}
