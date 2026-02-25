package ChessLogic;

import ChessLogic.Configurations.Configurations;
import ChessLogic.Debug.DebugMode;
import ChessLogic.Debug.Debuggable;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.ChessBoard.ChessBoardUI;
import ChessResources.ChessErrors.OutOfOldTurns;
import ChessResources.ChessHistoryTracker.BoardStateChanges.BoardStateChange;
import ChessResources.ChessHistoryTracker.BoardStateChanges.PropertiesStatsChange;
import ChessResources.ChessHistoryTracker.ChessHistoryTracker;
import ChessResources.ChessHistoryTracker.GameStateChanges;
import ChessResources.ChessListener.StateChangeListener;
import ChessResources.GetMovesLogic.ChessSpaces;
import ChessResources.Hasher.HashGenerator;
import ChessResources.HelperFuncs.BoardScan.BoardScan;
import ChessResources.HelperFuncs.BoardScan.ScanResult;
import ChessResources.Pieces.PieceData;
import ChessResources.GetMovesLogic.PossibleMoves;
import ChessResources.Pieces.MovingPieceData;
import ChessResources.PreCalc;

import java.util.ArrayList;
import java.util.function.BiFunction;

public class MinimalChessGame<Board extends ChessBoard> implements Debuggable {
    //region BEFORE_CONSTRUCTORS
    //region GAME_DATAS
    public final Board chessBoard;
    public final HashGenerator<Board> hashGenerator = new HashGenerator<>( this);

    BiFunction<Integer, Boolean, Short> choosePromotionPiece;

    protected PossibleMoves possibleMoves;
    public static final int BLACK_PM = 0;
    public static final int WHITE_PM = 1;

    public ChessHistoryTracker chessHistoryTracker = new ChessHistoryTracker();

    Configurations configurations;
    //endregion

    //region GAME_PROPERTIES
    public static final int GAME_PROPERTIES_LEN = 5;
    public boolean[] gameProperties = new boolean[GAME_PROPERTIES_LEN];

    public static int BLACK_CASTLE_QUEEN = 0;
    public static int BLACK_CASTLE_KING = 1;

    public static int WHITE_CASTLE_QUEEN = 2;
    public static int WHITE_CASTLE_KING = 3;

    public static int SIDE_TO_MOVE = 4;
    //endregion
    
    //region GAME_STATS
    public static final int GAME_STATS_LEN = 5;
    public int[] gameStats = new int[GAME_STATS_LEN];

    private int currEnPassantTarget = INVALID_ENPASSANT_TARGET;

    public static int ENPASSANT_TARGET = 0; //spaceId format.
    public static final int HALF_MOVES_SICE_CAPTURE_OR_PAWN_MOVES = 1;
    public static final int TOTAL_MOVES_ELAPSED = 2;
    public static final int WHITE_KING_SPACEID = 3;
    public static final int BLACK_KING_SPACEID = 4;

    public static final int INVALID_ENPASSANT_TARGET = -1;
    private final ArrayList<StateChangeListener<PropertiesStatsChange>>
            propertiesStatsChangeListenerList = new ArrayList<>();

    private final ArrayList<StateChangeListener<GameStateChanges>>
            turnEndListenerList = new ArrayList<>();

    private final ArrayList<StateChangeListener<GameStateChanges>>
            undoTurnListenerList = new ArrayList<>();
    //endregion

    //region SAVE_GAME_OPCODE
    public static final int NORMAL_MOVE = 0;
    public static final int PROMOTION = 1;
    public static final int ENPASSANT = 2;
    //endregion

    //region ENDGAME_OPCODE
    private int endGameCode = INDETERMINATE;
    public static final int WHITE_WON = 0;
    public static final int BLACK_WON = 1;

    public static final int DRAW = 2;
    public static final int INDETERMINATE = 3;
    //endregion

    //region LAMBDAS
    public static final BiFunction<Integer, Boolean, Short> DEFAULT_PROMOTION_FUNC =
            (Integer _, Boolean color) -> {
                if (color == PieceData.BLACK) return PieceData.BQUEEN;
                else return PieceData.WQUEEN;
            };

