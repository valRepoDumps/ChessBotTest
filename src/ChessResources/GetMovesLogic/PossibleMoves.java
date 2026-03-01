package ChessResources.GetMovesLogic;

import ChessLogic.Debug.DebugMode;
import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoardUI;
import ChessResources.Pieces.PieceData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PossibleMoves {
    //region PRE_CONSTRUCTOR
    //region DATAS
    public HashMap<Integer, ChessSpaces> possibleMoves = new HashMap<>();
    protected MinimalChessGame<?> chessGame;
    //endregion
    //endregion

    public PossibleMoves(MinimalChessGame<?> chessGame)
    {
        this.chessGame = chessGame;
    }
    public PossibleMoves(MinimalChessGame<?> chessGame, HashMap<Integer, ChessSpaces> possibleMoves){
        this(chessGame);
        this.possibleMoves = (HashMap<Integer, ChessSpaces>) possibleMoves.clone();
    }
    public PossibleMoves(PossibleMoves pm){
        this(pm.chessGame, pm.possibleMoves);
    }
    //region MOVE_GEN
    public void generateMoves()
    {
        HashSet<Integer> currPieceLocation = chessGame.getCurrentColorToMove() == PieceData.WHITE?
                chessGame.getBoard().currPieceLocationWhite : chessGame.getBoard().currPieceLocationBlack;

        boolean kingSpaceUnderThreat = chessGame.spaceUnderThreat(chessGame.getKingToMoveSpaceId());

        ChessSpaces spacesToMoveToStopKingThreat = ChessSpaces.UNIVERSE_SET;

        if (kingSpaceUnderThreat) {
            //perform deeper scan.
            spacesToMoveToStopKingThreat = chessGame.getSpacesToMoveToStopThreats(
                    chessGame.getKingSpaceId(chessGame.getCurrentColorToMove()),
                    chessGame.getCurrentColorToMove());
        }

        if (chessGame.isDebuggable()) DebugMode.debugPrint(chessGame, spacesToMoveToStopKingThreat);

        //return if king can no longer be helped
        ChessSpaces tmpSpc = new ChessSpaces();
        for (int startSquare : currPieceLocation)
        {
            tmpSpc.clear();
            short pieceId = chessGame.getBoard().getPiece(startSquare);
            assert(chessGame.getBoard().isPieceAt(startSquare));

            PieceData.getPossibleMoves(chessGame, pieceId, startSquare, tmpSpc);

            if (tmpSpc.isEmpty()) {
                //empty, immediately continue.
                continue;
            }
            else{
                possibleMoves.put(startSquare, tmpSpc.clone());
            }

            if (kingSpaceUnderThreat &&
                    startSquare != chessGame.getKingSpaceId(chessGame.getCurrentColorToMove())){
                tmpSpc.clear();

                for (int spaceId : possibleMoves.get(startSquare).chessMoves){
                    if (spacesToMoveToStopKingThreat.containSpace(spaceId)){
                        tmpSpc.addMoves(spaceId);
                    }
                }
                if (tmpSpc.isEmpty()) {
                    possibleMoves.remove(startSquare);

                }else{
                    possibleMoves.put(startSquare, tmpSpc.clone());
                }
            }

        }
    }

    //endregion

    //region HELPERS
    public void addMoves(int spaceId, int spaceIdToMoveTo){
        if (!possibleMoves.containsKey(spaceId)){
            possibleMoves.put(spaceId, new ChessSpaces());
        }
        possibleMoves.get(spaceId).addMoves(spaceIdToMoveTo);
    }
    public void clearPossibleMoves()
    {
        possibleMoves.clear();
    }

    public PossibleMoves getClone(){
        return new PossibleMoves(this);
    }

    public void setPossibleMoves(PossibleMoves pm){
        this.chessGame = pm.chessGame;
        this.possibleMoves = (HashMap<Integer, ChessSpaces>) pm.possibleMoves.clone();
    }

    public void highlightPossibleMoves(int spaceId, ChessBoardUI chessBoardUI)
    {
        if (possibleMoves.containsKey(spaceId)) {
            for (int space : possibleMoves.get(spaceId).chessMoves)
            {
                chessBoardUI.highlightSpace(space);
            }
        }
    }

    public void unHighlightPossibleMoves(int spaceId, ChessBoardUI chessBoardUI)
    {
        if (possibleMoves.containsKey(spaceId)) {
            for (int space : possibleMoves.get(spaceId).chessMoves)
            {
                chessBoardUI.unHighlightSpace(space);
            }
        }
    }

    public boolean isEmpty(){
        return possibleMoves.isEmpty();
    }

    @SuppressWarnings("unused")
    public HashMap<Integer, ChessSpaces> getMoves(){return possibleMoves;}
    public boolean canMoveToFrom(int spaceId, int spaceIdArriveAt) {
        ChessSpaces s = possibleMoves.get(spaceId);
        return s != null && s.containSpace(spaceIdArriveAt);
    }
    //endregion

    @Override
    public String toString() {
        if (possibleMoves == null || possibleMoves.isEmpty()) {
            return "possibleMoves: <empty>";
        }
        StringBuilder sb = new StringBuilder("possibleMoves:\n");
        for (Map.Entry<Integer, ChessSpaces> entry : possibleMoves.entrySet()) {
            sb.append("  ")
                    .append(entry.getKey())
                    .append(" -> ")
                    .append(entry.getValue())
                    .append('\n');
        }
        return sb.toString();
    }
}
