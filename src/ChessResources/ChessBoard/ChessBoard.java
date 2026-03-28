package ChessResources.ChessBoard;

import ChessLogic.Configurations.PropertiesStats;
import ChessLogic.Debug.DebugMode;
import ChessLogic.Debug.Debuggable;
import ChessResources.BitMasks;
import ChessResources.ChessHistoryTracker.BoardStateChanges.BoardStateChange;
import ChessResources.ChessListener.StateChangeListener;
import ChessResources.Pieces.PieceData;
import ChessResources.PreCalc;

import java.util.*;

public class ChessBoard implements Debuggable {
    //region PRE_CODE
    //region BOARD_SIZE
    public static final int BOARD_SIZE = 8;
    public static final int TOTAL_SPACES = BOARD_SIZE*BOARD_SIZE;
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

    public static final int[] KNIGHT_OFFSETS = {2*directionOffsets[NORTH] + directionOffsets[EAST],
            2*directionOffsets[NORTH] + directionOffsets[WEST],
            2*directionOffsets[SOUTH] + directionOffsets[EAST], 2*directionOffsets[SOUTH] + directionOffsets[WEST],
            2*directionOffsets[EAST] + directionOffsets[NORTH], 2*directionOffsets[EAST] + directionOffsets[SOUTH],
            2*directionOffsets[WEST] + directionOffsets[NORTH], 2*directionOffsets[WEST] + directionOffsets[SOUTH]};
    public static final short K_NE = 0;
    public static final short K_NW = 1;
    public static final short K_SE = 2;
    public static final short K_SW = 3;
    public static final short K_EN = 4;
    public static final short K_ES = 5;
    public static final short K_WN = 6;
    public static final short K_WS = 7;

    //endregion

    public static final boolean BLACK = PieceData.BLACK;
    public static final boolean WHITE = PieceData.WHITE;

    protected long[] boardSquares = new long[PieceData.TOTAL_PIECES];

//    public ArrayList<Integer> currPieceLocationWhite = new ArrayList<>();
//    public ArrayList<Integer> currPieceLocationBlack = new ArrayList<>();
    public short[] currPieceAtLocation = new short[ChessBoard.TOTAL_SPACES];

    protected ArrayList<StateChangeListener<BoardStateChange>> boardMoveListeners = new ArrayList<>();
    protected ArrayList<StateChangeListener<BoardStateChange>> stateChangeListeners = new ArrayList<>();
    protected boolean debugMode = false;

    public long ANTI_TO_MOVE_PIECES;
    public long ANTI_NOT_TO_MOVE_PIECES;
    public long NOT_TO_MOVE_PIECES;
    public long TO_MOVE_PIECES;
    public long NOT_TO_MOVE_PIECES_AND_ENPASSANT;
    public long TO_MOVE_PIECES_AND_ENPASSANT;
    public long EMPTY;
    public long OCCUPIED;
    public long WHITE_PIECES;
    public long BLACK_PIECES;
    public long ENPASSANT;
    public boolean CAN_CASTLE_KING;
    public boolean CAN_CASTLE_QUEEN;
    public long OCCUPIED_NO_KING;

    public short[] TO_MOVE_THREATS_PIECE_ID;
    //endregion

    public final StateChangeListener<BoardStateChange> BOARD_STATE_CHANGE_LOGGER =
            (BoardStateChange boardStateChange)->{

                int spaceId = boardStateChange.getSpaceId();
                int spaceIdArriveAt = boardStateChange.getSpaceIdArriveAt();

                short pieceData = boardStateChange.getPiece();
                short pieceDataAtArrival = boardStateChange.getPieceCaptured();

                if (isValidSpaceId(spaceId)) currPieceAtLocation[spaceId] = PieceData.INVALID_PIECES;
                if (isValidSpaceId(spaceIdArriveAt)) currPieceAtLocation[spaceIdArriveAt] = pieceData;
                // Only update arrays/sets when the target index is valid
                    // arriveAt invalid means a piece was removed from 'spaceId' (source)
                if (!ChessBoard.isValidSpaceId(spaceIdArriveAt) &&
                        ChessBoard.isValidSpaceId(spaceId) &&
                        PieceData.isValidPieceId(pieceData)){
                    currPieceAtLocation[spaceId] = PieceData.INVALID_PIECES;
                }
            };

    //region CONSTRUCTOR
    public ChessBoard(){
        Arrays.fill(currPieceAtLocation, PieceData.INVALID_PIECES);
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

                setPieceAt(i*ChessBoard.BOARD_SIZE + col, PreCalc.FEN_MAP[c], PieceData.INVALID_PIECES);

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
        return spaceId + getOffsets(offset, dir);
    }

