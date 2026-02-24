package ChessLogic.Configurations;

public class Configurations {
    private boolean enableBoardGraphic;
    private boolean debugMode;
    private boolean allowGameEnd;

    public Configurations(){
        this(true,true,true);
    }

    public Configurations(boolean enableBoardGraphic,
                          boolean debugMode, boolean allowGameEnd){
        this.enableBoardGraphic = enableBoardGraphic;
        this.debugMode = debugMode;
        this.allowGameEnd = allowGameEnd;
    }

    //region SETTERS/GETTERS
    //region TUNING_GRAPHIC
    @SuppressWarnings("unused")
    public void setEnableBoardGraphic(boolean enableBoardGraphic) {
        this.enableBoardGraphic = enableBoardGraphic;
    }

    public boolean isEnableBoardGraphic() {
        return enableBoardGraphic;
    }
    //endregion
    //region MOVE_CHECKER
    @SuppressWarnings("unused")
    public void enableDebugMode(){
        debugMode = true;
    }
    //endregion
    //region DEBUG
    @SuppressWarnings("unused")
    public void disableDebugMode(){
        debugMode = false;
    }

    public boolean isDebugMode(){return debugMode;}
    //endregion
    //region GAME_END
    public boolean isAllowGameEnd() {
        return allowGameEnd;
    }
    @SuppressWarnings("unused")
    public void allowGameEnd(){
        this.allowGameEnd = true;
    }
    @SuppressWarnings("unused")
    public void disAllowGameEnd(){
        this.allowGameEnd = false;
    }
    //endregion
    //endregion

    //region HELPER
    public static Configurations createCloneGameConfig(){
        return new Configurations(false, false, false);
    }
    //endregion
}
