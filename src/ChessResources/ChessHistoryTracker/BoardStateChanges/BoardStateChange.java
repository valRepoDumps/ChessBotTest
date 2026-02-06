package ChessResources.ChessHistoryTracker.BoardStateChanges;

import ChessResources.Pieces.PieceData;

public class BoardStateChange{
    private PieceData piece;
    private int spaceIdArriveAt;
    private int spaceId;

    public BoardStateChange(PieceData piece, int spaceId, int spaceIdArriveAt)
    {
        this.piece = PieceData.copyPiece(piece); //should always be a copy.
        this.spaceId = spaceId;
        this.spaceIdArriveAt = spaceIdArriveAt;
    }

    public PieceData getPiece(){return piece;}
    public int getSpaceIdArriveAt(){return spaceIdArriveAt;}
    public int getSpaceId(){return spaceId;}
    @Override
    public String toString()
    {
        return piece.name + ": " + spaceId + "->" + spaceIdArriveAt;
    }
}