    public final StateChangeListener<BoardStateChange> BOARD_STATE_LOGGER =
            (BoardStateChange boardStateChange)->{
        this.chessHistoryTracker.pushBoardStateChange(boardStateChange);
    };

    public final StateChangeListener<BoardStateChange> KING_SPACEID_LOGGER =
            (BoardStateChange boardStateChange)->{
                if (boardStateChange == null || !PieceData.isValidPieceId(boardStateChange.getPiece())) return;

                if (boardStateChange.getPiece() == PieceData.WKING){
                    gameStats[WHITE_KING_SPACEID] = boardStateChange.getSpaceIdArriveAt();
                }
                else if (boardStateChange.getPiece() == PieceData.BKING){
                    gameStats[BLACK_KING_SPACEID] = boardStateChange.getSpaceIdArriveAt();
                }
            };

    public final StateChangeListener<PropertiesStatsChange> PROPERTIES_STATS_LOGGER =
            (PropertiesStatsChange propertiesStatsChange)->{
        this.chessHistoryTracker.setGamePropertiesStats(propertiesStatsChange);
    };

    //endregion

    //endregion

    //region Constructor

    public MinimalChessGame(Board chessBoard, BiFunction<Integer, Boolean, Short> choosePromotionPiece,
                            Configurations configurations)
    {
        this.configurations = configurations;

        this.chessBoard = chessBoard;
        this.choosePromotionPiece = choosePromotionPiece;

        this.addMoveListener(BOARD_STATE_LOGGER);
        this.addPropertiesStatsChangeListener(PROPERTIES_STATS_LOGGER);
        this.chessBoard.addStateChangeListener(KING_SPACEID_LOGGER);

        possibleMoves = new PossibleMoves(this);

    }

    public MinimalChessGame(Board chessBoard, BiFunction<Integer, Boolean, Short> choosePromotionPiece,
                            boolean[] gameProperties, int[] gameStats, Configurations configurations)
    {
        this(chessBoard, choosePromotionPiece, configurations);

        this.gameProperties = gameProperties;
        this.gameStats = gameStats;

        updateConfigurations();

        generatePossibleMoves();
    }
    @SuppressWarnings("unused")
    public MinimalChessGame(String fen, Board chessBoard, BiFunction<Integer, Boolean, Short> choosePromotionPiece,
                            Configurations configurations)
    {

        this(chessBoard, choosePromotionPiece, configurations);

        updateConfigurations();

        fenTranslator(fen);

        generatePossibleMoves();

    }
    //endregion

    //region GETTERS
    public ChessBoard getBoard(){
        return chessBoard;
    }

    public int[] getGameStats() {
        return gameStats;
    }

    public boolean[] getGameProperties() {
        return gameProperties;
    }

    public PossibleMoves getPossibleMoves(){
        return possibleMoves;
    }

    public boolean getCurrentColorToMove(){
        return gameProperties[SIDE_TO_MOVE];
    }
    //endregion

    //region HELPERS

    //region MISC_FUNCS
    public void fenTranslator(String fen){
        String[] args = fen.trim().split(" ");
        chessBoard.setUpPieces(args[0]);
        if (args.length != 6) {
            throw new IllegalArgumentException("Invalid FEN: Must include 6 arguments.");
        }
        //region ASSIGN_SIDE_TO_MOVE
        if (args[1].equals("w")) gameProperties[SIDE_TO_MOVE] = PieceData.WHITE;
        else if (args[1].equals("b")) gameProperties[SIDE_TO_MOVE] = PieceData.BLACK;
        else
            throw new IllegalArgumentException("Invalid FEN: Side to Move is invalid (" + args[1] + ").");
        //endregion
        //region ASSIGN_CASTLING_RIGHTS
        gameProperties[BLACK_CASTLE_KING]  = args[2].contains("k");
        gameProperties[BLACK_CASTLE_QUEEN] = args[2].contains("q");
        gameProperties[WHITE_CASTLE_KING]  = args[2].contains("K");
        gameProperties[WHITE_CASTLE_QUEEN] = args[2].contains("Q");
        //endregion
        //region ASSIGN_EN_PASSANT_TARGET
        if (args[3].length() == 1 && args[3].equals("-")) {
            gameStats[ENPASSANT_TARGET] = -1;
        } else if (args[3].length() == 2 && Character.isAlphabetic(args[3].charAt(0))
                && Character.isDigit(args[3].charAt(1))) {
            gameStats[ENPASSANT_TARGET] = ChessBoardUI.convertSquareNotationToSpaceId(
                    args[3].charAt(0), args[3].charAt(1));
        } else
            throw new IllegalArgumentException("Invalid files and ranks input (" + args[3] + ").");
        //endregion
        gameStats[HALF_MOVES_SICE_CAPTURE_OR_PAWN_MOVES] = Integer.parseInt(args[4]);
        gameStats[TOTAL_MOVES_ELAPSED] = Integer.parseInt(args[5]);
        //region ASSIGN_KINGS_LOCATION
//        gameStats[WHITE_KING_SPACEID] = chessBoard.findPiece(shorts.WKING_DATA.getPieceId());
//        gameStats[BLACK_KING_SPACEID] = chessBoard.findPiece(shorts.BKING_DATA.getPieceId());
        //endregion
    }

