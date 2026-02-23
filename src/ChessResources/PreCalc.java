package ChessResources;

import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.ChessSpaces;

import java.util.Arrays;

public class PreCalc {
    public static final int[][][] PRECOMPUTED_MOVES = new int[ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE][][];
    public static final int[][] NUM_SQUARES_TO_EDGE = new int[ChessBoard.BOARD_SIZE* ChessBoard.BOARD_SIZE][8];
    public static final ChessSpaces ALL_BOARD_SPACES = new ChessSpaces();

    static {
        preComputeAllMoves();
        System.out.println(Arrays.toString(PRECOMPUTED_MOVES[0][ChessBoard.SOUTH]));
    }

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

    private static void preComputeAllMoves(){
        precomputeSquaresToEdgeData();
        for (int i = 0; i < ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE; ++i){
            PRECOMPUTED_MOVES[i] = preComputeMoves(i);
        }

    }

    private static void preComputeAllBoardSpaces(){
        for (int i = 0; i < ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE; ++i){
            ALL_BOARD_SPACES.addMoves(i);
        }
    }
}
