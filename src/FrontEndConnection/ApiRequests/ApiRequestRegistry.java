package FrontEndConnection.ApiRequests;

import FrontEndConnection.ApiRequests.ChangeBoardStateRequests.ComputerMoveRequest;
import FrontEndConnection.ApiRequests.ChangeBoardStateRequests.MoveRequest;
import FrontEndConnection.ApiRequests.ChangeBoardStateRequests.NewGameRequest;
import FrontEndConnection.ApiRequests.ChangeBoardStateRequests.UndoTurnRequest;
import FrontEndConnection.ApiRequests.EngineRequests.*;
import FrontEndConnection.ApiRequests.MiscRequests.GetValidMovesRequest;
import FrontEndConnection.ApiRequests.SaveRequests.DeleteSaveRequest;
import FrontEndConnection.ApiRequests.SaveRequests.ListSavesRequest;
import FrontEndConnection.ApiRequests.SaveRequests.LoadGameRequest;
import FrontEndConnection.ApiRequests.SaveRequests.SaveGameRequest;

import java.util.Map;

/** Maps the "type" field of an incoming message to the request that handles it. */
public final class ApiRequestRegistry {
    private static final Map<String, ApiRequest> HANDLERS = Map.ofEntries(
            Map.entry("move_request",        new MoveRequest()),
            Map.entry("computer_move",       new ComputerMoveRequest()),
            Map.entry("get_valid_moves",     new GetValidMovesRequest()),
            Map.entry("new_game",            new NewGameRequest()),
            Map.entry("save_game",           new SaveGameRequest()),
            Map.entry("load_game",           new LoadGameRequest()),
            Map.entry("delete_save",         new DeleteSaveRequest()),
            Map.entry("list_saves",          new ListSavesRequest()),
            Map.entry("undo_turn",           new UndoTurnRequest()),
            Map.entry("list_engines",        new ListEnginesRequest()),
            Map.entry("use_engine",          new UseEngineRequest()),
            Map.entry("list_engine_options", new ListEngineOptionsRequest()),
            Map.entry("set_engine_option",   new SetEngineOptionRequest()),
            Map.entry("set_engine_preset",   new SetEnginePresetRequest()),
            Map.entry("install_engine",      new InstallEngineRequest())
    );

    private ApiRequestRegistry() {}

    /** @return the handler for this message type, or null if there is none */
    public static ApiRequest get(String type) {
        return type == null ? null : HANDLERS.get(type);
    }
}