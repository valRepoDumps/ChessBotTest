package ChessResources.Pieces;

import ChessLogic.ChessGame;
import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.ChessBoard.ChessBoard;

import javax.swing.*;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.BiFunction;

public class SlidingPieceData extends PieceData {
    //region PRE_CONSTRUCTOR
    public static final int NO_RANGE_LIMIT = ChessBoard.BOARD_SIZE;

    //region PIECE FUNCS
    //region PAWN FUNCS

    public static BiFunction<Object, Integer, short[]> BPAWN_DIR_FUNC =
            (Object game, Integer spaceId) ->{
                ChessBoard chessBoard = ((MinimalChessGame<?>)game).chessBoard;
                SlidingPieceData piece = (SlidingPieceData) chessBoard.getPiece(spaceId);

                int opCode = 1;

                if (spaceId <= ChessBoard.BOARD_SIZE * (ChessBoard.BOARD_SIZE-1))
                {
                    if (chessBoard.isEnemyPieceAt(spaceId +
                            ChessBoard.directionOffsets[ChessBoard.SOUTH_EAST], piece.color) ||
                            (ChessBoard.isImmediateSouth(spaceId, 
                                    ((MinimalChessGame<?>) game).gameStats[ChessGame.ENPASSANT_TARGET]) &&
                                    ChessBoard.isImmediateEast(spaceId, ((MinimalChessGame<?>) game).gameStats[ChessGame.ENPASSANT_TARGET]))
                    )
                    {
                        opCode = 2;
                        if (chessBoard.isEnemyPieceAt(spaceId +
                                ChessBoard.directionOffsets[ChessBoard.SOUTH_WEST], piece.color) ||
                                (ChessBoard.isImmediateSouth(spaceId, ((MinimalChessGame<?>) game).gameStats[ChessGame.ENPASSANT_TARGET]) &&
                                        ChessBoard.isImmediateWest(spaceId, ((MinimalChessGame<?>) game).gameStats[ChessGame.ENPASSANT_TARGET])))
                        {
                            opCode = 3;
                        }
                    }

                    else if (chessBoard.isEnemyPieceAt(spaceId +
                            ChessBoard.directionOffsets[ChessBoard.SOUTH_WEST], piece.color) ||
                            (ChessBoard.isImmediateSouth(spaceId, ((MinimalChessGame<?>) game).gameStats[ChessGame.ENPASSANT_TARGET]) &&
                                    ChessBoard.isImmediateWest(spaceId, ((MinimalChessGame<?>) game).gameStats[ChessGame.ENPASSANT_TARGET])))
                    {
                        opCode = 4;
                    }

                    if (!chessBoard.isEnemyPieceAt(spaceId +
                            ChessBoard.directionOffsets[ChessBoard.SOUTH], piece.color))
                    {
                        opCode*=-1;
                    }
                }

                return switch (opCode) {
                    case -1 -> new short[]{ChessBoard.SOUTH};
                    case 2 -> new short[]{ChessBoard.SOUTH_EAST};
                    case -2 -> new short[]{ChessBoard.SOUTH_EAST, ChessBoard.SOUTH};
                    case 3 -> new short[]{ChessBoard.SOUTH_WEST, ChessBoard.SOUTH_EAST};
                    case -3 -> new short[]{ChessBoard.SOUTH, ChessBoard.SOUTH_WEST, ChessBoard.SOUTH_EAST};
                    case 4 -> new short[]{ChessBoard.SOUTH_WEST};
                    case -4 -> new short[]{ChessBoard.SOUTH_WEST, ChessBoard.SOUTH};
                    default -> new short[]{};
                };
            };
    public static BiFunction<Object, Integer, Integer> BPAWN_RANGE_FUNC =
            (game, spaceId) -> {
                ChessBoard chessBoard = ((MinimalChessGame<?>) game).chessBoard;
                SlidingPieceData piece = (SlidingPieceData) chessBoard.getPiece(spaceId);

                if (chessBoard.isEnemyPieceAt(spaceId + ChessBoard.directionOffsets[ChessBoard.SOUTH], piece.color) ||
                chessBoard.isEnemyPieceAt(spaceId + 2* ChessBoard.directionOffsets[ChessBoard.SOUTH], piece.color))
                {
                    return 1; //if enemy is right in front, range will always be 1.
                }
                return spaceId / ChessBoard.BOARD_SIZE == 1 ? 2 : 1;
            };

