package ChessLogic;

import ChessGUI.ChessGUI;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.ChessBoard.ChessBoardUI;
import ChessResources.GetMovesLogic.ChessSpaces;
import ChessResources.Pieces.PieceDatas.PieceData;
import ChessResources.Pieces.PieceDatas.PieceDatas;
import ChessResources.GetMovesLogic.PossibleMoves;

import java.util.function.BiFunction;
import java.util.function.IntConsumer;

public class MinimalChessGame<Board extends ChessBoard> {
    public final Board chessBoard;

    public static final BiFunction<Integer, Boolean, Short> DEFAULT_PROMOTION_FUNC =
            (Integer spaceId, Boolean color) -> {
                if (color == PieceData.BLACK) return PieceData.BQUEEN;
                else return PieceData.WQUEEN;
            };

    BiFunction<Integer, Boolean, Short> choosePromotionPiece;

    PossibleMoves[] possibleMoves;
    static final int BLACK_PM = 0;
    static final int WHITE_PM = 1;

    //region GAME_TRACKER
    public boolean blackCastleRightsQueenSide;
    public boolean blackCastleRightsKingSide;
    public boolean whiteCastleRightsQueenSide;
    public boolean whiteCastleRightsKingSide;

    public boolean sideToMove;
    public static final int INVALID_ENPASSANT_TARGET = -1;
    public int enPassantTarget; //spaceId format.
    public int halfMovesSinceCaptureOrPawnMove;
    public int totalMovesElapsed;
    //endregion
    public MinimalChessGame(Board chessBoard, BiFunction<Integer, Boolean, Short> choosePromotionPiece)
    {
        this.chessBoard = chessBoard;

        this.choosePromotionPiece = choosePromotionPiece;
    }
    public MinimalChessGame(String fen, Board chessBoard, BiFunction<Integer, Boolean, Short> choosePromotionPiece)
    {
        String[] args = fen.trim().split(" ");
        this.chessBoard = chessBoard;
        this.choosePromotionPiece = choosePromotionPiece;

        possibleMoves = new PossibleMoves[]{new PossibleMoves(this, PieceData.BLACK),
                new PossibleMoves(this, PieceData.WHITE)};

        //fen translator.

        if (args.length != 6) {
            throw new IllegalArgumentException("Invalid FEN: Must include 6 arguments.");
        }

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

        possibleMoves[BLACK_PM].generateMoves();
        possibleMoves[WHITE_PM].generateMoves();
    }

    public boolean movePiece(int spaceIdToMove, int spaceIdArriveAt)
    {
        int promotionSpaceId = -1; //should doubled as check to see if promotion is possible.
        PieceData piece = chessBoard.getPiece(spaceIdToMove);
        if (possibleMoves[sideToMove == PieceData.WHITE? WHITE_PM : BLACK_PM]
                .possibleMoves.containsKey(spaceIdToMove) &&
                possibleMoves[sideToMove == PieceData.WHITE? WHITE_PM : BLACK_PM]
                        .possibleMoves.get(spaceIdToMove).containSpace(spaceIdArriveAt)
        ) {
            int spaceIdCaptureAt = spaceIdArriveAt;

            enPassantTarget = INVALID_ENPASSANT_TARGET;
            if (piece.pieceId == PieceData.BPAWN || piece.pieceId == PieceData.WPAWN)
            {
                //region EN_PASSANT_LOGIC
                if (chessBoard.isEmptySpaceAt(spaceIdArriveAt) && (
                        ChessBoard.isImmediateWest(spaceIdToMove, spaceIdArriveAt) ||
                                ChessBoard.isImmediateEast(spaceIdToMove, spaceIdArriveAt)))
                {//if the spaceIdArriveAt is east or west of pawn, must be diagonal move. With no piece -> enpassant

                    short northId = chessBoard.getPieceIdAt(spaceIdArriveAt +
                            ChessBoard.directionOffsets[ChessBoard.NORTH]);
//                    short southId = chessBoard.getPieceIdAt(spaceIdArriveAt +
//                            ChessBoard.directionOffsets[ChessBoard.SOUTH]);

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
                //endregion

                //region PROMOTION_LOGIC
                if (piece.pieceId == PieceData.BPAWN && ChessBoard.getRow(spaceIdArriveAt) == ChessBoard.BOARD_SIZE-1)
                    //check pawn color, do not assume pawn dont go backward or start at like row 0 or 8.
                {
                    promotionSpaceId = spaceIdArriveAt;
                }
                else if (piece.pieceId == PieceData.WPAWN && ChessBoard.getRow(spaceIdArriveAt) == 0)
                {
                    promotionSpaceId = spaceIdArriveAt;
                }
                //endregion
            }

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
                    int rookSpaceId = ChessBoard.BOARD_SIZE* ChessBoard.BOARD_SIZE-1;
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

            //region WINNING_LOGIC
            if (chessBoard.getPieceIdAt(spaceIdArriveAt) == PieceData.BKING)
            {
                System.out.println("WHITE WIN!!!");
                System.exit(0);
            }
            else if (chessBoard.getPieceIdAt(spaceIdArriveAt) == PieceData.WKING)
            {
                System.out.println("BLACK WIN!!!");
                System.exit(0);
            }
            //endregion

            System.out.println("SpaceIdCapture: " + spaceIdCaptureAt);
            chessBoard.movePieceCapture(spaceIdToMove, spaceIdArriveAt, spaceIdCaptureAt);
            if (promotionSpaceId >= 0) //make no further checks, shouldnt be invalid.
            {
                promotionHandler(spaceIdArriveAt, piece.color, choosePromotionPiece.apply(spaceIdArriveAt, piece.color));
            }
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

    private void promotionHandler(int spaceId, boolean color, int pieceId)
    {
        chessBoard.setPieceAt(spaceId, PieceDatas.getCopyOfPiece(pieceId));
    }
}
