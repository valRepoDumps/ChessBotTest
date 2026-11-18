package FrontEndConnection;

import ChessLogic.Debug.Debuggable;
import ChessLogic.Evaluation.Evaluation;
import ChessLogic.MinimalChessGame;
import ChessResources.GameSaver.GameSaveManager;
import Engine.Engine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Everything an ApiRequest may need to read or change: the current game,
 * the evaluator attached to it and the save manager.
 */
public class FrontEndState implements Debuggable {
    private static final Pattern SAVE_ID_PATTERN = Pattern.compile("save_(\\d{1,9})");

    private volatile MinimalChessGame game;   // replaced by new_game / load_game
    private final Evaluation evaluation = new Evaluation();
    private final GameSaveManager saves;
    public boolean isDebuggable = true;

    public FrontEndState(MinimalChessGame game, Path saveDir) {
        this.game = game;
        this.saves = new GameSaveManager(saveDir);
        evaluation.attach(game, false);
    }

    public boolean isDebuggable() {
        return isDebuggable;
    }

    public MinimalChessGame getGame() {
        return game;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public GameSaveManager getSaves() {
        return saves;
    }

    /** Swaps in a new game and re-attaches the evaluator to it. */
    public void installGame(MinimalChessGame g) {
        this.game = g;
        evaluation.attach(g, false);
    }

    public Engine currentEngine() {
        Engine engine = evaluation.getEngine();
        if (engine == null) throw new IllegalStateException("No engine selected");
        return engine;
    }

    public void adjustEvaluator(boolean turnEvaluatorOn, boolean sideToMove) throws IOException {
        evaluation.changeSideToMove(sideToMove);
        evaluation.turnEvaluatorOnOff(turnEvaluatorOn);
        if (turnEvaluatorOn) evaluation.evaluateGame();
    }

    /** save_0001, save_0002, ... continuing from the highest existing number. */
    public String nextSaveId() throws IOException {
        int max = 0;
        for (GameSaveManager.SaveMeta m : saves.list()) {
            Matcher mt = SAVE_ID_PATTERN.matcher(m.saveId());
            if (mt.matches()) max = Math.max(max, Integer.parseInt(mt.group(1)));
        }
        return String.format("save_%04d", max + 1);
    }
}