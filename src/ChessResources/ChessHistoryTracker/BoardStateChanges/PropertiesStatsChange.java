package ChessResources.ChessHistoryTracker.BoardStateChanges;

import java.util.Arrays;

public class PropertiesStatsChange {

    private boolean[] gameProperties = new boolean[5]; //store all the property of the chess game
    private int[] gameStats = new int[5];

    public PropertiesStatsChange(boolean[] gameProperties, int[] gameStats){
        setPropertiesStats(gameProperties, gameStats);
    }

    public void setPropertiesStats(boolean[] gameProperties, int[] gameStats)
    {
        this.gameProperties = gameProperties.clone();
        this.gameStats = gameStats.clone();
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
