package ChessResources.Pieces.PieceDatas;

import ChessLogic.ChessGame;
import ChessResources.ChessBoardUI;

import javax.swing.*;
import java.util.function.BiFunction;

public class SlidingPieceData extends PieceData {
    public static final int NO_RANGE_LIMIT = ChessBoardUI.BOARD_SIZE;

    //region PIECE FUNCS
    //region PAWN FUNCS

    public static BiFunction<Object, Integer, short[]> BPAWN_DIR_FUNC =
            (Object game, Integer spaceId) ->{
                ChessBoardUI chessBoardUI = ((ChessGame)game).chessBoardUI;
                SlidingPieceData piece = (SlidingPieceData) chessBoardUI.getPiece(spaceId);

                int opCode = 1;
//                System.out.println("EP TARGET IN FUNC: " + ((ChessGame) game).enPassantTarget +
//                        ChessBoard.isImmediateSouth(spaceId, ((ChessGame) game).enPassantTarget)
//                        + ChessBoard.isImmediateEast(spaceId, ((ChessGame) game).enPassantTarget)
//                        + ChessBoard.isImmediateWest(spaceId, ((ChessGame) game).enPassantTarget)
//                        +ChessBoard.isImmediateNorth(spaceId, ((ChessGame) game).enPassantTarget));
                if (spaceId <= ChessBoardUI.BOARD_SIZE * (ChessBoardUI.BOARD_SIZE-1))
                {
                    if (chessBoardUI.isEnemyPieceAt(spaceId +
                            ChessBoardUI.directionOffsets[ChessBoardUI.SOUTH_EAST], piece.color) ||
                            (ChessBoardUI.isImmediateSouth(spaceId, ((ChessGame) game).enPassantTarget) &&
                                    ChessBoardUI.isImmediateEast(spaceId, ((ChessGame) game).enPassantTarget))
                    )
                    {
                        opCode = 2;
                        if (chessBoardUI.isEnemyPieceAt(spaceId +
                                ChessBoardUI.directionOffsets[ChessBoardUI.SOUTH_WEST], piece.color) ||
                                (ChessBoardUI.isImmediateSouth(spaceId, ((ChessGame) game).enPassantTarget) &&
                                        ChessBoardUI.isImmediateWest(spaceId, ((ChessGame) game).enPassantTarget)))
                        {
                            opCode = 3;
                        }
                    }

                    else if (chessBoardUI.isEnemyPieceAt(spaceId +
                            ChessBoardUI.directionOffsets[ChessBoardUI.SOUTH_WEST], piece.color) ||
                            (ChessBoardUI.isImmediateSouth(spaceId, ((ChessGame) game).enPassantTarget) &&
                                    ChessBoardUI.isImmediateWest(spaceId, ((ChessGame) game).enPassantTarget)))
                    {
                        opCode = 4;
                    }

                    if (!chessBoardUI.isEnemyPieceAt(spaceId +
                            ChessBoardUI.directionOffsets[ChessBoardUI.SOUTH], piece.color))
                    {
                        opCode*=-1;
                    }
                }

                return switch (opCode) {
                    case -1 -> new short[]{ChessBoardUI.SOUTH};
                    case 2 -> new short[]{ChessBoardUI.SOUTH_EAST};
                    case -2 -> new short[]{ChessBoardUI.SOUTH_EAST, ChessBoardUI.SOUTH};
                    case 3 -> new short[]{ChessBoardUI.SOUTH_WEST, ChessBoardUI.SOUTH_EAST};
                    case -3 -> new short[]{ChessBoardUI.SOUTH, ChessBoardUI.SOUTH_WEST, ChessBoardUI.SOUTH_EAST};
                    case 4 -> new short[]{ChessBoardUI.SOUTH_WEST};
                    case -4 -> new short[]{ChessBoardUI.SOUTH_WEST, ChessBoardUI.SOUTH};
                    default -> new short[]{};
                };
            };
    public static BiFunction<Object, Integer, Integer> BPAWN_RANGE_FUNC =
            (game, spaceId) -> {
                ChessBoardUI chessBoardUI = ((ChessGame) game).chessBoardUI;
                SlidingPieceData piece = (SlidingPieceData) chessBoardUI.getPiece(spaceId);

                if (chessBoardUI.isEnemyPieceAt(spaceId + ChessBoardUI.directionOffsets[ChessBoardUI.SOUTH], piece.color) ||
                chessBoardUI.isEnemyPieceAt(spaceId + 2* ChessBoardUI.directionOffsets[ChessBoardUI.SOUTH], piece.color))
                {
                    return 1; //if enemy is right in front, range will always be 1.
                }
                return spaceId / ChessBoardUI.BOARD_SIZE == 1 ? 2 : 1;
            };

