package ChessLogic.Debug;

public class Move {
    int spaceId;
    int spaceIdArriveAt;

    public Move(int spaceId, int spaceIdArriveAt){
        this.spaceIdArriveAt = spaceIdArriveAt;
        this.spaceId = spaceId;
    }
}
