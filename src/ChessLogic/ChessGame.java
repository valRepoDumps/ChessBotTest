package ChessLogic;

import ChessGUI.ChessGUI;
import ChessLogic.Configurations.Configurations;
import ChessResources.BitMasks;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.ChessBoard.DrawBoard;
import ChessResources.ChessErrors.OutOfOldTurns;
import ChessResources.GetMovesLogic.ChessMove;
import ChessResources.GetMovesLogic.PossibleMoves;
import ChessResources.Pieces.PieceData;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ChessGame extends MinimalChessGame{
    ChessGUI chessGUI;
    public DrawBoard drawBoard;

    Function<Boolean, Short> promotionFunc;

    //region SELECTED_ROW_AND_COL
    public static final int INVALID_SPACE_ID = -1;
    public int selectedSpaceId = INVALID_SPACE_ID;
    //endregion

    public static final Function<Boolean, Short> DEFAULT_PROMOTION_FUNC =
            (Boolean color) -> {
                if (color == PieceData.BLACK) return PieceData.BQUEEN;
                else return PieceData.WQUEEN;
            };

    public ChessGame(String fen, ChessGUI chessGUI, Function<Boolean, Short> choosePromotionPiece,
                     Configurations configurations) {
        //String[] args = fen.trim().split(" ");
        this.chessGUI = chessGUI;
        super(fen, new ChessBoard(), configurations);
        drawBoard = new DrawBoard(this.chessBoard);
        drawBoard.setOnSquareClicked(this::playerClick);
        promotionFunc = choosePromotionPiece;
    }

    public ChessGame(ChessGUI chessGUI, Function<Boolean, Short> choosePromotionPiece) {
        this("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", chessGUI,
                choosePromotionPiece, new Configurations(true, true, true));
    }

    private void playerClick(int spaceId)
    {
        System.out.println("Click: " + spaceId);
        short piece = chessBoard.getPiece(spaceId);

        //ensure valid choice before proceeding. Won't handle cases where sleected row exceed max and min
        // posisble, as it shouldt happen
        if (selectedSpaceId == INVALID_SPACE_ID)
        {
            if (PieceData.isValidPieceId(piece) && (PieceData.getColor(piece) == gameProperties.getSideToMove())) {
//                System.out.println(Arrays.toString(gameProperties) + " " + piece + " in" + shorts.getColor(piece));
                selectedSpaceId = spaceId;
                drawBoard.highlightSpace(spaceId);
                possibleMoves.highlightPossibleMoves(selectedSpaceId, drawBoard);
            }
        }
        else
        {
            drawBoard.unHighlightSpace(spaceId);
            drawBoard.unHighlightSpace(selectedSpaceId);

            ChessMove move = possibleMoves.getMove(selectedSpaceId, spaceId);
            for (int i = 0; i < possibleMoves.currLen; ++i){
                System.out.println(possibleMoves.getMoves()[i]);
            }
            if (move != null) {
                if (move.isPromotion()) {
                    System.out.println(move.getPromotionPieceId());
                    short pieceId = promotionFunc.apply(this.getCurrentColorToMove());
                    System.out.println(pieceId);
                    move = possibleMoves.getMovePromotion(selectedSpaceId, spaceId, pieceId);
                }
                System.out.println(move);
                movePiece(move);
                selectedSpaceId = INVALID_SPACE_ID;
            }
            else//case user choose an unmoveable square
            {
                possibleMoves.unHighlightPossibleMoves(selectedSpaceId, drawBoard);
                //unhighlight possible moves relating to previous selected space id

                selectedSpaceId = spaceId;
                drawBoard.highlightSpace(spaceId);
                if (PieceData.isValidPieceId(piece))
                {
                    possibleMoves.highlightPossibleMoves(selectedSpaceId, drawBoard);
                }
            }
        }
    }

    @Override
    public void undoTurn() throws NullPointerException, OutOfOldTurns
    {
        super.undoTurn();
        drawBoard.updateBoardGraphic();
    }

    @Override
    public boolean movePiece(ChessMove move){
        boolean a = super.movePiece(move);
        drawBoard.updateBoardGraphic();
        return a;
    }

    @Override
    protected void setGame(MinimalChessGame src){
        super.setGame(src);
        drawBoard.setBoard(chessBoard);
        drawBoard.updateBoardGraphic();
    }

    @Override
    protected void setGameNonCopy(MinimalChessGame src){
        super.setGame(src);
        drawBoard.setBoard(chessBoard);
        drawBoard.updateBoardGraphic();
    }
}
