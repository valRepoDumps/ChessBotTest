package ChessResources.Pieces.PieceDatas;

import ChessResources.ChessBoard;

import javax.swing.*;
import java.util.function.BiFunction;

public class JumpingPieceData extends PieceData{

    //region KNIGHT_FUNCS
    public static final BiFunction<Object, Integer, int[]> KNIGHT_MOVES_FUNC =
            (Object board, Integer spaceId) ->{
                ChessBoard chessBoard = (ChessBoard) board;
                JumpingPieceData piece = (JumpingPieceData) chessBoard.getPiece(spaceId);

                int[] possibleSquares = new int[] {ChessBoard.INVALID_SPACE_ID,ChessBoard.INVALID_SPACE_ID,
                        ChessBoard.INVALID_SPACE_ID,ChessBoard.INVALID_SPACE_ID,ChessBoard.INVALID_SPACE_ID,
                        ChessBoard.INVALID_SPACE_ID,ChessBoard.INVALID_SPACE_ID,ChessBoard.INVALID_SPACE_ID};

                int row = spaceId/ChessBoard.BOARD_SIZE;
                int col = spaceId%ChessBoard.BOARD_SIZE;

                if (row - 2 >= 0 && col-1 >= 0) { //assumes input is already valid.
                    possibleSquares[0] = spaceId + 2 * ChessBoard.directionOffsets[ChessBoard.NORTH] + //upleft
                            ChessBoard.directionOffsets[ChessBoard.WEST];
                }

                if (row-2>=0 && col+1 < ChessBoard.BOARD_SIZE){
                    possibleSquares[1] = spaceId + 2*ChessBoard.directionOffsets[ChessBoard.NORTH] + //upright
                        ChessBoard.directionOffsets[ChessBoard.EAST];
                }

                if (row+2<ChessBoard.BOARD_SIZE && col-1 >= 0) {
                    possibleSquares[2] = spaceId + 2 * ChessBoard.directionOffsets[ChessBoard.SOUTH] + //downleft
                            ChessBoard.directionOffsets[ChessBoard.WEST];
                }

                if (row+2<ChessBoard.BOARD_SIZE && col+1 < ChessBoard.BOARD_SIZE){
                    possibleSquares[3] = spaceId + 2*ChessBoard.directionOffsets[ChessBoard.SOUTH] + //downright
                        ChessBoard.directionOffsets[ChessBoard.EAST];
                }

                if (col-2 >= 0 && row -1 >= 0){
                    possibleSquares[4] = spaceId + 2*ChessBoard.directionOffsets[ChessBoard.WEST] + //leftup
                        ChessBoard.directionOffsets[ChessBoard.NORTH];}
                if (col-2 >= 0 && row +1 <ChessBoard.BOARD_SIZE){
                    possibleSquares[5] = spaceId + 2*ChessBoard.directionOffsets[ChessBoard.WEST] + //leftdown
                        ChessBoard.directionOffsets[ChessBoard.SOUTH];
                }

                if (col+2 < ChessBoard.BOARD_SIZE && row-1 >= 0){
                    possibleSquares[6] = spaceId + 2*ChessBoard.directionOffsets[ChessBoard.EAST] + //rightup
                        ChessBoard.directionOffsets[ChessBoard.NORTH];
                }
                if (col+2 < ChessBoard.BOARD_SIZE && row + 1 < ChessBoard.BOARD_SIZE) {
                    possibleSquares[7] = spaceId + 2 * ChessBoard.directionOffsets[ChessBoard.EAST] + //rightdown
                            ChessBoard.directionOffsets[ChessBoard.SOUTH];
                }

                return possibleSquares;
            };

    //endregion
    public BiFunction<Object, Integer, int[]> movesFunc;
    public JumpingPieceData(short pieceId, boolean color, int value, String name, ImageIcon graphic,
                            BiFunction<Object, Integer, int[]> movesFunc) {
        super(pieceId, color, value, name, graphic);
        this.movesFunc = movesFunc;
    }

    public JumpingPieceData(short pieceId)
    {
        super(pieceId);
        switch (pieceId)
        {
            case BKNIGHT: case WKNIGHT:
                this.movesFunc = KNIGHT_MOVES_FUNC;
        }
    }

    public JumpingPieceData(JumpingPieceData piece)
    {
        super(piece);
        this.movesFunc = piece.movesFunc;
    }
    public int[] getPossibleMoves(ChessBoard board, int spaceId)
    {
        System.out.println("DEBUG");
        return movesFunc.apply(board, spaceId);
    }
}