    public void generatePossibleMoves(){

        possibleMoves.clearPossibleMoves();
        possibleMoves.generateMoves();
    }

    public boolean spaceNotUnderThreat(int spaceId, boolean alliedColor)
    {
        int[][] slidingMoves = PreCalc.SLIDING_MOVES[spaceId];
        int[] ids = ((alliedColor == PieceData.WHITE) ? PreCalc.WHITE_THREAT_IDS : PreCalc.BLACK_THREAT_IDS);

        boolean metEnemiesFlag;

        if (ids[0] == PieceData.BPAWN)
            metEnemiesFlag = BoardScan.rayScanFor(this, slidingMoves, 1,
                new short[]{ChessBoard.NORTH_EAST, ChessBoard.NORTH_WEST}, ids[0]);
        else {
            metEnemiesFlag = BoardScan.rayScanFor(this, slidingMoves, 1,
                    new short[]{ChessBoard.SOUTH_EAST, ChessBoard.SOUTH_WEST}, ids[0]);
        }

        if (metEnemiesFlag) return false;

        metEnemiesFlag = BoardScan.rayScanFor(this, slidingMoves, MovingPieceData.NO_RANGE_LIMIT,
            MovingPieceData.BISHOP_DIR, new int[]{ids[1], ids[2]});
        if (metEnemiesFlag) return false;

        metEnemiesFlag = BoardScan.rayScanFor(this, slidingMoves, MovingPieceData.NO_RANGE_LIMIT,
                MovingPieceData.ROOK_DIR, new int[]{ids[1], ids[3]});
        if (metEnemiesFlag) return false;

       metEnemiesFlag = BoardScan.jumpScanFor(this,
                PreCalc.KNIGHT_MOVES[spaceId], ids[4]);
        if (metEnemiesFlag) return false;

        boolean metKing = BoardScan.rayScanFor(this, slidingMoves, 1,
                MovingPieceData.QUEEN_DIR, new int[]{ids[5]});

        return !metKing;
    }

