package ChessLogic;

import ChessLogic.Configurations.Configurations;
import ChessLogic.Configurations.PropertiesStats;
import ChessLogic.Debug.DebugMode;
import ChessLogic.Debug.Debuggable;
import ChessResources.BitMasks;
import ChessResources.ChessBoard.ChessBoard;

import ChessResources.ChessErrors.OutOfOldTurns;

import ChessResources.ChessHistoryTracker.BoardStateChanges.BoardStateChange;
import ChessResources.ChessHistoryTracker.ChessHistoryTracker;
import ChessResources.ChessListener.StateChangeListener;
import ChessResources.GetMovesLogic.ChessMove;
import ChessResources.Hasher.HashContainer;
import ChessResources.Hasher.HashGenerator;
import ChessResources.Pieces.MovesGeneration;
import ChessResources.Pieces.PieceData;
import ChessResources.GetMovesLogic.PossibleMoves;
import ChessResources.PreCalc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiFunction;

public class MinimalChessGame implements Debuggable {
    //region BEFORE_CONSTRUCTORS
    //region GAME_DATAS
    public ChessBoard chessBoard;
    protected HashGenerator hashGenerator;
    HashContainer hs;

    protected PossibleMoves possibleMoves;

    public ChessHistoryTracker<MinimalChessGame> chessHistoryTracker = new ChessHistoryTracker<>();

    Configurations configurations;
    //endregion

    //region GAME_PROPERTIES
    PropertiesStats gameProperties = new PropertiesStats();

    private int currEnPassantTarget = INVALID_ENPASSANT_TARGET;

    public static final int INVALID_ENPASSANT_TARGET = -1;

    //endregion
    //region ENDGAME_OPCODE
    private int endGameCode = INDETERMINATE;
    public static final int WHITE_WON = 0;
    public static final int BLACK_WON = 1;

    public static final int DRAW = 2;
    public static final int INDETERMINATE = 3;
    //endregion

    //region CASTLING STATICS
    public static final int BLACK_CASTLE_QUEEN_ROOK_ID = 0;
    public static final int BLACK_CASTLE_QUEEN_ROOK_ARRIVE = 3;

    public static final int WHITE_CASTLE_QUEEN_ROOK_ID = 56;
    public static final int WHITE_CASTLE_QUEEN_ROOK_ARRIVE = 59;

    public static final int BLACK_CASTLE_KING_ROOK_ID = 7;
    public static final int BLACK_CASTLE_KING_ROOK_ARRIVE = 5;

    public static final int WHITE_CASTLE_KING_ROOK_ID = 63;
    public static final int WHITE_CASTLE_KING_ROOK_ARRIVE = 61;
    //endregion

    //region GAME_BITMASKS
    //endregion

    //region LAMBDAS
    //endregion

    //endregion

    //region Constructor
    public MinimalChessGame(){}
    public MinimalChessGame(MinimalChessGame src) {
        this.configurations = src.configurations;
        this.hashGenerator = src.hashGenerator;
        this.hs = new HashContainer(src.hs.getHash());
        // Deep copies
        this.gameProperties = new PropertiesStats(src.gameProperties);

        this.currEnPassantTarget = src.currEnPassantTarget;
        this.endGameCode = src.endGameCode;

        // ChessBoard must define its own clone / copy
        this.chessBoard = src.chessBoard.cloneBoard();

        // Possible moves snapshot
        this.possibleMoves = new PossibleMoves(src.possibleMoves);

    }


    public MinimalChessGame(ChessBoard chessBoard,
                            Configurations configurations)
    {
        this.hashGenerator = new HashGenerator( this);

        this.configurations = configurations;

        this.chessBoard = chessBoard;
//
//        this.chessBoard.addStateChangeListener(KING_SPACEID_LOGGER);

        possibleMoves = new PossibleMoves(this);
        calculateHash();
        chessHistoryTracker.pushTurn(this);
    }

