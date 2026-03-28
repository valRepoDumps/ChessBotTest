package ChessResources.ChessHistoryTracker;

import ChessLogic.ChessGame;
import ChessLogic.MinimalChessGame;

import ChessResources.Hasher.HashContainer;

import java.util.ArrayList;
import java.util.Hashtable;

public class ChessHistoryTracker <ChessGame extends MinimalChessGame>{
    //region PRE_CONSTRUCTOR
    protected ArrayList<ChessGame> history = new ArrayList<>();
    protected Hashtable<HashContainer, Integer> tableOfPositions = new Hashtable<>();
    ChessGame tmpStore;
    protected boolean threeFoldRepitionFlag = false;
    //endregion

    public ChessHistoryTracker()
    {}

    //region HELPERS
    public void pushTurn(ChessGame game) {
        history.add((ChessGame) game.cloneGame());
    }



    @SuppressWarnings("unused")
    public int totalTurns(){
        return history.size();
    }
    public boolean isEmpty(){return history.isEmpty();}


    //region SETTERS
    public ChessGame peekTurn()
    {
        if (isEmpty()) return null;
        return history.getLast();
    }

    @SuppressWarnings("unused")
    public ChessGame getTurn(int idx){
        return history.get(idx);
    }

    //endregion

    //region GETTERS
    public ChessGame popTurn() {
        if (history.size() < 2) return null; // need at least current + one prior

        history.removeLast(); // discard current
        return history.getLast(); // return previous
    }

    public boolean isThreeFoldRepitionFlag(){
        return threeFoldRepitionFlag;
    }

    public Hashtable<HashContainer, Integer> getTableOfPositions(){return tableOfPositions;}
    //endregion

}
