package Engine;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;

/**
 * Safely installs a chess engine file into the default engines folder.
 */
public final class EngineInstaller {

    private EngineInstaller() {}

    /**
     * Installs an engine file into {@link EngineDetector#ENGINES_FOLDER} safely.
     *
     * <p>To prevent unfinished or partial files, this copies the engine to a temporary
     * file in the target directory first, then renames it once the copy is complete.
     *
     * @param sourceEngine The source executable file (e.g. stockfish.exe).
     * @return The final Path of the installed engine.
     * @throws IOException If an I/O error occurs during copying or moving.
     */
    @NotNull
    public static Path install(@NotNull Path sourceEngine) throws IOException {
        if (!Files.isRegularFile(sourceEngine)) {
            throw new IllegalArgumentException("Source engine must be a valid file: " + sourceEngine);
        }

        Path enginesDir = EngineDetector.ENGINES_FOLDER;
        Files.createDirectories(enginesDir);

        Path fileName = sourceEngine.getFileName();
        if (fileName == null) {
            throw new IllegalArgumentException("Invalid source file name: " + sourceEngine);
        }

        Path targetPath = enginesDir.resolve(fileName);
        Path tempPath = enginesDir.resolve(fileName.toString() + ".tmp");

        try {
            // 1. Copy to a temp file in the same directory (guarantees same file store for atomic move)
            Files.copy(sourceEngine, tempPath, StandardCopyOption.REPLACE_EXISTING);

            // 2. Atomically rename temp file to the final target filename
            try {
                Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                // Fallback if the filesystem doesn't support atomic moves
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return targetPath;
        } finally {
            // Ensure temp file is cleaned up if anything failed before the rename
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException ignored) {}
        }
    }
}