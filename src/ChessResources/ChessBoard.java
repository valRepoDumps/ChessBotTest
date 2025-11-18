package ChessResources;

import ChessResources.Pieces.PieceDatas.PieceDatas;
import ChessResources.Pieces.PieceDatas.PieceData;
import ChessResources.Pieces.PieceDatas.SlidingPieceData;

import javax.swing.*;
import java.awt.*;
import java.util.function.IntConsumer;

public class ChessBoard {
    //region DATA
    //region BOARD_SIZE
    public static final int BOARD_SIZE = 8;
    public  static final int SQUARE_PIXEL_SIZE = 100;
    //endregion

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

    //region BOARD_DATAS
    public PieceData[] boardSquares = new PieceData[BOARD_SIZE*BOARD_SIZE];
    protected static boolean[] boardSquaresColor = new boolean[BOARD_SIZE*BOARD_SIZE];
    //endregion

    //region COLOR
    public static final boolean BLACK = false;
    public static final boolean WHITE = true;

    public static final Color BLACK_COLOR = Color.DARK_GRAY;
    public static final Color WHITE_COLOR = Color.WHITE;
    //endregion

    //region GRAPHIC
    public final JPanel boardGraphic = new JPanel();
    public JButton[] boardGraphicSquareList = new JButton[BOARD_SIZE*BOARD_SIZE];
    //endregion

    private IntConsumer onSquareClicked = null;

    //endregion

    public ChessBoard(String piecePlacement)
    {
        for (int row = 0; row < BOARD_SIZE; ++row)
        {
            for (int col = 0; col < BOARD_SIZE; ++col) {
                setUpSpaces(row, col);
            }
        }
        setUpPieces(piecePlacement);
        makeBoardGraphic(); //create graphic for board.
    }

    public void setOnSquareClicked(IntConsumer handler) {
        this.onSquareClicked = handler;
    }

    //region SETTING_UP_BOARD
    private void setUpPieces(String piecesPlacement)
    {
        clearBoard();

        String[] rows = piecesPlacement.split("/");
        if (rows.length > 8)
            throw new IllegalArgumentException("Invalid FEN: Must include 8 ranks.");

        for (int i = 0; i < rows.length; ++i)
        {
            String row = rows[i];
            int currCol = 0;
            for (char c : row.toCharArray())
            {
                if (Character.isDigit(c))
                {
                    currCol += (int)(c-'0');

                }
                else
                {
                    if (currCol < 0 || currCol > 7)
                        throw new IllegalArgumentException("Column Index Out of Range (" + currCol + ").");

                    switch(c)
                    {
                        case 'k': boardSquares[i*BOARD_SIZE+currCol] = copyPiece(PieceDatas.BKING_DATA); break;
                        case 'q': boardSquares[i*BOARD_SIZE+currCol] = copyPiece(PieceDatas.BQUEEN_DATA); break;
                        case 'b': boardSquares[i*BOARD_SIZE+currCol] = copyPiece(PieceDatas.BBISHOP_DATA); break;
                        case 'n': boardSquares[i*BOARD_SIZE+currCol] = copyPiece(PieceDatas.BKNIGHT_DATA); break;
                        case 'r': boardSquares[i*BOARD_SIZE+currCol] = copyPiece(PieceDatas.BROOK_DATA); break;
                        case 'p': boardSquares[i*BOARD_SIZE+currCol] = copyPiece(PieceDatas.BPAWN_DATA); break;

                        case 'K': boardSquares[i*BOARD_SIZE+currCol] = copyPiece(PieceDatas.WKING_DATA); break;
                        case 'Q': boardSquares[i*BOARD_SIZE+currCol] = copyPiece(PieceDatas.WQUEEN_DATA); break;
                        case 'B': boardSquares[i*BOARD_SIZE+currCol] = copyPiece(PieceDatas.WBISHOP_DATA); break;
                        case 'N': boardSquares[i*BOARD_SIZE+currCol] = copyPiece(PieceDatas.WKNIGHT_DATA); break;
                        case 'R': boardSquares[i*BOARD_SIZE+currCol] = copyPiece(PieceDatas.WROOK_DATA); break;
                        case 'P': boardSquares[i*BOARD_SIZE+currCol] = copyPiece(PieceDatas.WPAWN_DATA); break;

                        default: throw new IllegalArgumentException("Incorrect FEN format!");
                    }
                    ++currCol;
                }
            }
        }
    }

