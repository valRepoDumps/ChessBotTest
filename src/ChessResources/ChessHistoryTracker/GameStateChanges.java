package ChessResources.ChessHistoryTracker;

import ChessResources.ChessHistoryTracker.BoardStateChanges.BoardStateChange;
import ChessResources.ChessHistoryTracker.BoardStateChanges.PropertiesStatsChange;
import ChessResources.Hasher.HashContainer;

import java.util.ArrayList;
import java.util.Arrays;

public class GameStateChanges {

    private PropertiesStatsChange currentPropertiesStats; //the properties and stats of the game BEFORE any moves
    private ArrayList<BoardStateChange> boardStateChanges = new ArrayList<>();
    private HashContainer hs;
    //region CONSTRUCTOR
    public GameStateChanges(BoardStateChange boardStateChange, boolean[] gameProperties,
                            int[] gameStats)
    {
        this.boardStateChanges = new ArrayList<>();
        this.boardStateChanges.add(boardStateChange);
        currentPropertiesStats = new PropertiesStatsChange(gameProperties, gameStats);
    }
    @SuppressWarnings("unused")
    public GameStateChanges(ArrayList<BoardStateChange> boardStateChanges, boolean[] gameProperties,
                            int[] gameStats)
    {
        this.boardStateChanges = boardStateChanges;
        currentPropertiesStats = new PropertiesStatsChange(gameProperties, gameStats);
    }
    public GameStateChanges(boolean[] gameProperties,
                            int[] gameStats)
    {
        currentPropertiesStats = new PropertiesStatsChange(gameProperties, gameStats);
    }

    public GameStateChanges(PropertiesStatsChange propertiesStatsChange)
    {
        currentPropertiesStats = propertiesStatsChange;
    }
    public GameStateChanges() {}

    public void pushBoardStateChange(BoardStateChange boardStateChange)
    {
        boardStateChanges.add(boardStateChange);
    }
    //endregion

    //region SETTERS
    public void setGamePropertiesStats(boolean[] gameProperties,
                                        int[] gameStats)
    {
        currentPropertiesStats.setPropertiesStats(gameProperties, gameStats);
    }
    public void setGamePropertiesStats(PropertiesStatsChange propertiesStatsChange)
    {
        currentPropertiesStats = propertiesStatsChange;
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
        return Arrays.copyOf(currentPropertiesStats.getGameStats(), currentPropertiesStats.getGameStats().length);
    }
    public boolean[] getGameProperties()
    {
        return Arrays.copyOf(currentPropertiesStats.getGameProperties(),
                currentPropertiesStats.getGameProperties().length);
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
        return str + " [" + Arrays.toString(currentPropertiesStats.getGameProperties())
                + ", " + Arrays.toString(currentPropertiesStats.getGameStats())+"]";
    }
}
