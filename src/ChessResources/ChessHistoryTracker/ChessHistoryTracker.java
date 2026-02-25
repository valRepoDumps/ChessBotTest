package ChessResources.ChessHistoryTracker;

import ChessResources.ChessHistoryTracker.BoardStateChanges.BoardStateChange;
import ChessResources.ChessHistoryTracker.BoardStateChanges.PropertiesStatsPossibleMovesChange;
import ChessResources.GetMovesLogic.PossibleMoves;
import ChessResources.Hasher.HashContainer;

import java.util.ArrayList;
import java.util.Hashtable;

public class ChessHistoryTracker {
    //region PRE_CONSTRUCTOR
    protected ArrayList<GameStateChanges> history = new ArrayList<>();
    protected Hashtable<HashContainer, Integer> tableOfPositions = new Hashtable<>();
    protected GameStateChanges currentGameState;

    protected boolean threeFoldRepitionFlag = false;
    //endregion

    public ChessHistoryTracker()
    {}

    //region HELPERS
    public void pushTurn(GameStateChanges gameStateChanges)
    {
        history.add(gameStateChanges);
    }
    @SuppressWarnings("unused")
    public void pushTurn(BoardStateChange boardStateChange,
                         boolean[] gameProperties, int[] gameStats, PossibleMoves pm)
    {
        history.add(new GameStateChanges(boardStateChange, gameProperties, gameStats, pm));
    }

    public void pushTurn()
    {
        if (currentGameState != null)
        {
            pushTurn(currentGameState);
        }
        else
        {
            System.out.println("There should exist a non-null current game state");
        }

        currentGameState = null; //reset temp currentGameState

        tableOfPositions.put(peekTurn().getHashOfPosition(),
                tableOfPositions.getOrDefault(peekTurn().getHashOfPosition(), 0)+1);

        if (tableOfPositions.getOrDefault(peekTurn().getHashOfPosition(), 0) == 3){
            threeFoldRepitionFlag = true;
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

    public void clearCurrentTurn(){
        currentGameState = null;
    }
    @SuppressWarnings("unused")
    public int totalTurns(){
        return history.size();
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
    //endregion

    //region SETTERS
    @SuppressWarnings("unused")
    public void setGamePropertiesStatsPossibleMoves(boolean[] gameProperties, int[] gameStats, PossibleMoves pm)
    {
        if(currentGameState == null)
        {
            currentGameState = new GameStateChanges(gameProperties, gameStats, pm);
        }
        else
        {
            currentGameState.setGamePropertiesStats(gameProperties, gameStats, pm);
        }
    }

    public void setGamePropertiesStatsPossibleMoves(PropertiesStatsPossibleMovesChange pm)
    {
        if(currentGameState == null)
        {
            currentGameState = new GameStateChanges(pm);
        }
        else
        {
            currentGameState.setGamePropertiesStats(pm);
        }
    }

    public void setHash(HashContainer hs){
        currentGameState.setHash(hs);
    }

    public GameStateChanges peekTurn()
    {
        return history.getLast();
    }

    @SuppressWarnings("unused")
    public GameStateChanges getTurn(int idx){
        return history.get(idx);
    }

    //endregion

    //region GETTERS
    public GameStateChanges popTurn()
    {
        tableOfPositions.put(peekTurn().getHashOfPosition(),
                tableOfPositions.getOrDefault(peekTurn().getHashOfPosition(), 0)-1);
        return history.removeLast();
    }

    public boolean isThreeFoldRepitionFlag(){
        return threeFoldRepitionFlag;
    }

    public Hashtable<HashContainer, Integer> getTableOfPositions(){return tableOfPositions;}
    //endregion

}
