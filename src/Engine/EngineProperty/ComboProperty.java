package Engine.EngineProperty;

import Engine.Engine;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class ComboProperty extends EngineProperty<String> {
    private final List<String> vars;

    public ComboProperty(String name, String defaultText, List<String> vars, Engine eng) {
        super(name, "combo", defaultText, eng);
        this.vars = vars;
    }

    public List<String> getValidValues() { return vars; }

    @Override
    protected String format(String value) { return value.trim(); }

    @Override
    protected String parse(String text) { return text; }

    @Override
    protected void validate(String value) {
        if (!vars.contains(value.trim())) {
            throw new IllegalArgumentException(name + " must be one of " + vars + ", got \"" + value + "\"");
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject s = super.toJson();
        JsonArray arr = new JsonArray();
        vars.forEach(arr::add);
        s.add("vars", arr);
        return s;
    }
}