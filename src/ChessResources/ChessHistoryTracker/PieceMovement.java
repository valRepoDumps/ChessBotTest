package ChessResources.ChessHistoryTracker;

import ChessResources.Pieces.PieceDatas.PieceData;

public class PieceMovement {

    public final int PIECE_SPAWNED = -1; //id to use if the piece is spawned, or promoted.
    //spawning should only happen at game's start, and promotion form should be PiecePromotedInto, spaceIn, spaceIn.
    PieceData piece;
    int spaceIdArriveAt;
    int spaceId;
    public PieceMovement(PieceData piece, int spaceIdArriveAt, int spaceId)
    {
        this.piece=piece;
        this.spaceIdArriveAt=spaceIdArriveAt;
        this.spaceId = spaceId;
    }
}
