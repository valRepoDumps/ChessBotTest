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

import Evaluation.Evaluation;

import java.util.ArrayList;
import java.util.List;


public class MinimalChessGame implements Debuggable {
    public ChessBoard chessBoard;
    public ChessHistoryTracker<MinimalChessGame> chessHistoryTracker = new ChessHistoryTracker<>();

    protected HashGenerator hashGenerator;
    protected PossibleMoves possibleMoves;

    Configurations configurations;
    HashContainer hs;

    protected ArrayList<StateChangeListener<MinimalChessGame>> endTurnListeners = new ArrayList<>();

    PropertiesStats gameProperties = new PropertiesStats();

    private int currEnPassantTarget = INVALID_ENPASSANT_TARGET;

    private int endGameCode = INDETERMINATE;
    public static final int WHITE_WON = 0;
    public static final int BLACK_WON = 1;
    public static final int DRAW = 2;
    public static final int INDETERMINATE = 3;

    public static final int INVALID_ENPASSANT_TARGET = -1;

    public static final int BLACK_CASTLE_QUEEN_ROOK_ID = 0;
    public static final int BLACK_CASTLE_QUEEN_ROOK_ARRIVE = 3;

    public static final int WHITE_CASTLE_QUEEN_ROOK_ID = 56;
    public static final int WHITE_CASTLE_QUEEN_ROOK_ARRIVE = 59;

    public static final int BLACK_CASTLE_KING_ROOK_ID = 7;
    public static final int BLACK_CASTLE_KING_ROOK_ARRIVE = 5;

    public static final int WHITE_CASTLE_KING_ROOK_ID = 63;
    public static final int WHITE_CASTLE_KING_ROOK_ARRIVE = 61;

    public MinimalChessGame(){}

//    public MinimalChessGame(ChessBoard chessBoard, Configurations configurations) {
//        this.hashGenerator = new HashGenerator(this);
//        this.configurations = configurations;
//        this.chessBoard = chessBoard;
//        possibleMoves = new PossibleMoves(this);
//        calculateHash();
//    }

//    public MinimalChessGame(ChessBoard chessBoard, PropertiesStats gameProperties, Configurations configurations) {
//        this(chessBoard, configurations);
//        this.gameProperties = gameProperties;
//        updateConfigurations();
//        generateBitMasks();
//        generatePossibleMoves();
//        calculateHash();
//        chessHistoryTracker.pushTurn(cloneMinimalGame());
//    }

    public MinimalChessGame(String fen, ChessBoard chessBoard, Configurations configurations) {
        this.hashGenerator = new HashGenerator(this);
        this.configurations = configurations;
        this.chessBoard = chessBoard;
        possibleMoves = new PossibleMoves(this);
        calculateHash();

        updateConfigurations();
        fenTranslator(fen);
        generateBitMasks();
        generatePossibleMoves();
        calculateHash();
        chessHistoryTracker.pushTurn(cloneMinimalGame());
        StateChangeListener.notifyListeners(endTurnListeners, this);

    }

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
        if (args[1].equals("w")) {
            if(gameProperties.getSideToMove() != PieceData.WHITE) gameProperties.flipSideToMove();
        }
        else if (args[1].equals("b")) {
            if(gameProperties.getSideToMove() != PieceData.BLACK) gameProperties.flipSideToMove();
        }
        else
            throw new IllegalArgumentException("Invalid FEN: Side to Move is invalid (" + args[1] + ").");

        long castlingRight = 0L;
        if (args[2].contains("k")) castlingRight |= PropertiesStats.BLACK_CASTLE_KING_SIDE;
        if (args[2].contains("q")) castlingRight |= PropertiesStats.BLACK_CASTLE_QUEEN_SIDE;
        if (args[2].contains("K")) castlingRight |= PropertiesStats.WHITE_CASTLE_KING_SIDE;
        if (args[2].contains("Q")) castlingRight |= PropertiesStats.WHITE_CASTLE_QUEEN_SIDE;

        gameProperties.setCastlingRight(castlingRight);

        if (args[3].length() == 1 && args[3].equals("-")) {
            gameProperties.clearEnPassantTarget();
        } else if (args[3].length() == 2 && Character.isAlphabetic(args[3].charAt(0))
                && Character.isDigit(args[3].charAt(1))) {
            gameProperties.setEnPassantTarget(ChessBoard.convertSquareNotationToSpaceId(
                    args[3].charAt(0), args[3].charAt(1)));
        } else
            throw new IllegalArgumentException("Invalid files and ranks input (" + args[3] + ").");

