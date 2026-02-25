package ChessResources.ChessHistoryTracker;

import ChessResources.ChessHistoryTracker.BoardStateChanges.BoardStateChange;
import ChessResources.ChessHistoryTracker.BoardStateChanges.PropertiesStatsPossibleMovesChange;
import ChessResources.GetMovesLogic.PossibleMoves;
import ChessResources.Hasher.HashContainer;

import java.util.ArrayList;
import java.util.Arrays;

public class GameStateChanges {

    private PropertiesStatsPossibleMovesChange currentPropertiesStatsPossibleMoves; //the properties and stats of the game BEFORE any moves
    private ArrayList<BoardStateChange> boardStateChanges = new ArrayList<>();
    private HashContainer hs;
    //region CONSTRUCTOR
    public GameStateChanges(BoardStateChange boardStateChange, boolean[] gameProperties,
                            int[] gameStats, PossibleMoves pm)
    {
        this.boardStateChanges = new ArrayList<>();
        this.boardStateChanges.add(boardStateChange);
        currentPropertiesStatsPossibleMoves = new PropertiesStatsPossibleMovesChange(gameProperties, gameStats, pm);
    }
    @SuppressWarnings("unused")
    public GameStateChanges(ArrayList<BoardStateChange> boardStateChanges, boolean[] gameProperties,
                            int[] gameStats, PossibleMoves pm)
    {
        this.boardStateChanges = boardStateChanges;
        currentPropertiesStatsPossibleMoves = new PropertiesStatsPossibleMovesChange(gameProperties, gameStats, pm);
    }
    public GameStateChanges(boolean[] gameProperties,
                            int[] gameStats, PossibleMoves pm)
    {
        currentPropertiesStatsPossibleMoves = new PropertiesStatsPossibleMovesChange(gameProperties, gameStats, pm);
    }

    public GameStateChanges(PropertiesStatsPossibleMovesChange propertiesStatsPossibleMovesChange)
    {
        currentPropertiesStatsPossibleMoves = propertiesStatsPossibleMovesChange;
    }
    public GameStateChanges() {}

    public void pushBoardStateChange(BoardStateChange boardStateChange)
    {
        boardStateChanges.add(boardStateChange);
    }
    //endregion

    //region SETTERS
    public void setGamePropertiesStats(boolean[] gameProperties,
                                        int[] gameStats, PossibleMoves pm)
    {
        currentPropertiesStatsPossibleMoves.setPropertiesStats(gameProperties, gameStats);
    }
    public void setGamePropertiesStats(PropertiesStatsPossibleMovesChange propertiesStatsPossibleMovesChange)
    {
        currentPropertiesStatsPossibleMoves = propertiesStatsPossibleMovesChange;
    }
    @SuppressWarnings("unused")
    public BoardStateChange popBoardStateChanges()
    {
        return boardStateChanges.getLast();
    }
    public void addBoardStateChanges(BoardStateChange b){boardStateChanges.add(b);}
    public void setHash(HashContainer hs){
        this.hs = hs.clone();
    }
    //endregion

    //region GETTERS
    public int[] getGameStats()
    {
        return Arrays.copyOf(currentPropertiesStatsPossibleMoves.getGameStats(), currentPropertiesStatsPossibleMoves.getGameStats().length);
    }
    public boolean[] getGameProperties()
    {
        return Arrays.copyOf(currentPropertiesStatsPossibleMoves.getGameProperties(),
                currentPropertiesStatsPossibleMoves.getGameProperties().length);
    }
    public PossibleMoves getPossibleMoves(){
        return currentPropertiesStatsPossibleMoves.getPossibleMoves();
    }
    public HashContainer getHashOfPosition(){
        return hs;
    }

    public ArrayList<BoardStateChange> getBoardStateChanges(){return  boardStateChanges;}

    //endregion

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        if (boardStateChanges != null) {
            for (BoardStateChange change : boardStateChanges) {
                str.append(change.toString()).append(" | ");
            }
        }
        else
        {
            str.append("NULL ");
        }
        return str + " [" + Arrays.toString(currentPropertiesStatsPossibleMoves.getGameProperties())
                + ", " + Arrays.toString(currentPropertiesStatsPossibleMoves.getGameStats())+"]";
    }
}
