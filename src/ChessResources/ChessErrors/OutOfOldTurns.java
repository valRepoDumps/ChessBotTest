package ChessResources.ChessErrors;

public class OutOfOldTurns extends Exception{
    public OutOfOldTurns(String msg){
        super("Cannot undo turn! " + msg);
    }
}
