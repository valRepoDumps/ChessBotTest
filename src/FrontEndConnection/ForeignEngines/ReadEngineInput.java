package FrontEndConnection.ForeignEngines;

import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.ChessMove;
import ChessResources.Pieces.PieceData;

import java.io.*;
import java.util.Objects;
import java.util.function.Consumer;

public class ReadEngineInput implements AutoCloseable {
    private final Process process;
    private final BufferedReader in;
    private final BufferedWriter out;

    public static final String STOCKFISH_PATH =
            "D:\\Coding\\Java\\ChessBot\\ChessBot\\resources\\Engines\\stockfish\\stockfish-windows-x86-64-universal.exe";
    public static final int DEFAULT_TIME_MS = 1000;
    public ReadEngineInput(String path) throws IOException {
        process = new ProcessBuilder(path).redirectErrorStream(true).start();
        in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        send("uci");
        waitFor("uciok");

        send("isready");
        waitFor("readyok");
    }

    public static ReadEngineInput getStockfishEngine(){
        try {
            return new ReadEngineInput(STOCKFISH_PATH);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(String cmd) throws IOException {
        out.write(cmd);
        out.newLine();
        out.flush();
    }

    private void waitFor(String token) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            if (line.equals(token)) return;
        }
        throw new IOException("Engine closed before sending " + token);
    }

    /** Blocks until the search ends, so call it off the UI thread. */
    public String bestMove(String fen, int moveTimeMs, Consumer<Info> onInfo) throws IOException {
        send("position fen " + fen);
        send("go movetime " + moveTimeMs);
        String line;

        while ((line = in.readLine()) != null) {
            if (line.startsWith("info ")) {
                Info info = Info.parse(line);
                if (info != null && onInfo != null) onInfo.accept(info);
            } else if (line.startsWith("bestmove")) {
                return line.split(" ")[1];   // "e2e4", or "(none)" if the game is over
            }
        }
        throw new IOException("Engine closed unexpectedly");
    }
    public String bestMove(String fen, int moveTimeMs) throws IOException {
        return bestMove(fen, moveTimeMs, info ->{});
    }
    public ChessMove bestMove(MinimalChessGame game) throws IOException {
        String move = bestMove(game.toFen(), DEFAULT_TIME_MS);
        if (Objects.equals(move, "(none)")){
            return null;
        }

        int startIdx = ChessBoard.convertSquareNotationToSpaceId(move.substring(0, 2));
        int endIdx = ChessBoard.convertSquareNotationToSpaceId(move.substring(2,4));

        short promotionId = PieceData.INVALID_PIECES;

        if (move.length() == 5){
            switch(move.charAt(4)){
                case 'q': promotionId = PieceData.QUEEN;
                case 'r': promotionId = PieceData.ROOK;
                case 'b': promotionId = PieceData.BISHOP;
                case 'n': promotionId = PieceData.KNIGHT;
                default: promotionId = PieceData.INVALID_PIECES;
            }
        }

        return game.getPossibleMoves().getMove(startIdx, endIdx, promotionId);
    }

    @Override
    public void close() {
        try { send("quit"); } catch (IOException ignored) {}
        process.destroy();
    }

    public record Info(int depth, int multiPv, Integer cp, Integer mate, String pv) {
        static Info parse(String line) {
            String[] t = line.split(" ");
            if (t.length < 2 || t[1].equals("string")) return null;
            int depth = 0, multiPv = 1;
            Integer cp = null, mate = null;
            StringBuilder pv = new StringBuilder();
            for (int i = 1; i < t.length; i++) {
                switch (t[i]) {
                    case "depth"   -> depth = Integer.parseInt(t[++i]);
                    case "multipv" -> multiPv = Integer.parseInt(t[++i]);
                    case "score" -> {
                        String type = t[++i];
                        int val = Integer.parseInt(t[++i]);
                        if (type.equals("cp")) cp = val; else mate = val;
                    }
                    case "pv" -> {
                        for (int j = i + 1; j < t.length; j++) pv.append(t[j]).append(' ');
                        i = t.length;   // pv runs to the end of the line
                    }
                    default -> {}
                }
            }
            if (cp == null && mate == null) return null;   // skip currmove/hashfull lines etc.
            return new Info(depth, multiPv, cp, mate, pv.toString().trim());
        }
    }
}