    public ChessSpaces getSpacesToMoveToStopThreats(int spaceId, boolean alliedColor)
    {
        //region ASSIGNMENT
        int[][] slidingMoves = PreCalc.SLIDING_MOVES[spaceId];
        int pawn   = PieceData.WPAWN;
        int queen = PieceData.WQUEEN;
        int bishop= PieceData.WBISHOP;
        int rook  = PieceData.WROOK;
        int knight= PieceData.WKNIGHT;
        int king  = PieceData.WKING;

        if (alliedColor == PieceData.WHITE){
            pawn   = PieceData.getOppositeColor(pawn);
            queen  = PieceData.getOppositeColor(queen);
            bishop = PieceData.getOppositeColor(bishop);
            rook   = PieceData.getOppositeColor(rook);
            knight = PieceData.getOppositeColor(knight);
            king   = PieceData.getOppositeColor(king);
        }
        //endregion

        short[] pawnDirs;
        ChessSpaces tmpSpaces = new ChessSpaces();
        if (pawn == PieceData.BPAWN)
            pawnDirs = new short[]{ChessBoard.NORTH_EAST, ChessBoard.NORTH_WEST};
        else {
            pawnDirs = new short[]{ChessBoard.SOUTH_EAST, ChessBoard.SOUTH_WEST};
        }

        ChessSpaces ans = ChessSpaces.getNewUniverseSet();
        //scanning for pawn
        for (short dir : pawnDirs) {
            ScanResult sr = BoardScan.rayScan(this, slidingMoves[dir], 1, null);
            if (sr.getPieceId() == pawn){
                ChessSpaces.fastIntersection(ans, tmpSpaces, sr.getSpaceId());
            }
        }

        if (ans.isEmpty()) return ans;
        ans.moveIntersection(getSpacesToMoveToStopThreats(MovingPieceData.BISHOP_DIR, spaceId, bishop));
        if (ans.isEmpty()) return ans;
        ans.moveIntersection(getSpacesToMoveToStopThreats(MovingPieceData.ROOK_DIR, spaceId, rook));
        if (ans.isEmpty()) return ans;
        ans.moveIntersection(getSpacesToMoveToStopThreats(MovingPieceData.QUEEN_DIR, spaceId, queen));
        if (ans.isEmpty()) return ans;
        //for king moves
        for (short dir : MovingPieceData.QUEEN_DIR) {
            ScanResult sr = BoardScan.rayScan(this,
                    slidingMoves[dir],
                    1,
                    null);

            if (sr.getPieceId() == king){
                ChessSpaces.fastIntersection(ans, tmpSpaces, sr.getSpaceId());
            }
        }
        if (ans.isEmpty()) return ans;

        ScanResult[] srs = BoardScan.jumpScan(this, PreCalc.KNIGHT_MOVES[spaceId], null);

        for (ScanResult sr : srs){
            if (!ScanResult.isValid(sr)) break;

            if (sr.getPieceId() == knight){
                ChessSpaces.fastIntersection(ans, tmpSpaces, sr.getSpaceId());
            }
            if (ans.isEmpty()) return ans;
        }

        return ans;
    }

    private ChessSpaces getSpacesToMoveToStopThreats(short[] dirs, int spaceId, int pieceId){

        int[][] slidingMoves = PreCalc.SLIDING_MOVES[spaceId];
        ChessSpaces ans = ChessSpaces.getNewUniverseSet();
        ChessSpaces tmp = new ChessSpaces();
        for (short dir : dirs) {
            tmp.clear();
            ScanResult sr = BoardScan.rayScan(this,
                    slidingMoves[dir],
                    MovingPieceData.NO_RANGE_LIMIT,
                    tmp);

            if (sr.getPieceId() == pieceId){
                //scan the pieceId needed to detect.
                tmp.addMoves(sr.getSpaceId());
                ans.moveIntersection(tmp);
            }

            if (ans.isEmpty()) return ans;
        }
        return ans;
    }

    public boolean spaceNotUnderThreat(int spaceId){
        return spaceNotUnderThreat(spaceId, getCurrentColorToMove());
    }

    public boolean spaceNotUnderThreatAndEmpty(int spaceId)
    {
        return spaceNotUnderThreat(spaceId) && chessBoard.isEmptySpaceAt(spaceId);
    }

    public boolean isAlliedPieceAt(int spaceId, boolean color){
        return chessBoard.isAlliedPieceAt(spaceId, color);
    }
    public boolean isAlliedPieceAt(int spaceId){
        return chessBoard.isAlliedPieceAt(spaceId, getCurrentColorToMove());
    }

    public boolean isEnemyPieceAt(int spaceId, boolean color){
        return chessBoard.isEnemyPieceAt(spaceId, color);
    }
    public boolean isEnemyPieceAt(int spaceId){
        return chessBoard.isEnemyPieceAt(spaceId, getCurrentColorToMove());
    }

    public int getKingSpaceId(boolean color){

        return color == PieceData.WHITE ?
                gameStats[WHITE_KING_SPACEID] : gameStats[BLACK_KING_SPACEID];
    }

    public int getKingToMoveSpaceId(){
        return getKingSpaceId(getCurrentColorToMove());
    }

    public int getEnpassantTarget(){
        return getGameStats()[ENPASSANT_TARGET];
    }

    public void pushCurrEnPassantTarget(){
        getGameStats()[ENPASSANT_TARGET] = currEnPassantTarget;
        currEnPassantTarget = INVALID_ENPASSANT_TARGET;
    }

