package ChessResources.GetMovesLogic;

import ChessResources.ChessBoard.ChessBoard;

import java.util.*;

public class ChessSpaces {
    public static final ChessSpaces UNIVERSE_SET = new ChessSpaces(true);
    public static final ChessSpaces EMPTY_SET = new ChessSpaces();

    protected Set<Integer> chessMoves = new HashSet<>();
    protected boolean containsAll = false;
    public ChessSpaces()
    {}

    public ChessSpaces(int spaceId)
    {
        this();
        chessMoves.add(spaceId);
    }

    public ChessSpaces(boolean containsAll)
    {
        this();
        this.containsAll = containsAll;
    }

    public void addMoves(int spaceId)
    {
        chessMoves.add(spaceId);
    }

    public boolean isEmpty(){
        return !containsAll && chessMoves.isEmpty();
    }

    public boolean containSpace(int spaceId) {return containsAll || chessMoves.contains(spaceId);}

    public Set<Integer> getChessMoves(){return chessMoves;}

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
        chessMoves.retainAll(cp.getChessMoves());
    }

    public void moveUnion(ChessSpaces cp){
        if (this.containsAll || cp.containsAll) {
            this.containsAll = true;
            chessMoves.clear();
            return;
        }
        chessMoves.addAll(cp.chessMoves);
    }

    public boolean isUniverse(){return containsAll;}

    public void setContainsAll(boolean containsAll){
        this.containsAll = containsAll;
    }

    public void copyChessSpaces(ChessSpaces cp){
        this.chessMoves = new HashSet<>(cp.getChessMoves());
        this.containsAll = cp.isUniverse();
    }

    public static ChessSpaces getNewUniverseSet(){
        ChessSpaces ans = new ChessSpaces();
        ans.copyChessSpaces(UNIVERSE_SET);
        return ans;
    }

    @Override
    public String toString(){
        return containsAll + " " + Arrays.toString(chessMoves.toArray());
    }

    public void clear(){
        this.containsAll = false;
        this.chessMoves.clear();
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
