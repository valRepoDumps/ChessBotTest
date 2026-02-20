//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import ChessGUI.*;
import ChessGUI.ChessGUI.*;
import ChessLogic.Configurations.Configurations;
import ChessLogic.Debug.Tests;
import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.Pieces.IrregularPieceData;
import ChessResources.Pieces.PieceData;

void main() {
//    long start = System.nanoTime();
//    ChessGUI gui = new ChessGUI();
//
////    int num = gui.test(2);
////    long end = System.nanoTime();
////    double durationMs = (end-start) / 1_000_000.0;
////    System.out.println("Num Pos: " +num + " Time: "+ durationMs + " ms");
//
//    //3-1020 ms
//    //2-460 ms

    long start = System.nanoTime();
    MinimalChessGame<ChessBoard> game = new MinimalChessGame<ChessBoard>(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            new ChessBoard(), MinimalChessGame.DEFAULT_PROMOTION_FUNC,
            new Configurations(true, true, false, true));

    int num = Tests.moveGenerationTest(4, game);

    long end = System.nanoTime();
    double durationMs = (end-start) / 1_000_000.0;
    System.out.println("Num Pos: " + num);
    System.out.println(" Time: "+ durationMs + " ms");
    start = System.nanoTime();
    game.generatePossibleMoves();
    end = System.nanoTime();
    durationMs = (end-start) / 1_000_000.0;
    System.out.println(" Time for gen moves: "+ durationMs + " ms");
    //3-8902-220 ms - 1000 ms if strict
    //4-197702-950 ms - 17000 ms if strict
}
