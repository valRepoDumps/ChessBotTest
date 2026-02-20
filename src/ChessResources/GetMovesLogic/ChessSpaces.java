package ChessResources.GetMovesLogic;

import java.util.ArrayList;
import java.util.List;

public class ChessSpaces {
    protected List<Integer> chessMoves;

    public ChessSpaces()
    {
        chessMoves = new ArrayList<>();
    }

    public ChessSpaces(int spaceId)
    {
        this();
        chessMoves.add(spaceId);
    }

    public void addMoves(int spaceId)
    {
        chessMoves.add(spaceId);
    }
    @SuppressWarnings("unused")
    public int totalPossibleMoves() {return chessMoves.size();}

    public boolean containSpace(int spaceId) {return chessMoves.contains(spaceId);}

    public List<Integer> getChessMoves(){return chessMoves;}
    //public List<Integer> getChessMoves(){return chessMoves;}
}
