package ChessResources;

import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.ChessSpaces;
import ChessResources.Pieces.MovesGeneration;
import ChessResources.Pieces.PieceConsts;
import ChessResources.Pieces.PieceData;

import java.util.Arrays;

public class PreCalc {

    public static final int QUEEN_SIDE_CASTLE = 8;
    public static final int KING_SIDE_CASTLE = 9;

    public static final int[][][] SLIDING_MOVES = new int[ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE][][];
    public static final int[][] NUM_SQUARES_TO_EDGE = new int[ChessBoard.BOARD_SIZE* ChessBoard.BOARD_SIZE][8];
    public static final ChessSpaces ALL_BOARD_SPACES = new ChessSpaces();

    public static int POSSIBLE_KNIGHT_MOVES = 8;
    public static int POSSIBLE_KING_MOVES = 6;

    public static int[][] KNIGHT_MOVES = new int[ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE][];
    public static int[][] KING_MOVES = new int[ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE][];

    public static final short[] FEN_MAP = new short[128];
    public static final short[] WHITE_PIECES = { PieceData.WPAWN, PieceData.WKNIGHT, PieceData.WBISHOP,
            PieceData.WROOK, PieceData.WQUEEN, PieceData.WKING };
    public static final short[] BLACK_PIECES = { PieceData.BPAWN, PieceData.BKNIGHT, PieceData.BBISHOP,
            PieceData.BROOK, PieceData.BQUEEN, PieceData.BKING };

    public static final short[] WHITE_THREAT_IDS = BLACK_PIECES;
    public static final short[] BLACK_THREAT_IDS = WHITE_PIECES;

    public static final PieceData[] PIECE_ID_TO_PIECE_DATA_MAP = new PieceData[PieceData.MAX_PIECES];

    static {
        assignFenMap();
        assignPieceIdToPieceDataMap();
    }

    //region SLIDING_PIECE_PRE_CALC
    private static void precomputeSquaresToEdgeData()
    {
        for (int row = 0; row< ChessBoard.BOARD_SIZE; ++row)
        {
            for (int col = 0; col < ChessBoard.BOARD_SIZE; ++col)
            {
                int numSouth = ChessBoard.BOARD_SIZE-1 - row;
                int numEast = ChessBoard.BOARD_SIZE-1 - col;

                int squareIdx = row* ChessBoard.BOARD_SIZE + col;

                NUM_SQUARES_TO_EDGE[squareIdx][ChessBoard.NORTH] = row;
                NUM_SQUARES_TO_EDGE[squareIdx][ChessBoard.SOUTH] = numSouth;
                NUM_SQUARES_TO_EDGE[squareIdx][ChessBoard.EAST] = numEast;
                NUM_SQUARES_TO_EDGE[squareIdx][ChessBoard.WEST] = col;
                NUM_SQUARES_TO_EDGE[squareIdx][ChessBoard.NORTH_WEST] = Math.min(row, col);
                NUM_SQUARES_TO_EDGE[squareIdx][ChessBoard.NORTH_EAST] = Math.min(row, numEast);
                NUM_SQUARES_TO_EDGE[squareIdx][ChessBoard.SOUTH_WEST] = Math.min(numSouth, col);
                NUM_SQUARES_TO_EDGE[squareIdx][ChessBoard.SOUTH_EAST] = Math.min(numSouth, numEast);

            }
        }
    }

    private static int[] preComputeDirectionalMoves(int spaceId, short dir){

        int[] moves = new int[8];
        Arrays.fill(moves, ChessBoard.INVALID_SPACE_ID);

        int counter = 1;
        int move = ChessBoard.getDirSpaceId(spaceId, counter, dir);

        while (ChessBoard.isValidSpaceId(move) && counter <= NUM_SQUARES_TO_EDGE[spaceId][dir]){

            moves[counter-1] = move;
            ++ counter;
            move = ChessBoard.getDirSpaceId(spaceId, counter, dir);
        }
        return moves;
    }

