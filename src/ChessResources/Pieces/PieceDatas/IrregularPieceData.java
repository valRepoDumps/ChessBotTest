package ChessResources.Pieces.PieceDatas;

import ChessLogic.ChessGame;
import ChessResources.ChessBoardUI;

import javax.swing.*;
import java.util.function.BiFunction;

public class IrregularPieceData extends PieceData{

    //region KNIGHT_FUNCS
    public static final BiFunction<Object, Integer, int[]> KNIGHT_MOVES_FUNC =
            (Object game, Integer spaceId) ->{
                ChessBoardUI chessBoardUI = ((ChessGame) game).chessBoardUI;
                IrregularPieceData piece = (IrregularPieceData) chessBoardUI.getPiece(spaceId);

                int[] possibleSquares = new int[] {ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID,
                        ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID,
                        ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID};

                int row = spaceId/ ChessBoardUI.BOARD_SIZE;
                int col = spaceId% ChessBoardUI.BOARD_SIZE;

                if (row - 2 >= 0 && col-1 >= 0) { //assumes input is already valid.
                    possibleSquares[0] = spaceId + 2 * ChessBoardUI.directionOffsets[ChessBoardUI.NORTH] + //upleft
                            ChessBoardUI.directionOffsets[ChessBoardUI.WEST];
                }

                if (row-2>=0 && col+1 < ChessBoardUI.BOARD_SIZE){
                    possibleSquares[1] = spaceId + 2* ChessBoardUI.directionOffsets[ChessBoardUI.NORTH] + //upright
                        ChessBoardUI.directionOffsets[ChessBoardUI.EAST];
                }

                if (row+2< ChessBoardUI.BOARD_SIZE && col-1 >= 0) {
                    possibleSquares[2] = spaceId + 2 * ChessBoardUI.directionOffsets[ChessBoardUI.SOUTH] + //downleft
                            ChessBoardUI.directionOffsets[ChessBoardUI.WEST];
                }

                if (row+2< ChessBoardUI.BOARD_SIZE && col+1 < ChessBoardUI.BOARD_SIZE){
                    possibleSquares[3] = spaceId + 2* ChessBoardUI.directionOffsets[ChessBoardUI.SOUTH] + //downright
                        ChessBoardUI.directionOffsets[ChessBoardUI.EAST];
                }

                if (col-2 >= 0 && row -1 >= 0){
                    possibleSquares[4] = spaceId + 2* ChessBoardUI.directionOffsets[ChessBoardUI.WEST] + //leftup
                        ChessBoardUI.directionOffsets[ChessBoardUI.NORTH];}
                if (col-2 >= 0 && row +1 < ChessBoardUI.BOARD_SIZE){
                    possibleSquares[5] = spaceId + 2* ChessBoardUI.directionOffsets[ChessBoardUI.WEST] + //leftdown
                        ChessBoardUI.directionOffsets[ChessBoardUI.SOUTH];
                }

                if (col+2 < ChessBoardUI.BOARD_SIZE && row-1 >= 0){
                    possibleSquares[6] = spaceId + 2* ChessBoardUI.directionOffsets[ChessBoardUI.EAST] + //rightup
                        ChessBoardUI.directionOffsets[ChessBoardUI.NORTH];
                }
                if (col+2 < ChessBoardUI.BOARD_SIZE && row + 1 < ChessBoardUI.BOARD_SIZE) {
                    possibleSquares[7] = spaceId + 2 * ChessBoardUI.directionOffsets[ChessBoardUI.EAST] + //rightdown
                            ChessBoardUI.directionOffsets[ChessBoardUI.SOUTH];
                }

                return possibleSquares;
            };

