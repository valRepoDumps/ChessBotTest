package ChessResources;

import ChessResources.Pieces.PieceDatas.PieceDatas;
import ChessResources.Pieces.PieceDatas.PieceData;

import javax.swing.*;
import java.awt.*;
import java.util.function.IntConsumer;

import static ChessResources.Pieces.PieceDatas.PieceData.copyPiece;

public class ChessBoardUI extends ChessBoard {
    //region DATA
    //region BOARD_DATAS
    protected static boolean[] boardSquaresColor = new boolean[BOARD_SIZE*BOARD_SIZE];
    //endregion
    //region COLOR
    public static final Color BLACK_COLOR = Color.DARK_GRAY;
    public static final Color WHITE_COLOR = Color.WHITE;
    //endregion
    //region GRAPHIC
    public final JPanel boardGraphic = new JPanel();
    public JButton[] boardGraphicSquareList = new JButton[BOARD_SIZE*BOARD_SIZE];
    //endregion
    private IntConsumer onSquareClicked = null;
    //endregion

    public ChessBoardUI(String piecePlacement)
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
                        case 'k': setPieceAt(i*BOARD_SIZE+currCol, copyPiece(PieceDatas.BKING_DATA)); break;
                        case 'q': setPieceAt(i*BOARD_SIZE+currCol, copyPiece(PieceDatas.BQUEEN_DATA)); break;
                        case 'b': setPieceAt(i*BOARD_SIZE+currCol, copyPiece(PieceDatas.BBISHOP_DATA)); break;
                        case 'n': setPieceAt(i*BOARD_SIZE+currCol, copyPiece(PieceDatas.BKNIGHT_DATA)); break;
                        case 'r': setPieceAt(i*BOARD_SIZE+currCol, copyPiece(PieceDatas.BROOK_DATA)); break;
                        case 'p': setPieceAt(i*BOARD_SIZE+currCol, copyPiece(PieceDatas.BPAWN_DATA)); break;

                        case 'K': setPieceAt(i*BOARD_SIZE+currCol, copyPiece(PieceDatas.WKING_DATA)); break;
                        case 'Q': setPieceAt(i*BOARD_SIZE+currCol, copyPiece(PieceDatas.WQUEEN_DATA)); break;
                        case 'B': setPieceAt(i*BOARD_SIZE+currCol, copyPiece(PieceDatas.WBISHOP_DATA)); break;
                        case 'N': setPieceAt(i*BOARD_SIZE+currCol, copyPiece(PieceDatas.WKNIGHT_DATA)); break;
                        case 'R': setPieceAt(i*BOARD_SIZE+currCol, copyPiece(PieceDatas.WROOK_DATA)); break;
                        case 'P': setPieceAt(i*BOARD_SIZE+currCol, copyPiece(PieceDatas.WPAWN_DATA)); break;

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
        for (int spaceId = 0; spaceId < BOARD_SIZE*BOARD_SIZE; ++spaceId)
        {
            setPieceAt(spaceId, PieceDatas.NO_PIECE);
        }
    }

    public void setPieceAt(char colId, char rowId, PieceData piece) {
        int spaceId = convertSquareNotationToSpaceId(colId, rowId);
        setPieceAt(spaceId, piece);
    }

    //endregion

    //region HELPER_FUNCS

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

    //region IMMEDIATE_SPACE_FUNCS
    public static boolean isImmediateNorth(int currSpaceId, int targetSpaceId)
    {
        return getRow(targetSpaceId) == getRow(currSpaceId) - 1;
    }
    public static int getNorthSpaceId(int currSpaceId, int offset)
    { //user ensure valid input. should use isImmediateNorth check first.
        return currSpaceId + offset*directionOffsets[NORTH];
    }

    public static boolean isImmediateSouth(int currSpaceId, int targetSpaceId)
    {
        return getRow(targetSpaceId) == getRow(currSpaceId) + 1;
    }
    public static int getSouthSpaceId(int currSpaceId, int offset)
    { //user ensure valid input.
        return currSpaceId + offset*directionOffsets[SOUTH];
    }

    public static boolean isImmediateEast(int currSpaceId, int targetSpaceId)
    {
        return getCol(targetSpaceId) == getCol(currSpaceId) + 1;
    }
    public static int getEastSpaceId(int currSpaceId, int offset)
    { //user ensure good input
        return currSpaceId + offset*directionOffsets[EAST];
    }

    public static boolean isImmediateWest(int currSpaceId, int targetSpaceId)
    {
        return getCol(targetSpaceId) == getCol(currSpaceId) - 1;
    }
    public static int getWestSpaceId(int currSpaceId, int offset)
    { //user ensure good input
        return currSpaceId + offset*directionOffsets[WEST];
    }

    public static int getNorthEastSpaceId(int currSpaceId, int offset)
    { //user ensure good input
        return currSpaceId + directionOffsets[NORTH_EAST]*offset;
    }
    public static int getNorthWestSpaceId(int currSpaceId, int offset)
    { //user ensure good input
        return currSpaceId + directionOffsets[NORTH_WEST]*offset;
    }
    public static int getSouthEastSpaceId(int currSpaceId, int offset)
    { //user ensure good input
        return currSpaceId + directionOffsets[SOUTH_EAST]*offset;
    }
    public static int getSouthWestSpaceId(int currSpaceId, int offset)
    { //user ensure good input
        return currSpaceId + directionOffsets[SOUTH_WEST]*offset;
    }
    //endregion
    public static int getCol(int spaceId)
    {
        assert spaceId >= 0 && spaceId < BOARD_SIZE*BOARD_SIZE;
        return spaceId % BOARD_SIZE;
    }

    public static int getRow(int spaceId)
    {
        assert spaceId >= 0 && spaceId < BOARD_SIZE*BOARD_SIZE;
        return spaceId / BOARD_SIZE;
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
                if (getPiece(row*BOARD_SIZE + col) != PieceDatas.NO_PIECE) {
                    square.setIcon(getPiece(row*BOARD_SIZE + col).graphic);
                }
                square.setBackground(boardSquaresColor[row*BOARD_SIZE + col] == BLACK ? BLACK_COLOR : WHITE_COLOR);

                boardGraphic.add(square);
                boardGraphicSquareList[row*BOARD_SIZE + col] = square;
            }
        }
    }

    private void updateBoardGraphic(int spaceId)
    {
        //this is private, so dont need much check.
        if (getPiece(spaceId) != PieceDatas.NO_PIECE) {
            boardGraphicSquareList[spaceId].setIcon(getPiece(spaceId).graphic);
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

    public void movePieceCapture(int spaceIdToMove, int spaceIdArriveAt, int spaceIdCaptureAt)
    { //all input should be valid.
        movePiece(spaceIdToMove, spaceIdArriveAt);

        if (spaceIdArriveAt != spaceIdCaptureAt) setPieceAt(spaceIdCaptureAt, PieceDatas.NO_PIECE);
        updateBoardGraphic();
    }

    public short getPieceIdAt(int spaceId)
    {
        if (getPiece(spaceId) == PieceDatas.NO_PIECE) return PieceData.INVALID_PIECES;
        else return getPiece(spaceId).pieceId;
    }
    //endregion
}