    private static int[][] preComputeMoves(int spaceId){
        int[][] multiDirectionsMoves = new int[8][];

        multiDirectionsMoves[ChessBoard.NORTH] = preComputeDirectionalMoves(spaceId, ChessBoard.NORTH);
        multiDirectionsMoves[ChessBoard.EAST] = preComputeDirectionalMoves(spaceId, ChessBoard.EAST);
        multiDirectionsMoves[ChessBoard.SOUTH] = preComputeDirectionalMoves(spaceId, ChessBoard.SOUTH);
        multiDirectionsMoves[ChessBoard.WEST] = preComputeDirectionalMoves(spaceId, ChessBoard.WEST);
        multiDirectionsMoves[ChessBoard.NORTH_EAST] = preComputeDirectionalMoves(spaceId, ChessBoard.NORTH_EAST);
        multiDirectionsMoves[ChessBoard.SOUTH_EAST] = preComputeDirectionalMoves(spaceId, ChessBoard.SOUTH_EAST);
        multiDirectionsMoves[ChessBoard.SOUTH_WEST] = preComputeDirectionalMoves(spaceId, ChessBoard.SOUTH_WEST);
        multiDirectionsMoves[ChessBoard.NORTH_WEST] = preComputeDirectionalMoves(spaceId, ChessBoard.NORTH_WEST);

        return multiDirectionsMoves;
    }

    public static void preComputeAllMoves(){
        precomputeSquaresToEdgeData();
        for (int i = 0; i < ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE; ++i){
            SLIDING_MOVES[i] = preComputeMoves(i);
        }
        preGenerateIrregularPieceMoves();
    }
    //endregion

    //region IRREGULAR_PRECALC
    private static void preComputeAllBoardSpaces(){
        for (int i = 0; i < ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE; ++i){
            ALL_BOARD_SPACES.addMoves(i);
        }
    }

    private static void preGenerateIrregularPieceMoves(){
        for (int i = 0; i < ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE; ++i){
            preGenerateKnightMoves(i);
            preGenerateKingMoves(i);
        }
    }

    public static int[] getPreGenerateKnightMoves(int spaceId){
        return KNIGHT_MOVES[spaceId];
    }
    private static void preGenerateKnightMoves(int spaceId){

        int[] possibleSquares = new int[] {ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID,
                ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID,
                ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID};

        int row = ChessBoard.getRow(spaceId);
        int col = ChessBoard.getCol(spaceId);

        if (row-2 >= 0 && col-1 >= 0) { //assumes input is already valid.
            possibleSquares[0] = spaceId +
                    2*ChessBoard.directionOffsets[ChessBoard.NORTH] + //upleft
                    ChessBoard.directionOffsets[ChessBoard.WEST];
        }

        if (row-2>=0 && col+1 < ChessBoard.BOARD_SIZE){
            possibleSquares[1] = spaceId +
                    2*ChessBoard.directionOffsets[ChessBoard.NORTH] + //upright
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
            possibleSquares[5] = spaceId +
                    2*ChessBoard.directionOffsets[ChessBoard.WEST] + //leftdown
                    ChessBoard.directionOffsets[ChessBoard.SOUTH];
        }

        if (col+2 < ChessBoard.BOARD_SIZE && row-1 >= 0){
            possibleSquares[6] = spaceId +
                    2*ChessBoard.directionOffsets[ChessBoard.EAST] + //rightup
                    ChessBoard.directionOffsets[ChessBoard.NORTH];
        }
        if (col+2 < ChessBoard.BOARD_SIZE && row + 1 < ChessBoard.BOARD_SIZE) {
            possibleSquares[7] = spaceId +
                    2*ChessBoard.directionOffsets[ChessBoard.EAST] + //rightdown
                    ChessBoard.directionOffsets[ChessBoard.SOUTH];
        }

        assignPreGenerateKnightMoves(spaceId, possibleSquares);
    }

