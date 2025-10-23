package ChessResources.Pieces.PieceDatas;

import ChessResources.ChessBoard;

public class SlidingPieceData extends PieceData {
    public static final int NO_RANGE_LIMIT = ChessBoard.BOARD_SIZE;

    public short[] possibleDirections; //8 possible direction

    public int maxRange;

    SlidingPieceData(short pieceId)
    {
        super(pieceId);
        switch(pieceId)
        {
            case BPAWN: case WPAWN:
                this.possibleDirections = new short[] {ChessBoard.NORTH};
                this.maxRange = 1;
                break;
            case BROOK: case WROOK:
                this.possibleDirections = new short[] {ChessBoard.NORTH, ChessBoard.SOUTH,
                        ChessBoard.WEST, ChessBoard.EAST};
                this.maxRange = NO_RANGE_LIMIT;
                break;
            case BBISHOP: case WBISHOP:
                this.possibleDirections = new short[] {ChessBoard.NORTH_WEST, ChessBoard.NORTH_EAST,
                        ChessBoard.SOUTH_WEST, ChessBoard.SOUTH_EAST};
                this.maxRange = NO_RANGE_LIMIT;
                break;
            case BQUEEN: case WQUEEN:
                this.possibleDirections = new short[] {ChessBoard.NORTH, ChessBoard.SOUTH,
                        ChessBoard.WEST, ChessBoard.EAST, ChessBoard.NORTH_WEST, ChessBoard.NORTH_EAST,
                        ChessBoard.SOUTH_WEST, ChessBoard.SOUTH_EAST};
                this.maxRange = NO_RANGE_LIMIT;
                break;
            case BKING: case WKING:
                this.possibleDirections = new short[] {ChessBoard.NORTH, ChessBoard.SOUTH,
                        ChessBoard.WEST, ChessBoard.EAST, ChessBoard.NORTH_WEST, ChessBoard.NORTH_EAST,
                        ChessBoard.SOUTH_WEST, ChessBoard.SOUTH_EAST};
                this.maxRange = 1;
                break;
        }
    }

    SlidingPieceData(short pieceId, short[] possibleDirections, int maxRange)
    {
        super(pieceId);
        this.possibleDirections = possibleDirections;
        this.maxRange = maxRange;
    }
}

