package ChessLogic.Configurations;

public class Configurations {
    private boolean enableBoardGraphic = true;
    private boolean strictMoveChecker = true;
    private boolean debugMode = true;
    private boolean allowGameEnd = true;

    public Configurations(){
        this(true,true,true, true);
    }

    public Configurations(boolean enableBoardGraphic, boolean strictMoveChecker,
                          boolean debugMode, boolean allowGameEnd){
        this.enableBoardGraphic = enableBoardGraphic;
        this.strictMoveChecker = strictMoveChecker;
        this.debugMode = debugMode;
        this.allowGameEnd = allowGameEnd;
    }

    //region TUNING_GRAPHIC

    public void setEnableBoardGraphic(boolean enableBoardGraphic) {
        this.enableBoardGraphic = enableBoardGraphic;
    }

    public boolean isEnableBoardGraphic() {
        return enableBoardGraphic;
    }
    //endregion

    public void setStrictMoveChecker(boolean strictMoveChecker){
        this.strictMoveChecker = strictMoveChecker;
    }

    public boolean isStrictMoveChecker() {
        return strictMoveChecker;
    }

    public void enableDebugMode(){
        debugMode = true;
    }

    public void disableDebugMode(){
        debugMode = false;
    }

    public boolean isDebugMode(){return debugMode;}

    public boolean isAllowGameEnd() {
        return allowGameEnd;
    }

    public void allowGameEnd(){
        this.allowGameEnd = true;
    }

    public void disAllowGameEnd(){
        this.allowGameEnd = false;
    }


    public static Configurations createCloneGameConfig(){
        return new Configurations(false, false, false, false);
    }
}