        gameProperties.setHalfMoves(Integer.parseInt(args[4]));
        gameProperties.setTotalMovesElapsed(Integer.parseInt(args[5]));
    }

    public void generatePossibleMoves(){
        possibleMoves.clearPossibleMoves();
        MovesGeneration.generateMoves(this);
    }

    public boolean spaceUnderThreat(int spaceId, short[] ids, long OCCUPIED, long setMoves) {
        if (!ChessBoard.isValidSpaceId(spaceId)) return false;

        if (ids[0] == PieceData.BPAWN) {
            if ((BitMasks.PAWN_CAPTURE_MASKS[BitMasks.WIDX][spaceId]
                    & chessBoard.getBitBoard(ids[0])&setMoves ) != 0) {
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

    public boolean spaceUnderThreatSlidingPiece(int spaceId, long OCCUPIED, short[] ids, long setMoves) {
        if (!ChessBoard.isValidSpaceId(spaceId)) return false;

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
        if (!ChessBoard.isValidSpaceId(spaceId)) return false;
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

    private void promotionHandler(int spaceId, short piece) {
        chessBoard.spawnPieceAt(spaceId, piece, getBoard().getPiece(spaceId));
    }

    private void resetHalfMoves(){
        gameProperties.resetHalfMoves();
    }

    public PossibleMoves getCurrentPossibleMoves(){
        return getPossibleMoves();
    }

    public boolean movePiece(ChessMove move){
        if (move == null) return false;

        DebugMode.debugPrint(this, move);
        short piece = move.getPieceId();

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

            if (move.isCapture()) {
                int captureSquare = move.getSpaceIdCaptureAt();
                if (captureSquare == WHITE_CASTLE_QUEEN_ROOK_ID)
                    gameProperties.revokeCastlingRight(PropertiesStats.WHITE_CASTLE_QUEEN_SIDE);
                else if (captureSquare == WHITE_CASTLE_KING_ROOK_ID)
                    gameProperties.revokeCastlingRight(PropertiesStats.WHITE_CASTLE_KING_SIDE);
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

            if (move.isCapture()) {
                int captureSquare = move.getSpaceIdCaptureAt();
                if (captureSquare == BLACK_CASTLE_QUEEN_ROOK_ID)
                    gameProperties.revokeCastlingRight(PropertiesStats.BLACK_CASTLE_QUEEN_SIDE);
                else if (captureSquare == BLACK_CASTLE_KING_ROOK_ID)
                    gameProperties.revokeCastlingRight(PropertiesStats.BLACK_CASTLE_KING_SIDE);
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

        if (move.isPromotion()) {
            promotionHandler(move.getSpaceIdArriveAt(), move.getPromotionPieceId());
        }

        if (move.isDoublePawnPush()){
            currEnPassantTarget = move.getDoublePawnPushId();
        }

        finishTurn();
        DebugMode.debugPrint(this, "Evaluation "+ Evaluation.evaluateGame(this));
        return true;
    }

    private void aSideWon() {
        if (!getPossibleMoves().isEmpty()) {
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
        endGameCode = spaceUnderThreat(getKingToMoveSpaceId(), getBoard().OCCUPIED, BitMasks.ALL_ONES)
                ? (gameProperties.getSideToMove() == PieceData.BLACK ? WHITE_WON : BLACK_WON)
                : DRAW;
    }

    private void finishTurn() {
        if (gameProperties.getSideToMove() == PieceData.BLACK){
            gameProperties.incrementTotalMoves();
        }
        gameProperties.incrementHalfMoves();
        gameProperties.flipSideToMove();
        pushCurrEnPassantTarget();

        if (isDebuggable()) DebugMode.debugPrint(this, "Finish turn + side: " +
                (gameProperties.getSideToMove() == ChessBoard.WHITE ?"white" : "black" ));

        generateBitMasks();
        generatePossibleMoves();
        calculateHash();
        chessHistoryTracker.pushTurn(cloneGame());

        tryEndGame();

        StateChangeListener.notifyListeners(endTurnListeners, this);
        DebugMode.debugPrint(this, "Current game properties: " + gameProperties);
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
        if (!configurations.isAllowGameEnd()) return;

        System.out.println(this);
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
    }

    public void undoTurn() throws OutOfOldTurns, NullPointerException {
        if (chessHistoryTracker == null) throw new NullPointerException("chessHistoryTracker is null!");
        if (chessHistoryTracker.isEmpty()) throw new OutOfOldTurns("No past turns available to undo.");

        MinimalChessGame gameStateChanges = chessHistoryTracker.popTurn();
        setGame(gameStateChanges);
    }

    protected void setGame(MinimalChessGame src){
        if (src == null) {
            throw new IllegalArgumentException("Source MinimalChessGame cannot be null");
        }

        this.configurations = new Configurations(src.configurations);
        this.hashGenerator = src.hashGenerator;
        this.hs = new HashContainer(src.hs.getHash());
        this.gameProperties = src.gameProperties.getCopy();
        this.currEnPassantTarget = src.currEnPassantTarget;
        this.endGameCode = src.endGameCode;

        this.chessBoard = src.chessBoard.cloneBoard();
        this.possibleMoves = new PossibleMoves(src.possibleMoves);
    }

    public void updateConfigurations(){
        if (configurations.isDebugMode()){
            chessBoard.enableDebugMode();
        }else{
            chessBoard.disableDebugMode();
        }
    }

    public void changeConfigurations(Configurations configurations){
        this.configurations = configurations;
        updateConfigurations();
    }

    public boolean isDebuggable(){
        return configurations.isDebugMode();
    }

    public HashContainer getHashOfPosition(){
        return hs;
    }

    public void calculateHash(){
        hs = hashGenerator.getCurrentHash();
    }

    public MinimalChessGame cloneGame(){
        return this.cloneMinimalGame();
    }

    public MinimalChessGame cloneMinimalGame(){
        MinimalChessGame game = new MinimalChessGame();
        game.setGame(this);
        game.chessHistoryTracker.pushTurn(game.cloneHistorylessGame());
        return game;
    }

    public MinimalChessGame cloneHistorylessGame(){
        MinimalChessGame game = new MinimalChessGame();
        game.setGame(this);
        return game;
    }
    public int sideWon(){
        return endGameCode;
    }

    public void addTurnEndListener(StateChangeListener<MinimalChessGame> listener){
        endTurnListeners.add(listener);//immediately notify listener of latest news.
        StateChangeListener.notifyListeners(listener, this);
    }

    public Configurations getConfigurations(){
        return configurations;
    }
}
