package ChessResources.ChessHistoryTracker;

import ChessResources.ChessHistoryTracker.BoardStateChanges.BoardStateChange;
import ChessResources.ChessHistoryTracker.BoardStateChanges.PropertiesStatsChange;

import java.util.ArrayList;

public class ChessHistoryTracker {
    ArrayList<GameStateChanges> history = new ArrayList<>();
    private GameStateChanges currentGameState;
    public ChessHistoryTracker()
    {}

    public void pushTurn(GameStateChanges gameStateChanges)
    {
        history.add(gameStateChanges);
    }
    public void pushTurn(BoardStateChange boardStateChange,
                         boolean[] gameProperties, int[] gameStats)
    {
        history.add(new GameStateChanges(boardStateChange, gameProperties, gameStats));
    }
    public void pushTurn()
    {
        if (currentGameState != null)
        {
            history.add(currentGameState);
        }
        else
        {
            System.out.println("There should exist a non-null current game state");
        }
        currentGameState = null; //reset temp currentGameState
    }
    public void setGamePropertiesStats(boolean[] gameProperties, int[] gameStats)
    {
        if(currentGameState == null)
        {
            currentGameState = new GameStateChanges(gameProperties, gameStats);
        }
        else
        {
            currentGameState.setGamePropertiesStats(gameProperties, gameStats);
        }
    }
    public void setGamePropertiesStats(PropertiesStatsChange propertiesStatsChange)
    {
        if(currentGameState == null)
        {
            currentGameState = new GameStateChanges(propertiesStatsChange);
        }
        else
        {
            currentGameState.setGamePropertiesStats(propertiesStatsChange);
        }
    }
    public void pushBoardStateChange(BoardStateChange boardStateChange)
    {
        if (currentGameState == null)
        {
            currentGameState = new GameStateChanges();
            currentGameState.pushBoardStateChange(boardStateChange);
        }
        else
        {
            currentGameState.pushBoardStateChange(boardStateChange);
        }
    }

    public GameStateChanges popTurn()
    {
        GameStateChanges result = history.getLast();
        history.removeLast();
        return result;
    }

    public void clearCurrentTurn(){
        currentGameState = null;
    }

    public GameStateChanges peekTurn()
    {
        return history.getLast();
    }
    public boolean isEmpty(){return history.isEmpty();}
    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        for (GameStateChanges stats : history)
        {
            str.append(stats.toString()).append('\n');
        }
        return str.toString();
    }
}
