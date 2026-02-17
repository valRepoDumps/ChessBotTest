package ChessResources.Pieces;

import ChessLogic.ChessGame;
import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;

import javax.swing.*;
import java.util.UUID;
import java.util.function.BiFunction;

public class IrregularPieceData extends PieceData{

    //region KNIGHT_FUNCS
    public static final BiFunction<Object, Integer, int[]> KNIGHT_MOVES_FUNC =
            (Object _, Integer spaceId) ->{

                int[] possibleSquares = new int[] {ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID,
                        ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID,
                        ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID};

                int row = spaceId/ ChessBoard.BOARD_SIZE;
                int col = spaceId% ChessBoard.BOARD_SIZE;

                if (row - 2 >= 0 && col-1 >= 0) { //assumes input is already valid.
                    possibleSquares[0] = spaceId + 2 * ChessBoard.directionOffsets[ChessBoard.NORTH] + //upleft
                            ChessBoard.directionOffsets[ChessBoard.WEST];
                }

                if (row-2>=0 && col+1 < ChessBoard.BOARD_SIZE){
                    possibleSquares[1] = spaceId + 2* ChessBoard.directionOffsets[ChessBoard.NORTH] + //upright
                        ChessBoard.directionOffsets[ChessBoard.EAST];
                }

                if (row+2< ChessBoard.BOARD_SIZE && col-1 >= 0) {
                    possibleSquares[2] = spaceId + 2 * ChessBoard.directionOffsets[ChessBoard.SOUTH] + //downleft
                            ChessBoard.directionOffsets[ChessBoard.WEST];
                }

                if (row+2< ChessBoard.BOARD_SIZE && col+1 < ChessBoard.BOARD_SIZE){
                    possibleSquares[3] = spaceId + 2* ChessBoard.directionOffsets[ChessBoard.SOUTH] + //downright
                        ChessBoard.directionOffsets[ChessBoard.EAST];
                }

                if (col-2 >= 0 && row -1 >= 0){
                    possibleSquares[4] = spaceId + 2* ChessBoard.directionOffsets[ChessBoard.WEST] + //leftup
                        ChessBoard.directionOffsets[ChessBoard.NORTH];}
                if (col-2 >= 0 && row +1 < ChessBoard.BOARD_SIZE){
                    possibleSquares[5] = spaceId + 2* ChessBoard.directionOffsets[ChessBoard.WEST] + //leftdown
                        ChessBoard.directionOffsets[ChessBoard.SOUTH];
                }

                if (col+2 < ChessBoard.BOARD_SIZE && row-1 >= 0){
                    possibleSquares[6] = spaceId + 2* ChessBoard.directionOffsets[ChessBoard.EAST] + //rightup
                        ChessBoard.directionOffsets[ChessBoard.NORTH];
                }
                if (col+2 < ChessBoard.BOARD_SIZE && row + 1 < ChessBoard.BOARD_SIZE) {
                    possibleSquares[7] = spaceId + 2 * ChessBoard.directionOffsets[ChessBoard.EAST] + //rightdown
                            ChessBoard.directionOffsets[ChessBoard.SOUTH];
                }

                return possibleSquares;
            };

