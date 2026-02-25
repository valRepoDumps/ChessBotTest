package ChessResources.ChessHistoryTracker.BoardStateChanges;

import ChessLogic.MinimalChessGame;
import ChessResources.GetMovesLogic.PossibleMoves;

import java.util.Arrays;

public class PropertiesStatsPossibleMovesChange {

    private boolean[] gameProperties = new boolean[MinimalChessGame.GAME_PROPERTIES_LEN]; //store all the property of the chess game
    private int[] gameStats = new int[MinimalChessGame.GAME_STATS_LEN];
    private PossibleMoves pm;
    public PropertiesStatsPossibleMovesChange(boolean[] gameProperties, int[] gameStats, PossibleMoves pm){
        setPropertiesStats(gameProperties, gameStats);
        setPossibleMoves(pm);
    }

    public void setPropertiesStats(boolean[] gameProperties, int[] gameStats)
    {
        this.gameProperties = Arrays.copyOf(gameProperties, gameProperties.length);
        this.gameStats = Arrays.copyOf(gameStats, gameStats.length);
    }

    public boolean[] getGameProperties() {
        return gameProperties;
    }

    public int[] getGameStats() {
        return gameStats;
    }

    public void setPossibleMoves(PossibleMoves pm){
        this.pm = pm.getClone();
    }
    public PossibleMoves getPossibleMoves(){
        return pm;
    }
    @Override
    public String toString(){
        return "Props: " + Arrays.toString(gameProperties) + " | Stats: " + Arrays.toString(gameStats);
    }
}