    public MinimalChessGame(ChessBoard chessBoard,
                            PropertiesStats gameProperties, Configurations configurations)
    {
        this(chessBoard, configurations);

        this.gameProperties = gameProperties;

        updateConfigurations();
        generateBitMasks();
        generatePossibleMoves();
        calculateHash();
        chessHistoryTracker.pushTurn(this);
    }
    @SuppressWarnings("unused")
    public MinimalChessGame(String fen, ChessBoard chessBoard,
                            Configurations configurations)
    {

        this(chessBoard, configurations);

        updateConfigurations();

        fenTranslator(fen);
        generateBitMasks();
        generatePossibleMoves();
        calculateHash();
        chessHistoryTracker.pushTurn(this);
    }
    //endregion

    //region GETTERS
    public ChessBoard getBoard(){
        return chessBoard;
    }

    public PropertiesStats getGameProperties() {
        return gameProperties;
    }

    public PossibleMoves getPossibleMoves(){
        return possibleMoves;
    }

    public boolean getCurrentColorToMove(){
        return gameProperties.getSideToMove();
    }
    //endregion

    //region HELPERS

    //region MISC_FUNCS

    public boolean canBlackCastleKing(){
        return gameProperties.canBlackCastleKing();
    }

    public boolean canWhiteCastleKing(){
        return gameProperties.canWhiteCastleKing();
    }

    public boolean canBlackCastleQueen(){
        return gameProperties.canBlackCastleQueen();
    }

    public boolean canWhiteCastleQueen(){
        return gameProperties.canWhiteCastleQueen();
    }

    public boolean canToMoveCastleKing(){
        if (getCurrentColorToMove() == PieceData.WHITE){
            return canWhiteCastleKing();
        }else{
            return canBlackCastleKing();
        }
    }

    public boolean canToMoveCastleQueen(){
        if (getCurrentColorToMove() == PieceData.WHITE){
            return canWhiteCastleQueen();
        }else{
            return canBlackCastleQueen();
        }
    }

    public void fenTranslator(String fen){
        String[] args = fen.trim().split(" ");
        chessBoard.setUpPieces(args[0]);
        if (args.length != 6) {
            throw new IllegalArgumentException("Invalid FEN: Must include 6 arguments.");
        }
        //region ASSIGN_SIDE_TO_MOVE
        if (args[1].equals("w")) {
            if(gameProperties.getSideToMove() != PieceData.WHITE) gameProperties.flipSideToMove();
        }
        else if (args[1].equals("b")) {
            if(gameProperties.getSideToMove() != PieceData.BLACK) gameProperties.flipSideToMove();
        }
        else
            throw new IllegalArgumentException("Invalid FEN: Side to Move is invalid (" + args[1] + ").");
        //endregion
        //region ASSIGN_CASTLING_RIGHTS
        long castlingRight = 0L;
        if (args[2].contains("k")) castlingRight |= PropertiesStats.BLACK_CASTLE_KING_SIDE;
        if (args[2].contains("q")) castlingRight |= PropertiesStats.BLACK_CASTLE_QUEEN_SIDE;
        if (args[2].contains("K")) castlingRight |= PropertiesStats.WHITE_CASTLE_KING_SIDE;
        if (args[2].contains("Q")) castlingRight |= PropertiesStats.WHITE_CASTLE_QUEEN_SIDE;

        gameProperties.setCastlingRight(castlingRight);
        //endregion
        //region ASSIGN_EN_PASSANT_TARGET
        if (args[3].length() == 1 && args[3].equals("-")) {
            gameProperties.clearEnPassantTarget();
        } else if (args[3].length() == 2 && Character.isAlphabetic(args[3].charAt(0))
                && Character.isDigit(args[3].charAt(1))) {
            gameProperties.setEnPassantTarget(ChessBoard.convertSquareNotationToSpaceId(
                    args[3].charAt(0), args[3].charAt(1)));
        } else
            throw new IllegalArgumentException("Invalid files and ranks input (" + args[3] + ").");
        //endregion
        gameProperties.setHalfMoves(Integer.parseInt(args[4]));
        gameProperties.setTotalMovesElapsed(Integer.parseInt(args[5]));
    }

    public void generatePossibleMoves(){
        possibleMoves.clearPossibleMoves();
        MovesGeneration.generateMoves(this);
    }

