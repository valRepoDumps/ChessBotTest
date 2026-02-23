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
    //protected boolean color;
    public int[][] numSquaresToEdge = new int[ChessBoard.BOARD_SIZE* ChessBoard.BOARD_SIZE][8];
    //endregion

    //region setting
    public boolean strictMovesChecker = false;//ensure all moves that threaten king unusable

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

            spacesToMoveToStopKingThreat = chessGame.getSpacesToMoveToStopThreats(
                    chessGame.getKingSpaceId(chessGame.getCurrentColorToMove()),
                    chessGame.getCurrentColorToMove());
        }

        DebugMode.debugPrint(chessGame, spacesToMoveToStopKingThreat);

        if (spacesToMoveToStopKingThreat.isEmpty() && !kingSpaceNotUnderThreat){
            PieceData king = chessGame.getBoard().
                    getPiece(chessGame.getKingSpaceId(chessGame.getCurrentColorToMove()));

            DebugMode.debugPrint(chessGame, spacesToMoveToStopKingThreat);

            ChessSpaces tmp = new ChessSpaces();
            king.getPossibleMoves(chessGame, chessGame.getKingSpaceId(chessGame.getCurrentColorToMove()), tmp);
            if (!tmp.isEmpty()){
                possibleMoves.put(chessGame.getKingSpaceId(chessGame.getCurrentColorToMove()), tmp);
            }
            return;
        }
        //return if king can no longer be helped

        for (Map.Entry<PieceData, Integer> entry : currPieceLocation.entrySet())
        {
            int startSquare = entry.getValue();
            PieceData piece = entry.getKey();

            if (piece != PieceDatas.NO_PIECE &&
                    piece.getColor() == chessGame.getCurrentColorToMove())
            {
                ChessSpaces spaces = new ChessSpaces();

                piece.getPossibleMoves(chessGame, startSquare, spaces);

                if (!spaces.isEmpty()) {
                    //only put in if spaces isnt empty
                    possibleMoves.put(startSquare, spaces);
                }
                else{
                    continue; //immediately continue if no move is possible.
                }

                if (!kingSpaceNotUnderThreat &&
                        startSquare != chessGame.getKingSpaceId(chessGame.getCurrentColorToMove())){
                    ChessSpaces replacement = new ChessSpaces();

                    for (int spaceId : possibleMoves.get(startSquare).chessMoves){
                        if (spacesToMoveToStopKingThreat.containSpace(spaceId)){
                            replacement.addMoves(spaceId);
                        }
                    }
                    if (!replacement.isEmpty()) {
                        possibleMoves.put(startSquare, replacement);
                    }else{
                        possibleMoves.remove(startSquare);
                    }
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
    public void enableStrictMovesChecker(){strictMovesChecker = true;}

    public  void disableStrictMovesChecker(){strictMovesChecker = false;}

    @SuppressWarnings("unused")
    public HashMap<Integer, ChessSpaces> getMoves(){return possibleMoves;}
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
