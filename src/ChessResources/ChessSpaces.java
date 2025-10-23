package ChessResources;

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
        chessMoves = new ArrayList<>();
        chessMoves.add(spaceId);
    }

    public void addMoves(int spaceId)
    {
        chessMoves.add(spaceId);
    }

    public boolean moveInList(int spaceId)
    {
        return chessMoves.contains(spaceId);
    }

    public int totalPossibleMoves() {return chessMoves.size();}

    public boolean containSpace(int spaceId) {return chessMoves.contains(spaceId);}
}