    public boolean spaceUnderThreat(int spaceId, short[] ids, long OCCUPIED, long setMoves)
    {
        //technically dont apply to all spaces. treat king as if it doesnt exist.
        if (ids[0] == PieceData.BPAWN) {
            if ((BitMasks.PAWN_CAPTURE_MASKS[BitMasks.WIDX][spaceId]
                    & chessBoard.getBitBoard(ids[0])) != 0) {
                return true;
            }
        }
        else {
            if ((BitMasks.PAWN_CAPTURE_MASKS[BitMasks.BIDX][spaceId]
                    & chessBoard.getBitBoard(ids[0]) & setMoves) != 0) {
                return true;
            }
        }

        if ((BitMasks.KNIGHT_MOVE_MASKS[spaceId]
                & chessBoard.getBitBoard(ids[PieceData.KNIGHT]) & setMoves) != 0 )
            return true;

        if ((BitMasks.KING_MOVE_MASKS[spaceId]
                & chessBoard.getBitBoard(ids[PieceData.KING]) & setMoves) != 0)
            return true;

        return spaceUnderThreatSlidingPiece(spaceId, OCCUPIED, ids, setMoves);
    }

    public boolean spaceUnderThreatSlidingPiece(int spaceId, long OCCUPIED, short[] ids,
                                                long setMoves)
    {
        //technically dont apply to all spaces. treat king as if it doesnt exist.
        //short[] ids = ((alliedColor == PieceData.WHITE) ? PreCalc.WHITE_THREAT_IDS : PreCalc.BLACK_THREAT_IDS);

        if ((MovesGeneration.getRookMoves(OCCUPIED, spaceId)
                & chessBoard.getBitBoard(ids[PieceData.ROOK]) & setMoves) != 0)
            return true;

        if (((MovesGeneration.getBishopMoves(OCCUPIED, spaceId)
                & chessBoard.getBitBoard(ids[PieceData.BISHOP])) & setMoves) != 0)
            return true;

        return ((MovesGeneration.getQueenMoves(OCCUPIED, spaceId)
                & chessBoard.getBitBoard(ids[PieceData.QUEEN]) & setMoves) != 0);
    }

    public boolean spaceUnderThreat(int spaceId, long OCCUPIED, long setMoves){
        return spaceUnderThreat(spaceId, getBoard().TO_MOVE_THREATS_PIECE_ID, OCCUPIED, setMoves);
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
                Long.numberOfTrailingZeros(getBoard().getBitBoard(PieceData.WKING)) :
                Long.numberOfTrailingZeros(getBoard().getBitBoard(PieceData.BKING));
    }

    public int getKingToMoveSpaceId(){
        return getKingSpaceId(getCurrentColorToMove());
    }

    public int getEnpassantTarget(){
        return gameProperties.getEnPassantTarget();
    }

    public void pushCurrEnPassantTarget(){
        gameProperties.setEnPassantTarget(currEnPassantTarget);
        currEnPassantTarget = INVALID_ENPASSANT_TARGET;
    }

    private void promotionHandler(int spaceId, short piece)
    {
        chessBoard.spawnPieceAt(spaceId, piece, getBoard().getPiece(spaceId));
    }

    private void resetHalfMoves(){
        gameProperties.resetHalfMoves();
    }

    public PossibleMoves getCurrentPossibleMoves(){
        return getPossibleMoves();
    }
    //endregion

    //region TURN_HANDLING