    public static BiFunction<Object, Integer, short[]> WPAWN_DIR_FUNC =
            (Object game, Integer spaceId) ->{
                ChessBoard chessBoard = ((MinimalChessGame<?>) game).chessBoard;
                SlidingPieceData piece = (SlidingPieceData) chessBoard.getPiece(spaceId);

                int opCode = 1;

                if (spaceId <= ChessBoard.BOARD_SIZE * (ChessBoard.BOARD_SIZE-1))
                {
                    if (chessBoard.isEnemyPieceAt(spaceId +
                            ChessBoard.directionOffsets[ChessBoard.NORTH_EAST], piece.color) ||
                            (ChessBoard.isImmediateNorth(spaceId, ((MinimalChessGame<?>) game).gameStats[ChessGame.ENPASSANT_TARGET]) &&
                                    ChessBoard.isImmediateEast(spaceId, ((MinimalChessGame<?>) game).gameStats[ChessGame.ENPASSANT_TARGET])))
                    {
                        opCode = 2;
                        if (chessBoard.isEnemyPieceAt(spaceId +
                                ChessBoard.directionOffsets[ChessBoard.NORTH_WEST], piece.color)||
                                (ChessBoard.isImmediateNorth(spaceId, ((MinimalChessGame<?>) game).gameStats[ChessGame.ENPASSANT_TARGET]) &&
                                        ChessBoard.isImmediateWest(spaceId, ((MinimalChessGame<?>) game).gameStats[ChessGame.ENPASSANT_TARGET])))
                        {
                            opCode = 3;
                        }
                    }

                    else if (chessBoard.isEnemyPieceAt(spaceId +
                            ChessBoard.directionOffsets[ChessBoard.NORTH_WEST], piece.color) ||
                            (ChessBoard.isImmediateNorth(spaceId, ((MinimalChessGame<?>) game).gameStats[ChessGame.ENPASSANT_TARGET]) &&
                                    ChessBoard.isImmediateWest(spaceId, ((MinimalChessGame<?>) game).gameStats[ChessGame.ENPASSANT_TARGET])))
                    {
                        opCode = 4;
                    }

                    if (!chessBoard.isEnemyPieceAt(spaceId +
                            ChessBoard.directionOffsets[ChessBoard.NORTH], piece.color))
                    {
                        opCode*=-1;
                    }
                }

                return switch (opCode) {
                    case -1 -> new short[]{ChessBoard.NORTH};
                    case 2 -> new short[]{ChessBoard.NORTH_EAST};
                    case -2 -> new short[]{ChessBoard.NORTH_EAST, ChessBoard.NORTH};
                    case 3 -> new short[]{ChessBoard.NORTH_WEST, ChessBoard.NORTH_EAST};
                    case -3 -> new short[]{ChessBoard.NORTH, ChessBoard.NORTH_WEST, ChessBoard.NORTH_EAST};
                    case 4 -> new short[]{ChessBoard.NORTH_WEST};
                    case -4 -> new short[]{ChessBoard.NORTH_WEST, ChessBoard.NORTH};
                    default -> new short[]{};
                };
            };

    public static BiFunction<Object, Integer, Integer> WPAWN_RANGE_FUNC =
            (game, spaceId) -> {
                ChessBoard chessBoard = ((MinimalChessGame<?>) game).chessBoard;
                SlidingPieceData piece = (SlidingPieceData) chessBoard.getPiece(spaceId);

                if (chessBoard.isEnemyPieceAt(spaceId + ChessBoard.directionOffsets[ChessBoard.NORTH], piece.color) ||
                        chessBoard.isEnemyPieceAt(spaceId + 2* ChessBoard.directionOffsets[ChessBoard.NORTH], piece.color))
                {
                    return 1; //if enemy is right in front, or up to 2 range in front, range will always be 1.
                }
                return spaceId / ChessBoard.BOARD_SIZE == 6 ? 2 : 1;
            };
    //endregion

    //region R_B_Q_K FUNCS
    public static BiFunction<Object, Integer, short[]> ROOK_DIR_FUNC =
            (Object _, Integer _) -> new short[]{ChessBoard.NORTH, ChessBoard.SOUTH, ChessBoard.WEST, ChessBoard.EAST};
    public static BiFunction<Object, Integer, Integer> ROOK_RANGE_FUNC =
            (_, _) -> NO_RANGE_LIMIT;

    public static BiFunction<Object, Integer, short[]> BISHOP_DIR_FUNC =
            (Object _, Integer _) -> new short[]{ChessBoard.NORTH_WEST, ChessBoard.NORTH_EAST, ChessBoard.SOUTH_WEST, ChessBoard.SOUTH_EAST};
    public static BiFunction<Object, Integer, Integer> BISHOP_RANGE_FUNC =
            (_, _) -> NO_RANGE_LIMIT;

    public static BiFunction<Object, Integer, short[]> QUEEN_DIR_FUNC =
            (Object _, Integer _) -> new short[]{ChessBoard.NORTH, ChessBoard.SOUTH, ChessBoard.WEST, ChessBoard.EAST,
                    ChessBoard.NORTH_WEST, ChessBoard.NORTH_EAST, ChessBoard.SOUTH_WEST, ChessBoard.SOUTH_EAST};
    public static BiFunction<Object, Integer, Integer> QUEEN_RANGE_FUNC =
            (_, _) -> NO_RANGE_LIMIT;
    //endregion

    //endregion

    public BiFunction<Object, Integer, Integer> maxRangeFunc;
    public BiFunction<Object, Integer, short[]> directionFunc;