    private void setUpSpaces(int row, int col)
    {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) return;
        boardSquaresColor[row*BOARD_SIZE + col] = ((row + col) % 2 == 0) ?  WHITE : BLACK;
    }

    private void clearBoard()
    {
        for (int row = 0; row < BOARD_SIZE; ++row)
        {
            for (int col = 0; col < BOARD_SIZE; ++col)
            {
                boardSquares[row*BOARD_SIZE + col] = null;
            }
        }
    }

    public void setPieceAt(int spaceId, PieceData piece)
    {
        if (isValidSpaceId(spaceId)) boardSquares[spaceId] = piece;
    }

    public void setPieceAt(char colId, char rowId, PieceData piece)
    {
        int spaceId = convertSquareNotationToSpaceId(colId, rowId);

        setPieceAt(spaceId, piece);
    }

    //endregion

    //region HELPER_FUNCS
    public static boolean isValidSpaceId(int spaceId)
    {
        return spaceId >= 0 && spaceId < BOARD_SIZE*BOARD_SIZE;
    }

    public static int convertSquareNotationToSpaceId(char colId, char rowId)
    {
        if (colId < 'a' || colId > 'h' || rowId < '1' || rowId > '8')
            throw new IllegalArgumentException("Invalid files and ranks input ("
                    + colId +", " + rowId+").");

        return (colId - 'a')* BOARD_SIZE + 8 - ('8'-rowId);
    }

    private static boolean isValidBoardIdx(int row, int col, Exception e) throws Exception
    {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE)
        {
            if (e != null)
            {
                throw e;
            }
            return false;
        }
        return true;
    }

    public static PieceData copyPiece(PieceData pieceData)
    {
        if (pieceData instanceof SlidingPieceData)
        {
            return new SlidingPieceData((SlidingPieceData) pieceData);
        }
        else
        {
            return new PieceData(pieceData);
        }
    }
    //endregion

    //region BOARD_GRAPHICS
    public JButton getGraphic(int spaceId)
    {
        if (isValidSpaceId(spaceId)) return boardGraphicSquareList[spaceId];
        else {
            System.out.println("Invalid spaceId at getGraphic");
            return null;
        }
    }

    private void makeBoardGraphic()
    {
        boardGraphic.setLayout(new GridLayout(BOARD_SIZE, BOARD_SIZE));

        for (int row = 0; row < BOARD_SIZE; ++row)
        {
            for (int col = 0; col < BOARD_SIZE; ++col) {
                JButton square = new JButton();

                final int r = row, c = col; //allow to be added to lambda in action event.
                square.addActionListener(e->onSquareClicked.accept(r*BOARD_SIZE+c));

                square.setPreferredSize(new Dimension(SQUARE_PIXEL_SIZE,SQUARE_PIXEL_SIZE));
                if (boardSquares[row*BOARD_SIZE + col] != null)  square.setIcon(boardSquares[row*BOARD_SIZE + col].graphic);
                square.setBackground(boardSquaresColor[row*BOARD_SIZE + col] == BLACK ? BLACK_COLOR : WHITE_COLOR);

                boardGraphic.add(square);
                boardGraphicSquareList[row*BOARD_SIZE + col] = square;
            }
        }
    }

    private void updateBoardGraphic(int spaceId)
    {
        //this is private, so dont need much check.
        if (boardSquares[spaceId] != PieceDatas.NO_PIECE) {
            boardGraphicSquareList[spaceId].setIcon(boardSquares[spaceId].graphic);
            boardGraphicSquareList[spaceId].setBorder(null); //reset all borders.
        }
        else
        {
            resetSpace(boardGraphicSquareList[spaceId]);
        }
        //System.out.println(spaceId);
    }

    private void updateBoardGraphic()
    {
        for (int spaceId = 0; spaceId < BOARD_SIZE*BOARD_SIZE; ++spaceId)
        {
            updateBoardGraphic(spaceId);
        }
    }

    private void resetSpace(JButton space)
    {
        space.setIcon(null);
        space.setBorder(null);
    }

    public void highlightSpace(int spaceId)
    {
        if (isValidSpaceId(spaceId))
            boardGraphicSquareList[spaceId].setBorder(BorderFactory.createLineBorder(Color.RED, 5));
    }

    public void unHighlightSpace(int spaceId)
    {
        boardGraphicSquareList[spaceId].setBorder(null);
    }
    //endregion

    //region PIECE_FUNCS
    public void movePiece(int spaceIdToMove, int spaceIdArriveAt)
    {
        if (isValidSpaceId(spaceIdToMove) && isValidSpaceId(spaceIdArriveAt))
        {
            PieceData piece = boardSquares[spaceIdToMove];
            boardSquares[spaceIdToMove] = PieceDatas.NO_PIECE;
            boardSquares[spaceIdArriveAt] = piece;
        }
        else {
            System.out.println("Invalid spaceId at movePiece");
        }

        updateBoardGraphic();
    }

    public PieceData getPiece(int spaceId)
    {
        if (isValidSpaceId(spaceId)) return boardSquares[spaceId];
        else {
            System.out.println("Invalid spaceId at getPiece");
            return null;
        }
    }

    public boolean isEnemyPieceAt(int spaceId, boolean pieceColor)
    {
        if (!isValidSpaceId(spaceId)) return false;
        if (boardSquares[spaceId] == PieceDatas.NO_PIECE) return false;//no enemy piece

        return boardSquares[spaceId].color != pieceColor;
    }
    //endregion
}