    public boolean movePiece(ChessMove move){
        if (move == null) return false;

        DebugMode.debugPrint(this, move);

        //region CASTLING_LOGIC
        short piece = move.getPieceId();
        //endregion

        //region ACTUALLY_MOVING_THE_PIECE
        if (piece == PieceData.WPAWN || piece == PieceData.BPAWN){
            resetHalfMoves();
        }
        else if (move.isCapture()){
            resetHalfMoves();
        }

        if (gameProperties.canWhiteCastle()) {
            if (piece == PieceData.WKING) {
                gameProperties.revokeCastlingRight(PropertiesStats.WHITE_CASTLE_QUEEN_SIDE
                        |PropertiesStats.WHITE_CASTLE_KING_SIDE);
            }else if (piece == PieceData.WROOK){
                if (ChessBoard.getCol(move.getSpaceIdToMove()) < ChessBoard.getCol(getKingToMoveSpaceId())){
                    gameProperties.revokeCastlingRight(PropertiesStats.WHITE_CASTLE_QUEEN_SIDE);
                }else{
                    gameProperties.revokeCastlingRight(PropertiesStats.WHITE_CASTLE_KING_SIDE);
                }
            }
        }

        if (gameProperties.canBlackCastle()){
            if (piece == PieceData.BKING) {
                gameProperties.revokeCastlingRight(PropertiesStats.BLACK_CASTLE_QUEEN_SIDE
                        |PropertiesStats.BLACK_CASTLE_KING_SIDE);
            }else if (piece == PieceData.BROOK){
                if (ChessBoard.getCol(move.getSpaceIdToMove()) < ChessBoard.getCol(getKingToMoveSpaceId())){
                    gameProperties.revokeCastlingRight(PropertiesStats.BLACK_CASTLE_QUEEN_SIDE);
                }else{
                    gameProperties.revokeCastlingRight(PropertiesStats.BLACK_CASTLE_KING_SIDE);
                }
            }
        }

        if (move.isCastling()){
            if (move.isBlackCastleQueen()){
                chessBoard.movePieceCapture(BLACK_CASTLE_QUEEN_ROOK_ID,
                        BLACK_CASTLE_QUEEN_ROOK_ARRIVE,
                        BLACK_CASTLE_QUEEN_ROOK_ARRIVE);
            }else if (move.isBlackCastleKing()){
                chessBoard.movePieceCapture(BLACK_CASTLE_KING_ROOK_ID,
                        BLACK_CASTLE_KING_ROOK_ARRIVE,
                        BLACK_CASTLE_KING_ROOK_ARRIVE);
            }else if (move.isWhiteCastleQueen()){
                chessBoard.movePieceCapture(WHITE_CASTLE_QUEEN_ROOK_ID,
                        WHITE_CASTLE_QUEEN_ROOK_ARRIVE,
                        WHITE_CASTLE_QUEEN_ROOK_ARRIVE);
            }else if (move.isWhiteCastleKing()){
                chessBoard.movePieceCapture(WHITE_CASTLE_KING_ROOK_ID,
                        WHITE_CASTLE_KING_ROOK_ARRIVE,
                        WHITE_CASTLE_KING_ROOK_ARRIVE);
            }
        }

        chessBoard.movePieceCapture(move.getSpaceIdToMove(),
                move.getSpaceIdArriveAt(),
                move.getSpaceIdCaptureAt());

        if (move.isPromotion()) //make no further checks, shouldnt be invalid.
        {
            promotionHandler(move.getSpaceIdArriveAt(), move.getPromotionPieceId());
        }

        if (move.isDoublePawnPush()){
            currEnPassantTarget = move.getDoublePawnPushId();
        }

        //endregion

        //region SAVE_GAME_P&P
        finishTurn();
        //endregion
        return true;
    }
    private void aSideWon() {
        if (!getPossibleMoves().isEmpty()) {

            // No checkmate/stalemate possible — check cheaper draws first
            if (gameProperties.getHalfMovesSinceCaptureOrPawnMove() >= 100) {
                endGameCode = DRAW; return;
            }
            if (chessHistoryTracker.isThreeFoldRepitionFlag()) {
                endGameCode = DRAW; return;
            }
            if (!ChessBoard.isValidSpaceId(getKingToMoveSpaceId())) {
                endGameCode = getCurrentColorToMove() == PieceData.WHITE ? WHITE_WON : BLACK_WON;
                return;
            }
            endGameCode = INDETERMINATE;
            return;
        }
        // Only call spaceNotUnderThreat if moves are exhausted (expensive)
        endGameCode = spaceUnderThreat(getKingToMoveSpaceId(), getBoard().OCCUPIED, BitMasks.ALL_ONES)
                ? (gameProperties.getSideToMove() == PieceData.BLACK ? WHITE_WON : BLACK_WON)
                : DRAW;
    }

