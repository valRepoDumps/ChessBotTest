package ChessLogic.Debug;

public class Move {
    int spaceIdToMove;
    int spaceIdArriveAt;

    public Move(int spaceIdToMove, int spaceIdArriveAt){
        this.spaceIdArriveAt = spaceIdArriveAt;
        this.spaceIdToMove = spaceIdToMove;
    }
}
