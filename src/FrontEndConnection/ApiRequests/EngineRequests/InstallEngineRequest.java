package FrontEndConnection.ApiRequests.EngineRequests;

import Engine.EngineInstaller;
import FrontEndConnection.ApiRequests.ApiRequest;
import FrontEndConnection.FrontEndState;
import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.nio.file.Paths;

import static Common.JsonUtils.require;

public class InstallEngineRequest implements ApiRequest {
    @Override
    public JsonObject request(FrontEndState state, JsonObject req, String id) throws Exception{
        String pathString = require(req, "path");
        Path path = Paths.get(pathString);

        try {
            EngineInstaller.install(path);
        }catch(Exception e){
            return ApiRequest.failure(id, e.getMessage());
        }

        return new ListEnginesRequest().request(state, req, id);
    }
}
