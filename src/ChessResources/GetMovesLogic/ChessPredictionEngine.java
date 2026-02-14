package ChessResources.GetMovesLogic;

import ChessLogic.Configurations.Configurations;
import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.ChessHistoryTracker.BoardStateChanges.BoardStateChange;
import ChessResources.ChessHistoryTracker.BoardStateChanges.PropertiesStatsChange;
import ChessResources.ChessHistoryTracker.GameStateChanges;
import ChessResources.ChessListener.StateChangeListener;
import ChessResources.Pieces.PieceData;

import javax.swing.*;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;

public class ChessPredictionEngine<T extends ChessBoard, U extends MinimalChessGame<T>> {
    MinimalChessGame<ChessBoard> simGame;
    U currentGame;
    public static final int ROOK_CHOICE = 1;
    public static final int BISHOP_CHOICE = 2;
    public static final int KNIGHT_CHOICE = 3;
    public static final int QUEEN_CHOICE = 4;

    private int currentChoice = ROOK_CHOICE;

    public final StateChangeListener<GameStateChanges> TURN_END_LOGGER =
            this::advanceSimulation;

    public final StateChangeListener<GameStateChanges> UNDO_TURN_LOGGER =
            (gameStateChanges)->{
               this.undoSimulateMove();
            };

    BiFunction<Integer, Boolean, Short> choosePiecePromotion =
            (Integer spaceId, Boolean color) ->    {
                String[] options = {"Queen", "Rook", "Bishop", "Knight"};

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

    public ChessPredictionEngine(U currentGame){
        this.currentGame = currentGame;
        this.currentGame.addTurnEndListener(TURN_END_LOGGER);
        this.currentGame.addUndoTurnListener(UNDO_TURN_LOGGER);
        simGame = cloneGame(currentGame);
    }

    public MinimalChessGame<ChessBoard> cloneGame(U game){
        ChessBoard boardClone = ChessBoard.cloneBoard(game.chessBoard);

        return new MinimalChessGame<ChessBoard>(boardClone, choosePiecePromotion,
                game.gameProperties, game.gameStats,
                Configurations.createCloneGameConfig());
    }

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
        //simulateMove(spaceId, spaceIdArriveAt);
        //simGame.chessHistoryTracker.pushTurn();
        boolean ans = simGame.spaceNotUnderThreat(simGame.getKingSpaceId(color), color);
        undoSimulateMove();
        return !ans;
    }
}