    //endregion
    //region KING FUNCS
    public static BiFunction<Object, Integer, int[]> WKING_MOVES_FUNC =
            (Object game, Integer spaceId) ->{
                int NORTHWEST = 0, NORTH = 1, NORTHEAST = 2, EAST = 3,
                        SOUTHEAST = 4, SOUTH = 5, SOUTHWEST = 6, WEST = 7,
                        CASTLE_KING_SIDE = 8, CASTLE_QUEEN_SIDE = 9;

                int[] possibleSquares = new int[] {ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID,
                        ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID,
                        ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID,
                        ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID};

                int r = ChessBoard.getRow(spaceId), c = ChessBoard.getCol(spaceId);
                //region MOVEMENT_NORTH
                if (r>0)
                {
                    if (((MinimalChessGame<?>) game).spaceNotUnderThreat(
                            ChessBoard.getNorthSpaceId(spaceId, 1), WHITE))
                        possibleSquares[NORTH] = ChessBoard.getNorthSpaceId(spaceId, 1);

                    if (c > 0)
                    {
                        if (((MinimalChessGame<?>) game).spaceNotUnderThreat(
                                ChessBoard.getNorthWestSpaceId(spaceId, 1), WHITE))
                            possibleSquares[NORTHWEST] = ChessBoard.getNorthWestSpaceId(spaceId, 1);
                    }
                    if (c < ChessBoard.BOARD_SIZE-1)
                    {
                        if (((MinimalChessGame<?>) game).spaceNotUnderThreat(
                                ChessBoard.getNorthEastSpaceId(spaceId, 1), WHITE))
                            possibleSquares[NORTHEAST] = ChessBoard.getNorthEastSpaceId(spaceId, 1);
                    }
                }
                //endregion

                //region MOVEMENT_SOUTH
                if(r< ChessBoard.BOARD_SIZE-1)
                {
                    if (((MinimalChessGame<?>) game).spaceNotUnderThreat(
                            ChessBoard.getSouthSpaceId(spaceId, 1), WHITE))
                        possibleSquares[SOUTH] = ChessBoard.getSouthSpaceId(spaceId, 1);
                    if (c > 0)
                    {
                        if (((MinimalChessGame<?>) game).spaceNotUnderThreat(
                                ChessBoard.getSouthWestSpaceId(spaceId, 1), WHITE))
                            possibleSquares[SOUTHWEST] = ChessBoard.getSouthWestSpaceId(spaceId, 1);
                    }
                    if (c < ChessBoard.BOARD_SIZE-1)
                    {
                        if (((MinimalChessGame<?>) game).spaceNotUnderThreat(
                                ChessBoard.getSouthEastSpaceId(spaceId, 1), WHITE))
                            possibleSquares[SOUTHEAST] = ChessBoard.getSouthEastSpaceId(spaceId, 1);
                    }
                }
                //endregion

                //region WEST_MOVEMENT
                if (c > 0)
                {
                    if (((MinimalChessGame<?>) game).spaceNotUnderThreat(
                            ChessBoard.getWestSpaceId(spaceId, 1), WHITE)) {
                        possibleSquares[WEST] = ChessBoard.getWestSpaceId(spaceId, 1);
                    }
                }
                //endregion

                //region MOVEMENT_EAST
                if (c < ChessBoard.BOARD_SIZE-1)
                {
                    if (((MinimalChessGame<?>) game).spaceNotUnderThreat(ChessBoard.getEastSpaceId(spaceId, 1), WHITE)) {
                        possibleSquares[EAST] = ChessBoard.getEastSpaceId(spaceId, 1);
                    }
                }
                //endregion

                //region CASTLING
                if ((((MinimalChessGame<?>) game).spaceNotUnderThreat(spaceId, WHITE)))
                {
                    if (((MinimalChessGame<?>)game).gameProperties[ChessGame.WHITE_CASTLE_KING]
                    && ((MinimalChessGame<?>) game).spaceNotUnderThreatAndEmpty(
                            ChessBoard.getEastSpaceId(spaceId, 1), WHITE)
                    && ((MinimalChessGame<?>) game).spaceNotUnderThreatAndEmpty(
                            ChessBoard.getEastSpaceId(spaceId, 2), WHITE)
                    )
                    {
                        possibleSquares[CASTLE_KING_SIDE] = ChessBoard.getEastSpaceId(spaceId, 2);
                    }

                    if (((MinimalChessGame<?>)game).gameProperties[ChessGame.WHITE_CASTLE_QUEEN]
                    && ((MinimalChessGame<?>) game).spaceNotUnderThreatAndEmpty(
                            ChessBoard.getWestSpaceId(spaceId, 1), WHITE)
                    &&((MinimalChessGame<?>) game).spaceNotUnderThreatAndEmpty(
                            ChessBoard.getWestSpaceId(spaceId, 2), WHITE)
                    )
                    {
                        possibleSquares[CASTLE_QUEEN_SIDE] = ChessBoard.getWestSpaceId(spaceId, 2);
                    }
                }
                //endregion
                return possibleSquares;
            };

