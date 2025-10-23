package ChessLogic;

import ChessResources.ChessBoard;
import ChessResources.ChessSpaces;
import ChessResources.Pieces.PieceDatas.PieceData;
import ChessResources.Pieces.PieceDatas.PieceDatas;
import ChessResources.PossibleMoves;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class ChessGame {

    protected final PieceDatas pieceDatas;
    public final ChessBoard chessBoard;

    public boolean blackHaveCastleRights;
    public boolean whiteHaveCastleRights;

    public boolean sideToMove;
    public int enPassantTarget = -1; //row, col format.
    public int halfMovesSinceCaptureOrPawnMove;
    public int totalMovesElapsed;

    PossibleMoves possibleMoves;

    //region SELECTED_ROW_AND_COL
    public static final int INVALID_SPACE_ID = -1;
    public int selectedSpaceId = INVALID_SPACE_ID;
    //endregion
    public PossibleMoves possibleMoves;

    public ChessGame(String fen) {
        possibleMoves = new PossibleMoves(this);
        //fen translator.
        String[] args = fen.trim().split(" ");

        if (args.length != 6) {
            throw new IllegalArgumentException("Invalid FEN: Must include 6 arguments.");
        }

        this.pieceDatas = new PieceDatas();
        this.chessBoard = new ChessBoard(pieceDatas, args[0], possibleMoves);

        //region ASSIGN_SIDE_TO_MOVE
        if (args[1].equals("w")) sideToMove = PieceData.WHITE;
        else if (args[1].equals("b")) sideToMove = PieceData.BLACK;
        else
            throw new IllegalArgumentException("Invalid FEN: Side to Move is invalid (" + args[1] + ").");
        //endregion

        //region ASSIGN_CASTLING_RIGHTS
        blackHaveCastleRights = args[2].contains("kq");
        whiteHaveCastleRights = args[2].contains("KQ");
        //endregion

        //region ASSIGN_EN_PASSANT_TARGET
        if (args[3].length() == 1 && args[3].equals("-")) {
            enPassantTarget = -1;
        } else if (args[3].length() == 2 && Character.isAlphabetic(args[3].charAt(0))
                && Character.isDigit(args[3].charAt(1))) {
            enPassantTarget = ChessBoard.convertSquareNotationToSpaceId(
                    args[3].charAt(0), args[3].charAt(1));
        } else
            throw new IllegalArgumentException("Invalid files and ranks input (" + args[3] + ").");
        //endregion

        this.halfMovesSinceCaptureOrPawnMove = Integer.parseInt(args[4]);
        this.totalMovesElapsed = Integer.parseInt(args[5]);
        chessBoard.setOnSquareClicked(this::playerClick);
    }

    public ChessGame() {
        this("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    private void playerClick(int spaceId)
    {
        Icon piece = clickedSpace.getIcon();

        //ensure valid choice before proceeding. Wont handle cases where sleected row exceed max and min
        // posisble, as it shouldt happen
        if (selectedSpaceId == INVALID_SPACE_ID)
        {
            if (piece != null) {
                selectedSpaceId = spaceId;
                chessBoard.highlightSpace(spaceId);
                colorPossibleMoves(selectedSpaceId);
            }
        }
        else
        {
            chessBoard.unHighlightSpace(spaceId);
            movePiece(selectedSpaceId, spaceId);

            //reset col and row
            selectedSpaceId = INVALID_SPACE_ID;
        }
    }

    public void movePiece(int spaceIdToMove, int spaceIdArriveAt)
    {
        possibleMoves.generateMoves();

        if (possibleMoves.possibleMoves.containsKey(spaceIdToMove) &&
                possibleMoves.possibleMoves.get(spaceIdToMove).containSpace(spaceIdArriveAt)
        ) {
            PieceData piece = boardSquares[spaceIdToMove];
            boardSquares[spaceIdToMove] = PieceDatas.NO_PIECE;
            boardSquares[spaceIdArriveAt] = piece;

        }

        updateBoardGraphic();
        possibleMoves.clearPossibleMoves();
        possibleMoves.generateMoves();
    }

    private void colorPossibleMoves(int spaceId)
    {
        if (possibleMoves.possibleMoves.containsKey(spaceId)) {
            for (int space : possibleMoves.possibleMoves.get(spaceId).chessMoves)
            {

            }
        }
    }

}
