//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import ChessGUI.ChessGUI;
import ChessLogic.Configurations.Configurations;
import ChessLogic.Debug.Tests;


String empty_board = "8/8/8/8/8/8/8/8 b - - ";
String start_position = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 ";
String tricky_position = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1 ";
String killer_position = "rnbqkb1r/pp1p1pPp/8/2p1pP2/1P1P4/3P3P/P1P1P3/RNBQKBNR w KQkq e6 0 1";
String cmk_position = "r2q1rk1/ppp2ppp/2n1bn2/2b1p3/3pP3/3P1NPP/PPP1NPB1/R1BQ1RK1 b - - 0 9 ";

void main() {
    ChessGUI gui = new ChessGUI(start_position,
            new Configurations(true, false, true));
//    //testings
//    Evaluation evaluator = new Evaluation(6);
//    evaluator.registerWhiteEvaluator(gui.chessGame);
    Tests.perftTest();
}
