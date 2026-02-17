package ChessResources.ChessHistoryTracker.BoardStateChanges;

import ChessLogic.Debug.DebugMode;
import ChessResources.Pieces.PieceData;
import ChessResources.Pieces.PieceDatas;

public class BoardStateChange{
    private PieceData piece;
    private int spaceIdArriveAt;
    private int spaceId;

    public BoardStateChange(PieceData piece, int spaceId, int spaceIdArriveAt)
    {
        if (piece != null)
            this.piece = piece.clonePiece(); //should always be a copy.

        this.spaceId = spaceId;
        this.spaceIdArriveAt = spaceIdArriveAt;
    }

    public PieceData getPiece(){return piece;}
    public int getSpaceIdArriveAt(){return spaceIdArriveAt;}
    public int getSpaceId(){return spaceId;}

    public static BoardStateChange getReverse(BoardStateChange b){
        return new BoardStateChange(b.piece, b.spaceIdArriveAt, b.spaceId);
    }

    public BoardStateChange getReverse(){
        return new BoardStateChange(this.piece, this.spaceIdArriveAt, this.spaceId);
    }

    @Override
    public String toString()
    {
        if (piece == null){
            return "null : " + spaceId +"->" + spaceIdArriveAt;
        }
        return piece.name + ": " + spaceId + "->" + spaceIdArriveAt;
    }
}
