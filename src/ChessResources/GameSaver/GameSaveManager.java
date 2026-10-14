package ChessResources.GameSaver;

import ChessLogic.MinimalChessGameCodec;
import ChessLogic.MinimalChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Layout on disk:
 *   <root>/<saveId>/meta.json   small: id, name, timestamp (cheap to list)
 *   <root>/<saveId>/game.json   full MinimalChessGameCodec output
 * game.json is written first and meta.json last, so a folder with meta.json is always a complete save.
 */
public final class GameSaveManager {
    public record SaveMeta(
            String saveId,
            String saveName,
            String savedAt,
            String fen,
            long savedAtEpochMs,
            int formatVersion,
            int totalMovesElapsed) {
        public String isoSeconds() {
            return Instant.parse(savedAt).truncatedTo(ChronoUnit.SECONDS).toString();
        }

        public JsonObject toJson(){
            JsonObject s = new JsonObject();
            s.addProperty("save_id", saveId);
            s.addProperty("name", saveName);
            s.addProperty("saved_at", isoSeconds());
            s.addProperty("fen", fen);
            return s;
        }
    }

    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9_-]{1,64}");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path root;

    public GameSaveManager(Path root) { this.root = root; }

    /** New save with a generated id. */
    public SaveMeta save(MinimalChessGame game, String saveName) throws IOException {
        return save(game, UUID.randomUUID().toString(), saveName);
    }

    /** Writes (or overwrites) the save with this id; savedAt is stamped now. */
    public SaveMeta save(MinimalChessGame game, String saveId, String saveName) throws IOException {
        requireSafeId(saveId);

        Instant now = Instant.now();
        SaveMeta meta = new SaveMeta(
                saveId,
                saveName,
                now.toString(),
                game.toFen(),
                now.toEpochMilli(),
                MinimalChessGameCodec.VERSION,
                game.getGameProperties().getTotalMovesElapsed());

        Path dir = root.resolve(saveId);
        Files.createDirectories(dir);
        writeAtomic(dir.resolve("game.json"), MinimalChessGameCodec.toJson(game));
        writeAtomic(dir.resolve("meta.json"), GSON.toJson(meta));
        return meta;
    }

    public MinimalChessGame load(String saveId) throws IOException {
        requireSafeId(saveId);
        String json =
                Files.readString(
                        root.resolve(saveId).
                                resolve("game.json"),
                        StandardCharsets.UTF_8);
        return MinimalChessGameCodec.fromJson(json);
    }

    public SaveMeta readMeta(String saveId) throws IOException {
        requireSafeId(saveId);
        return GSON.fromJson(Files.readString(root.resolve(saveId).resolve("meta.json"), StandardCharsets.UTF_8),
                SaveMeta.class);
    }

    /** All complete saves, newest first. */
    public List<SaveMeta> list() throws IOException {
        List<SaveMeta> out = new ArrayList<>();
        if (!Files.isDirectory(root)) return out;
        try (Stream<Path> dirs = Files.list(root)) {
            for (Path d : (Iterable<Path>) dirs.filter(Files::isDirectory)::iterator) {
                if (Files.exists(d.resolve("meta.json"))) out.add(readMeta(d.getFileName().toString()));
            }
        }
        out.sort(Comparator.comparingLong(SaveMeta::savedAtEpochMs).reversed());
        return out;
    }

    public void delete(String saveId) throws IOException {
        requireSafeId(saveId);
        Path dir = root.resolve(saveId);
        if (!Files.exists(dir)) return;
        try (Stream<Path> walk = Files.walk(dir)) {
            for (Path p : (Iterable<Path>) walk.sorted(Comparator.reverseOrder())::iterator) Files.delete(p);
        }
    }

    // ---- helpers ----

    private static void requireSafeId(String id) {
        //ensure id follows a format.
        if (id == null || !SAFE_ID.matcher(id).matches())
            throw new IllegalArgumentException("saveId must match [A-Za-z0-9_-]{1,64}: " + id);
    }

    /** Write to a temp file in the same folder, then rename,
     *  so a crash never leaves a half-written file. */
    private static void writeAtomic(Path target, String content) throws IOException {
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        Files.writeString(tmp, content, StandardCharsets.UTF_8);
        try {
            Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}