    private void promotionHandler(int spaceId, short piece)
    {
        //chessBoard.movePieceCapture(ChessBoard.INVALID_SPACE_ID, spaceId, spaceId);
        chessBoard.spawnPieceAt(spaceId, piece);
    }

    private void resetHalfMoves(){
        gameStats[HALF_MOVES_SICE_CAPTURE_OR_PAWN_MOVES] = 0;
    }

    public PossibleMoves getCurrentPossibleMoves(){
        return getPossibleMoves();
    }
    //endregion

    //region TURN_HANDLING
    public boolean movePiece(int spaceIdToMove, int spaceIdArriveAt)
    {
        if (!possibleMoves.canMoveToFrom(spaceIdToMove,spaceIdArriveAt)) return false;

        StateChangeListener.notifyListeners(propertiesStatsChangeListenerList,
                new PropertiesStatsChange(gameProperties, gameStats)); //announce new rules added.

        int opCode = NORMAL_MOVE;
        short piece = (short) chessBoard.getPiece(spaceIdToMove);
        final boolean isPawn = piece == PieceData.BPAWN || piece == PieceData.WPAWN;
        boolean currentColor = getCurrentColorToMove();

        int spaceIdCaptureAt = spaceIdArriveAt;

        if (isPawn)
        {
            short dir = (currentColor == PieceData.WHITE ? ChessBoard.SOUTH : ChessBoard.NORTH);
            //region EN_PASSANT_LOGIC
            if (spaceIdArriveAt == getEnpassantTarget())
            {
                spaceIdCaptureAt = spaceIdArriveAt + ChessBoard.directionOffsets[dir];
            }
            //en passant logic
            else if (Math.abs(ChessBoard.getRow(spaceIdToMove) - ChessBoard.getRow(spaceIdArriveAt)) == 2) //pawn moved 2 space
            {
                currEnPassantTarget = spaceIdArriveAt + ChessBoard.directionOffsets[dir];
            }
            //endregion

            //region PROMOTION_LOGIC
            if (ChessBoard.getRow(spaceIdArriveAt) == ChessBoard.BOARD_SIZE-1
                    || ChessBoard.getRow(spaceIdArriveAt) == 0)
            //check pawn color, do not assume pawn dont go backward or start at like row 0 or 8.
            {
                //promotionSpaceId = spaceIdArriveAt;
                opCode = PROMOTION;
            }
            //endregion
        }

        //region CASTLING_LOGIC
        else if (piece== PieceData.WKING)
        {
            if (ChessBoard.getCol(spaceIdToMove) - ChessBoard.getCol(spaceIdArriveAt) > 1)
            {
                int rookSpaceId = ChessBoard.BOARD_SIZE*(ChessBoard.BOARD_SIZE-1);
                int rookMoveId = ChessBoard.getEastSpaceId(spaceIdArriveAt, 1); //rook should be to the east of new king.
                assert(gameProperties[WHITE_CASTLE_QUEEN]); //should be true
                chessBoard.movePieceCapture(rookSpaceId, rookMoveId, rookMoveId);
            }
            else if (ChessBoard.getCol(spaceIdToMove) - ChessBoard.getCol(spaceIdArriveAt) < -1)
            {
                int rookSpaceId = ChessBoard.BOARD_SIZE* ChessBoard.BOARD_SIZE-1;
                int rookMoveId = ChessBoard.getWestSpaceId(spaceIdArriveAt, 1); //rook should be to the west of new king.
                assert(gameProperties[WHITE_CASTLE_KING]); //should be true
                chessBoard.movePieceCapture(rookSpaceId, rookMoveId, rookMoveId);
            }

            gameProperties[WHITE_CASTLE_QUEEN] = false;
            gameProperties[WHITE_CASTLE_KING] = false;
        }
        else if (piece == PieceData.BKING)
        {
            if (ChessBoard.getCol(spaceIdToMove) - ChessBoard.getCol(spaceIdArriveAt) > 1)
            {
                int rookSpaceId = 0;
                int rookMoveId = ChessBoard.getEastSpaceId(spaceIdArriveAt, 1); //rook should be to the east of new king.
                assert(gameProperties[BLACK_CASTLE_QUEEN]); //should be true
                chessBoard.movePieceCapture(rookSpaceId, rookMoveId, rookMoveId);
            }
            else if (ChessBoard.getCol(spaceIdToMove) - ChessBoard.getCol(spaceIdArriveAt) < -1)
            {
                int rookSpaceId = ChessBoard.BOARD_SIZE-1;
                int rookMoveId = ChessBoard.getWestSpaceId(spaceIdArriveAt, 1); //rook should be to the west of new king.
                assert(gameProperties[BLACK_CASTLE_KING]); //should be true

                chessBoard.movePieceCapture(rookSpaceId, rookMoveId, rookMoveId);
            }
            gameProperties[BLACK_CASTLE_QUEEN] = false;
            gameProperties[BLACK_CASTLE_KING] = false;
        }
        else if (piece == PieceData.WROOK) //these rook funcs wont work if i ever want to implement chess960.
        {
            if (gameProperties[WHITE_CASTLE_KING] && ChessBoard.getCol(spaceIdToMove) == 7)
            {
                gameProperties[WHITE_CASTLE_KING] = false;
            }
            else if (gameProperties[WHITE_CASTLE_QUEEN] && ChessBoard.getCol(spaceIdToMove) == 0)
            {
                gameProperties[WHITE_CASTLE_QUEEN] = false;
            }
        }
        else if (piece == PieceData.BROOK)
        {
            if (gameProperties[BLACK_CASTLE_KING] && ChessBoard.getCol(spaceIdToMove) == 7)
            {
                gameProperties[BLACK_CASTLE_KING] = false;
            }
            else if (gameProperties[BLACK_CASTLE_QUEEN] && ChessBoard.getCol(spaceIdToMove) == 0)
            {
                gameProperties[BLACK_CASTLE_QUEEN] = false;
            }
        }
        //endregion

        //region ACTUALLY_MOVING_THE_PIECE
        if (isPawn){
            resetHalfMoves();
        }
        else if (chessBoard.isPieceAt(spaceIdCaptureAt)){
            resetHalfMoves();
        }

        chessBoard.movePieceCapture(spaceIdToMove, spaceIdArriveAt, spaceIdCaptureAt);

        if (opCode == PROMOTION) //make no further checks, shouldnt be invalid.
        {
            try{
            promotionHandler(spaceIdArriveAt,
                    choosePromotionPiece.apply(spaceIdArriveAt, PieceData.getColor(piece)));
            }
            catch(NullPointerException e){
                promotionHandler(spaceIdArriveAt,
                        DEFAULT_PROMOTION_FUNC.apply(spaceIdArriveAt, PieceData.getColor(piece)));
            }
        }
        //endregion

        //region SAVE_GAME_P&P
        finishTurn();
        //endregion
        return true;
    }

