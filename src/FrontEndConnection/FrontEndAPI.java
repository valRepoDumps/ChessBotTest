package FrontEndConnection;

import ChessLogic.Configurations.Configurations;
import ChessLogic.Debug.DebugMode;
import ChessLogic.Debug.Debuggable;
import ChessLogic.Evaluation.Evaluation;
import ChessLogic.MinimalChessGame;
import ChessResources.ChessBoard.ChessBoard;
import ChessResources.GameSaver.GameSaveManager;
import ChessResources.GetMovesLogic.ChessMove;
import ChessResources.Pieces.PieceData;
import Engine.Engine;
import Engine.EngineDetector;
import Engine.EngineProperty.CompositeEngineProperty;
import Engine.EngineProperty.EngineProperty;
import Engine.ForeignEngine;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletionStage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FrontEndAPI implements Debuggable {
    public static final int PORT = 8765;
    public volatile MinimalChessGame game;     // replaced by new_game / load_game
    public Evaluation evaluation = new Evaluation();

    String url;
    public boolean isDebuggable = true;

    private final GameSaveManager saves;// restores named listeners on load_game; may be null
    private static final Pattern SAVE_ID_PATTERN = Pattern.compile("save_(\\d{1,9})");

    public FrontEndAPI(MinimalChessGame game) {
        this(game, Path.of("saves"));
    }

    public FrontEndAPI(MinimalChessGame game, Path saveDir) {
        this.game = game;
        this.url = "ws://127.0.0.1:" + PORT;
        this.saves = new GameSaveManager(saveDir);
        evaluation.attach(game, false);
    }

    private static final Map<Character, Integer> PIECE_INDICES = new HashMap<>();
    static {
        char[] pieces = {'P', 'N', 'B', 'R', 'Q', 'K', 'p', 'n', 'b', 'r', 'q', 'k'};
        for (int i = 0; i < pieces.length; i++) {
            PIECE_INDICES.put(pieces[i], i);
        }
    }

    public boolean isDebuggable() {
        return isDebuggable;
    }

    public void connect() {
        HttpClient client = HttpClient.newHttpClient();
        System.out.println("Connecting to " + url + "...");

        WebSocket.Listener listener = new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                System.out.println("Connected to " + url);
                WebSocket.Listener.super.onOpen(webSocket);
            }

            // A text message can arrive in several pieces; only the piece with last == true ends it.
            private final StringBuilder partial = new StringBuilder();

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                partial.append(data);
                if (last) {
                    String message = partial.toString();
                    partial.setLength(0);
                    handleMessage(webSocket, message);
                }
                return WebSocket.Listener.super.onText(webSocket, data, last);
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                System.out.println("Disconnected, retrying in 2s...");
                retryConnection();
                return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                System.out.println("Connection error: " + error.getMessage());
            }
        };

        client.newWebSocketBuilder()
                .buildAsync(URI.create(url), listener)
                .join();

        // Keep the main thread alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void retryConnection() {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                connect();
            } catch (InterruptedException ignored) {}
        }).start();
    }
    //region dispatch

    private void handleMessage(WebSocket ws, String raw) {
//        DebugMode.debugPrint(this, "Received: " + raw);
        JsonObject req;
        try {
            req = JsonParser.parseString(raw).getAsJsonObject();
        } catch (RuntimeException e) {
            send(ws, failure(null, "Malformed JSON"));
            return;
        }

        String type = str(req, "type");
        String id = str(req, "id");
        DebugMode.debugPrint(this, "Received: " + req.toString());

        JsonObject response;
        try {
            response = switch (type == null ? "" : type) {
                case "move_request"        -> moveRequest(req, id);
                case "computer_move"       -> computerMoveRequest(req, id);
                case "get_valid_moves"     -> capturableRequest(req, id);
                case "new_game"            -> newGameRequest(req, id);
                case "save_game"           -> saveGameRequest(req, id);
                case "load_game"           -> loadGameRequest(req, id);
                case "delete_save"         -> deleteSaveRequest(req, id);
                case "list_saves"          -> listSavesRequest(id);
                case "undo_turn"           -> undoTurnRequest(req, id);
                case "list_engines"        -> listEngines(req, id);
                case "use_engine"          -> useEngine(req, id);
                case "list_engine_options" -> listEngineOptions(req, id);
                case "set_engine_option"   -> setEngineOption(req, id);
                case "set_engine_preset"   -> setEnginePreset(req, id);
                default                    -> failure(id, "Unknown message type: " + type);
            };
        } catch (Exception e) {
            // bad square, missing field, corrupt save, ... -> a well-formed rejection instead of silence
            response = failure(id, describe(e));
        }
        send(ws, response);
    }

    private void send(WebSocket ws, JsonObject response) {
        String text = response.toString();
        DebugMode.debugPrint(this, "Response: " + text);
        ws.sendText(text, true);
    }

    //endregion

    //region handlers
    private Engine currentEngine() {
        Engine engine = evaluation.getEngine();
        if (engine == null) throw new IllegalStateException("No engine selected");
        return engine;
    }

    /** Always returns the presets; the raw option list only when the request has "advanced": true. */
    private JsonObject listEngineOptions(JsonObject req, String id) {
        Engine engine = currentEngine();

        JsonArray presets = new JsonArray();
        for (CompositeEngineProperty preset : CompositeEngineProperty.PRESETS) {
            presets.add(preset.toJson(engine));
        }

        JsonObject o = base("list_engine_options_response", id);
        o.addProperty("ok", true);
        o.add("preset_options", presets);

        if (req.has("advanced") && req.get("advanced").getAsBoolean()) {
            JsonArray advanced = new JsonArray();
            for (EngineProperty<?> option : engine.getOptions()) {
                advanced.add(option.toJson());
            }
            o.add("advanced_options", advanced);
        }
        return o;
    }

    /** {"type":"set_engine_option","property":"Hash","value":"256"}  (value omitted for buttons) */
    private JsonObject setEngineOption(JsonObject req, String id) throws IOException {
        Engine engine = currentEngine();
        String name = require(req, "property");
        engine.setOption(name, str(req, "value"));   // parsed + validated by the property

        JsonObject o = base("set_engine_option_response", id);
        o.addProperty("ok", true);
        o.add("property", engine.getTypedProperty(name).toJson());
        return o;
    }

    /** {"type":"set_engine_preset","preset":"elo","values":[true,1500]} - one value per option, in order */
    private JsonObject setEnginePreset(JsonObject req, String id) throws IOException {
        Engine engine = currentEngine();
        String label = require(req, "preset");

        CompositeEngineProperty preset = CompositeEngineProperty.PRESETS.stream()
                .filter(p -> p.getLabel().equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown preset: " + label));

        List<String> values = new ArrayList<>();
        for (var v : req.getAsJsonArray("values")) values.add(v.getAsString());
        preset.applyText(engine, values);

        JsonObject o = base("set_engine_preset_response", id);
        o.addProperty("ok", true);
        o.add("preset", preset.toJson(engine));
        return o;
    }

    private JsonObject useEngine(JsonObject req, String id){
        JsonObject engineJson = req.getAsJsonObject("engine");
        boolean builtin = engineJson.has("builtin") && engineJson.get("builtin").getAsBoolean();

        try {
            if (builtin) evaluation.useDefaultEngine();
            else evaluation.addNewEngine(new ForeignEngine(require(engineJson, "path")));
            evaluation.evaluateGame();
        }
        catch (Exception e){
            return failure(id, e.getMessage());
        }

        return updateBoardRequest(id);
    }

    private JsonObject listEngines(JsonObject req, String id){
        JsonArray engines = new JsonArray();

        // the built-in engine is always available and is the default one
        JsonObject builtIn = new JsonObject();
        builtIn.addProperty("name", "MyEngine");
        builtIn.addProperty("author", "Built-in");
        builtIn.addProperty("builtin", true);
        engines.add(builtIn);

        for (EngineDetector.EngineInfo engineInfo : EngineDetector.detect(EngineDetector.ENGINES_FOLDER)){
            engines.add(engineInfo.toJson());
        }
        JsonObject o = base("list_engines_response", id);
        o.addProperty("ok", true);
        o.add("engines", engines);
        return o;
    }

    private JsonObject updateBoardRequest(String id){
        return updateBoardRequest(id, false);
    }

    private JsonObject updateBoardRequest(String id, boolean isNewGame){
        JsonObject o = base("update_board", id);
        o.addProperty("ok", true);
        o.addProperty("fen", game.toFen());
        o.addProperty("is_new_game", isNewGame);

        String result;
        switch(game.sideWon()){
            case MinimalChessGame.DRAW -> {
                result = "draw";
            }
            case MinimalChessGame.BLACK_WON -> result = "b";
            case MinimalChessGame.WHITE_WON -> result = "w";
            default -> result = "indeterminate";
        }
        o.addProperty("game_over", result);
        return o;
    }

    private JsonObject computerMoveRequest(JsonObject req, String id) throws IOException {
        switch (require(req, "computer_side")) {
            case "b" -> adjustEvaluator(true, PieceData.BLACK);
            case "w" -> adjustEvaluator(true, PieceData.WHITE);
            default  -> { return failure(id, "No computer side selected"); }
        }
        return updateBoardRequest(id);   // unchanged board if it was not the computer's turn
    }

    private JsonObject moveRequest(JsonObject req, String id) {
        int from = ChessBoard.convertSquareNotationToSpaceId(require(req, "from"));
        int to = ChessBoard.convertSquareNotationToSpaceId(require(req, "to"));

        String promotion = require(req, "promotion");

        short promotedPieceId = PieceData.INVALID_PIECES;

        promotedPieceId = switch (promotion) {
            case "Q" -> PieceData.QUEEN;
            case "R" -> PieceData.ROOK;
            case "B" -> PieceData.BISHOP;
            case "N" -> PieceData.KNIGHT;
            default -> promotedPieceId;
        };
        DebugMode.debugPrint(this, "move request: from=" + from + ", to=" + to);

        ChessMove move = game.getPossibleMoves().getMove(from, to, promotedPieceId);

        if (move == null) {
            return failure(id,
                    game.sideWon() != MinimalChessGame.INDETERMINATE
                            ? "Game is over"
                            : "Illegal move");
        }
        if (!game.movePiece(move)) return failure(id, "Move could not be applied");

        return updateBoardRequest(id);
    }

    private JsonObject capturableRequest(JsonObject req, String id) {
        int spaceId = ChessBoard.convertSquareNotationToSpaceId(require(req, "square"));
        DebugMode.debugPrint(this, "get valid moves request: from=" + spaceId);

        List<ChessMove> moves = game.getPossibleMoves().canMoveToFrom(spaceId);
        JsonObject o = base("get_valid_moves_response", id);
        o.add("moves", movesToJsonArray(moves));
        return o;
    }

    private JsonObject newGameRequest(JsonObject req, String id) {
        String fen = str(req, "fen");
        if (fen == null) return failure(id, "Malformed FEN");

        fen = fen.trim().replaceAll("\\s+", " ");

        String problem = validateFen(fen);
        if (problem != null) return failure(id, problem);

        MinimalChessGame fresh;
        try {
            fresh = new MinimalChessGame(fen, new ChessBoard(), new Configurations(game.getConfigurations()));
        } catch (IllegalArgumentException e) {
            return failure(id, describe(e));
        } catch (RuntimeException e) {
            return failure(id, "Invalid FEN");
        }

        installGame(fresh);

        return updateBoardRequest(id, true);
    }

    private JsonObject saveGameRequest(JsonObject req, String id) {
        String name = str(req, "name");
        if (name == null || name.isBlank()) name = "Untitled";

        GameSaveManager.SaveMeta meta;
        try {
            meta = saves.save(game, nextSaveId(), name.trim());
        } catch (IOException e) {
            return failure(id, describe(e));
        }

        JsonObject o = base("save_game_response", id);
        o.addProperty("ok", true);
        o.addProperty("save_id", meta.saveId());
        o.addProperty("name", meta.saveName());
        o.addProperty("saved_at", meta.isoSeconds());
        return o;
    }

    private JsonObject loadGameRequest(JsonObject req, String id) {
        String saveId = require(req, "save_id");

        MinimalChessGame loaded;
        GameSaveManager.SaveMeta meta;
        try {
            meta = saves.readMeta(saveId);
            loaded = saves.load(saveId);   // throws before anything is swapped, so a bad save changes nothing
        } catch (NoSuchFileException e) {
            return failure(id, "Save not found: " + saveId);
        } catch (IOException e) {
            return failure( id, describe(e));
        }

        installGame(loaded);

        return updateBoardRequest(id, true);
    }

    private JsonObject deleteSaveRequest(JsonObject req, String id){
        String saveId = require(req, "save_id");
        try {
            saves.delete(saveId);
        }catch (Exception e){
            return failure(id, e.getMessage());
        }

        return listSavesRequest(id);
    }
    private JsonObject listSavesRequest(String id) {
        List<GameSaveManager.SaveMeta> metas;
        try {
            metas = saves.list();
        } catch (IOException e) {
            return failure(id, describe(e));
        }

        JsonArray arr = new JsonArray();
        for (GameSaveManager.SaveMeta m : metas) {
            arr.add(m.toJson());
        }
        JsonObject o = base("list_saves_response", id);
        o.addProperty("ok", true);
        o.add("saves", arr);
        return o;
    }

    private JsonObject undoTurnRequest(JsonObject req, String id){
        String turnsUndo = require(req, "turns");
        int turnsToUndo;
        try {
            turnsToUndo = Integer.parseInt(turnsUndo);

        } catch(NumberFormatException e){
            return failure(id, "Invalid number of turns to undo: "+turnsUndo);
        }

        try {
            game.undoTurn(turnsToUndo);
        } catch(Exception e){
            return failure(id, e.getMessage());
        }
        return updateBoardRequest(id);
    }
    //endregion

    //region helpers

    private void installGame(MinimalChessGame g) {
        this.game = g;
        evaluation.attach(g, false);
    }

    private void adjustEvaluator(boolean turnEvaluatorOn, boolean sideToMove) throws IOException {
        evaluation.changeSideToMove(sideToMove);
        evaluation.turnEvaluatorOnOff(turnEvaluatorOn);
        if (turnEvaluatorOn) evaluation.evaluateGame();
    }

    /** save_0001, save_0002, ... continuing from the highest existing number. */
    private String nextSaveId() throws IOException {
        int max = 0;
        for (GameSaveManager.SaveMeta m : saves.list()) {
            Matcher mt = SAVE_ID_PATTERN.matcher(m.saveId());
            if (mt.matches()) max = Math.max(max, Integer.parseInt(mt.group(1)));
        }
        return String.format("save_%04d", max + 1);
    }

    /** Returns a human-readable problem, or null if the FEN is acceptable. */
    private static String validateFen(String fen) {
        String[] f = fen.split(" ");
        if (f.length != 6) return "FEN must have 6 fields";
        if (f[0].split("/", -1).length != 8) return "FEN placement must have 8 ranks";

        int whiteKings = 0, blackKings = 0;
        for (char c : f[0].toCharArray()) {
            if (c == 'K') whiteKings++;
            else if (c == 'k') blackKings++;
        }
        if (whiteKings == 0) return "White king is missing";
        if (whiteKings > 1) return "White has more than one king";
        if (blackKings == 0) return "Black king is missing";
        if (blackKings > 1) return "Black has more than one king";
        return null;
    }

    private static JsonObject base(String type, String id) {
        JsonObject o = new JsonObject();
        o.addProperty("type", type);
        if (id != null) o.addProperty("id", id);
        return o;
    }

    /** Rejection in the shape the frontend expects for that request type. */
    private static JsonObject failure(String id, String reason) {
        JsonObject o = base("failure", id);
        o.addProperty("ok", false);
        o.addProperty("reason", reason);
        return o;
    }

    public static JsonArray movesToJsonArray(List<ChessMove> moves) {
        JsonArray arr = new JsonArray();
        for (ChessMove move : moves) {
            JsonObject m = new JsonObject();
            m.addProperty("from", ChessBoard.convertSpaceIdToSquareNotation(move.getSpaceIdToMove()));
            m.addProperty("to", ChessBoard.convertSpaceIdToSquareNotation(move.getSpaceIdArriveAt()));

            int captureId = move.getSpaceIdCaptureAt();
            if (captureId < 0 || captureId > 63) m.add("captured", com.google.gson.JsonNull.INSTANCE); // your "no capture" value
            else m.addProperty("captured", ChessBoard.convertSpaceIdToSquareNotation(captureId));
            arr.add(m);
        }
        return arr;
    }

    /** Kept for existing callers. */
    public static String movesToJson(List<ChessMove> moves) {
        return movesToJsonArray(moves).toString();
    }

    private static String str(JsonObject o, String field) {
        return (o.has(field) && !o.get(field).isJsonNull()) ? o.get(field).getAsString() : null;
    }

    private static String require(JsonObject o, String field) {
        String v = str(o, field);
        if (v == null) throw new IllegalArgumentException("Missing field: " + field);
        return v;
    }

    private static String orDefault(String v, String fallback) {
        return (v == null || v.isBlank()) ? fallback : v;
    }

    private static String describe(Exception e) {
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }
    //endregion
}