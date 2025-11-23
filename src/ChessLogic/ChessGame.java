package ChessLogic;

import ChessResources.ChessBoard;
import ChessResources.ChessSpaces;
import ChessResources.Pieces.PieceDatas.PieceData;
import ChessResources.Pieces.PieceDatas.PieceDatas;
import ChessResources.Pieces.PieceDatas.SlidingPieceData;
import ChessResources.PossibleMoves;

import javax.swing.*;

public class ChessGame {

    public final ChessBoard chessBoard;

    public boolean blackCastleRightsQueenSide;
    public boolean blackCastleRightsKingSide;
    public boolean whiteCastleRightsQueenSide;
    public boolean whiteCastleRightsKingSide;

    public boolean sideToMove;
    public static final int INVALID_ENPASSANT_TARGET = -1;
    public int enPassantTarget = -1; //spaceId format.
    public int halfMovesSinceCaptureOrPawnMove;
    public int totalMovesElapsed;

    PossibleMoves[] possibleMoves;
    static final int BLACK_PM = 0;
    static final int WHITE_PM = 1;
    //region SELECTED_ROW_AND_COL
    public static final int INVALID_SPACE_ID = -1;
    public int selectedSpaceId = INVALID_SPACE_ID;
    //endregion

    public ChessGame(String fen) {
        possibleMoves = new PossibleMoves[]{new PossibleMoves(this, PieceData.BLACK),
                new PossibleMoves(this, PieceData.WHITE)};

        //fen translator.
        String[] args = fen.trim().split(" ");

        if (args.length != 6) {
            throw new IllegalArgumentException("Invalid FEN: Must include 6 arguments.");
        }

        this.chessBoard = new ChessBoard(args[0]);

        //region ASSIGN_SIDE_TO_MOVE
        if (args[1].equals("w")) sideToMove = PieceData.WHITE;
        else if (args[1].equals("b")) sideToMove = PieceData.BLACK;
        else
            throw new IllegalArgumentException("Invalid FEN: Side to Move is invalid (" + args[1] + ").");
        //endregion

        //region ASSIGN_CASTLING_RIGHTS
        blackCastleRightsKingSide  = args[2].contains("k");
        blackCastleRightsQueenSide = args[2].contains("q");
        whiteCastleRightsKingSide  = args[2].contains("K");
        whiteCastleRightsQueenSide = args[2].contains("Q");
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
        possibleMoves[BLACK_PM].generateMoves();
        possibleMoves[WHITE_PM].generateMoves();
    }

