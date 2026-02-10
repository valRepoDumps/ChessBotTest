package ChessLogic.Configurations;

public class Configurations {
    private boolean enableBoardGraphic = true;
    private boolean strictMoveChecker = true;
    private boolean debugMode = true;
    public Configurations(){
        this(true,true , true);
    }

    public Configurations(boolean enableBoardGraphic, boolean strictMoveChecker, boolean debugMode){
        this.enableBoardGraphic = enableBoardGraphic;
        this.strictMoveChecker = strictMoveChecker;
        this.debugMode = debugMode;
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
}
