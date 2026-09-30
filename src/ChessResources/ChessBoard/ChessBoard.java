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
    public static final int MAX_COL = BOARD_SIZE-1;
    public static final int MAX_ROW = BOARD_SIZE-1;
    public static final int MIN_COL = 0;
    public static final int MIN_ROW = 0;
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
    //list of all the bitboard containing the pieces.

    public short[] currPieceAtLocation = new short[ChessBoard.TOTAL_SPACES];

    protected ArrayList<StateChangeListener<BoardStateChange>> boardMoveListeners = new ArrayList<>();
    protected ArrayList<StateChangeListener<BoardStateChange>> stateChangeListeners = new ArrayList<>();
    protected boolean debugMode = false;

    public long ANTI_TO_MOVE_PIECES; //reverse of pieces that cna move this turn.
    public long ANTI_NOT_TO_MOVE_PIECES;//reverse of pieces that cant move this turn.
    public long NOT_TO_MOVE_PIECES; //pieces that cant move this turn.
    public long TO_MOVE_PIECES; //pieces that cna move this turn
    public long NOT_TO_MOVE_PIECES_AND_ENPASSANT; //bit board of pieces whose cant move this turn, and enpassant.
    public long TO_MOVE_PIECES_AND_ENPASSANT;
    public long EMPTY;
    public long OCCUPIED; //spaces on board thats occupied.
    public long WHITE_PIECES;
    public long BLACK_PIECES;
    public long ENPASSANT;
    public boolean CAN_CASTLE_KING;
    public boolean CAN_CASTLE_QUEEN;
    public long OCCUPIED_NO_KING;
    public long KING_TO_MOVE_BITBOARD;
    //bit board of the side to move king.


    public short[] TO_MOVE_THREATS_PIECE_ID;
    //piece id of the piece whose turn to mov eis now.
    //endregion

    public final StateChangeListener<BoardStateChange> BOARD_STATE_CHANGE_LOGGER =
            (BoardStateChange boardStateChange)->{

                int spaceId = boardStateChange.getSpaceId();
                int spaceIdArriveAt = boardStateChange.getSpaceIdArriveAt();

                short pieceData = boardStateChange.getPiece();

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
    public static int getOffsets(int offset, short dir){
        return directionOffsets[dir]*offset;
    }

    public static int getOffsets(short dir){
        return getOffsets(1, dir);
    }

    public static int getNOffsets(short dir){
        return KNIGHT_OFFSETS[dir];
    }
    //endregion

    //region GET_ROW_COL_FUNCS

    public long getRoundSpaceId(int spaceId, int range){
        return getNearFile(spaceId, range) &
                getNearRow(spaceId, range);
    }

    public long getNearFile(int sqr, int range){
        int currFile = ChessBoard.getCol(sqr);
        long ans = 0L;
        for (int i = currFile; i >= 0 && i >= currFile-range; --i){
            ans |= BitMasks.COL_MASKS[i];
        }

        for (int i = currFile; i <= ChessBoard.MAX_COL && i <= currFile+range; ++i){
            ans |= BitMasks.COL_MASKS[i];
        }

        return ans;
    }

    public long getNearRow(int sqr, int range){
        int currRow = ChessBoard.getRow(sqr);
        long ans = 0L;
        for (int i = currRow; i >= 0 && i >= currRow-range; --i){
            ans |= BitMasks.ROW_MASKS[i];
        }

        for (int i = currRow+1; i <= ChessBoard.MAX_ROW && i <= currRow+range; ++i){
            ans |= BitMasks.ROW_MASKS[i];
        }
        return ans;
    }

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
                                       short pieceIdToMove)
    {
        setPieceAt(spaceIdToMove, PieceData.INVALID_PIECES, pieceIdToMove);
        setPieceAt(spaceIdArriveAt, pieceIdToMove, PieceData.INVALID_PIECES);
    }

    protected void movePiece(int spaceIdToMove, int spaceIdArriveAt,
                             short pieceIdToMove)
    {
        //move piece and just overwrite piece in that location. Only movePieceCapture should be public.

        //follow the philosophy of all piece should land on empty space,
        //assume enemy piece disappear before allied piece lands. this function dont handle capture.
        //notifyMoveListener(new BoardStateChange(capturedPiece, spaceIdArriveAt, ChessBoard.INVALID_SPACE_ID));

        movePiecePrimitive(spaceIdToMove, spaceIdArriveAt, pieceIdToMove);
        StateChangeListener.notifyListeners(this.boardMoveListeners,
                new BoardStateChange(pieceIdToMove, spaceIdToMove, spaceIdArriveAt, PieceData.INVALID_PIECES));
    }

    public void movePieceCapture(int spaceIdToMove,
                                 int spaceIdArriveAt, int spaceIdCaptureAt)
    { //all input should be valid. //Order of operation: Enemy piece disappear, our piece land.
        short pieceIdArriveAt = getPiece(spaceIdArriveAt);
        short pieceIdCapture = spaceIdArriveAt == spaceIdCaptureAt ? pieceIdArriveAt : getPiece(spaceIdCaptureAt);
        short pieceIdToMove = getPiece(spaceIdToMove);

        deSpawnPieceAt(spaceIdCaptureAt, pieceIdCapture);
        movePiece(spaceIdToMove, spaceIdArriveAt, pieceIdToMove); //piece land.
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
        if (color == PieceData.WHITE) {
            WHITE_PIECES = getWhitePieceBitBoard();
            BLACK_PIECES = getBlackPieceBitBoard();
            TO_MOVE_PIECES = WHITE_PIECES;
            NOT_TO_MOVE_PIECES = BLACK_PIECES;
            kingBM = BitMasks.WHITE_CASTLE_KING;
            queenBM = BitMasks.WHITE_CASTLE_QUEEN;
            KING_TO_MOVE_BITBOARD = getBitBoard(PieceData.WKING);

            TO_MOVE_THREATS_PIECE_ID = PreCalc.WHITE_THREAT_IDS;
        }else{
            WHITE_PIECES = getWhitePieceBitBoard();
            BLACK_PIECES = getBlackPieceBitBoard();
            TO_MOVE_PIECES = BLACK_PIECES;
            NOT_TO_MOVE_PIECES = WHITE_PIECES;
            kingBM = BitMasks.BLACK_CASTLE_KING;
            queenBM = BitMasks.BLACK_CASTLE_QUEEN;
            KING_TO_MOVE_BITBOARD = getBitBoard(PieceData.BKING);

            TO_MOVE_THREATS_PIECE_ID = PreCalc.BLACK_THREAT_IDS;
        }

        ANTI_TO_MOVE_PIECES = ~TO_MOVE_PIECES;
        ANTI_NOT_TO_MOVE_PIECES = ~NOT_TO_MOVE_PIECES;
        OCCUPIED = TO_MOVE_PIECES|NOT_TO_MOVE_PIECES;
        OCCUPIED_NO_KING = OCCUPIED&(~KING_TO_MOVE_BITBOARD);
        EMPTY = ~OCCUPIED;

        if (PropertiesStats.isValidEnPassantTarget(enPassantTarget))
            ENPASSANT = BitMasks.getSingleSpaceBitBoard(enPassantTarget);
        else ENPASSANT = 0;

        TO_MOVE_PIECES_AND_ENPASSANT = TO_MOVE_PIECES | ENPASSANT;

        NOT_TO_MOVE_PIECES_AND_ENPASSANT = NOT_TO_MOVE_PIECES | ENPASSANT;

        CAN_CASTLE_KING = ((EMPTY & kingBM) == kingBM);
        CAN_CASTLE_QUEEN = ((EMPTY & queenBM) == queenBM);
    }

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

        OCCUPIED_NO_KING = board.OCCUPIED_NO_KING;
        TO_MOVE_THREATS_PIECE_ID = board.TO_MOVE_THREATS_PIECE_ID;
        KING_TO_MOVE_BITBOARD = board.KING_TO_MOVE_BITBOARD;

        debugMode = board.debugMode;
    }

    public ChessBoard cloneBoard(){
        ChessBoard clone = new ChessBoard();
        clone.setBoard(this);

        return clone;
    }

    public void printBoard() {
        System.out.print(this);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int spaceId = row * BOARD_SIZE + col;
                short piece = getPiece(spaceId);
                sb.append(pieceToUnicode(piece)).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
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
