package FrontEndConnection;

import ChessLogic.Debug.DebugMode;
import ChessLogic.Debug.Debuggable;
import ChessLogic.Evaluation.Evaluation;
import ChessLogic.MinimalChessGame;
import ChessResources.GetMovesLogic.ChessMove;
import Common.JsonUtils;
import FrontEndConnection.ApiRequests.ApiRequest;
import FrontEndConnection.ApiRequests.ApiRequestRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static Common.JsonUtils.str;

public class FrontEndAPI implements Debuggable {
    public static final int PORT = 8765;

    private final FrontEndState state;

    String url;
    public boolean isDebuggable = true;

    public FrontEndAPI(MinimalChessGame game) {
        this(game, Path.of("saves"));
    }

    public FrontEndAPI(MinimalChessGame game, Path saveDir) {
        this.state = new FrontEndState(game, saveDir);
        this.url = "ws://127.0.0.1:" + PORT;
    }

    public FrontEndState getState() {
        return state;
    }

    public MinimalChessGame getGame() {
        return state.getGame();
    }

    public Evaluation getEvaluation() {
        return state.getEvaluation();
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
        JsonObject req;
        try {
            req = JsonParser.parseString(raw).getAsJsonObject();
        } catch (RuntimeException e) {
            send(ws, ApiRequest.failure(null, "Malformed JSON"));
            return;
        }

        String type = str(req, "type");
        String id = str(req, "id");
        DebugMode.debugPrint(this, "Received: " + req);

        JsonObject response;
        ApiRequest handler = ApiRequestRegistry.get(type);
        if (handler == null) {
            response = ApiRequest.failure(id, "Unknown message type: " + type);
        } else {
            try {
                response = handler.request(state, req, id);
            } catch (Exception e) {
                // bad square, missing field, corrupt save, ... -> a well-formed rejection instead of silence
                response = ApiRequest.failure(id, ApiRequest.describe(e));
            }
        }
        send(ws, response);
    }

    private void send(WebSocket ws, JsonObject response) {
        String text = response.toString();
        DebugMode.debugPrint(this, "Response: " + text);
        ws.sendText(text, true);
    }

    //endregion

    /** Kept for existing callers. */
    public static JsonArray movesToJsonArray(List<ChessMove> moves) {
        return JsonUtils.movesToJsonArray(moves);
    }


}