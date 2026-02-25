package ChessResources.ChessBoard;

import ChessLogic.Debug.DebugMode;
import ChessLogic.Debug.Debuggable;
import ChessResources.ChessHistoryTracker.BoardStateChanges.BoardStateChange;
import ChessResources.ChessListener.StateChangeListener;
import ChessResources.Pieces.PieceData;
import ChessResources.PreCalc;

import java.util.*;

import static ChessResources.Pieces.PieceData.getUniqueClone;

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

    protected long[] boardSquares = new long[PieceData.TOTAL_PIECES];

    public HashSet<Integer> currPieceLocationWhite = new HashSet<>();
    public HashSet<Integer> currPieceLocationBlack = new HashSet<>();

    protected ArrayList<StateChangeListener<BoardStateChange>> boardMoveListeners = new ArrayList<>();
    protected ArrayList<StateChangeListener<BoardStateChange>> stateChangeListeners = new ArrayList<>();
    protected boolean debugMode = false;
    //endregion

    public final StateChangeListener<BoardStateChange> BOARD_STATE_CHANGE_LOGGER =
            (BoardStateChange boardStateChange)->{

                int spaceId = boardStateChange.getSpaceId();
                int spaceIdArriveAt = boardStateChange.getSpaceIdArriveAt();

                short pieceData = boardStateChange.getPiece();
                short pieceDataAtArrival = this.getPiece(spaceIdArriveAt);

                if (ChessBoard.isValidSpaceId(boardStateChange.getSpaceIdArriveAt())){
                    if (!PieceData.isValidPieceId(pieceData)){
                        if (!PieceData.isValidPieceId(pieceDataAtArrival)) return;

                        if (PieceData.getColor(pieceDataAtArrival) == PieceData.WHITE)
                            currPieceLocationWhite.remove(spaceIdArriveAt);
                        else
                            currPieceLocationBlack.remove(spaceIdArriveAt);
                    }
                    else {
                        if (PieceData.getColor(pieceData) == PieceData.WHITE)
                            currPieceLocationWhite.add(spaceIdArriveAt);
                        else
                            currPieceLocationBlack.add(spaceIdArriveAt);
                    }
                }
                else{
                    if (!PieceData.isValidPieceId(pieceData)) return;
                    if (PieceData.getColor(pieceData) == PieceData.WHITE)
                        currPieceLocationWhite.remove(spaceId);
                    else
                        currPieceLocationBlack.remove(spaceId);
                }
            };

    //region CONSTRUCTOR
    public ChessBoard(){
        addStateChangeListener(BOARD_STATE_CHANGE_LOGGER);
    }
    
    public ChessBoard(String piecePlacement)
    {
        this();
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

        for (int i = 0; i < rows.length; ++i) {

            String row = rows[i];
            int col = 0;
            for (int j = 0, len = row.length(); j < len; j++) {
                char c = row.charAt(j);
                if (c >= '1' && c <= '8') { col += c - '0'; continue; }
                if (col > 7) throw new IllegalArgumentException("Column overflow");

                setPieceAt(i*ChessBoard.BOARD_SIZE + col, PreCalc.FEN_MAP[c]);

                col++;
            }
        }

    }

    protected void clearBoard()
    {
        Arrays.fill(boardSquares, 0L);
    }

    //endregion
    //region IMMEDIATE_SPACE_FUNCS

    public static int getEastSpaceId(int currSpaceId, int offset)
    { //user ensure good input
        return getDirSpaceId(currSpaceId, offset,EAST);
    }

    public static int getWestSpaceId(int currSpaceId, int offset)
    { //user ensure good input
        return getDirSpaceId(currSpaceId, offset,WEST);
    }

    public static int getDirSpaceId(int spaceId, int offset, short dir){
        return spaceId + directionOffsets[dir]*offset;
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

    public long getBitBoard(short pieceId){
        if (!PieceData.isValidPieceId(pieceId))
            throw new IllegalArgumentException("Invalid pieceId input at getBitBoard: " + pieceId);

        return boardSquares[PieceData.convertPieceIdToArrayIdx(pieceId)];
    }

    protected void setPieceAt(int spaceId, short piece)
    {//protected to avoid uses messing with listener. used during initilization
        StateChangeListener.notifyListeners(this.stateChangeListeners,
                new BoardStateChange(piece, ChessBoard.INVALID_SPACE_ID, spaceId));

        if (PieceData.isValidPieceId(piece) && isValidSpaceId(spaceId))
            boardSquares[PieceData.convertPieceIdToArrayIdx(piece)] |= (1L << spaceId);
        else if(!PieceData.isValidPieceId(piece)){
            for (int i = 0; i < boardSquares.length; ++i) {
                boardSquares[i] &= ~(1L << spaceId);
            }
        }
    }

    public void spawnPieceAt(int spaceId, short piece)
    {
        setPieceAt(spaceId, PieceData.INVALID_PIECES);
        setPieceAt(spaceId, piece);

        //spawn piece by moving it in from nowhere.
        StateChangeListener.notifyListeners(this.boardMoveListeners,
                new BoardStateChange(piece, ChessBoard.INVALID_SPACE_ID, spaceId));

    }

    protected void deSpawnPieceAt(int spaceId){
        short disappearedPiece = getPiece(spaceId);

        if (PieceData.isValidPieceId(disappearedPiece)) //ensure proper reconstruction later.
        {
            StateChangeListener.notifyListeners(this.boardMoveListeners,
                    new BoardStateChange(disappearedPiece, spaceId, ChessBoard.INVALID_SPACE_ID));
        }
        setPieceAt(spaceId, PieceData.INVALID_PIECES); //disappear piece.
    }

    protected short movePiecePrimitive(int spaceIdToMove, int spaceIdArriveAt)
    {
        short piece = getPiece(spaceIdToMove);

        setPieceAt(spaceIdToMove, PieceData.INVALID_PIECES);
        setPieceAt(spaceIdArriveAt, piece);
        return piece;
    }

    protected void movePiece(int spaceIdToMove, int spaceIdArriveAt)
    {
        //move piece and just overwrite piece in that location. Only movePieceCapture should be public.

        //follow the philosophy of all piece should land on empty space,
        //assume enemy piece disappear before allied piece lands. this function dont handle capture.
        //notifyMoveListener(new BoardStateChange(capturedPiece, spaceIdArriveAt, ChessBoard.INVALID_SPACE_ID));

        short piece = movePiecePrimitive(spaceIdToMove, spaceIdArriveAt);
        StateChangeListener.notifyListeners(this.boardMoveListeners,
                new BoardStateChange(piece, spaceIdToMove, spaceIdArriveAt));
    }

    public void movePieceCapture(int spaceIdToMove, int spaceIdArriveAt, int spaceIdCaptureAt)
    { //all input should be valid. //Order of operation: Enemy piece disappear, our piece land.
        deSpawnPieceAt(spaceIdCaptureAt);
        movePiece(spaceIdToMove, spaceIdArriveAt); //piece land.
    }

    public short getPiece(int spaceId)
    {
        if (isValidSpaceId(spaceId)){
            for (short i = 0; i < boardSquares.length; ++i){
                if ((boardSquares[i] & (1L << spaceId)) != 0)
                    return PieceData.convertArrayIdxToPieceId(i);
            }
            return PieceData.INVALID_PIECES;
        }
        else {
            DebugMode.debugPrint(this, "Invalid spaceId at getPiece");
            return PieceData.INVALID_PIECES;
        }
    }

    public boolean isPieceAt(int spaceId)
    {
        if (!isValidSpaceId(spaceId)) return false;
        return getPiece(spaceId) != PieceData.INVALID_PIECES;
    }

    public boolean isEnemyPieceAt(int spaceId, boolean pieceColor)
    {
        if (!isValidSpaceId(spaceId)) return false;
        else if (getPiece(spaceId) == PieceData.INVALID_PIECES) return false;//no enemy piece

        return PieceData.getColor(getPiece(spaceId)) != pieceColor;
    }


    public boolean isAlliedPieceAt(int spaceId, boolean pieceColor)
    {
        if (getPiece(spaceId) == PieceData.INVALID_PIECES) return false; //no alied piece.
        return PieceData.getColor(getPiece(spaceId)) == pieceColor;
    }

    public short getPieceIdAt(int spaceId)
    {
        if (getPiece(spaceId) == PieceData.INVALID_PIECES) return PieceData.INVALID_PIECES;
        else return getPiece(spaceId);
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
        return !PieceData.isValidPieceId(getPiece(spaceId));
    }

    @Override
    public ChessBoard clone(){
        ChessBoard clone = new ChessBoard();
        for (int i = 0; i < ChessBoard.BOARD_SIZE*ChessBoard.BOARD_SIZE; ++i){
            clone.setPieceAt(i, getPiece(i));
        }

        return clone;
    }

    //endregion

    //region LISTENERS
    //ways to addMoveListener to Board
    public void addMoveListener(StateChangeListener<BoardStateChange> moveChangeListener)
    {
        boardMoveListeners.add(moveChangeListener);
    }
    public void addStateChangeListener(StateChangeListener<BoardStateChange> stateChangeListener){
        stateChangeListeners.add(stateChangeListener);
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
                DebugMode.debugPrint(this, "Spawned in, piece is: " + PieceData.getName(boardStateChange.getPiece()) );
                setPieceAt(boardStateChange.getSpaceId(), boardStateChange.getPiece());
            } else if (boardStateChange.getSpaceId() == INVALID_SPACE_ID) {//piece is spawned, must be taken out of board.
                DebugMode.debugPrint(this, "Delete Piece");
                setPieceAt(boardStateChange.getSpaceIdArriveAt(), PieceData.INVALID_PIECES);
            } else {
                DebugMode.debugPrint(this, "Moving piece back");
                setPieceAt(boardStateChange.getSpaceIdArriveAt(), PieceData.INVALID_PIECES);
                setPieceAt(boardStateChange.getSpaceId(), boardStateChange.getPiece());
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