    private static void preGenerateKingMoves(int spaceId){

        int[] possibleSquares = new int[] {ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID,
                ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID,
                ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID,
                ChessBoard.INVALID_SPACE_ID, ChessBoard.INVALID_SPACE_ID};

        int row = ChessBoard.getRow(spaceId);
        int col = ChessBoard.getCol(spaceId);

        if (row < ChessBoard.BOARD_SIZE-1){
            possibleSquares[0] = spaceId + ChessBoard.directionOffsets[ChessBoard.SOUTH];
            if (col < ChessBoard.BOARD_SIZE-1){
                possibleSquares[1] = spaceId + ChessBoard.directionOffsets[ChessBoard.SOUTH_EAST];
            }
            if (col > 0){
                possibleSquares[2] = spaceId + ChessBoard.directionOffsets[ChessBoard.SOUTH_WEST];
            }
        }

        if (row > 0){
            possibleSquares[3] = spaceId + ChessBoard.directionOffsets[ChessBoard.NORTH];
            if (col < ChessBoard.BOARD_SIZE-1){
                possibleSquares[4] = spaceId + ChessBoard.directionOffsets[ChessBoard.NORTH_EAST];
            }
            if (col > 0){
                possibleSquares[5] = spaceId + ChessBoard.directionOffsets[ChessBoard.NORTH_WEST];
            }
        }

        if (col > 0){
            possibleSquares[6] = spaceId + ChessBoard.directionOffsets[ChessBoard.WEST];
        }

        if (col < ChessBoard.BOARD_SIZE-1){
            possibleSquares[7] = spaceId + ChessBoard.directionOffsets[ChessBoard.EAST];
        }

        //
        if (row == 0 || row == ChessBoard.BOARD_SIZE - 1){
            if (col > 3){
                possibleSquares[QUEEN_SIDE_CASTLE] = spaceId + 2*ChessBoard.directionOffsets[ChessBoard.WEST];
            }
            if (col < 5){
                possibleSquares[KING_SIDE_CASTLE] = spaceId + 2*ChessBoard.directionOffsets[ChessBoard.EAST];
            }
        }

        assignPreGenerateKingMoves(spaceId, possibleSquares);
    }

    public static int[] getPreGenerateKingMoves(int spaceId){
        return KING_MOVES[spaceId];
    }

    private static void assignPreGenerateKnightMoves(int spaceId, int[] moves){
        KNIGHT_MOVES[spaceId] = moves;
    }

    private static void assignPreGenerateKingMoves(int spaceId, int[] moves){
        KING_MOVES[spaceId] = moves;
    }
    //endregion

    //region FEN_MAP
    public static void assignFenMap(){
        FEN_MAP['k'] = PieceData.BKING;
        FEN_MAP['q'] = PieceData.BQUEEN;
        FEN_MAP['b'] = PieceData.BBISHOP;
        FEN_MAP['n'] = PieceData.BKNIGHT;
        FEN_MAP['r'] = PieceData.BROOK;
        FEN_MAP['p'] = PieceData.BPAWN;

        FEN_MAP['K'] = PieceData.WKING;
        FEN_MAP['Q'] = PieceData.WQUEEN;
        FEN_MAP['B'] = PieceData.WBISHOP;
        FEN_MAP['N'] = PieceData.WKNIGHT;
        FEN_MAP['R'] = PieceData.WROOK;
        FEN_MAP['P'] = PieceData.WPAWN;
    }
    //endregion

    //region PIECE_ID_TO_PIECE_DATA_MAP
    public static void assignPieceIdToPieceDataMap(){
        Arrays.fill(PIECE_ID_TO_PIECE_DATA_MAP, PieceConsts.NO_PIECE);

        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.BPAWN] = PieceData.BPAWN_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.WPAWN] = PieceData.WPAWN_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.BROOK] = PieceData.BROOK_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.WROOK] = PieceData.WROOK_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.BKNIGHT] = PieceData.BKNIGHT_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.WKNIGHT] = PieceData.WKNIGHT_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.BBISHOP] = PieceData.BBISHOP_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.WBISHOP] = PieceData.WBISHOP_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.BQUEEN] = PieceData.BQUEEN_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.WQUEEN] = PieceData.WQUEEN_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.BKING] = PieceData.BKING_DATA;
        PIECE_ID_TO_PIECE_DATA_MAP[PieceData.WKING] = PieceData.WKING_DATA;
    }
    //endregion
}
