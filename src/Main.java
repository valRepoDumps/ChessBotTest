//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import ChessGUI.ChessGUI;
import ChessLogic.Configurations.Configurations;
import ChessLogic.Debug.Tests;
import ChessLogic.MinimalChessGame;
import ChessResources.BitMasks;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.Pieces.PieceData;


void main() {
//    ChessGUI gui = new ChessGUI();
//    int num = Tests.moveGenerationTest(3, gui.chessGame);
//    System.out.println(num);
//    3-1034 ms
//    2-460 ms

    MinimalChessGame game = new MinimalChessGame(
     "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
            new ChessBoard(),
            new Configurations(true, false, true));

    long start = System.nanoTime();
    int[] returnVals = Tests.moveGenerationTest(3, game);
    long end = System.nanoTime();
    double durationMs = (end-start) / 1_000_000.0;
    System.out.println("Num Pos: " + returnVals[Tests.TOTAL_MOVES]);
    System.out.println("Num Capture: " + returnVals[Tests.TOTAL_CAPTURE_MOVES]);
    System.out.println("Num EnPassant: " + returnVals[Tests.TOTAL_ENPASSANT_MOVES]);
    System.out.println("Num Castles: " + returnVals[Tests.TOTAL_CASTLING_MOVES]);
    System.out.println("Num Promotions: " + returnVals[Tests.TOTAL_PROMOTION_MOVES]);
    System.out.println(" Time: "+ durationMs + " ms");

//    System.out.println(game.getBoard().getBitBoard(PieceData.WPAWN));

    //3-8902/ 377 -300 - 191 - 130 - 65 - 46 - 39
    //4-197281/ 2798 - 1600 - 2500 - 1500 - 700 - 470
    //5-4865609/ 60000 - 33000 - 30000 - 21300 - 8000 - 7600 - 5600 - 4950
    //6-119060324/862469ms -129270
}
