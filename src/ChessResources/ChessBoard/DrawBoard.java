package ChessResources.ChessBoard;

import ChessLogic.Debug.DebugMode;
import ChessResources.ChessHistoryTracker.BoardStateChanges.BoardStateChange;
import ChessResources.ChessListener.StateChangeListener;
import ChessResources.Pieces.PieceData;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.IntConsumer;

import static ChessResources.ChessBoard.ChessBoard.BOARD_SIZE;

public class DrawBoard {
    //region DATA
    //region BOARD_DATAS
    protected static boolean[] boardSquaresColor = new boolean[BOARD_SIZE * BOARD_SIZE];
    protected boolean displayGraphic = true;
    //endregion
    //region COLOR
    public static final Color BLACK_COLOR = Color.DARK_GRAY;
    public static final Color WHITE_COLOR = Color.WHITE;

    public static final int SQUARE_PIXEL_SIZE = 100;
    //endregion
    //region GRAPHIC
    public JPanel boardGraphic = new JPanel();
    public JButton[] boardGraphicSquareList = new JButton[BOARD_SIZE * BOARD_SIZE];
    ChessBoard board;
    //endregion
    private IntConsumer onSquareClicked = null;
    //endregion

    public final StateChangeListener<BoardStateChange> GRAPHIC_UPDATE_LOGGER =
            (BoardStateChange boardStateChange) -> {
                updateBoardGraphic();
            };

    public DrawBoard(ChessBoard board) {
        this.board = board;

        // initialize square colors
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                setUpSpaces(row, col);
            }
        }

        makeBoardGraphic();

        // listen for board changes
        board.addStateChangeListener(GRAPHIC_UPDATE_LOGGER);
    }


    @SuppressWarnings("unused")

    public void setOnSquareClicked(IntConsumer handler) {
        this.onSquareClicked = handler;
    }

    private void setUpSpaces(int row, int col) //setting up black and white spaces.
    {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) return;
        boardSquaresColor[row * BOARD_SIZE + col] = ((row + col) % 2 == 0) ? PieceData.WHITE : PieceData.BLACK;
    }

    //region BOARD_GRAPHICS
    public JButton getGraphicAt(int spaceId) {
        if (ChessBoard.isValidSpaceId(spaceId)) return boardGraphicSquareList[spaceId];
        return null;
    }

    public void setGraphicAt(int spaceId, JButton graphic) {
        assert (ChessBoard.isValidSpaceId(spaceId));
        if (ChessBoard.isValidSpaceId(spaceId)) {
            boardGraphicSquareList[spaceId] = graphic;
        }
    }

    private void makeBoardGraphic() {
        boardGraphic.setLayout(new GridLayout(BOARD_SIZE, BOARD_SIZE));

        for (int row = 0; row < BOARD_SIZE; ++row) {
            for (int col = 0; col < BOARD_SIZE; ++col) {
                JButton square = new JButton();

                final int r = row, c = col; //allow to be added to lambda in action event.
                square.addActionListener(_ -> onSquareClicked.accept(r * BOARD_SIZE + c));

                square.setPreferredSize(new Dimension(SQUARE_PIXEL_SIZE, SQUARE_PIXEL_SIZE));
                if (PieceData.isValidPieceId(board.getPiece(row * BOARD_SIZE + col))) {
                    square.setIcon(PieceData.getGraphic(board.getPiece(row * BOARD_SIZE + col)));
                }
                square.setBackground(boardSquaresColor[row * BOARD_SIZE + col] == PieceData.BLACK ? BLACK_COLOR : WHITE_COLOR);

                boardGraphic.add(square);
                setGraphicAt(row * BOARD_SIZE + col, square);
            }
        }
    }

    public void updateBoardGraphic(int spaceId) {
        if (!displayGraphic) return;
        if (!ChessBoard.isValidSpaceId(spaceId)) return;
        //this is private, so dont need much check.
        if (PieceData.isValidPieceId(board.getPiece(spaceId))) {
            getGraphicAt(spaceId).setIcon(PieceData.getGraphic(board.getPiece(spaceId)));
            getGraphicAt(spaceId).setBorder(null); //reset all borders.
        } else {
            resetSpace(getGraphicAt(spaceId));
        }
        //System.out.println(spaceId);
    }

    public void updateBoardGraphic() {
        if (!displayGraphic) return; //dont update if display graphic disabled.
        for (int spaceId = 0; spaceId < BOARD_SIZE * BOARD_SIZE; ++spaceId) {
            updateBoardGraphic(spaceId);
        }
    }

    private void resetSpace(JButton space) {
        space.setIcon(null);
        space.setBorder(null);
    }

    public void highlightSpace(int spaceId) {
        if (ChessBoard.isValidSpaceId(spaceId)) {
            getGraphicAt(spaceId).setBorder(BorderFactory.createLineBorder(Color.RED, 5));
        }
    }

    public void highlightSpace(long bitBoard) {
        for (long i = bitBoard; i != 0; i &= (i - 1)) {
            highlightSpace(Long.numberOfTrailingZeros(i));
        }
    }

    public void unHighlightSpace(int spaceId) {
        boardGraphicSquareList[spaceId].setBorder(null);
    }

    public void unHighlightSpace(long bitBoard) {
        for (long i = bitBoard; i != 0; i &= (i - 1)) {
            unHighlightSpace(Long.numberOfTrailingZeros(i));
        }
    }
    //endregion

    public void setBoard(ChessBoard board){
        this.board = board;
    }
    //region TUNING_GRAPHIC
    public void enableGraphic() {
        this.displayGraphic = true;
    }

    public void disableGraphic() {
        this.displayGraphic = false;
    }
    //endregion

}
