package ChessResources;

import ChessResources.Pieces.PieceDatas.PieceData;
import ChessResources.Pieces.PieceDatas.PieceDatas;

public class ChessBoard {
    //region BOARD_SIZE
    public static final int BOARD_SIZE = 8;
    public  static final int SQUARE_PIXEL_SIZE = 100;
    //endregion
    public static final int INVALID_SPACE_ID = -1;

    //region DIRECTIONAL_OFFSETS
    //assume board 0 index is in top left.
    public static final int[] directionOffsets = {8, -8, -1, 1, 7, -7, 9, -9};
    //use SOUTH,NORTH,... with directionOffsets to calculate movement.
    public static final short SOUTH = 0;
    public static final short NORTH = 1;
    public static final short WEST = 2;
    public static final short EAST = 3;
    public static final short SOUTH_WEST = 4;
    public static final short NORTH_EAST = 5;
    public static final short SOUTH_EAST = 6;
    public static final short NORTH_WEST = 7;
    //endregion
    public static final boolean BLACK = false;
    public static final boolean WHITE = true;

    public PieceData[] boardSquares = new PieceData[BOARD_SIZE*BOARD_SIZE];

    public ChessBoard()
    {}

    public static boolean isValidSpaceId(int spaceId)
    {
        return spaceId >= 0 && spaceId < BOARD_SIZE*BOARD_SIZE;
    }

    public void setPieceAt(int spaceId, PieceData piece)
    {
        if (isValidSpaceId(spaceId)) boardSquares[spaceId] = piece;
    }

    public PieceData getPiece(int spaceId)
    {
        if (isValidSpaceId(spaceId)) return boardSquares[spaceId];
        else {
            System.out.println("Invalid spaceId at getPiece");
            return null;
        }
    }

    public void movePiece(int spaceIdToMove, int spaceIdArriveAt)
    {//move piece and just overwrite piece in that location.
        if (isValidSpaceId(spaceIdToMove) && isValidSpaceId(spaceIdArriveAt))
        {
            PieceData piece = getPiece(spaceIdToMove);
            setPieceAt(spaceIdToMove, PieceDatas.NO_PIECE);
            setPieceAt(spaceIdArriveAt, piece);
        }
        else {
            System.out.println("Invalid spaceId at movePiece");
        }
    }

    public boolean isPieceAt(int spaceId)
    {
        if (!isValidSpaceId(spaceId)) return false;
        return getPiece(spaceId) != PieceDatas.NO_PIECE;
    }

    public boolean isEnemyPieceAt(int spaceId, boolean pieceColor)
    {
        if (!isValidSpaceId(spaceId)) return false;
        if (getPiece(spaceId) == PieceDatas.NO_PIECE) return false;//no enemy piece

        return getPiece(spaceId).color != pieceColor;
    }

    public boolean isAlliedPieceAt(int spaceId, boolean pieceColor)
    {
        if (getPiece(spaceId) == PieceDatas.NO_PIECE) return false; //no alied piece.
        return getPiece(spaceId).color == pieceColor;
    }

    public boolean isEmptySpaceAt(int spaceId)
    {
        return getPiece(spaceId) == PieceDatas.NO_PIECE;
    }
}
