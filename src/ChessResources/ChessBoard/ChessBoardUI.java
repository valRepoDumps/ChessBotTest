package ChessResources.ChessBoard;

import ChessResources.ChessHistoryTracker.BoardStateChanges.BoardStateChange;
import ChessResources.Pieces.PieceDatas;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.IntConsumer;

public class ChessBoardUI extends ChessBoard {
    //region DATA
    //region BOARD_DATAS
    protected static boolean[] boardSquaresColor = new boolean[BOARD_SIZE*BOARD_SIZE];
    protected boolean displayGraphic = true;
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
        super(piecePlacement);
        for (int row = 0; row < BOARD_SIZE; ++row)
        {
            for (int col = 0; col < BOARD_SIZE; ++col) {
                setUpSpaces(row, col);
            }
        }
        makeBoardGraphic(); //create graphic for board.
    }

    public void setOnSquareClicked(IntConsumer handler) {
        this.onSquareClicked = handler;
    }

    private void setUpSpaces(int row, int col) //setting up black and white spaces.
    {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) return;
        boardSquaresColor[row*BOARD_SIZE + col] = ((row + col) % 2 == 0) ?  WHITE : BLACK;
    }

    //region BOARD_GRAPHICS
    public JButton getGraphicAt(int spaceId)
    {
        if (isValidSpaceId(spaceId)) return boardGraphicSquareList[spaceId];
        else {
            System.out.println("Invalid spaceId at getGraphic");
            return null;
        }
    }
    public void setGraphicAt(int spaceId, JButton graphic)
    {
        assert (isValidSpaceId(spaceId));
        if (isValidSpaceId(spaceId))
        {
            boardGraphicSquareList[spaceId] = graphic;
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
                setGraphicAt(row*BOARD_SIZE + col, square);
            }
        }
    }

    public void updateBoardGraphic(int spaceId)
    {
        if (!displayGraphic) return;

        //this is private, so dont need much check.
        if (getPiece(spaceId) != PieceDatas.NO_PIECE) {
            getGraphicAt(spaceId).setIcon(getPiece(spaceId).graphic);
            getGraphicAt(spaceId).setBorder(null); //reset all borders.
        }
        else
        {
            resetSpace(getGraphicAt(spaceId));
        }
        //System.out.println(spaceId);
    }

    public void updateBoardGraphic()
    {
        if (!displayGraphic) return; //dont update if display graphic disabled.
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
        if (isValidSpaceId(spaceId)) {
            getGraphicAt(spaceId).setBorder(BorderFactory.createLineBorder(Color.RED, 5));
        }
    }

    public void unHighlightSpace(int spaceId)
    {
        boardGraphicSquareList[spaceId].setBorder(null);
    }
    //endregion

    //region TUNING_GRAPHIC
    public void enableGraphic(){
        this.displayGraphic = true;
    }
    public void disableGraphic(){
        this.displayGraphic = false;
    }

    //endregion

    @Override
    public void movePieceCapture(int spaceIdToMove, int spaceIdArriveAt, int spaceIdCaptureAt)
    { //all input should be valid.
        super.movePieceCapture(spaceIdToMove, spaceIdArriveAt, spaceIdCaptureAt);
        updateBoardGraphic();
    }

    @Override
    public void undoBoardState(ArrayList<BoardStateChange> boardStateChanges){
        super.undoBoardState(boardStateChanges);
        updateBoardGraphic();
    }
}