    public ChessGame() {
        this("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
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
        PieceData piece = chessBoard.getPiece(spaceIdToMove);
        if (possibleMoves[sideToMove == PieceData.WHITE? WHITE_PM : BLACK_PM]
                .possibleMoves.containsKey(spaceIdToMove) &&
            possibleMoves[sideToMove == PieceData.WHITE? WHITE_PM : BLACK_PM]
                    .possibleMoves.get(spaceIdToMove).containSpace(spaceIdArriveAt)
        ) {
            int spaceIdCaptureAt = spaceIdArriveAt;
            //region ENPASSANT LOGIC
            enPassantTarget = INVALID_ENPASSANT_TARGET;
            if (piece.pieceId == PieceData.BPAWN || piece.pieceId == PieceData.WPAWN)
            {
                if (chessBoard.isEmptySpaceAt(spaceIdArriveAt) && (
                        ChessBoard.isImmediateWest(spaceIdToMove, spaceIdArriveAt) ||
                        ChessBoard.isImmediateEast(spaceIdToMove, spaceIdArriveAt)))
                {//if the spaceIdArriveAt is east or west of pawn, must be diagonal move. With no piece -> enpassant

                    short northId = chessBoard.getPieceIdAt(spaceIdArriveAt +
                            ChessBoard.directionOffsets[ChessBoard.NORTH]);
                    short southId = chessBoard.getPieceIdAt(spaceIdArriveAt +
                            ChessBoard.directionOffsets[ChessBoard.SOUTH]);

                    if (northId == PieceData.BPAWN || northId == PieceData.WPAWN)
                    {
                        spaceIdCaptureAt = spaceIdArriveAt + ChessBoard.directionOffsets[ChessBoard.NORTH];
                    }
                    else //assumes once pawn do enpassant, must exist a valid capture piece.
                    {
                        spaceIdCaptureAt = spaceIdArriveAt + ChessBoard.directionOffsets[ChessBoard.SOUTH];
                    }
                }
                //en passant logic
                if (Math.abs(ChessBoard.getRow(spaceIdToMove) - ChessBoard.getRow(spaceIdArriveAt)) == 2) //pawn moved 2 space
                {
                    if (piece.pieceId == PieceData.BPAWN)
                    {
                        this.enPassantTarget = spaceIdArriveAt + ChessBoard.directionOffsets[ChessBoard.NORTH];
                        //no need to check column and row validity, always exist North in case pawn moved 2 space in standard board.
                    }
                    else //case white pawn
                    {
                        this.enPassantTarget = spaceIdArriveAt + ChessBoard.directionOffsets[ChessBoard.SOUTH];
                        //same as above.
                    }
                }
            }
            //endregion

            //region CASTLING_LOGIC
            if (piece.pieceId == PieceData.WKING)
            {
                if (ChessBoard.getCol(spaceIdToMove) - ChessBoard.getCol(spaceIdArriveAt) > 1)
                {
                    int rookSpaceId = ChessBoard.BOARD_SIZE*(ChessBoard.BOARD_SIZE-1);
                    int rookMoveId = ChessBoard.getEastSpaceId(spaceIdArriveAt, 1); //rook should be to the east of new king.
                    assert(whiteCastleRightsQueenSide); //should be true
                    assert(chessBoard.boardSquares[rookSpaceId].equals(PieceDatas.WROOK_DATA));
                    chessBoard.movePieceCapture(rookSpaceId, rookMoveId, rookMoveId);
                }
                else if (ChessBoard.getCol(spaceIdToMove) - ChessBoard.getCol(spaceIdArriveAt) < -1)
                {
                    int rookSpaceId = ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE-1;
                    int rookMoveId = ChessBoard.getWestSpaceId(spaceIdArriveAt, 1); //rook should be to the west of new king.
                    assert(whiteCastleRightsKingSide); //should be true
                    assert(chessBoard.boardSquares[rookSpaceId].equals(PieceDatas.WROOK_DATA));
                    chessBoard.movePieceCapture(rookSpaceId, rookMoveId, rookMoveId);
                }

                whiteCastleRightsQueenSide = false;
                whiteCastleRightsKingSide = false;
            }
            else if (piece.pieceId == PieceData.BKING)
            {
                if (ChessBoard.getCol(spaceIdToMove) - ChessBoard.getCol(spaceIdArriveAt) > 1)
                {
                    int rookSpaceId = 0;
                    int rookMoveId = ChessBoard.getEastSpaceId(spaceIdArriveAt, 1); //rook should be to the east of new king.
                    assert(blackCastleRightsQueenSide); //should be true
                    assert(chessBoard.boardSquares[rookSpaceId].equals(PieceDatas.BROOK_DATA));
                    chessBoard.movePieceCapture(rookSpaceId, rookMoveId, rookMoveId);
                }
                else if (ChessBoard.getCol(spaceIdToMove) - ChessBoard.getCol(spaceIdArriveAt) < -1)
                {
                    int rookSpaceId = ChessBoard.BOARD_SIZE-1;
                    int rookMoveId = ChessBoard.getWestSpaceId(spaceIdArriveAt, 1); //rook should be to the west of new king.
                    assert(blackCastleRightsKingSide); //should be true
                    assert(chessBoard.boardSquares[rookSpaceId].equals(PieceDatas.BROOK_DATA));
                    chessBoard.movePieceCapture(rookSpaceId, rookMoveId, rookMoveId);
                }
                blackCastleRightsQueenSide = false;
                blackCastleRightsKingSide = false;
            }
            else if (piece.pieceId == PieceData.WROOK) //these rook funcs wont work if i ever want to implement chess960.
            {
                if (whiteCastleRightsKingSide && ChessBoard.getCol(spaceIdToMove) == 7)
                {
                    whiteCastleRightsKingSide = false;
                }
                else if (whiteCastleRightsQueenSide && ChessBoard.getCol(spaceIdToMove) == 0)
                {
                    whiteCastleRightsQueenSide = false;
                }
            }
            else if (piece.pieceId == PieceData.BROOK)
            {
                if (blackCastleRightsKingSide && ChessBoard.getCol(spaceIdToMove) == 7)
                {
                   blackCastleRightsKingSide = false;
                }
                else if (blackCastleRightsQueenSide && ChessBoard.getCol(spaceIdToMove) == 0)
                {
                    blackCastleRightsQueenSide = false;
                }
            }
            //endregion
            System.out.println("SpaceIdCapture: " + spaceIdCaptureAt);
            chessBoard.movePieceCapture(spaceIdToMove, spaceIdArriveAt, spaceIdCaptureAt);
            sideToMove = !sideToMove;//change move side

            //generate possible moves after changing side
            possibleMoves[BLACK_PM].clearPossibleMoves();
            possibleMoves[BLACK_PM].generateMoves();

            possibleMoves[WHITE_PM].clearPossibleMoves();
            possibleMoves[WHITE_PM].generateMoves();
            return true;
        }
        return false;
    }

    public boolean spaceNotUnderThreat(int spaceId, boolean color)
    {
        //ensure spaceId valid.
        for (ChessSpaces moves : possibleMoves[color == PieceData.WHITE? BLACK_PM : WHITE_PM].possibleMoves.values())
        {
            if (moves.containSpace(spaceId)) return false;
        }
        return true;
    }

    public boolean spaceNotUnderThreatAndEmpty(int spaceId, boolean color)
    {
        return spaceNotUnderThreat(spaceId, color) && chessBoard.isEmptySpaceAt(spaceId);
    }
}