    private void finishTurn()
    {
        DebugMode.debugPrint(this, gameProperties);
        //peek the turn just pushed.

        //region UPDATING_GAME_PROPERTIES_AND STATS
        gameProperties.incrementHalfMoves();
        gameProperties.flipSideToMove();
        pushCurrEnPassantTarget();
        //endregion
        if (isDebuggable()) DebugMode.debugPrint(this, "Finish turn + side: " +
                (gameProperties.getSideToMove() == ChessBoard.WHITE ?"white" : "black" ));

        generateBitMasks();

        //region GENERATE_POSSIBLE_MOVES
        //generate possible moves after changing side.
        // Doing it this way ensure when a new moves is generated, new piece location information is taken into account
        generatePossibleMoves();
        //endregion
//        StateChangeListener.<PropertiesStatsChange>notifyListeners(propertiesStatsChangeListenerList,         new PropertiesStatsChange(gameProperties, gameStats));
        calculateHash();
        chessHistoryTracker.pushTurn(this);

        tryEndGame();

//        long tm = getThreatMap(getKingSpaceId(getCurrentColorToMove()), getCurrentColorToMove());
//        BitMasks.printBitBoard(tm);
    }

    public void generateBitMasks(){
        getBoard().generateBitMasks(getCurrentColorToMove(), getEnpassantTarget());
    }
    private void tryEndGame(){
        aSideWon();
        if (endGameCode != INDETERMINATE)
            endGame();
    }

    private void endGame() {
        if (!configurations.isAllowGameEnd()) return; //game cant end. assume is a simulation.
//
//        if (endGameCode == WHITE_WON) {
//            System.out.println("WHITE WON");
//        } else if (endGameCode == BLACK_WON) {
//            System.out.println("BLACK WON");
//        } else if (endGameCode == DRAW) {
//            System.out.println("DRAW");
//        } else {
//            System.out.println("NO REASON. ");
//        }
//        if (isDebuggable()) DebugMode.debugPrint(this, chessHistoryTracker);
        //System.exit(0);
    }

    public void undoTurn() throws OutOfOldTurns, NullPointerException
    {
        if (chessHistoryTracker == null) throw new NullPointerException("chessHistoryTracker is null!");

        if(chessHistoryTracker.isEmpty()) throw new OutOfOldTurns("");

        MinimalChessGame gameStateChanges = chessHistoryTracker.popTurn();

        setGame(gameStateChanges);

    }

    protected void setGame(MinimalChessGame src){
//        this.chessHistoryTracker; //dont copy this
        this.configurations = src.configurations;
        this.hashGenerator = src.hashGenerator;
        this.hs = new HashContainer(src.hs.getHash());
        // Deep copies
        this.gameProperties = src.gameProperties.getCopy();
        this.currEnPassantTarget = src.currEnPassantTarget;
        this.endGameCode = src.endGameCode;

        // ChessBoard must define its own clone / copy
        this.chessBoard = src.chessBoard.cloneBoard();

        // Possible moves snapshot
        this.possibleMoves = new PossibleMoves(src.possibleMoves);

    }

    protected void setGameNonCopy(MinimalChessGame src){
        this.configurations = src.configurations;
        this.hashGenerator = src.hashGenerator;
        this.hs = src.hs;
        this.gameProperties = src.gameProperties.getCopy();

        this.currEnPassantTarget = src.currEnPassantTarget;
        this.endGameCode = src.endGameCode;

        this.chessBoard = src.chessBoard;

        // Possible moves snapshot
        this.possibleMoves = src.possibleMoves;
    }
    //endregion

    //endregion

    //region LISTENERS
    public void addMoveListener(StateChangeListener<BoardStateChange> stateChangeListener)
    {
        chessBoard.addMoveListener(stateChangeListener);
    }

    //endregion

    //region CONFIGS+DEBUG
    public void updateConfigurations(){

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

    public HashContainer getHashOfPosition(){
        return hs;
    }

    public void calculateHash(){
        hs = hashGenerator.getCurrentHash();
    }

    public MinimalChessGame cloneGame(){
        MinimalChessGame game = new MinimalChessGame();
        game.setGame(this);
        return game;
    }
}

