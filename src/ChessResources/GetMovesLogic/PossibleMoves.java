package ChessResources.GetMovesLogic;

import ChessLogic.Debug.DebugMode;
import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.ChessBoard.ChessBoardUI;
import ChessResources.Pieces.IrregularPieceData;
import ChessResources.Pieces.PieceData;
import ChessResources.Pieces.PieceDatas;
import ChessResources.Pieces.SlidingPieceData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

    //region MOVE_GEN
    public void generateMoves()
    {
        Map<PieceData, Integer> currPieceLocation = chessGame.getCurrentColorToMove() == PieceData.WHITE?
                chessGame.chessBoard.currPieceLocationWhite : chessGame.chessBoard.currPieceLocationBlack;

        boolean kingSpaceNotUnderThreat = chessGame.spaceNotUnderThreat(chessGame.getKingToMoveSpaceId());

        ChessSpaces spacesToMoveToStopKingThreat = ChessSpaces.UNIVERSE_SET;

        if (!kingSpaceNotUnderThreat) {
            //perform deeper scan.
            spacesToMoveToStopKingThreat = chessGame.getSpacesToMoveToStopThreats(
                    chessGame.getKingSpaceId(chessGame.getCurrentColorToMove()),
                    chessGame.getCurrentColorToMove());
        }

        if (chessGame.isDebuggable()) DebugMode.debugPrint(chessGame, spacesToMoveToStopKingThreat);

//        if (spacesToMoveToStopKingThreat.isEmpty()){
//            PieceData king = chessGame.getBoard().
//                    getPiece(chessGame.getKingSpaceId(chessGame.getCurrentColorToMove()));
//
//            if (chessGame.isDebuggable()) DebugMode.debugPrint(chessGame, spacesToMoveToStopKingThreat);
//
//            ChessSpaces tmp = new ChessSpaces();
//            king.getPossibleMoves(chessGame, chessGame.getKingSpaceId(chessGame.getCurrentColorToMove()), tmp);
//            if (!tmp.isEmpty()){
//                possibleMoves.put(chessGame.getKingSpaceId(chessGame.getCurrentColorToMove()), tmp);
//            }
//            return;
//        }
        //return if king can no longer be helped

        for (Map.Entry<PieceData, Integer> entry : currPieceLocation.entrySet())
        {
            int startSquare = entry.getValue();
            PieceData piece = entry.getKey();

            assert(piece != PieceDatas.NO_PIECE);

            ChessSpaces spaces = new ChessSpaces();

            piece.getPossibleMoves(chessGame, startSquare, spaces);

            if (spaces.isEmpty()) {
                //empty, immediately continue.
                continue;
            }
            else{
                possibleMoves.put(startSquare, spaces);
            }

            if (!kingSpaceNotUnderThreat &&
                    startSquare != chessGame.getKingSpaceId(chessGame.getCurrentColorToMove())){
                ChessSpaces replacement = new ChessSpaces();

                for (int spaceId : possibleMoves.get(startSquare).chessMoves){
                    if (spacesToMoveToStopKingThreat.containSpace(spaceId)){
                        replacement.addMoves(spaceId);
                    }
                }
                if (replacement.isEmpty()) {
                    possibleMoves.remove(startSquare);

                }else{
                    possibleMoves.put(startSquare, replacement);
                }
            }

        }
    }

    //endregion

    //region HELPERS
    public void clearPossibleMoves()
    {
        possibleMoves.clear();
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
