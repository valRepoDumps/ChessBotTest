package ChessResources.ChessHistoryTracker.BoardStateChanges;

import ChessResources.Pieces.PieceData;

public class BoardStateChange{
    private short piece;
    private int spaceIdArriveAt;
    private int spaceId;

    public BoardStateChange(short piece, int spaceId, int spaceIdArriveAt)
    {
        this.piece = piece; //should always be a copy.
        this.spaceId = spaceId;
        this.spaceIdArriveAt = spaceIdArriveAt;
    }

    public short getPiece(){return piece;}
    public int getSpaceIdArriveAt(){return spaceIdArriveAt;}
    public int getSpaceId(){return spaceId;}
    @Override
    public String toString()
    {
        if (!PieceData.isValidPieceId(piece)){
            return "null : " + spaceId +"->" + spaceIdArriveAt;
        }
        return PieceData.getName(piece) + ": " + spaceId + "->" + spaceIdArriveAt;
    }
}
