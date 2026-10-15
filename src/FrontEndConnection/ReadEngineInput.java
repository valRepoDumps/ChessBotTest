package FrontEndConnection;

import java.io.*;
import java.util.function.Consumer;

public class ReadEngineInput implements AutoCloseable {
    private final Process process;
    private final BufferedReader in;
    private final BufferedWriter out;

    public ReadEngineInput(String path) throws IOException {
        process = new ProcessBuilder(path).redirectErrorStream(true).start();
        in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        send("uci");
        waitFor("uciok");

        send("isready");
        waitFor("readyok");
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