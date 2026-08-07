package ChessResources.ChessHistoryTracker;

import ChessLogic.ChessGame;
import ChessLogic.MinimalChessGame;

import ChessResources.Hasher.HashContainer;

import java.util.ArrayList;
import java.util.Hashtable;

import static java.lang.System.exit;

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
        //System.out.println("PUSHED GAME: " + game.toString());
        history.add(game);
        int currRepetitions = tableOfPositions.getOrDefault(game.getHashOfPosition(), 0);
        if (currRepetitions == 2){
            threeFoldRepitionFlag = true;
        }
        tableOfPositions.put(game.getHashOfPosition(), currRepetitions+1);

        //System.out.println(tableOfPositions.toString());
        if (this.peekTurn() == null){
            Thread.dumpStack();
            exit(0);
        }
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
        threeFoldRepitionFlag = false;
        if (tableOfPositions.getOrDefault(history.getLast().getHashOfPosition(), 0) != 2)
            tableOfPositions.remove(history.getLast().getHashOfPosition());
        else{
            tableOfPositions.put(history.getLast().getHashOfPosition(), 1);
        }
        history.removeLast(); // discard current
        return history.getLast(); // return previous
    }

    public boolean isThreeFoldRepitionFlag(){
        return threeFoldRepitionFlag;
    }

    public Hashtable<HashContainer, Integer> getTableOfPositions(){return tableOfPositions;}
    //endregion

}