    private void aSideWon(){
        //should only be called after everything in turn is updated.

        if (getPossibleMoves().isEmpty()){
            if (spaceNotUnderThreat(getKingToMoveSpaceId())){
                endGameCode = DRAW;
            }
            else endGameCode = gameProperties[SIDE_TO_MOVE] == PieceData.BLACK ? WHITE_WON : BLACK_WON;
        }
        else if(!ChessBoard.isValidSpaceId(getKingToMoveSpaceId())){
            endGameCode = getCurrentColorToMove() == PieceData.WHITE ? WHITE_WON : BLACK_WON;
        }
        else if (gameStats[HALF_MOVES_SICE_CAPTURE_OR_PAWN_MOVES] == 50){
            endGameCode = DRAW;
        }
        else if (chessHistoryTracker.isThreeFoldRepitionFlag()) endGameCode = DRAW;
        else endGameCode = INDETERMINATE;
        //endregion
    }

    private void finishTurn()
    {
        chessHistoryTracker.setHash(hashGenerator.getCurrentHash());
        chessHistoryTracker.pushTurn();

        StateChangeListener.notifyListeners(turnEndListenerList, chessHistoryTracker.peekTurn());
        //peek the turn just pushed.

        //region UPDATING_GAME_PROPERTIES_AND STATS
        gameStats[HALF_MOVES_SICE_CAPTURE_OR_PAWN_MOVES]++;
        gameProperties[SIDE_TO_MOVE] = !gameProperties[SIDE_TO_MOVE];//change move sid
        pushCurrEnPassantTarget();
        //endregion
        if (isDebuggable()) DebugMode.debugPrint(this, "Finish turn + side: " +
                (gameProperties[SIDE_TO_MOVE] == ChessBoard.WHITE ?"white" : "black" ));

        //region GENERATE_POSSIBLE_MOVES
        //generate possible moves after changing side.
        // Doing it this way ensure when a new moves is generated, new piece location information is taken into account
        generatePossibleMoves();
        //endregion
//        StateChangeListener.<PropertiesStatsChange>notifyListeners(propertiesStatsChangeListenerList,         new PropertiesStatsChange(gameProperties, gameStats));

        tryEndGame();
    }

