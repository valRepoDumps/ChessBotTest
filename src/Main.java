//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import ChessGUI.ChessGUI;
import ChessLogic.Configurations.Configurations;
import ChessLogic.Debug.Tests;
import ChessLogic.MinimalChessGame;
import ChessResources.BitMasks;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.Pieces.PieceData;
import Evaluation.Evaluation;


void main() {
    ChessGUI gui = new ChessGUI("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    Evaluation evaluator = new Evaluation(3);
    evaluator.registerBlackEvaluator(gui.chessGame);

    //3-8902/ 377 -300 - 191 - 130 - 65 - 46 - 39
    //4-197281/ 2798 - 1600 - 2500 - 1500 - 700 - 470
    //5-4865609/ 60000 - 33000 - 30000 - 21300 - 8000 - 7600 - 5600 - 4950
    //6-119060324/862469ms -129270
}
