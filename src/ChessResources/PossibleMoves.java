package ChessResources;

import ChessLogic.ChessGame;
import ChessResources.Pieces.PieceDatas.ConditionalSlidingPieceData;
import ChessResources.Pieces.PieceDatas.PieceData;
import ChessResources.Pieces.PieceDatas.PieceDatas;
import ChessResources.Pieces.PieceDatas.SlidingPieceData;

import java.util.HashMap;

public class PossibleMoves {
    public HashMap<Integer, ChessSpaces> possibleMoves = new HashMap<>();
    protected ChessGame chessGame;

    public int[][] numSquaresToEdge = new int[ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE][8];

    public PossibleMoves(ChessGame chessGame)
    {
        this.chessGame = chessGame;
        precomputeSquaresToEdgeData();
    }

    public void generateMoves()
    {
        for (int startSquare = 0; startSquare < ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE; ++startSquare)
        {
            PieceData piece = chessGame.chessBoard.boardSquares[startSquare];

            if (piece != PieceDatas.NO_PIECE && piece.color == chessGame.sideToMove)
            {
                if (piece instanceof SlidingPieceData)
                {
                    generateSlidingMoves(startSquare, (SlidingPieceData) piece);
                }
            }
        }
    }

    private void generateSlidingMoves(int startSquare, SlidingPieceData piece)
    {
        int maxRange = piece.maxRange;
        if (piece instanceof ConditionalSlidingPieceData)
        {
            if (((ConditionalSlidingPieceData)piece).useSecondMaxRange)
            {
                maxRange = ((ConditionalSlidingPieceData) piece).secondMaxRange;
            }
        }

        for (int direction : piece.possibleDirections)
        {
            for (int n = 0; n < maxRange && n < numSquaresToEdge[startSquare][direction]; ++n)
            {
                int targetSquare = startSquare + ChessBoard.directionOffsets[direction]*(n+1);
                if (targetSquare >= 64) break;
                PieceData pieceOnTarget = chessGame.chessBoard.boardSquares[targetSquare];

                if (pieceOnTarget != PieceDatas.NO_PIECE && pieceOnTarget.color == piece.color) break;
                //encounter piece of our color, dont move in this dir any more

                if (possibleMoves.containsKey(startSquare))
                {
                    possibleMoves.get(startSquare).addMoves(targetSquare);
                }
                else possibleMoves.put(startSquare, new ChessSpaces(targetSquare));

                if (pieceOnTarget != PieceDatas.NO_PIECE && pieceOnTarget.color != piece.color) break; //encounter opponents color, also break.
            }
        }
    }

    public void precomputeSquaresToEdgeData()
    {
        for (int row = 0; row< ChessBoard.BOARD_SIZE; ++row)
        {
            for (int col = 0; col < ChessBoard.BOARD_SIZE; ++col)
            {
                int numNorth = row;
                int numSouth = 7 - row;
                int numWest = col;
                int numEast = 7 - col;

                int squareIdx = row*ChessBoard.BOARD_SIZE + col;

                numSquaresToEdge[squareIdx][ChessBoard.NORTH] = numNorth;
                numSquaresToEdge[squareIdx][ChessBoard.SOUTH] = numSouth;
                numSquaresToEdge[squareIdx][ChessBoard.EAST] = numEast;
                numSquaresToEdge[squareIdx][ChessBoard.WEST] = numWest;
                numSquaresToEdge[squareIdx][ChessBoard.NORTH_WEST] = Math.min(numNorth,numWest);
                numSquaresToEdge[squareIdx][ChessBoard.NORTH_EAST] = Math.min(numNorth, numEast);
                numSquaresToEdge[squareIdx][ChessBoard.SOUTH_WEST] = Math.min(numSouth, numWest);
                numSquaresToEdge[squareIdx][ChessBoard.SOUTH_EAST] = Math.min(numSouth, numEast);

            }
        }
    }

    public void clearPossibleMoves()
    {
        possibleMoves = new HashMap<>();
    }

    public void highlightPossibleMoves(int spaceId, ChessBoard chessBoard)
    {
        if (possibleMoves.containsKey(spaceId)) {
            for (int space : possibleMoves.get(spaceId).chessMoves)
            {
                chessBoard.highlightSpace(space);
            }
        }
    }

    public void unHighlightPossibleMoves(int spaceId, ChessBoard chessBoard)
    {
        if (possibleMoves.containsKey(spaceId)) {
            for (int space : possibleMoves.get(spaceId).chessMoves)
            {
                chessBoard.unHighlightSpace(space);
            }
        }
    }

}
