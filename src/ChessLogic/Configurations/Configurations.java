package ChessLogic.Configurations;

public class Configurations {
    private boolean enableNotifs = true;
    private boolean enableBoardGraphic = true;

    //region TUNING_GRAPHIC
    public void enableGraphic(){
        enableBoardGraphic = true;
    }
    public void disableGraphic(){
        enableBoardGraphic = false;
    }
    //endregion
}
