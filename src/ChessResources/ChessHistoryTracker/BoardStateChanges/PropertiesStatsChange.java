package ChessResources.ChessHistoryTracker.BoardStateChanges;

import ChessLogic.MinimalChessGame;

import java.util.Arrays;

public class PropertiesStatsChange {

    private boolean[] gameProperties = new boolean[MinimalChessGame.GAME_PROPERTIES_LEN]; //store all the property of the chess game
    private int[] gameStats = new int[MinimalChessGame.GAME_STATS_LEN];

    public PropertiesStatsChange(boolean[] gameProperties, int[] gameStats){
        setPropertiesStats(gameProperties, gameStats);
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

    @Override
    public String toString(){
        return "Props: " + Arrays.toString(gameProperties) + " | Stats: " + Arrays.toString(gameStats);
    }
}
