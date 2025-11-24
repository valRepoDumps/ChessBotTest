package ChessLogic;

import ChessGUI.ChessGUI;
import ChessResources.ChessBoard.ChessBoardUI;
import ChessResources.Pieces.PieceDatas.PieceData;
import ChessResources.GetMovesLogic.PossibleMoves;

import java.util.function.BiFunction;

public class ChessGame extends MinimalChessGame<ChessBoardUI>{

    ChessGUI chessGUI;
    //region SELECTED_ROW_AND_COL
    public static final int INVALID_SPACE_ID = -1;
    public int selectedSpaceId = INVALID_SPACE_ID;
    //endregion

    public ChessGame(String fen, ChessGUI chessGUI, BiFunction<Integer, Boolean, Short> choosePromotionPiece) {
        String[] args = fen.trim().split(" ");
        this.chessGUI = chessGUI;
        super(fen, new ChessBoardUI(args[0]), choosePromotionPiece);
;
        ((ChessBoardUI) chessBoard).setOnSquareClicked(this::playerClick);
    }

    public ChessGame(ChessGUI chessGUI, BiFunction<Integer, Boolean, Short> choosePromotionPiece) {
        this("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", chessGUI, choosePromotionPiece);
    }

    private void playerClick(int spaceId)
    {
        System.out.println("Click: " + spaceId);
        PieceData piece = chessBoard.getPiece(spaceId);

        //ensure valid choice before proceeding. Wont handle cases where sleected row exceed max and min
        // posisble, as it shouldt happen
        if (selectedSpaceId == INVALID_SPACE_ID)
        {
            if (piece != null && PieceData.getColor(piece) == sideToMove) {
                selectedSpaceId = spaceId;
                chessBoard.highlightSpace(spaceId);
                possibleMoves[sideToMove == PieceData.WHITE? WHITE_PM : BLACK_PM]
                        .highlightPossibleMoves(selectedSpaceId, chessBoard);
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
                possibleMoves[sideToMove == PieceData.WHITE? WHITE_PM : BLACK_PM]
                        .unHighlightPossibleMoves(selectedSpaceId, chessBoard);
                //unhighlight possible moves relating to previous selected space id

                selectedSpaceId = spaceId;
                chessBoard.highlightSpace(spaceId);
                if (piece != null)
                {
                    possibleMoves[sideToMove == PieceData.WHITE? WHITE_PM : BLACK_PM]
                            .highlightPossibleMoves(selectedSpaceId, chessBoard);
                }
            }
        }
    }

    public boolean movePiece(int spaceIdToMove, int spaceIdArriveAt)
    {
        boolean result = super.movePiece(spaceIdToMove, spaceIdArriveAt);
        chessBoard.updateBoardGraphic();
        return result;
    }

}
