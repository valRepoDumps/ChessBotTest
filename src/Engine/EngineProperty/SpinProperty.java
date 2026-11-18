package Engine.EngineProperty;

import Engine.Engine;
import com.google.gson.JsonObject;

/** Long, not Integer: some engines declare ranges above 2^31 (hash sizes, node limits). */
public class SpinProperty extends EngineProperty<Long> {
    private final Long min;
    private final Long max;

    public SpinProperty(String name, String defaultText, Long min, Long max, Engine eng) {
        super(name, "spin", defaultText, eng);
        this.min = min;
        this.max = max;
    }

    public Long getMin() { return min; }
    public Long getMax() { return max; }

    @Override
    protected String format(Long value) { return Long.toString(value); }

    @Override
    protected Long parse(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be an integer, got \"" + text + "\"");
        }
    }

    @Override
    protected void validate(Long value) {
        if ((min != null && value < min) || (max != null && value > max)) {
            throw new IllegalArgumentException(name + " must be between "
                    + (min != null ? min : "-inf") + " and " + (max != null ? max : "+inf")
                    + ", got " + value);
        }
    }

    @Override
    public JsonObject toJson() {
        JsonObject s = super.toJson();
        s.addProperty("min", min);
        s.addProperty("max", max);
        return s;
    }
}