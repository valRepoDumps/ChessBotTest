package ChessResources.GetMovesLogic;

import ChessLogic.Configurations.Configurations;
import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.ChessHistoryTracker.GameStateChanges;
import ChessResources.ChessListener.StateChangeListener;
import ChessResources.Pieces.PieceData;

import javax.swing.*;
import java.util.function.BiFunction;

public class ChessPredictionEngine<T extends ChessBoard, U extends MinimalChessGame<T>> {
    //region PRE_CONSTRUCTOR
    MinimalChessGame<ChessBoard> simGame;
    U currentGame;
    //region CHOICES
    @SuppressWarnings("unused")
    public static final int ROOK_CHOICE = 1;
    @SuppressWarnings("unused")
    public static final int BISHOP_CHOICE = 2;
    @SuppressWarnings("unused")
    public static final int KNIGHT_CHOICE = 3;
    @SuppressWarnings("unused")
    public static final int QUEEN_CHOICE = 4;
    //endregion

    private int currentChoice = ROOK_CHOICE;

    public final StateChangeListener<GameStateChanges> TURN_END_LOGGER =
            this::advanceSimulation;

    public final StateChangeListener<GameStateChanges> UNDO_TURN_LOGGER =
            (_)-> this.undoSimulateMove();

    BiFunction<Integer, Boolean, Short> choosePiecePromotion =
            (Integer _, Boolean color) ->    {

                int choice = currentChoice;

                // If closed or invalid → default to queen
                if (color == PieceData.WHITE)
                {
                    return switch (choice) {
                        case 1 -> PieceData.WROOK;
                        case 2 -> PieceData.WBISHOP;
                        case 3 -> PieceData.WKNIGHT;
                        default -> PieceData.WQUEEN;
                    };
                }
                else
                {
                    return switch (choice) {
                        case 1 -> PieceData.BROOK;
                        case 2 -> PieceData.BBISHOP;
                        case 3 -> PieceData.BKNIGHT;
                        default -> PieceData.BQUEEN;
                    };
                }
            };
    //endregion

    public ChessPredictionEngine(U currentGame){
        this.currentGame = currentGame;
        this.currentGame.addTurnEndListener(TURN_END_LOGGER);
        this.currentGame.addUndoTurnListener(UNDO_TURN_LOGGER);
        simGame = cloneGame(currentGame);
    }

    public MinimalChessGame<ChessBoard> cloneGame(U game){
        ChessBoard boardClone = (game.chessBoard.clone());

        return new MinimalChessGame<>(boardClone, choosePiecePromotion,
                game.gameProperties, game.gameStats,
                Configurations.createCloneGameConfig());
    }

    //region HELPERS
    public void advanceSimulation(GameStateChanges gameStateChanges){
        simGame.advanceBoardState(gameStateChanges);
    }

    public boolean simulateMove(int spaceIdToMove, int spaceIdArriveAt){
        return simGame.movePiece(spaceIdToMove, spaceIdArriveAt);
    }

    public void undoSimulateMove(){
        try{simGame.undoTurn();}
        catch(Exception e){
            System.out.println(e.getMessage() +"predictionEngine");
        }
    }

    public boolean singleSimCheck(int spaceId, int spaceIdArriveAt, boolean color){
        if(!simulateMove(spaceId, spaceIdArriveAt)) {return false;}

        boolean ans = simGame.spaceNotUnderThreat(simGame.getKingSpaceId(color), color);
        undoSimulateMove();
        return !ans;
    }

    @SuppressWarnings("unused")
    public void setCurrentChoice(int choice){
        this.currentChoice = choice;
    }
    //endregion
}
