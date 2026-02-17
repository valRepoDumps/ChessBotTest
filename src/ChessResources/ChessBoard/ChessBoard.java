package ChessResources.ChessBoard;

import ChessLogic.Debug.DebugMode;
import ChessLogic.Debug.Debuggable;
import ChessResources.ChessHistoryTracker.BoardStateChanges.BoardStateChange;
import ChessResources.ChessListener.StateChangeListener;
import ChessResources.Pieces.PieceData;
import ChessResources.Pieces.PieceDatas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static ChessResources.Pieces.PieceDatas.copyPiece;

public class ChessBoard implements Debuggable {
    //region PRE_CODE
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

    protected PieceData[] boardSquares = new PieceData[BOARD_SIZE*BOARD_SIZE];
    protected HashMap<PieceData, Integer> currPieceLocation = new HashMap<>();
    protected final ArrayList<StateChangeListener<BoardStateChange>> stateChangeListenerList = new ArrayList<>();

    protected boolean debugMode = false;
    //endregion

    //region CONSTRUCTOR
    public ChessBoard(){}
    
    public ChessBoard(String piecePlacement)
    {
        setUpPieces(piecePlacement);
    }
    //endregion

    //region SETTING_UP_BOARD
    public void setUpPieces(String piecesPlacement)
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
                    currCol += (c-'0');

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

    protected void clearBoard()
    {
        for (int spaceId = 0; spaceId < BOARD_SIZE*BOARD_SIZE; ++spaceId)
        {
            setPieceAt(spaceId, PieceDatas.NO_PIECE);
        }
    }

    //endregion

    //region GETTERS
    public PieceData[] getBoardSquares(){return Arrays.copyOf(boardSquares, boardSquares.length);}
    //endregion

    //region SETTERS
    //this method wrongs, should clone all PieceData within too.
//    public void setBoardSquares(PieceData[] boardSquares){
//        this.boardSquares = boardSquares.clone();
//    }
    //endregion

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

    //region GET_ROW_COL_FUNCS
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

    //region PIECE_FUNCS
    protected void setPieceAt(int spaceId, PieceData piece)
    {//protected to avoid uses messing with listener. used during initilization
        if (isValidSpaceId(spaceId)) boardSquares[spaceId] = piece;
    }

    public void spawnPieceAt(int spaceId, PieceData piece)
    {
        setPieceAt(spaceId, piece);

        //spawn piece by moving it in from nowhere.
        StateChangeListener.notifyListeners(this.stateChangeListenerList,
                new BoardStateChange(piece, ChessBoard.INVALID_SPACE_ID, spaceId));

    }

    public void deSpawnPieceAt(int spaceId){
        PieceData disappearedPiece = getPiece(spaceId);

        if (disappearedPiece != PieceDatas.NO_PIECE) //ensure proper reconstruction later.
        {
            StateChangeListener.notifyListeners(this.stateChangeListenerList,
                    new BoardStateChange(disappearedPiece, spaceId, ChessBoard.INVALID_SPACE_ID));
        }
        setPieceAt(spaceId, PieceDatas.NO_PIECE); //disappear piece.
    }

    protected PieceData movePiecePrimitive(int spaceIdToMove, int spaceIdArriveAt)
    {
        assert (isValidSpaceId(spaceIdToMove) && isValidSpaceId(spaceIdArriveAt));
        PieceData piece = getPiece(spaceIdToMove);
        assert(piece != PieceDatas.NO_PIECE);

        setPieceAt(spaceIdToMove, PieceDatas.NO_PIECE);
        setPieceAt(spaceIdArriveAt, piece);
        return piece;
    }

    protected void movePiece(int spaceIdToMove, int spaceIdArriveAt)
    {
        //move piece and just overwrite piece in that location. Only movePieceCapture should be public.

        //follow the philosophy of all piece should land on empty space,
        //assume enemy piece disappear before allied piece lands. this function dont handle capture.
        //notifyMoveListener(new BoardStateChange(capturedPiece, spaceIdArriveAt, ChessBoard.INVALID_SPACE_ID));

        PieceData piece = movePiecePrimitive(spaceIdToMove, spaceIdArriveAt);
        StateChangeListener.notifyListeners(this.stateChangeListenerList,
                new BoardStateChange(piece, spaceIdToMove, spaceIdArriveAt));
    }

    public void movePieceCapture(int spaceIdToMove, int spaceIdArriveAt, int spaceIdCaptureAt)
    { //all input should be valid. //Order of operation: Enemy piece disappear, our piece land.
        deSpawnPieceAt(spaceIdCaptureAt);
        movePiece(spaceIdToMove, spaceIdArriveAt); //piece land.
    }