    protected int[][][] preComputeBishopMoves = new int[ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE][][];
    protected int[][][] preComputeRookMoves = new int[ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE][][];
    //endregion

    //region CONSTRUCTOR
    SlidingPieceData(short pieceId)
    {
        super(pieceId);
        switch(pieceId)
        {
            case BPAWN:
                this.maxRangeFunc = BPAWN_RANGE_FUNC;
                this.directionFunc = BPAWN_DIR_FUNC;
                break;
            case WPAWN:
                this.maxRangeFunc = WPAWN_RANGE_FUNC;
                this.directionFunc = WPAWN_DIR_FUNC;
                break;
            case BROOK: case WROOK:
                this.maxRangeFunc = ROOK_RANGE_FUNC;
                this.directionFunc = ROOK_DIR_FUNC;
                break;
            case BBISHOP: case WBISHOP:
                this.maxRangeFunc = BISHOP_RANGE_FUNC;
                this.directionFunc = BISHOP_DIR_FUNC;
                break;
            case BQUEEN: case WQUEEN:
                this.maxRangeFunc = QUEEN_RANGE_FUNC;
                this.directionFunc = QUEEN_DIR_FUNC;
                break;
        }
    }

    @SuppressWarnings("unused")
    SlidingPieceData(short pieceId, boolean color, int value, String name, ImageIcon graphic,
                     BiFunction<Object, Integer, Integer> maxRangeFunc,
                     BiFunction<Object, Integer, short[]> directionFunc)
    {
        super(pieceId, color, value, name, graphic);
        this.maxRangeFunc = maxRangeFunc;
        this.directionFunc = directionFunc;
    }

    SlidingPieceData(UUID uuid, short pieceId, boolean color, int value, String name, ImageIcon graphic,
                     BiFunction<Object, Integer, Integer> maxRangeFunc,
                     BiFunction<Object, Integer, short[]> directionFunc)
    {
        super(uuid, pieceId, color, value, name, graphic);
        this.maxRangeFunc = maxRangeFunc;
        this.directionFunc = directionFunc;
    }

    public SlidingPieceData(SlidingPieceData piece)
    {
        super(piece);
        this.maxRangeFunc = piece.maxRangeFunc;
        this.directionFunc = piece.directionFunc;
    }

    //endregion

    public short[] getPossibleDirections(MinimalChessGame<?> chessGame, int spaceId) {
        return directionFunc.apply(chessGame, spaceId);
    }
    public int getMaxRange(MinimalChessGame<?> chessGame, int spaceId) {
        return maxRangeFunc.apply(chessGame, spaceId);
    }

    @Override
    public SlidingPieceData clone(){
        return new SlidingPieceData(uuid, pieceId, color, value, name, graphic, maxRangeFunc, directionFunc);
    }

    @Override
    public SlidingPieceData getUniqueClone(){
        return new SlidingPieceData(pieceId, color, value, name, graphic, maxRangeFunc, directionFunc);
    }

    private static int[][] preComputeBishopMoves(int spaceId){
        int[][] bishopMultiDirectionsMoves = new int[4][];

        bishopMultiDirectionsMoves[0] = preComputeDirectionalMoves(spaceId, ChessBoard.NORTH_EAST);
        bishopMultiDirectionsMoves[1] = preComputeDirectionalMoves(spaceId, ChessBoard.SOUTH_EAST);
        bishopMultiDirectionsMoves[2] = preComputeDirectionalMoves(spaceId, ChessBoard.SOUTH_WEST);
        bishopMultiDirectionsMoves[3] = preComputeDirectionalMoves(spaceId, ChessBoard.NORTH_WEST);

        return bishopMultiDirectionsMoves;
    }

    private static int[][] preComputeRookMoves(int spaceId){
        int[][] rookMultiDirectionsMoves = new int[4][];

        rookMultiDirectionsMoves[0] = preComputeDirectionalMoves(spaceId, ChessBoard.NORTH);
        rookMultiDirectionsMoves[1] = preComputeDirectionalMoves(spaceId, ChessBoard.EAST);
        rookMultiDirectionsMoves[2] = preComputeDirectionalMoves(spaceId, ChessBoard.SOUTH);
        rookMultiDirectionsMoves[3] = preComputeDirectionalMoves(spaceId, ChessBoard.WEST);

        return rookMultiDirectionsMoves;
    }

    private static int[] preComputeDirectionalMoves(int spaceId, int dir){
        int[] bishopMoves = new int[7];
        Arrays.fill(bishopMoves, ChessBoard.INVALID_SPACE_ID);

        int counter = 1;
        int move = ChessBoard.getDirSpaceId(spaceId, dir, counter);
        while (ChessBoard.isValidSpaceId(move)){
            bishopMoves[counter-1] = move;
            ++ counter;
            move = ChessBoard.getDirSpaceId(spaceId, dir, counter);
        }
        return bishopMoves;
    }
}

