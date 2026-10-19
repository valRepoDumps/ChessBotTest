package Engine.EngineProperty;

import Engine.Engine;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A preset: several engine options handled as one (e.g. "elo" = UCI_LimitStrength + UCI_Elo).
 * Supported only if the engine has all of them. apply() takes one value per option, in the same order
 * as the option names: ELO.apply(engine, true, 1500).
 */
public class CompositeEngineProperty {
    public static final CompositeEngineProperty ELO =
            new CompositeEngineProperty("ELO", "UCI_LimitStrength", "UCI_Elo");

    /** Presets offered to the user by default; add more here. */
    public static final List<CompositeEngineProperty> PRESETS = List.of(ELO);

    private final String label;
    private final List<String> optionNames;

    public CompositeEngineProperty(String label, String... optionNames) {
        this.label = label;
        this.optionNames = List.of(optionNames);
    }

    public String getLabel() { return label; }
    public List<String> getOptionNames() { return optionNames; }

    public boolean isSupported(Engine engine) {
        return optionNames.stream().allMatch(engine::hasOption);
    }

    /** Typed convenience: values are converted to text, then parsed and checked by each property. */
    public void apply(Engine engine, Object... values) throws IOException {
        applyText(engine, Arrays.stream(values).map(String::valueOf).toList());
    }

    /** Checks every value first, so a bad value leaves all options untouched. */
    public void applyText(Engine engine, List<String> values) throws IOException {
        if (!isSupported(engine)) {
            throw new IOException("Preset " + label + " is not supported by this engine.");
        }
        if (values.size() != optionNames.size()) {
            throw new IllegalArgumentException("Preset " + label + " needs " + optionNames.size()
                    + " values " + optionNames + ", got " + values.size());
        }
        List<EngineProperty<?>> props = properties(engine);
        for (int i = 0; i < props.size(); i++) props.get(i).validateText(values.get(i));
        for (int i = 0; i < props.size(); i++) props.get(i).setFromText(values.get(i));
    }

    public void reset(Engine engine) throws IOException {
        if (!isSupported(engine)) return;
        for (EngineProperty<?> prop : properties(engine)) prop.reset();
    }

    /** {label, supported, properties: [...]}; properties only when supported (they carry min/max/current value). */
    public JsonObject toJson(Engine engine) {
        JsonObject o = new JsonObject();
        o.addProperty("label", label);
        boolean supported = isSupported(engine);
        o.addProperty("supported", supported);
        if (supported) {
            JsonArray arr = new JsonArray();
            for (EngineProperty<?> prop : properties(engine)) arr.add(prop.toJson());
            o.add("properties", arr);
        }
        return o;
    }

    private List<EngineProperty<?>> properties(Engine engine) {
        List<EngineProperty<?>> props = new ArrayList<>();
        for (String n : optionNames) props.add(engine.getTypedProperty(n));
        return props;
    }
}