//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import FrontEndConnection.ForeignEngines.EngineDetector;
import FrontEndConnection.ForeignEngines.ReadEngineInput;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {
    String empty_board = "8/8/8/8/8/8/8/8 b - - ";
    String start_position = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1 ";
    String tricky_position = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1 ";
    String killer_position = "rnbqkb1r/pp1p1pPp/8/2p1pP2/1P1P4/3P3P/P1P1P3/RNBQKBNR w KQkq e6 0 1";
    String cmk_position = "r2q1rk1/ppp2ppp/2n1bn2/2b1p3/3pP3/3P1NPP/PPP1NPB1/R1BQ1RK1 b - - 0 9 ";

    void main() throws IOException {
//        MinimalChessGame game =
//                new MinimalChessGame(start_position,
//                    new ChessBoard(),
//                    new Configurations(false, false, true));
//        FrontEndAPI api = new FrontEndAPI(game);
//        api.connect();
        List<EngineDetector.Engine> l = EngineDetector.detect(Path.of("D:\\Coding\\Java\\ChessBot\\ChessBot\\resources\\Engines"));
        for (EngineDetector.Engine e : l){
            System.out.println(e.toString());
        }
        //ChessGUI game = new ChessGUI(start_position, new Configurations(true, true, true));
//        Tests.perftTest();
    }
}