    public static int getOffsets(int offset, short dir){
        return directionOffsets[dir]*offset;
    }
    public static int getOffsets(short dir){
        return getOffsets(1, dir);
    }

    public static int getNOffsets(short dir){
        return KNIGHT_OFFSETS[dir];
    }

    public static int getNDirSpaceId(int spaceId, short dir){
        return spaceId + getNOffsets(dir);
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

    public static int getLRUpDiag(int spaceId){
        return getRow(spaceId) + getCol(spaceId);
    }

    public static int getLRDownDiag(int spaceId){
        return ChessBoard.BOARD_SIZE -1 - getRow(spaceId) + getCol(spaceId);
    }
    //endregion

    //region PIECE_FUNCS

    public long getBlackPieceBitBoard(){
        return boardSquares[PieceData.convertPieceIdToArrayIdx(PieceData.BPAWN)]
                | boardSquares[PieceData.convertPieceIdToArrayIdx(PieceData.BROOK)]
                | boardSquares[PieceData.convertPieceIdToArrayIdx(PieceData.BKNIGHT)]
                | boardSquares[PieceData.convertPieceIdToArrayIdx(PieceData.BBISHOP)]
                | boardSquares[PieceData.convertPieceIdToArrayIdx(PieceData.BQUEEN)]
                | boardSquares[PieceData.convertPieceIdToArrayIdx(PieceData.BKING)];
    }

    public long getWhitePieceBitBoard(){
        return boardSquares[PieceData.convertPieceIdToArrayIdx(PieceData.WPAWN)]
                | boardSquares[PieceData.convertPieceIdToArrayIdx(PieceData.WROOK)]
                | boardSquares[PieceData.convertPieceIdToArrayIdx(PieceData.WKNIGHT)]
                | boardSquares[PieceData.convertPieceIdToArrayIdx(PieceData.WBISHOP)]
                | boardSquares[PieceData.convertPieceIdToArrayIdx(PieceData.WQUEEN)]
                | boardSquares[PieceData.convertPieceIdToArrayIdx(PieceData.WKING)];
    }

    public long getEmptySpacesBitBoard(){
        return ~(getWhitePieceBitBoard() | getBlackPieceBitBoard());
    }
    public long getBitBoard(short pieceId){
        if (!PieceData.isValidPieceId(pieceId))
            throw new IllegalArgumentException("Invalid pieceId input at getBitBoard: " + pieceId);

        return boardSquares[PieceData.convertPieceIdToArrayIdx(pieceId)];
    }

    protected void setPieceAt(int spaceId, short piece, short previousPiece)
    {//protected to avoid uses messing with listener. used during initilization
        StateChangeListener.notifyListeners(this.stateChangeListeners,
                new BoardStateChange(piece, ChessBoard.INVALID_SPACE_ID, spaceId, previousPiece));

        if (PieceData.isValidPieceId(piece) && isValidSpaceId(spaceId))
            boardSquares[PieceData.convertPieceIdToArrayIdx(piece)] |= (1L << spaceId);
        else if(!PieceData.isValidPieceId(piece)){
            for (int i = 0; i < boardSquares.length; ++i) {
                boardSquares[i] &= ~(1L << spaceId);
            }
        }
    }

    public void spawnPieceAt(int spaceId, short piece, short pieceAtSpace)
    {
        setPieceAt(spaceId, PieceData.INVALID_PIECES, pieceAtSpace);
        setPieceAt(spaceId, piece, PieceData.INVALID_PIECES);

        //spawn piece by moving it in from nowhere.
        StateChangeListener.notifyListeners(this.boardMoveListeners,
                new BoardStateChange(piece, ChessBoard.INVALID_SPACE_ID, spaceId, pieceAtSpace));

    }

    protected void deSpawnPieceAt(int spaceId, short pieceAtSpace){

        if (PieceData.isValidPieceId(pieceAtSpace)) //ensure proper reconstruction later.
        {
            StateChangeListener.notifyListeners(this.boardMoveListeners,
                    new BoardStateChange(pieceAtSpace, spaceId,
                            ChessBoard.INVALID_SPACE_ID, PieceData.INVALID_PIECES));
        }
        setPieceAt(spaceId, PieceData.INVALID_PIECES, pieceAtSpace); //disappear piece.
    }

    protected void movePiecePrimitive(int spaceIdToMove, int spaceIdArriveAt,
                                       short pieceIdToMove, short pieceIdArriveAt)
    {
        setPieceAt(spaceIdToMove, PieceData.INVALID_PIECES, pieceIdToMove);
        setPieceAt(spaceIdArriveAt, pieceIdToMove, pieceIdArriveAt);
    }

    protected void movePiece(int spaceIdToMove, int spaceIdArriveAt,
                             short pieceIdToMove, short pieceIdArriveAt)
    {
        //move piece and just overwrite piece in that location. Only movePieceCapture should be public.

        //follow the philosophy of all piece should land on empty space,
        //assume enemy piece disappear before allied piece lands. this function dont handle capture.
        //notifyMoveListener(new BoardStateChange(capturedPiece, spaceIdArriveAt, ChessBoard.INVALID_SPACE_ID));

        movePiecePrimitive(spaceIdToMove, spaceIdArriveAt, pieceIdToMove, pieceIdArriveAt);
        StateChangeListener.notifyListeners(this.boardMoveListeners,
                new BoardStateChange(pieceIdToMove, spaceIdToMove, spaceIdArriveAt, pieceIdArriveAt));
    }

    public void movePieceCapture(int spaceIdToMove,
                                 int spaceIdArriveAt, int spaceIdCaptureAt)
    { //all input should be valid. //Order of operation: Enemy piece disappear, our piece land.
        short pieceIdArriveAt = getPiece(spaceIdArriveAt);
        short pieceIdCapture = spaceIdArriveAt == spaceIdCaptureAt ? pieceIdArriveAt : getPiece(spaceIdCaptureAt);
        short pieceIdToMove = getPiece(spaceIdToMove);

        deSpawnPieceAt(spaceIdCaptureAt, pieceIdCapture);
        movePiece(spaceIdToMove, spaceIdArriveAt, pieceIdToMove, PieceData.INVALID_PIECES); //piece land.
    }

    public short getPiece(int spaceId)
    {
        if (isValidSpaceId(spaceId)){
            return currPieceAtLocation[spaceId];
        }
        else {
            DebugMode.debugPrint(this, "Invalid spaceId at getPiece");
            return PieceData.INVALID_PIECES;
        }
    }

    public boolean isPieceAt(int spaceId)
    {
        if (!isValidSpaceId(spaceId)) return false;
        return (OCCUPIED & BitMasks.getSingleSpaceBitBoard(spaceId)) != 0;
    }

    public boolean isEnemyPieceAt(int spaceId, boolean pieceColor)
    {
        long pieceBoard = BitMasks.getSingleSpaceBitBoard(spaceId);
        if (!isValidSpaceId(spaceId)) return false;

        if (pieceColor == PieceData.WHITE){
            return (BLACK_PIECES & pieceBoard) != 0;
        }else{
            return (WHITE_PIECES&pieceBoard) != 0;
        }
    }

    public boolean isAlliedPieceAt(int spaceId, boolean pieceColor)
    {
        long pieceBoard = BitMasks.getSingleSpaceBitBoard(spaceId);
        if (!isValidSpaceId(spaceId)) return false;

        if (pieceColor == PieceData.WHITE){
            return (WHITE_PIECES & pieceBoard) != 0;
        }else{
            return (BLACK_PIECES & pieceBoard) != 0;
        }
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

        int col = colId - 'a';
        int row = '8' - rowId; // '8' -> row 0, '1' -> row 7
        return row*BOARD_SIZE + col;
    }

    public boolean isEmptySpaceAt(int spaceId)
    {
        return (OCCUPIED&BitMasks.getSingleSpaceBitBoard(spaceId)) == 0;
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
            int spaceId = boardStateChange.getSpaceId();
            int spaceIdArriveAt = boardStateChange.getSpaceIdArriveAt();
            short piece = boardStateChange.getPiece();
            short pieceCaptured = boardStateChange.getPieceCaptured();

            if (boardStateChange.getSpaceIdArriveAt() == INVALID_SPACE_ID) {//piece is taken out of board, thus must be spawned back in.
                setPieceAt(boardStateChange.getSpaceId(),
                        boardStateChange.getPiece(),
                        getPiece(spaceId));
            } else if (boardStateChange.getSpaceId() == INVALID_SPACE_ID) {
                //piece is spawned, must be taken out of board.
                setPieceAt(boardStateChange.getSpaceIdArriveAt(),
                        PieceData.INVALID_PIECES,
                        piece);
            } else {
                setPieceAt(spaceIdArriveAt,
                        PieceData.INVALID_PIECES,
                        piece);
                setPieceAt(spaceId,
                        piece,
                        PieceData.INVALID_PIECES);
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

    public void generateBitMasks(boolean color, int enPassantTarget){
        long kingBM;
        long queenBM;
        long kingBitBoard;
        if (color == PieceData.WHITE) {
            WHITE_PIECES = getWhitePieceBitBoard();
            BLACK_PIECES = getBlackPieceBitBoard();
            TO_MOVE_PIECES = WHITE_PIECES;
            NOT_TO_MOVE_PIECES = BLACK_PIECES;
            kingBM = BitMasks.WHITE_CASTLE_KING;
            queenBM = BitMasks.WHITE_CASTLE_QUEEN;
            kingBitBoard = getBitBoard(PieceData.WKING);

            TO_MOVE_THREATS_PIECE_ID = PreCalc.WHITE_THREAT_IDS;
        }else{
            WHITE_PIECES = getWhitePieceBitBoard();
            BLACK_PIECES = getBlackPieceBitBoard();
            TO_MOVE_PIECES = BLACK_PIECES;
            NOT_TO_MOVE_PIECES = WHITE_PIECES;
            kingBM = BitMasks.BLACK_CASTLE_KING;
            queenBM = BitMasks.BLACK_CASTLE_QUEEN;
            kingBitBoard = getBitBoard(PieceData.BKING);

            TO_MOVE_THREATS_PIECE_ID = PreCalc.BLACK_THREAT_IDS;
        }

        ANTI_TO_MOVE_PIECES = ~TO_MOVE_PIECES;
        ANTI_NOT_TO_MOVE_PIECES = ~NOT_TO_MOVE_PIECES;
        OCCUPIED = TO_MOVE_PIECES|NOT_TO_MOVE_PIECES;
        OCCUPIED_NO_KING = OCCUPIED&(~kingBitBoard);
        EMPTY = ~OCCUPIED;

        if (PropertiesStats.isValidEnPassantTarget(enPassantTarget))
            ENPASSANT = BitMasks.getSingleSpaceBitBoard(enPassantTarget);
        else ENPASSANT = 0;

        TO_MOVE_PIECES_AND_ENPASSANT = TO_MOVE_PIECES | ENPASSANT;

        NOT_TO_MOVE_PIECES_AND_ENPASSANT = NOT_TO_MOVE_PIECES | ENPASSANT;

        CAN_CASTLE_KING = ((EMPTY & kingBM) == kingBM);
        CAN_CASTLE_QUEEN = ((EMPTY & queenBM) == queenBM);
    };

    public void setBoard(ChessBoard board){
        boardSquares = Arrays.copyOf(board.boardSquares, board.boardSquares.length);
        currPieceAtLocation = board.currPieceAtLocation.clone();

        ANTI_TO_MOVE_PIECES = board.ANTI_TO_MOVE_PIECES;
        ANTI_NOT_TO_MOVE_PIECES = board.ANTI_NOT_TO_MOVE_PIECES;
        NOT_TO_MOVE_PIECES = board.NOT_TO_MOVE_PIECES;
        TO_MOVE_PIECES = board.TO_MOVE_PIECES;
        NOT_TO_MOVE_PIECES_AND_ENPASSANT = board.NOT_TO_MOVE_PIECES_AND_ENPASSANT;
        TO_MOVE_PIECES_AND_ENPASSANT = board.TO_MOVE_PIECES_AND_ENPASSANT;
        EMPTY = board.EMPTY;
        OCCUPIED = board.OCCUPIED;
        WHITE_PIECES = board.WHITE_PIECES;
        BLACK_PIECES = board.BLACK_PIECES;
        ENPASSANT = board.ENPASSANT;

        CAN_CASTLE_QUEEN = board.CAN_CASTLE_QUEEN;
        CAN_CASTLE_KING = board.CAN_CASTLE_KING;
    }

    public ChessBoard cloneBoard(){
        ChessBoard clone = new ChessBoard();
        clone.setBoard(this);

        return clone;
    }

    public void printBoard() {
        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int spaceId = row * BOARD_SIZE + col;
                short piece = getPiece(spaceId);
                sb.append(pieceToUnicode(piece)).append(' ');
            }
            sb.append('\n');
        }

        System.out.print(sb.toString());
    }

    private static char pieceToUnicode(short piece) {
        return switch (piece) {
            case PieceData.WKING   -> '♔';
            case PieceData.WQUEEN  -> '♕';
            case PieceData.WROOK   -> '♖';
            case PieceData.WBISHOP -> '♗';
            case PieceData.WKNIGHT -> '♘';
            case PieceData.WPAWN   -> '♙';

            case PieceData.BKING   -> '♚';
            case PieceData.BQUEEN  -> '♛';
            case PieceData.BROOK   -> '♜';
            case PieceData.BBISHOP -> '♝';
            case PieceData.BKNIGHT -> '♞';
            case PieceData.BPAWN   -> '♟';

            default -> '　'; // empty square
        };
    }

}