    //endregion
    //region KING FUNCS
    public static BiFunction<Object, Integer, int[]> WKING_MOVES_FUNC =
            (Object game, Integer spaceId) ->{
                ChessBoardUI chessBoardUI = ((ChessGame)game).chessBoardUI;
                int NORTHWEST = 0, NORTH = 1, NORTHEAST = 2, EAST = 3,
                        SOUTHEAST = 4, SOUTH = 5, SOUTHWEST = 6, WEST = 7,
                        CASTLE_KING_SIDE = 8, CASTLE_QUEEN_SIDE = 9;

                int[] possibleSquares = new int[] {ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID,
                        ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID,
                        ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID,
                        ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID};

                int r = ChessBoardUI.getRow(spaceId), c = ChessBoardUI.getCol(spaceId);
                if (r>0)
                {
                    if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                            ChessBoardUI.getNorthSpaceId(spaceId, 1), WHITE))
                        possibleSquares[NORTH] = ChessBoardUI.getNorthSpaceId(spaceId, 1);

                    if (c > 0)
                    {
                        if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                                ChessBoardUI.getNorthWestSpaceId(spaceId, 1), WHITE))
                            possibleSquares[NORTHWEST] = ChessBoardUI.getNorthWestSpaceId(spaceId, 1);
                    }
                    if (c < ChessBoardUI.BOARD_SIZE-1)
                    {
                        if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                                ChessBoardUI.getNorthEastSpaceId(spaceId, 1), WHITE))
                            possibleSquares[NORTHEAST] = ChessBoardUI.getNorthEastSpaceId(spaceId, 1);
                    }
                }
                if(r< ChessBoardUI.BOARD_SIZE-1)
                {
                    if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                            ChessBoardUI.getSouthSpaceId(spaceId, 1), WHITE))
                        possibleSquares[SOUTH] = ChessBoardUI.getSouthSpaceId(spaceId, 1);
                    if (c > 0)
                    {
                        if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                                ChessBoardUI.getSouthWestSpaceId(spaceId, 1), WHITE))
                            possibleSquares[SOUTHWEST] = ChessBoardUI.getSouthWestSpaceId(spaceId, 1);
                    }
                    if (c < ChessBoardUI.BOARD_SIZE-1)
                    {
                        if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                                ChessBoardUI.getSouthEastSpaceId(spaceId, 1), WHITE))
                            possibleSquares[SOUTHEAST] = ChessBoardUI.getSouthEastSpaceId(spaceId, 1);
                    }
                }

                if (c > 0)
                {
                    if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                            ChessBoardUI.getWestSpaceId(spaceId, 1), WHITE)) {
                        possibleSquares[WEST] = ChessBoardUI.getWestSpaceId(spaceId, 1);

                        if (((ChessGame)game).whiteCastleRightsQueenSide)
                        { //allow castle if no piece in the space between king and rook
                            if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                                    ChessBoardUI.getWestSpaceId(spaceId, 2), WHITE))
                                possibleSquares[CASTLE_QUEEN_SIDE] = ChessBoardUI.getWestSpaceId(spaceId, 2);
                        }
                    }
                }
                if (c < ChessBoardUI.BOARD_SIZE-1)
                {
                    if (((ChessGame) game).spaceNotUnderThreatAndEmpty(ChessBoardUI.getEastSpaceId(spaceId, 1), WHITE)) {
                        possibleSquares[EAST] = ChessBoardUI.getEastSpaceId(spaceId, 1);

                        if (((ChessGame)game).whiteCastleRightsKingSide)
                        {
                            if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                                    ChessBoardUI.getEastSpaceId(spaceId, 2), WHITE))
                            {
                                possibleSquares[CASTLE_KING_SIDE] = ChessBoardUI.getEastSpaceId(spaceId, 2);
                            }
                        }
                    }
                }
                return possibleSquares;
            };

    public static BiFunction<Object, Integer, int[]> BKING_MOVES_FUNC =
            (Object game, Integer spaceId) ->{
                ChessBoardUI chessBoardUI = ((ChessGame)game).chessBoardUI;
                int NORTHWEST = 0, NORTH = 1, NORTHEAST = 2, EAST = 3,
                        SOUTHEAST = 4, SOUTH = 5, SOUTHWEST = 6, WEST = 7,
                        CASTLE_KING_SIDE = 8, CASTLE_QUEEN_SIDE = 9;

                int[] possibleSquares = new int[] {ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID,
                        ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID,
                        ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID,
                        ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID, ChessBoardUI.INVALID_SPACE_ID};

                int r = ChessBoardUI.getRow(spaceId), c = ChessBoardUI.getCol(spaceId);
                if (r>0)
                {
                    if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                            ChessBoardUI.getNorthSpaceId(spaceId, 1), BLACK))
                        possibleSquares[NORTH] = ChessBoardUI.getNorthSpaceId(spaceId, 1);

                    if (c > 0)
                    {
                        if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                                ChessBoardUI.getNorthWestSpaceId(spaceId, 1), BLACK))
                            possibleSquares[NORTHWEST] = ChessBoardUI.getNorthWestSpaceId(spaceId, 1);
                    }
                    if (c < ChessBoardUI.BOARD_SIZE-1)
                    {
                        if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                                ChessBoardUI.getNorthEastSpaceId(spaceId, 1), BLACK))
                            possibleSquares[NORTHEAST] = ChessBoardUI.getNorthEastSpaceId(spaceId, 1);
                    }
                }
                if(r< ChessBoardUI.BOARD_SIZE-1)
                {
                    if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                            ChessBoardUI.getSouthSpaceId(spaceId, 1), BLACK))
                        possibleSquares[SOUTH] = ChessBoardUI.getSouthSpaceId(spaceId, 1);
                    if (c > 0)
                    {
                        if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                                ChessBoardUI.getSouthWestSpaceId(spaceId, 1), BLACK))
                            possibleSquares[SOUTHWEST] = ChessBoardUI.getSouthWestSpaceId(spaceId, 1);
                    }
                    if (c < ChessBoardUI.BOARD_SIZE-1)
                    {
                        if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                                ChessBoardUI.getSouthEastSpaceId(spaceId, 1), BLACK))
                            possibleSquares[SOUTHEAST] = ChessBoardUI.getSouthEastSpaceId(spaceId, 1);
                    }
                }

                if (c > 0)
                {
                    if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                            ChessBoardUI.getWestSpaceId(spaceId, 1), BLACK)) {
                        possibleSquares[WEST] = ChessBoardUI.getWestSpaceId(spaceId, 1);

                        if (((ChessGame)game).blackCastleRightsQueenSide)
                        { //allow castle if no piece in the space between king and rook
                            if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                                    ChessBoardUI.getWestSpaceId(spaceId, 2), BLACK))
                                possibleSquares[CASTLE_QUEEN_SIDE] = ChessBoardUI.getWestSpaceId(spaceId, 2);
                        }
                    }
                }
                if (c < ChessBoardUI.BOARD_SIZE-1)
                {
                    if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                            ChessBoardUI.getEastSpaceId(spaceId, 1), BLACK)) {
                        possibleSquares[EAST] = ChessBoardUI.getEastSpaceId(spaceId, 1);

                        if (((ChessGame)game).blackCastleRightsKingSide)
                        {
                            if (((ChessGame) game).spaceNotUnderThreatAndEmpty(
                                    ChessBoardUI.getEastSpaceId(spaceId, 2), BLACK))
                            {
                                possibleSquares[CASTLE_KING_SIDE] = ChessBoardUI.getEastSpaceId(spaceId, 2);
                            }
                        }
                    }
                }
                return possibleSquares;
            };
    //endregion

    public BiFunction<Object, Integer, int[]> movesFunc;

    public IrregularPieceData(short pieceId, boolean color, int value, String name, ImageIcon graphic,
                              BiFunction<Object, Integer, int[]> movesFunc) {
        super(pieceId, color, value, name, graphic);
        this.movesFunc = movesFunc;
    }

    public IrregularPieceData(short pieceId)
    {
        super(pieceId);
        switch (pieceId)
        {
            case BKNIGHT: case WKNIGHT:
                this.movesFunc = KNIGHT_MOVES_FUNC; break;
            case BKING: this.movesFunc = BKING_MOVES_FUNC; break;
            case WKING: this.movesFunc = WKING_MOVES_FUNC; break;
        }
    }

    public IrregularPieceData(IrregularPieceData piece)
    {
        super(piece);
        this.movesFunc = piece.movesFunc;
    }
    public int[] getPossibleMoves(ChessGame game, int spaceId)
    {
        return movesFunc.apply(game, spaceId);
    }
}
