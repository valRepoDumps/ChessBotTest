package ChessLogic;

import ChessGUI.ChessGUI;
import ChessLogic.Configurations.Configurations;
import ChessLogic.Debug.Tests;
import ChessResources.ChessBoard.ChessBoardUI;
import ChessResources.ChessErrors.OutOfOldTurns;
import ChessResources.Pieces.PieceData;
import ChessResources.Pieces.PieceDatas;

import java.util.Arrays;
import java.util.function.BiFunction;

public class ChessGame extends MinimalChessGame<ChessBoardUI>{
    ChessGUI chessGUI;
    //region SELECTED_ROW_AND_COL
    public static final int INVALID_SPACE_ID = -1;
    public int selectedSpaceId = INVALID_SPACE_ID;
    //endregion

    public ChessGame(String fen, ChessGUI chessGUI, BiFunction<Integer, Boolean, Short> choosePromotionPiece,
                     Configurations configurations) {
        //String[] args = fen.trim().split(" ");
        this.chessGUI = chessGUI;
        super(fen, new ChessBoardUI(), choosePromotionPiece, configurations);
        chessBoard.setOnSquareClicked(this::playerClick);
    }

    public ChessGame(ChessGUI chessGUI, BiFunction<Integer, Boolean, Short> choosePromotionPiece) {
        this("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", chessGUI,
                choosePromotionPiece, new Configurations(true, false,
                        true, true));
    }

    private void playerClick(int spaceId)
    {
        System.out.println("Click: " + spaceId);
        PieceData piece = chessBoard.getPiece(spaceId);

        //ensure valid choice before proceeding. Won't handle cases where sleected row exceed max and min
        // posisble, as it shouldt happen
        if (selectedSpaceId == INVALID_SPACE_ID)
        {
            System.out.println(Arrays.toString(gameProperties) + " " + piece);
            if (piece != null && PieceDatas.getColor(piece) == gameProperties[SIDE_TO_MOVE]) {
//                System.out.println(Arrays.toString(gameProperties) + " " + piece + " in" + PieceDatas.getColor(piece));
                selectedSpaceId = spaceId;
                chessBoard.highlightSpace(spaceId);
                possibleMoves.highlightPossibleMoves(selectedSpaceId, chessBoard);
            }
        }
        else
        {
            chessBoard.unHighlightSpace(spaceId);
            chessBoard.unHighlightSpace(selectedSpaceId);

            if (movePiece(selectedSpaceId, spaceId)) //piece moved
            {
                selectedSpaceId = INVALID_SPACE_ID;
            }
            else//case user choose an unmoveable square
            {
                possibleMoves.unHighlightPossibleMoves(selectedSpaceId, chessBoard);
                //unhighlight possible moves relating to previous selected space id

                selectedSpaceId = spaceId;
                chessBoard.highlightSpace(spaceId);
                if (piece != null)
                {
                    possibleMoves.highlightPossibleMoves(selectedSpaceId, chessBoard);
                }
            }
        }
    }

    @Override
    public void undoTurn() throws NullPointerException, OutOfOldTurns
    {
        super.undoTurn();
        chessBoard.updateBoardGraphic();
    }

}
