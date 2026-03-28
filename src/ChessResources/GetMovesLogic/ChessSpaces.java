package ChessResources.GetMovesLogic;

import ChessResources.BitMasks;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.Pieces.PieceData;

import java.util.*;

public class ChessSpaces {
    public static final ChessSpaces UNIVERSE_SET = new ChessSpaces(Long.MAX_VALUE, PieceData.INVALID_PIECES);
    public static final ChessSpaces EMPTY_SET = new ChessSpaces(0L, PieceData.INVALID_PIECES);

    protected long chessMoves = 0L;
    short pieceId;
    boolean promoted;
    boolean capture;
    boolean doublePawnPush;
    boolean enPassant;
    boolean castling;
    public ChessSpaces()
    {}

    public ChessSpaces(long moveMask, short PieceId)
    {
        this();
        chessMoves |= moveMask;
    }


    public void addMoves(long spaceId)
    {
        chessMoves |= spaceId;
    }

    public boolean isEmpty(){
        return chessMoves == 0L;
    }
    public boolean containsSpace(int spaceId){
        return (BitMasks.getSingleSpaceBitBoard(spaceId) & chessMoves) != 0;
    }

    public long getChessMoves(){return chessMoves;}

    public void moveIntersection(ChessSpaces cp) {
        if (cp.isUniverse()) {
            // A ∩ U = A → no-op
            return;
        }
        if (this.isUniverse()) {
            // U ∩ B = B
            copyChessSpaces(cp);
            return;
        }
        // A ∩ B
        chessMoves &= cp.getChessMoves();
    }

    public void moveUnion(ChessSpaces cp){
        chessMoves |= cp.chessMoves;
    }

    public boolean isUniverse(){return chessMoves == Long.MAX_VALUE;}

    public void copyChessSpaces(ChessSpaces cp){
        this.chessMoves = cp.chessMoves;
    }

    public static ChessSpaces getNewUniverseSet(){
        ChessSpaces ans = new ChessSpaces();
        ans.copyChessSpaces(UNIVERSE_SET);
        return ans;
    }

    @Override
    public String toString(){
        return Long.toString(chessMoves);
    }

    public void clear(){
        this.chessMoves = 0;
    }

    public static void fastIntersection(ChessSpaces ans, ChessSpaces tmp, int spaceId){
        tmp.addMoves(spaceId);
        ans.moveIntersection(tmp);
        tmp.clear();
    }

    @Override
    public ChessSpaces clone(){
        ChessSpaces clone = new ChessSpaces();
        clone.copyChessSpaces(this);
        return clone;
    }
}