    public PieceData getPiece(int spaceId)
    {
        if (isValidSpaceId(spaceId)) return boardSquares[spaceId];
        else {
            DebugMode.debugPrint(this, "Invalid spaceId at getPiece");
            return null;
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

        return getPiece(spaceId).getColor() != pieceColor;
    }

    public boolean isAlliedPieceAt(int spaceId, boolean pieceColor)
    {
        if (getPiece(spaceId) == PieceDatas.NO_PIECE) return false; //no alied piece.
        return getPiece(spaceId).getColor() == pieceColor;
    }

    public short getPieceIdAt(int spaceId)
    {
        if (getPiece(spaceId) == PieceDatas.NO_PIECE) return PieceData.INVALID_PIECES;
        else return getPiece(spaceId).getPieceId();
    }

    public int findPiece(int pieceId){
        for (int i = 0; i < BOARD_SIZE*BOARD_SIZE; ++i){
            if (boardSquares[i] != null && boardSquares[i].getPieceId() == pieceId) return i;
        }
        return INVALID_SPACE_ID;
    }
    //endregion

    //region MISC_FUNCS

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

    public boolean isEmptySpaceAt(int spaceId)
    {
        return getPiece(spaceId) == PieceDatas.NO_PIECE;
    }

    @Override
    public ChessBoard clone(){
        ChessBoard clone = new ChessBoard();
        for (int i = 0; i < ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE; ++i){
            clone.setPieceAt(i, PieceDatas.copyPiece(this.getPiece(i)));
        }

        return clone;
    }

    //endregion

    //region LISTENERS
    //ways to addMoveListener to Board
    public void addMoveListener(StateChangeListener<BoardStateChange> stateChangeListener)
    {
        stateChangeListenerList.add(stateChangeListener);
    }
    //endregion


    //region UNDO_MOVE
    public void undoBoardState(ArrayList<BoardStateChange> boardStateChanges)
    {
        if (boardStateChanges == null)
        {
            throw new NullPointerException("boardStateChange is null!");
        }

        for (int i = boardStateChanges.size()-1; i>=0; --i) {
            BoardStateChange boardStateChange = boardStateChanges.get(i);

           // BoardStateChange boardStateChange = boardStateChanges.get(i);

            assert (boardStateChange.getSpaceIdArriveAt() != INVALID_SPACE_ID ||
                    boardStateChange.getSpaceId() != INVALID_SPACE_ID);

            if (boardStateChange.getSpaceIdArriveAt() == INVALID_SPACE_ID) {//piece is taken out of board, thus must be spawned back in.
                DebugMode.debugPrint(this, "Spawned in, piece is: " + boardStateChange.getPiece().getName() );
                setPieceAt(boardStateChange.getSpaceId(), boardStateChange.getPiece());
            } else if (boardStateChange.getSpaceId() == INVALID_SPACE_ID) {//piece is spawned, must be taken out of board.
                DebugMode.debugPrint(this, "Delete Piece");
                setPieceAt(boardStateChange.getSpaceIdArriveAt(), PieceDatas.NO_PIECE);
            } else {
                DebugMode.debugPrint(this, "Moving piece back");
                setPieceAt(boardStateChange.getSpaceId(), boardStateChange.getPiece());
                setPieceAt(boardStateChange.getSpaceIdArriveAt(), PieceDatas.NO_PIECE);
            }
        }
    }

    public void advanceBoardState(ArrayList<BoardStateChange> boardStateChanges)
    {
        if (boardStateChanges == null)
        {
            throw new NullPointerException("boardStateChange is null!");
        }

        for (BoardStateChange boardStateChange : boardStateChanges) {

            // BoardStateChange boardStateChange = boardStateChanges.get(i);

            assert (boardStateChange.getSpaceIdArriveAt() != INVALID_SPACE_ID ||
                    boardStateChange.getSpaceId() != INVALID_SPACE_ID);

            if (boardStateChange.getSpaceIdArriveAt() == INVALID_SPACE_ID) {
                //Piece is capture, redo capture move.
                deSpawnPieceAt(boardStateChange.getSpaceId());
            } else if (boardStateChange.getSpaceId() == INVALID_SPACE_ID) {
                spawnPieceAt(boardStateChange.getSpaceIdArriveAt(), boardStateChange.getPiece());
                //piece is spawned
            } else {
                movePiece(boardStateChange.getSpaceId(), boardStateChange.getSpaceIdArriveAt()); //piece land.
            }
        }
    }
    //endregion

    //region DEBUGGING
    public void enableDebugMode(){
        debugMode = true;
    }
    public void disableDebugMode(){
        debugMode = false;
    }

    public boolean isDebuggable(){
        return debugMode;
    }
    //endregion
}
