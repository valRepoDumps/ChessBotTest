//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import ChessGUI.*;
import ChessLogic.Configurations.Configurations;
import ChessLogic.Debug.Tests;
import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;

void main() {
//    ChessGUI gui = new ChessGUI();

//    long start = System.nanoTime();
//    ChessBoard board = new ChessBoard();
//    board.setUpPieces("");
//    long end = System.nanoTime();
//    double durationMs = (end-start) / 1_000_000.0;
//    System.out.println(" Time: "+ durationMs + " ms");

//    3-1034 ms
//    2-460 ms
//
    MinimalChessGame<ChessBoard> game = new MinimalChessGame<ChessBoard>(
     "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            new ChessBoard(), MinimalChessGame.DEFAULT_PROMOTION_FUNC,
            new Configurations(true, false, true));
    long start = System.nanoTime();
    int num = Tests.moveGenerationTest(4, game);

    long end = System.nanoTime();
    double durationMs = (end-start) / 1_000_000.0;
    System.out.println("Num Pos: " + num);
    System.out.println(" Time: "+ durationMs + " ms");

    //3-8902/ 377 -300 - 191 - 130
    //4-197281/ 2798 - 1600 - 2500 - 1500 (dont create new chessspaces object every pawn cycle) -1200 (cache possible moves)
    //5-4865609/ 60000 - 33000 - 30000 - 21300
    //6-??/862469ms
}
