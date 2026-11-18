package ChessLogic.Evaluation;

import ChessLogic.MinimalChessGame;
import ChessResources.ChessListener.StateChangeListener;
import ChessResources.GetMovesLogic.ChessMove;
import ChessResources.Pieces.PieceData;
import Engine.Engine;
import Engine.MyEngine;

import java.io.IOException;

public class Evaluation {
    private static final Engine BACKUP_ENGINE = new MyEngine(4);

    MinimalChessGame game;
    Engine engine = BACKUP_ENGINE;

    private boolean sideToMove = PieceData.WHITE;
    private boolean isEvaluationOn = false;

    private final StateChangeListener<MinimalChessGame> evaluator =
            (MinimalChessGame game)->{
                ChessMove bestMove = null;
                if (game != null && isEvaluationOn &&
                        game.getCurrentColorToMove() == sideToMove &&
                        game.sideWon() == MinimalChessGame.INDETERMINATE) {
                    try {
                        bestMove = engine.bestMove(game);
                    } catch (IOException e) {

                    }
                }
            };


    public void turnEvaluatorOnOff(boolean adjustment){
        isEvaluationOn = adjustment;
    }

    public void attach(MinimalChessGame game, boolean isEvaluationOn) {
        turnEvaluatorOnOff(isEvaluationOn);
        changeCurrentGame(game);
    }

    public void changeSideToMove(boolean newSideToMove){
        this.sideToMove = newSideToMove;
    }

    public void addNewEngine(Engine engine){
        // stop the previous external engine process (the built-in one has nothing to close)
        if (this.engine != engine && this.engine instanceof AutoCloseable old) {
            try { old.close(); } catch (Exception ignored) {}
        }
        this.engine = engine;
    }

    public void useDefaultEngine(){
        addNewEngine(BACKUP_ENGINE);
    }

    public boolean evaluateGame() throws IOException {
        if (game == null || !isEvaluationOn
                || game.getCurrentColorToMove() != sideToMove
                || game.sideWon() != MinimalChessGame.INDETERMINATE) {
            return false;   // nothing to do
        }

        ChessMove move = engine.bestMove(game);
        if (move == null) throw new IllegalStateException("Engine did not return a move");
        if (!game.movePiece(move)) throw new IllegalStateException("Engine move could not be applied: " + move.toUciMove());
        return true;
    }

    private ChessMove bestMove() throws IOException {
        return engine.bestMove(game);
    }

    public void changeCurrentGame(MinimalChessGame game){
        this.game = game;
    }

    public Engine getEngine(){
        return engine;
    }
}