package ChessLogic;

import ChessResources.ChessBoardUI;
import ChessResources.ChessSpaces;
import ChessResources.Pieces.PieceDatas.PieceData;
import ChessResources.Pieces.PieceDatas.PieceDatas;
import ChessResources.PossibleMoves;

public class ChessGame {

    public final ChessBoardUI chessBoardUI;

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

        this.chessBoardUI = new ChessBoardUI(args[0]);

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
            enPassantTarget = ChessBoardUI.convertSquareNotationToSpaceId(
                    args[3].charAt(0), args[3].charAt(1));
        } else
            throw new IllegalArgumentException("Invalid files and ranks input (" + args[3] + ").");
        //endregion

        this.halfMovesSinceCaptureOrPawnMove = Integer.parseInt(args[4]);
        this.totalMovesElapsed = Integer.parseInt(args[5]);
        chessBoardUI.setOnSquareClicked(this::playerClick);
        possibleMoves[BLACK_PM].generateMoves();
        possibleMoves[WHITE_PM].generateMoves();
    }

    public ChessGame() {
        this("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    private void playerClick(int spaceId)
    {
        System.out.println("Click: " + spaceId);
        PieceData piece = chessBoardUI.getPiece(spaceId);

        //ensure valid choice before proceeding. Wont handle cases where sleected row exceed max and min
        // posisble, as it shouldt happen
        if (selectedSpaceId == INVALID_SPACE_ID)
        {
            if (piece != null && PieceData.getColor(piece) == sideToMove) {
                selectedSpaceId = spaceId;
                chessBoardUI.highlightSpace(spaceId);
                possibleMoves[sideToMove == PieceData.WHITE? WHITE_PM : BLACK_PM]
                        .highlightPossibleMoves(selectedSpaceId, chessBoardUI);
            }
        }
        else
        {
            chessBoardUI.unHighlightSpace(spaceId);
            chessBoardUI.unHighlightSpace(selectedSpaceId);

            if (movePiece(selectedSpaceId, spaceId)) //piece moved
            {
                selectedSpaceId = INVALID_SPACE_ID;
            }
            else//case user choose an unmoveable square
            {
                possibleMoves[sideToMove == PieceData.WHITE? WHITE_PM : BLACK_PM]
                        .unHighlightPossibleMoves(selectedSpaceId, chessBoardUI);
                //unhighlight possible moves relating to previous selected space id

                selectedSpaceId = spaceId;
                chessBoardUI.highlightSpace(spaceId);
                if (piece != null)
                {
                    possibleMoves[sideToMove == PieceData.WHITE? WHITE_PM : BLACK_PM]
                            .highlightPossibleMoves(selectedSpaceId, chessBoardUI);
                }
            }
        }
    }

    public boolean movePiece(int spaceIdToMove, int spaceIdArriveAt)
    {
        PieceData piece = chessBoardUI.getPiece(spaceIdToMove);
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
                if (chessBoardUI.isEmptySpaceAt(spaceIdArriveAt) && (
                        ChessBoardUI.isImmediateWest(spaceIdToMove, spaceIdArriveAt) ||
                        ChessBoardUI.isImmediateEast(spaceIdToMove, spaceIdArriveAt)))
                {//if the spaceIdArriveAt is east or west of pawn, must be diagonal move. With no piece -> enpassant

                    short northId = chessBoardUI.getPieceIdAt(spaceIdArriveAt +
                            ChessBoardUI.directionOffsets[ChessBoardUI.NORTH]);
                    short southId = chessBoardUI.getPieceIdAt(spaceIdArriveAt +
                            ChessBoardUI.directionOffsets[ChessBoardUI.SOUTH]);

                    if (northId == PieceData.BPAWN || northId == PieceData.WPAWN)
                    {
                        spaceIdCaptureAt = spaceIdArriveAt + ChessBoardUI.directionOffsets[ChessBoardUI.NORTH];
                    }
                    else //assumes once pawn do enpassant, must exist a valid capture piece.
                    {
                        spaceIdCaptureAt = spaceIdArriveAt + ChessBoardUI.directionOffsets[ChessBoardUI.SOUTH];
                    }
                }
                //en passant logic
                if (Math.abs(ChessBoardUI.getRow(spaceIdToMove) - ChessBoardUI.getRow(spaceIdArriveAt)) == 2) //pawn moved 2 space
                {
                    if (piece.pieceId == PieceData.BPAWN)
                    {
                        this.enPassantTarget = spaceIdArriveAt + ChessBoardUI.directionOffsets[ChessBoardUI.NORTH];
                        //no need to check column and row validity, always exist North in case pawn moved 2 space in standard board.
                    }
                    else //case white pawn
                    {
                        this.enPassantTarget = spaceIdArriveAt + ChessBoardUI.directionOffsets[ChessBoardUI.SOUTH];
                        //same as above.
                    }
                }
            }
            //endregion

            //region CASTLING_LOGIC
            if (piece.pieceId == PieceData.WKING)
            {
                if (ChessBoardUI.getCol(spaceIdToMove) - ChessBoardUI.getCol(spaceIdArriveAt) > 1)
                {
                    int rookSpaceId = ChessBoardUI.BOARD_SIZE*(ChessBoardUI.BOARD_SIZE-1);
                    int rookMoveId = ChessBoardUI.getEastSpaceId(spaceIdArriveAt, 1); //rook should be to the east of new king.
                    assert(whiteCastleRightsQueenSide); //should be true
                    assert(chessBoardUI.boardSquares[rookSpaceId].equals(PieceDatas.WROOK_DATA));
                    chessBoardUI.movePieceCapture(rookSpaceId, rookMoveId, rookMoveId);
                }
                else if (ChessBoardUI.getCol(spaceIdToMove) - ChessBoardUI.getCol(spaceIdArriveAt) < -1)
                {
                    int rookSpaceId = ChessBoardUI.BOARD_SIZE* ChessBoardUI.BOARD_SIZE-1;
                    int rookMoveId = ChessBoardUI.getWestSpaceId(spaceIdArriveAt, 1); //rook should be to the west of new king.
                    assert(whiteCastleRightsKingSide); //should be true
                    assert(chessBoardUI.boardSquares[rookSpaceId].equals(PieceDatas.WROOK_DATA));
                    chessBoardUI.movePieceCapture(rookSpaceId, rookMoveId, rookMoveId);
                }

                whiteCastleRightsQueenSide = false;
                whiteCastleRightsKingSide = false;
            }
            else if (piece.pieceId == PieceData.BKING)
            {
                if (ChessBoardUI.getCol(spaceIdToMove) - ChessBoardUI.getCol(spaceIdArriveAt) > 1)
                {
                    int rookSpaceId = 0;
                    int rookMoveId = ChessBoardUI.getEastSpaceId(spaceIdArriveAt, 1); //rook should be to the east of new king.
                    assert(blackCastleRightsQueenSide); //should be true
                    assert(chessBoardUI.boardSquares[rookSpaceId].equals(PieceDatas.BROOK_DATA));
                    chessBoardUI.movePieceCapture(rookSpaceId, rookMoveId, rookMoveId);
                }
                else if (ChessBoardUI.getCol(spaceIdToMove) - ChessBoardUI.getCol(spaceIdArriveAt) < -1)
                {
                    int rookSpaceId = ChessBoardUI.BOARD_SIZE-1;
                    int rookMoveId = ChessBoardUI.getWestSpaceId(spaceIdArriveAt, 1); //rook should be to the west of new king.
                    assert(blackCastleRightsKingSide); //should be true
                    assert(chessBoardUI.boardSquares[rookSpaceId].equals(PieceDatas.BROOK_DATA));
                    chessBoardUI.movePieceCapture(rookSpaceId, rookMoveId, rookMoveId);
                }
                blackCastleRightsQueenSide = false;
                blackCastleRightsKingSide = false;
            }
            else if (piece.pieceId == PieceData.WROOK) //these rook funcs wont work if i ever want to implement chess960.
            {
                if (whiteCastleRightsKingSide && ChessBoardUI.getCol(spaceIdToMove) == 7)
                {
                    whiteCastleRightsKingSide = false;
                }
                else if (whiteCastleRightsQueenSide && ChessBoardUI.getCol(spaceIdToMove) == 0)
                {
                    whiteCastleRightsQueenSide = false;
                }
            }
            else if (piece.pieceId == PieceData.BROOK)
            {
                if (blackCastleRightsKingSide && ChessBoardUI.getCol(spaceIdToMove) == 7)
                {
                   blackCastleRightsKingSide = false;
                }
                else if (blackCastleRightsQueenSide && ChessBoardUI.getCol(spaceIdToMove) == 0)
                {
                    blackCastleRightsQueenSide = false;
                }
            }
            //endregion

            if (chessBoardUI.getPieceIdAt(spaceIdArriveAt) == PieceData.BKING)
            {
                System.out.println("WHITE WIN!!!");
                System.exit(0);
            }
            else if (chessBoardUI.getPieceIdAt(spaceIdArriveAt) == PieceData.WKING)
            {
                System.out.println("BLACK WIN!!!");
                System.exit(0);
            }
            System.out.println("SpaceIdCapture: " + spaceIdCaptureAt);
            chessBoardUI.movePieceCapture(spaceIdToMove, spaceIdArriveAt, spaceIdCaptureAt);
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
        return spaceNotUnderThreat(spaceId, color) && chessBoardUI.isEmptySpaceAt(spaceId);
    }
}
