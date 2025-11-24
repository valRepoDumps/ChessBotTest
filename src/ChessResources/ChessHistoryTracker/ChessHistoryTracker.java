package ChessResources.ChessHistoryTracker;

import ChessResources.Pieces.PieceDatas.PieceData;

import java.util.ArrayList;

public class ChessHistoryTracker {
    ArrayList<PieceMovement> history = new ArrayList<>();
    public ChessHistoryTracker()
    {}
    public void addMoves(PieceData piece, int spaceIdArriveAt, int spaceId)
    {
        history.add(new PieceMovement(piece, spaceIdArriveAt, spaceId));
    }
}
