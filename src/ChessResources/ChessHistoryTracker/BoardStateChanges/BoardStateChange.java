package ChessResources.ChessHistoryTracker.BoardStateChanges;

import ChessResources.Pieces.PieceData;

public class BoardStateChange{
    private short piece;
    private int spaceIdArriveAt;
    private int spaceId;
    private short pieceCaptured;

    public BoardStateChange(short piece, int spaceId,
                            int spaceIdArriveAt, short pieceCaptured)
    {
        this.piece = piece; //should always be a copy.
        this.spaceId = spaceId;
        this.spaceIdArriveAt = spaceIdArriveAt;
        this.pieceCaptured = pieceCaptured;
    }

    public short getPiece(){return piece;}
    public int getSpaceIdArriveAt(){return spaceIdArriveAt;}
    public int getSpaceId(){return spaceId;}
    public short getPieceCaptured(){return pieceCaptured;}
    @Override
    public String toString()
    {
        if (!PieceData.isValidPieceId(piece)){
            return "null : " + spaceId +"->" + spaceIdArriveAt;
        }
        return PieceData.getName(piece) + ": " + spaceId + "->" + spaceIdArriveAt;
    }
}
