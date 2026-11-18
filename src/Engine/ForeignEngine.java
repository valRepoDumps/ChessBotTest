package Engine;

import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GetMovesLogic.ChessMove;
import ChessResources.Pieces.PieceData;
import Engine.EngineProperty.*;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * Talks to any UCI engine over stdin/stdout.
 *
 * Options: the engine lists its options in reply to "uci". They are read once into a table, so you can
 * list them, change them (checked against the type / range the engine declared), read the value you
 * last set, and reset them to the engine's defaults. UCI has no command for asking the engine for the
 * current value of an option, so getOption() is "last value set through this class, else the default".
 * Change options only while no search is running: they share the engine's output stream.
 */

public class ForeignEngine implements AutoCloseable, Engine {
    private final Process process;
    private final BufferedReader in;
    private final BufferedWriter out;

    public static final String STOCKFISH_PATH =
            "D:\\Coding\\Java\\ChessBot\\ChessBot\\resources\\Engines\\stockfish\\stockfish-windows-x86-64-universal.exe";
    public static final int DEFAULT_TIME_MS = 1000;

    /** Standard UCI option names for strength limiting. Optional: check supportsElo() before relying on them. */
    public static final String LIMIT_STRENGTH = "UCI_LimitStrength";
    public static final String ELO = "UCI_Elo";

    private static final List<String> OPTION_KEYWORDS = List.of("name", "type", "default", "min", "max", "var");

    private final Map<String, EngineProperty<?>> properties = new LinkedHashMap<>();

    private String engineName;
    private String engineAuthor;

    public ForeignEngine(String path) throws IOException {
        File exe = new File(path).getAbsoluteFile();
        process = new ProcessBuilder(exe.getPath())
                .directory(exe.getParentFile())
                .redirectErrorStream(true)
                .start();

        in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        try {
            send("uci");
            readHandshake();
            send("isready");
            waitFor("readyok");
        } catch (IOException | RuntimeException e) {
            process.destroyForcibly();
            throw e;
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
            if (line.trim().equals(token)) return;
        }
        throw new IOException("Engine closed before sending " + token);
    }

    //region HANDSHAKE AND OPTIONS

    /** Reads everything up to "uciok": engine name, author and the option table. */
    private void readHandshake() throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (line.equals("uciok")) return;

            if (line.startsWith("id name ")) {
                engineName = line.substring(8).trim();
            } else if (line.startsWith("id author ")) {
                engineAuthor = line.substring(10).trim();
            } else if (line.startsWith("option ")) {
                EngineProperty<?> prop = parseProperty(line, this);
                if (prop != null) properties.put(Engine.key(prop.getName()), prop);
            }
        }
        throw new IOException("Engine closed before sending uciok");
    }

    static EngineProperty<?> parseProperty(String line, Engine engine) {
        String[] t = line.trim().split("\\s+");
        Map<String, String> fields = new HashMap<>();
        List<String> vars = new ArrayList<>();

        for (int i = 1; i < t.length; ) {
            String keyword = t[i++];
            StringBuilder value = new StringBuilder();
            while (i < t.length && !OPTION_KEYWORDS.contains(t[i])) {
                if (!value.isEmpty()) value.append(' ');
                value.append(t[i++]);
            }
            if (keyword.equals("var")) vars.add(value.toString());
            else fields.put(keyword, value.toString());
        }

        String name = fields.get("name");
        String type = fields.get("type");
        if (name == null || name.isEmpty() || type == null) return null;

        String def = fields.get("default");
        Long min = longOrNull(fields.get("min"));
        Long max = longOrNull(fields.get("max"));

        return switch (type.toLowerCase(Locale.ROOT)) {
            case "check" -> new CheckProperty(name, def, engine);
            case "spin" -> new SpinProperty(name, def, min, max, engine);
            case "combo" -> new ComboProperty(name, def, List.copyOf(vars), engine);
            case "button" -> new ButtonProperty(name, engine);
            case "string" -> new StringProperty(name, def, engine);
            default -> null;
        };
    }

    public String getEngineName()   { return engineName; }
    public String getEngineAuthor() { return engineAuthor; }

    //endregion

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
                String[] t = line.trim().split("\\s+");
                return t.length > 1 ? t[1] : "(none)";   // "e2e4", or "(none)" / "0000" if there is no move
            }
        }

        throw new IOException("Engine closed unexpectedly");
    }

    public String bestMove(String fen, int moveTimeMs) throws IOException {
        return bestMove(fen, moveTimeMs, info ->{});
    }

    @Override
    public ChessMove bestMove(MinimalChessGame game) throws IOException {
        String move = bestMove(game.toFen(), DEFAULT_TIME_MS);
        if (Objects.equals(move, "(none)") || Objects.equals(move, "0000")){   // engines differ on "no move"
            return null;
        }

        int startIdx = ChessBoard.convertSquareNotationToSpaceId(move.substring(0, 2));
        int endIdx = ChessBoard.convertSquareNotationToSpaceId(move.substring(2,4));

        short promotionId = PieceData.INVALID_PIECES;

        if (move.length() == 5){
            // arrow cases do not fall through (the old "case 'q':" version always ended on INVALID_PIECES)
            promotionId = switch (move.charAt(4)) {
                case 'q' -> PieceData.QUEEN;
                case 'r' -> PieceData.ROOK;
                case 'b' -> PieceData.BISHOP;
                case 'n' -> PieceData.KNIGHT;
                default  -> PieceData.INVALID_PIECES;
            };
        }

        return game.getPossibleMoves().getMove(startIdx, endIdx, promotionId);
    }

    @Override
    public void close() {
        try { send("quit"); } catch (IOException ignored) {}
        process.destroy();
    }

    private static Long longOrNull(String s) {
        try { return s == null ? null : Long.parseLong(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    @Override
    public Map<String, EngineProperty<?>> getPropertiesMap() { return properties; }

    @Override
    public void sendOption(EngineProperty<?> prop, String value) throws IOException {
        send(prop.getType().equals("button")
                ? "setoption name " + prop.getName()
                : "setoption name " + prop.getName() + " value " + value);
        send("isready");
        waitFor("readyok");
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