    private void tryEndGame(){
        aSideWon();
        if (endGameCode != INDETERMINATE)
            endGame();
    }

    private void endGame() {
        if (!configurations.isAllowGameEnd()) return; //game cant end. assume is a simulation.

        if (endGameCode == WHITE_WON) {
            System.out.println("WHITE WON");
        } else if (endGameCode == BLACK_WON) {
            System.out.println("BLACK WON");
        } else if (endGameCode == DRAW) {
            System.out.println("DRAW");
        } else {
            System.out.println("NO REASON. ");
        }
        if (isDebuggable()) DebugMode.debugPrint(this, chessHistoryTracker);
        //System.exit(0);
    }

    public void undoTurn() throws OutOfOldTurns, NullPointerException
    {
        if (chessHistoryTracker == null) throw new NullPointerException("chessHistoryTracker is null!");

        if(chessHistoryTracker.isEmpty()) throw new OutOfOldTurns("");

        GameStateChanges gameStateChanges = chessHistoryTracker.popTurn();
        chessHistoryTracker.clearCurrentTurn();

        this.gameStats = gameStateChanges.getGameStats();
        this.gameProperties = gameStateChanges.getGameProperties();

        chessBoard.undoBoardState(gameStateChanges.getBoardStateChanges()); //throws nullpointerexcep

        //region GENERATE_POSSIBLE_MOVES
        //generate possible moves after changing side.
        // Doing it this way ensure when a new moves is generated, new piece location information is taken into account
        generatePossibleMoves();
        //endregion

        StateChangeListener.notifyListeners(undoTurnListenerList, gameStateChanges);
    }

    public void advanceBoardState(GameStateChanges gameStateChanges){
        this.gameStats = gameStateChanges.getGameStats();
        this.gameProperties = gameStateChanges.getGameProperties();

        StateChangeListener.notifyListeners(propertiesStatsChangeListenerList,
                new PropertiesStatsChange(gameProperties, gameStats)); //announce new rules added.

        chessBoard.advanceBoardState(gameStateChanges.getBoardStateChanges()); //throws nullpointerexcep

        finishTurn();
    }
    //endregion

    //endregion

    //region LISTENERS
    public void addMoveListener(StateChangeListener<BoardStateChange> stateChangeListener)
    {
        chessBoard.addMoveListener(stateChangeListener);
    }

    public void addPropertiesStatsChangeListener(StateChangeListener<PropertiesStatsChange> listener){
        propertiesStatsChangeListenerList.add(listener);
    }

    public void addTurnEndListener(StateChangeListener<GameStateChanges> listener){
        turnEndListenerList.add(listener);
    }

    public void addUndoTurnListener(StateChangeListener<GameStateChanges> listener){
        undoTurnListenerList.add(listener);
    }
    //endregion

    //region CONFIGS+DEBUG
    public void updateConfigurations(){
        if (chessBoard instanceof  ChessBoardUI){
            if (configurations.isEnableBoardGraphic())
                ((ChessBoardUI) chessBoard).enableGraphic();
            else
                ((ChessBoardUI) chessBoard).disableGraphic();
        }

        if (configurations.isDebugMode()){
            chessBoard.enableDebugMode();
        }else{
            chessBoard.disableDebugMode();
        }
    }
    @SuppressWarnings("unused")
    public void changeConfigurations(Configurations configurations){
        this.configurations = configurations;
        updateConfigurations();
    }

    public boolean isDebuggable(){
        return configurations.isDebugMode();
    }
    //endregion
}