    public static BiFunction<Object, Integer, short[]> WPAWN_DIR_FUNC =
            (Object game, Integer spaceId) ->{
                ChessBoardUI chessBoardUI = ((ChessGame) game).chessBoardUI;
                SlidingPieceData piece = (SlidingPieceData) chessBoardUI.getPiece(spaceId);

                int opCode = 1;

//                System.out.println("EP TARGET IN FUNC: " + ((ChessGame) game).enPassantTarget +
//                        ChessBoard.isImmediateSouth(spaceId, ((ChessGame) game).enPassantTarget)
//                        + ChessBoard.isImmediateEast(spaceId, ((ChessGame) game).enPassantTarget)
//                        + ChessBoard.isImmediateWest(spaceId, ((ChessGame) game).enPassantTarget)
//                        +ChessBoard.isImmediateNorth(spaceId, ((ChessGame) game).enPassantTarget));

                if (spaceId <= ChessBoardUI.BOARD_SIZE * (ChessBoardUI.BOARD_SIZE-1))
                {
                    if (chessBoardUI.isEnemyPieceAt(spaceId +
                            ChessBoardUI.directionOffsets[ChessBoardUI.NORTH_EAST], piece.color) ||
                            (ChessBoardUI.isImmediateNorth(spaceId, ((ChessGame) game).enPassantTarget) &&
                                    ChessBoardUI.isImmediateEast(spaceId, ((ChessGame) game).enPassantTarget)))
                    {
                        opCode = 2;
                        if (chessBoardUI.isEnemyPieceAt(spaceId +
                                ChessBoardUI.directionOffsets[ChessBoardUI.NORTH_WEST], piece.color)||
                                (ChessBoardUI.isImmediateNorth(spaceId, ((ChessGame) game).enPassantTarget) &&
                                        ChessBoardUI.isImmediateWest(spaceId, ((ChessGame) game).enPassantTarget)))
                        {
                            opCode = 3;
                        }
                    }

                    else if (chessBoardUI.isEnemyPieceAt(spaceId +
                            ChessBoardUI.directionOffsets[ChessBoardUI.NORTH_WEST], piece.color) ||
                            (ChessBoardUI.isImmediateNorth(spaceId, ((ChessGame) game).enPassantTarget) &&
                                    ChessBoardUI.isImmediateWest(spaceId, ((ChessGame) game).enPassantTarget)))
                    {
                        opCode = 4;
                    }

                    if (!chessBoardUI.isEnemyPieceAt(spaceId +
                            ChessBoardUI.directionOffsets[ChessBoardUI.NORTH], piece.color))
                    {
                        opCode*=-1;
                    }
                }

                return switch (opCode) {
                    case -1 -> new short[]{ChessBoardUI.NORTH};
                    case 2 -> new short[]{ChessBoardUI.NORTH_EAST};
                    case -2 -> new short[]{ChessBoardUI.NORTH_EAST, ChessBoardUI.NORTH};
                    case 3 -> new short[]{ChessBoardUI.NORTH_WEST, ChessBoardUI.NORTH_EAST};
                    case -3 -> new short[]{ChessBoardUI.NORTH, ChessBoardUI.NORTH_WEST, ChessBoardUI.NORTH_EAST};
                    case 4 -> new short[]{ChessBoardUI.NORTH_WEST};
                    case -4 -> new short[]{ChessBoardUI.NORTH_WEST, ChessBoardUI.NORTH};
                    default -> new short[]{};
                };
            };

