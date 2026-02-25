//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import ChessGUI.ChessGUI;
import ChessLogic.Configurations.Configurations;
import ChessLogic.Debug.Tests;
import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;

void main() {
//    ChessGUI gui = new ChessGUI();
//    System.out.println(Arrays.toString(gui.chessGame.getBoard().getBoardSquares()));
//    long start = System.nanoTime();
//    ChessBoard board = new ChessBoard();
//    board.setUpPieces("");
//    long end = System.nanoTime();
//    double durationMs = (end-start) / 1_000_000.0;
//    System.out.println(" Time: "+ durationMs + " ms");

//    3-1034 ms
//    2-460 ms
//
    long start = System.nanoTime();
    MinimalChessGame<ChessBoard> game = new MinimalChessGame<ChessBoard>(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            new ChessBoard(), MinimalChessGame.DEFAULT_PROMOTION_FUNC,
            new Configurations(true, false, true));

    int num = Tests.moveGenerationTest(4, game);

    long end = System.nanoTime();
    double durationMs = (end-start) / 1_000_000.0;
    System.out.println("Num Pos: " + num);
    System.out.println(" Time: "+ durationMs + " ms");

//    Scanner s = new Scanner(System.in);
//    start = System.nanoTime();
//    game.generatePossibleMoves();
//    end = System.nanoTime();
//    durationMs = (end-start) / 1_000_000.0;
//    System.out.println(" Time to get moves: "+ durationMs + " ms");
//    System.out.println("Expected time: " + 8902*durationMs);
    //3-8902/ 377 -300 -270 - 300
    //4-197281/ 2798 - 1600 - 2500 - 1500 (dont create new chessspaces object every pawn cycle,
    // and use int in board, dont create new chess pieces object everytime.)

    //5-4865609/ 60000 - 33000 - 30000
    //6-??/862469ms
}