    public static BiFunction<Object, Integer, int[]> BKING_MOVES_FUNC =
            (Object game, Integer spaceId) ->{
                int NORTHWEST = 0, NORTH = 1, NORTHEAST = 2, EAST = 3,
                        SOUTHEAST = 4, SOUTH = 5, SOUTHWEST = 6, WEST = 7,
                        CASTLE_KING_SIDE = 8, CASTLE_QUEEN_SIDE = 9;

                int[] possibleSquares = new int[] {ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID,
                        ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID,
                        ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID,
                        ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID};

                int r = ChessBoard.getRow(spaceId), c = ChessBoard.getCol(spaceId);
                //region MOVEMENT_NORTH
                if (r>0)
                {
                    if (((MinimalChessGame<?>) game).spaceNotUnderThreat(
                            ChessBoard.getNorthSpaceId(spaceId, 1), BLACK))
                        possibleSquares[NORTH] = ChessBoard.getNorthSpaceId(spaceId, 1);

                    if (c > 0)
                    {
                        if (((MinimalChessGame<?>) game).spaceNotUnderThreat(
                                ChessBoard.getNorthWestSpaceId(spaceId, 1), BLACK))
                            possibleSquares[NORTHWEST] = ChessBoard.getNorthWestSpaceId(spaceId, 1);
                    }
                    if (c < ChessBoard.BOARD_SIZE-1)
                    {
                        if (((MinimalChessGame<?>) game).spaceNotUnderThreat(
                                ChessBoard.getNorthEastSpaceId(spaceId, 1), BLACK))
                            possibleSquares[NORTHEAST] = ChessBoard.getNorthEastSpaceId(spaceId, 1);
                    }
                }
                //endregion
                //region MOVEMENT_SOUTH
                if(r< ChessBoard.BOARD_SIZE-1)
                {
                    if (((MinimalChessGame<?>) game).spaceNotUnderThreat(
                            ChessBoard.getSouthSpaceId(spaceId, 1), BLACK))
                        possibleSquares[SOUTH] = ChessBoard.getSouthSpaceId(spaceId, 1);
                    if (c > 0)
                    {
                        if (((MinimalChessGame<?>) game).spaceNotUnderThreat(
                                ChessBoard.getSouthWestSpaceId(spaceId, 1), BLACK))
                            possibleSquares[SOUTHWEST] = ChessBoard.getSouthWestSpaceId(spaceId, 1);
                    }
                    if (c < ChessBoard.BOARD_SIZE-1)
                    {
                        if (((MinimalChessGame<?>) game).spaceNotUnderThreat(
                                ChessBoard.getSouthEastSpaceId(spaceId, 1), BLACK))
                            possibleSquares[SOUTHEAST] = ChessBoard.getSouthEastSpaceId(spaceId, 1);
                    }
                }
                //endregion
                //region MOVEMENT_WEST
                if (c > 0)
                {
                    if (((MinimalChessGame<?>) game).spaceNotUnderThreat(
                            ChessBoard.getWestSpaceId(spaceId, 1), BLACK)) {
                        possibleSquares[WEST] = ChessBoard.getWestSpaceId(spaceId, 1);
                    }
                }
                //endregion
                //region MOVEMENT_EAST
                if (c < ChessBoard.BOARD_SIZE-1)
                {
                    if (((MinimalChessGame<?>) game).spaceNotUnderThreat(
                            ChessBoard.getEastSpaceId(spaceId, 1), BLACK)) {
                        possibleSquares[EAST] = ChessBoard.getEastSpaceId(spaceId, 1);
                    }
                }
                //endregion

                //region CASTLING
                if ((((MinimalChessGame<?>) game).spaceNotUnderThreat(spaceId, BLACK)))
                {
                    if (((MinimalChessGame<?>)game).gameProperties[ChessGame.BLACK_CASTLE_KING]
                            && ((MinimalChessGame<?>) game).spaceNotUnderThreatAndEmpty(
                            ChessBoard.getEastSpaceId(spaceId, 1), BLACK)
                            && ((MinimalChessGame<?>) game).spaceNotUnderThreatAndEmpty(
                            ChessBoard.getEastSpaceId(spaceId, 2), BLACK)
                    )
                    {
                        possibleSquares[CASTLE_KING_SIDE] = ChessBoard.getEastSpaceId(spaceId, 2);
                    }

                    if (((MinimalChessGame<?>)game).gameProperties[ChessGame.BLACK_CASTLE_QUEEN]
                            && ((MinimalChessGame<?>) game).spaceNotUnderThreatAndEmpty(
                            ChessBoard.getWestSpaceId(spaceId, 1), BLACK)
                            &&((MinimalChessGame<?>) game).spaceNotUnderThreatAndEmpty(
                            ChessBoard.getWestSpaceId(spaceId, 2), BLACK)
                    )
                    {
                        possibleSquares[CASTLE_QUEEN_SIDE] = ChessBoard.getWestSpaceId(spaceId, 2);
                    }
                }
                //endregion

                return possibleSquares;
            };
    //endregion

    public BiFunction<Object, Integer, int[]> movesFunc;

    @SuppressWarnings("unused")
    public IrregularPieceData(short pieceId, boolean color, int value, String name, ImageIcon graphic,
                              BiFunction<Object, Integer, int[]> movesFunc) {
        super(pieceId, color, value, name, graphic);
        this.movesFunc = movesFunc;
    }

    public IrregularPieceData(UUID uniqueId, short pieceId, boolean color, int value, String name, ImageIcon graphic,
                              BiFunction<Object, Integer, int[]> movesFunc) {
        super(uniqueId, pieceId, color, value, name, graphic);
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

    public int[] getPossibleMoves(MinimalChessGame<?> game, int spaceId)
    {
        return movesFunc.apply(game, spaceId);
    }

    @Override
    public IrregularPieceData clonePiece() {
        return new IrregularPieceData(uniqueId, pieceId, color, value, name, graphic, movesFunc);
    }

    @Override
    public IrregularPieceData getCopyofPiece(){
        return new IrregularPieceData(this);
    }
}
