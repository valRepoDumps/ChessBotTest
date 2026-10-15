package FrontEndConnection.ForeignEngines;

import com.google.gson.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;

/**
 * Finds UCI chess engines (Stockfish, Leela, Komodo, Ethereal, ...) near a folder you already know.
 *
 * It does not rely on file names. Every executable-looking file within maxDepth levels
 * below the known folder is launched and sent "uci". If it answers "uciok" it is an engine,
 * and its "id name" / "id author" lines are read so you can show a proper name in your GUI.
 *
 * "2 levels" means the engine can sit directly in the folder or inside a subfolder:
 *   known/engine.exe
 *   known/some-folder/engine.exe
 *
 * You can also pass a file instead of a folder (for example your own jar); its parent folder is used.
 *
 * Usage:
 *   List<EngineDetector.Engine> all = EngineDetector.detect(Path.of("C:/chess"));
 *   Optional<EngineDetector.Engine> first = EngineDetector.detectFirst(Path.of("C:/chess"));
 *
 * Warning: this launches programs. Point it at a folder meant for engines, not at Downloads
 * or Program Files, where it could start installers or unrelated apps for a moment.
 */
public final class EngineDetector {
    public static final Path ENGINES_FOLDER = Path.of("D:\\Coding\\Java\\ChessBot\\ChessBot\\resources\\Engines");
    public record Engine(Path path, String name, String author) {
        @Override
        public String toString() {
            return name + " (" + path + ")";
        }

        public JsonObject toJson() {
            JsonObject s = new JsonObject();
            s.addProperty("path", path.toString());
            s.addProperty("name", name);
            s.addProperty("author", author);
            return s;
        }
    }

    private static final boolean WINDOWS =
            System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");

    private static final int DEFAULT_DEPTH = 2;
    private static final int PROBE_TIMEOUT_SECONDS = 5;

    /** Files that are never the engine itself (checked on every OS). */
    private static final List<String> NOT_PROGRAMS = List.of(
            ".zip", ".tar", ".gz", ".xz", ".7z", ".txt", ".md", ".pdf", ".nnue", ".pb",
            ".bin", ".dll", ".so", ".dylib", ".jar", ".java", ".class", ".json", ".ini",
            ".cfg", ".log", ".png", ".jpg", ".svg", ".html", ".c", ".cpp", ".h", ".o");

    /** Name fragments of programs we should not start just to see if they are an engine. */
    private static final List<String> SKIP_NAME_PARTS = List.of(
            "unins", "setup", "install", "update", "redist", "crash");

    private EngineDetector() {}

    /** All engines within 2 levels of the known folder. */
    public static List<Engine> detect(Path known) {
        return detect(known, DEFAULT_DEPTH, Integer.MAX_VALUE);
    }

    /** All engines within maxDepth levels of the known folder. */
    public static List<Engine> detect(Path known, int maxDepth) {
        return detect(known, maxDepth, Integer.MAX_VALUE);
    }

    /** The first engine found (stops probing as soon as one answers). */
    public static Optional<Engine> detectFirst(Path known) {
        return detect(known, DEFAULT_DEPTH, 1).stream().findFirst();
    }

    private static List<Engine> detect(Path known, int maxDepth, int limit) {
        Path root = Files.isRegularFile(known) ? known.toAbsolutePath().getParent() : known;
        List<Engine> engines = new ArrayList<>();
        for (Path candidate : candidates(root, maxDepth)) {
            probe(candidate).ifPresent(engines::add);
            if (engines.size() >= limit) break;
        }
        return engines;
    }

    /** Files that might be programs, shallowest first. Nothing is launched here. */
    private static List<Path> candidates(Path root, int maxDepth) {
        List<Path> hits = new ArrayList<>();
        if (root == null || !Files.isDirectory(root)) return hits;

        try {
            Files.walkFileTree(root, EnumSet.noneOf(FileVisitOption.class), maxDepth,
                    new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            if (looksLikeProgram(file)) hits.add(file.toAbsolutePath().normalize());
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException e) {
                            return FileVisitResult.CONTINUE;   // permission denied etc.
                        }
                    });
        } catch (IOException ignored) {
            // unreadable folder: use whatever was found so far
        }

        hits.sort(Comparator.comparingInt(Path::getNameCount).thenComparing(Comparator.naturalOrder()));
        return hits;
    }

    private static boolean looksLikeProgram(Path file) {
        Path fileName = file.getFileName();
        if (fileName == null || !Files.isRegularFile(file)) return false;   // follows symlinks

        String name = fileName.toString().toLowerCase(Locale.ROOT);
        if (NOT_PROGRAMS.stream().anyMatch(name::endsWith)) return false;
        if (SKIP_NAME_PARTS.stream().anyMatch(name::contains)) return false;

        return WINDOWS ? name.endsWith(".exe") : Files.isExecutable(file);
    }

    /** Launches the file, sends "uci", and waits briefly for "uciok". */
    private static Optional<Engine> probe(Path exe) {
        Process proc = null;
        try {
            proc = new ProcessBuilder(exe.toString())
                    .directory(exe.getParent().toFile())   // engines often load nets/weights from their own folder
                    .redirectErrorStream(true)
                    .start();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(proc.getOutputStream(), StandardCharsets.UTF_8));

            out.write("uci");
            out.newLine();
            out.flush();

            // readLine() blocks, so read on another thread and give up after a few seconds
            return CompletableFuture.supplyAsync(() -> readIdentity(in, exe))
                    .get(PROBE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();   // couldn't start, crashed, or timed out
        } finally {
            if (proc != null) proc.destroyForcibly();   // also unblocks the reader thread
        }
    }

    private static Optional<Engine> readIdentity(BufferedReader in, Path exe) {
        String name = null;
        String author = null;
        try {
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("id name ")) {
                    name = line.substring(8).trim();
                } else if (line.startsWith("id author ")) {
                    author = line.substring(10).trim();
                } else if (line.equals("uciok")) {
                    String shown = (name != null && !name.isEmpty()) ? name : exe.getFileName().toString();
                    return Optional.of(new Engine(exe, shown, author));
                }
            }
        } catch (IOException ignored) {
            // stream closed because the process died or was killed
        }
        return Optional.empty();
    }
}