    public static BiFunction<Object, Integer, Integer> WPAWN_RANGE_FUNC =
            (game, spaceId) -> {
                ChessBoardUI chessBoardUI = ((ChessGame) game).chessBoardUI;
                SlidingPieceData piece = (SlidingPieceData) chessBoardUI.getPiece(spaceId);

                if (chessBoardUI.isEnemyPieceAt(spaceId + ChessBoardUI.directionOffsets[ChessBoardUI.NORTH], piece.color) ||
                        chessBoardUI.isEnemyPieceAt(spaceId + 2* ChessBoardUI.directionOffsets[ChessBoardUI.NORTH], piece.color))
                {
                    return 1; //if enemy is right in front, or up to 2 range in front, range will always be 1.
                }
                return spaceId / ChessBoardUI.BOARD_SIZE == 6 ? 2 : 1;
            };
    //endregion

    public static BiFunction<Object, Integer, short[]> ROOK_DIR_FUNC =
            (Object game, Integer spaceId) ->{

                return new short[]{ChessBoardUI.NORTH, ChessBoardUI.SOUTH, ChessBoardUI.WEST, ChessBoardUI.EAST};
            };
    public static BiFunction<Object, Integer, Integer> ROOK_RANGE_FUNC =
            (game, spaceId) -> {
                return NO_RANGE_LIMIT;
            };

    public static BiFunction<Object, Integer, short[]> BISHOP_DIR_FUNC =
            (Object game, Integer spaceId) ->{
                return new short[]{ChessBoardUI.NORTH_WEST, ChessBoardUI.NORTH_EAST, ChessBoardUI.SOUTH_WEST, ChessBoardUI.SOUTH_EAST};
            };
    public static BiFunction<Object, Integer, Integer> BISHOP_RANGE_FUNC =
            (game, spaceId) -> {
                return NO_RANGE_LIMIT;
            };

    public static BiFunction<Object, Integer, short[]> QUEEN_DIR_FUNC =
            (Object game, Integer spaceId) ->{

                return new short[]{ChessBoardUI.NORTH, ChessBoardUI.SOUTH, ChessBoardUI.WEST, ChessBoardUI.EAST,
                        ChessBoardUI.NORTH_WEST, ChessBoardUI.NORTH_EAST, ChessBoardUI.SOUTH_WEST, ChessBoardUI.SOUTH_EAST};
            };
    public static BiFunction<Object, Integer, Integer> QUEEN_RANGE_FUNC =
            (game, spaceId) -> {
                return NO_RANGE_LIMIT;
            };

    //region KING FUNCS
    public static BiFunction<Object, Integer, short[]> KING_DIR_FUNC =
            (Object game, Integer spaceId) ->{

                return new short[]{ChessBoardUI.NORTH, ChessBoardUI.SOUTH, ChessBoardUI.WEST, ChessBoardUI.EAST,
                        ChessBoardUI.NORTH_WEST, ChessBoardUI.NORTH_EAST, ChessBoardUI.SOUTH_WEST, ChessBoardUI.SOUTH_EAST};
            };
    public static BiFunction<Object, Integer, Integer> KING_RANGE_FUNC =
            (game, spaceId) -> {
                return 1;
            };
    //endregion
    //endregion

    public BiFunction<Object, Integer, Integer> maxRangeFunc;
    public BiFunction<Object, Integer, short[]> directionFunc;

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
            case BKING: case WKING:
                this.maxRangeFunc = KING_RANGE_FUNC;
                this.directionFunc = KING_DIR_FUNC;
                break;
        }
    }

    SlidingPieceData(short pieceId, boolean color, int value, String name, ImageIcon graphic,
                     BiFunction<Object, Integer, Integer> maxRangeFunc,
                     BiFunction<Object, Integer, short[]> directionFunc)
    {
        super(pieceId, color, value, name, graphic);
        this.maxRangeFunc = maxRangeFunc;
        this.directionFunc = directionFunc;
    }

    public SlidingPieceData(SlidingPieceData piece)
    {
        super(piece);
        this.maxRangeFunc = piece.maxRangeFunc;
        this.directionFunc = piece.directionFunc;
    }

    public short[] getPossibleDirections(ChessGame chessGame, int spaceId) {
        return directionFunc.apply(chessGame, spaceId);
    }
    public int getMaxRange(ChessGame chessGame, int spaceId) {
        return maxRangeFunc.apply(chessGame, spaceId);
    }
}

