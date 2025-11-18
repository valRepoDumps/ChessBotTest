package ChessResources;

import ChessLogic.ChessGame;
import ChessResources.Pieces.PieceDatas.JumpingPieceData;
import ChessResources.Pieces.PieceDatas.PieceData;
import ChessResources.Pieces.PieceDatas.PieceDatas;
import ChessResources.Pieces.PieceDatas.SlidingPieceData;

import java.util.Arrays;
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
                else if (piece instanceof JumpingPieceData)
                {
                    generateJumpingMoves(startSquare, (JumpingPieceData) piece);
                }
            }
        }
    }

    private void generateSlidingMoves(int startSquare, SlidingPieceData piece)
    {
        int maxRange = piece.getMaxRange(chessGame.chessBoard, startSquare);
        short[] possibleDirections = piece.getPossibleDirections(chessGame.chessBoard, startSquare);
        System.out.println(piece.pieceId + ": " + piece.name);
        for (short direction : possibleDirections)
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
                int numSouth = 7 - row;
                int numEast = 7 - col;

                int squareIdx = row*ChessBoard.BOARD_SIZE + col;

                numSquaresToEdge[squareIdx][ChessBoard.NORTH] = row;
                numSquaresToEdge[squareIdx][ChessBoard.SOUTH] = numSouth;
                numSquaresToEdge[squareIdx][ChessBoard.EAST] = numEast;
                numSquaresToEdge[squareIdx][ChessBoard.WEST] = col;
                numSquaresToEdge[squareIdx][ChessBoard.NORTH_WEST] = Math.min(row, col);
                numSquaresToEdge[squareIdx][ChessBoard.NORTH_EAST] = Math.min(row, numEast);
                numSquaresToEdge[squareIdx][ChessBoard.SOUTH_WEST] = Math.min(numSouth, col);
                numSquaresToEdge[squareIdx][ChessBoard.SOUTH_EAST] = Math.min(numSouth, numEast);

            }
        }
    }

    private void generateJumpingMoves(int startSquare, JumpingPieceData piece)
    {
        int[] moves = piece.getPossibleMoves(chessGame.chessBoard, startSquare);
        System.out.println(Arrays.toString(moves));
        for (int move : moves)
        {
            if (ChessBoard.isValidSpaceId(move) && !chessGame.chessBoard.isAlliedPieceAt(move, piece.color))
            {
                if (possibleMoves.containsKey(startSquare)) {
                    possibleMoves.get(startSquare).addMoves(move);
                }
                else {
                    possibleMoves.put(startSquare, new ChessSpaces(move));
                }
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
