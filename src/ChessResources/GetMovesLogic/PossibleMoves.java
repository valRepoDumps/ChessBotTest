package ChessResources.GetMovesLogic;

import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.ChessBoard.ChessBoardUI;
import ChessResources.Pieces.IrregularPieceData;
import ChessResources.Pieces.PieceData;
import ChessResources.Pieces.PieceDatas;
import ChessResources.Pieces.SlidingPieceData;

import java.util.HashMap;

public class PossibleMoves {
    //region PRE_CONSTRUCTOR
    //region DATAS
    public HashMap<Integer, ChessSpaces> possibleMoves = new HashMap<>();
    protected MinimalChessGame<?> chessGame;
    protected boolean color;
    public int[][] numSquaresToEdge = new int[ChessBoard.BOARD_SIZE* ChessBoard.BOARD_SIZE][8];
    //endregion

    //region setting
    public boolean strictMovesChecker = false;//ensure all moves that threaten king unusable

    //endregion
    //endregion


    public PossibleMoves(MinimalChessGame<?> chessGame, boolean color)
    {
        this.chessGame = chessGame;
        this.color = color;
        precomputeSquaresToEdgeData();
    }

    //region MOVE_GEN
    public void generateMoves()
    {
        for (int startSquare = 0; startSquare < ChessBoardUI.BOARD_SIZE* ChessBoardUI.BOARD_SIZE; ++startSquare)
        {
            PieceData piece = chessGame.chessBoard.getPiece(startSquare);

            if (piece != PieceDatas.NO_PIECE && piece.getColor() == color)
            {
                if (piece instanceof SlidingPieceData)
                {
                    generateSlidingMoves(startSquare, (SlidingPieceData) piece);
                }
                else if (piece instanceof IrregularPieceData)
                {
                    generateJumpingMoves(startSquare, (IrregularPieceData) piece);
                }

            }
        }
    }

    private void generateSlidingMoves(int startSquare, SlidingPieceData piece)
    {
        int maxRange = piece.getMaxRange(chessGame, startSquare);
        short[] possibleDirections = piece.getPossibleDirections(chessGame, startSquare);

        for (short direction : possibleDirections)
        {
            for (int n = 0; n < maxRange && n < numSquaresToEdge[startSquare][direction]; ++n)
            {
                int targetSquare = startSquare + ChessBoardUI.directionOffsets[direction]*(n+1);

                if (targetSquare >= 64) break;

                if (chessGame.isAlliedPieceAt(targetSquare, color)) break;
                //encounter piece of our color, dont move in this dir any more

                addMoveToPossibleMoves(startSquare, targetSquare, piece);

                if (chessGame.isEnemyPieceAt(targetSquare, color)) break; //encounter opponents color, also break.
            }
        }
    }

    public void precomputeSquaresToEdgeData()
    {
        for (int row = 0; row< ChessBoardUI.BOARD_SIZE; ++row)
        {
            for (int col = 0; col < ChessBoardUI.BOARD_SIZE; ++col)
            {
                int numSouth = 7 - row;
                int numEast = 7 - col;

                int squareIdx = row* ChessBoardUI.BOARD_SIZE + col;

                numSquaresToEdge[squareIdx][ChessBoardUI.NORTH] = row;
                numSquaresToEdge[squareIdx][ChessBoardUI.SOUTH] = numSouth;
                numSquaresToEdge[squareIdx][ChessBoardUI.EAST] = numEast;
                numSquaresToEdge[squareIdx][ChessBoardUI.WEST] = col;
                numSquaresToEdge[squareIdx][ChessBoardUI.NORTH_WEST] = Math.min(row, col);
                numSquaresToEdge[squareIdx][ChessBoardUI.NORTH_EAST] = Math.min(row, numEast);
                numSquaresToEdge[squareIdx][ChessBoardUI.SOUTH_WEST] = Math.min(numSouth, col);
                numSquaresToEdge[squareIdx][ChessBoardUI.SOUTH_EAST] = Math.min(numSouth, numEast);

            }
        }
    }

    private void generateJumpingMoves(int startSquare, IrregularPieceData piece)
    {
        int[] moves = piece.getPossibleMoves(chessGame, startSquare);
        for (int move : moves)
        {
            if (ChessBoard.isValidSpaceId(move) && !chessGame.isAlliedPieceAt(move, piece.getColor()))
            {
                addMoveToPossibleMoves(startSquare, move, piece);
            }
        }
    }

    private void addMoveToPossibleMoves(int startSquare, int move, PieceData piece){
        if (strictMovesChecker && chessGame.selfMoveWontThreatenSelfKing(startSquare, move, piece.getColor())) return;

        if (possibleMoves.containsKey(startSquare)) {
            possibleMoves.get(startSquare).addMoves(move);
        } else {
            possibleMoves.put(startSquare, new ChessSpaces(move));
        }
    }
    //endregion
    //region HELPERS
    public void clearPossibleMoves()
    {
        possibleMoves = new HashMap<>();
    }

    public void highlightPossibleMoves(int spaceId, ChessBoardUI chessBoardUI)
    {
        if (possibleMoves.containsKey(spaceId)) {
            for (int space : possibleMoves.get(spaceId).chessMoves)
            {
                chessBoardUI.highlightSpace(space);
            }
        }
    }

    public void unHighlightPossibleMoves(int spaceId, ChessBoardUI chessBoardUI)
    {
        if (possibleMoves.containsKey(spaceId)) {
            for (int space : possibleMoves.get(spaceId).chessMoves)
            {
                chessBoardUI.unHighlightSpace(space);
            }
        }
    }

    public boolean isEmpty(){
        return possibleMoves.isEmpty();
    }
    public void enableStrictMovesChecker(){strictMovesChecker = true;}

    public  void disableStrictMovesChecker(){strictMovesChecker = false;}

    @SuppressWarnings("unused")
    public HashMap<Integer, ChessSpaces> getMoves(){return possibleMoves;}
    //endregion
}
