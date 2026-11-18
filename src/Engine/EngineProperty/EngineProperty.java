package Engine.EngineProperty;

import Engine.Engine;
import com.google.gson.JsonObject;

import java.io.IOException;

/**
 * One UCI option. The "text" form (what goes over the wire and what Engine stores) is the single
 * source of truth; each subclass says how to turn T into text and text back into T.
 *
 * Subclasses implement only format() and parse() (and validate() if they have limits).
 * Null handling is done once here, so format()/parse() never see null.
 */
public abstract class EngineProperty<T> {
    protected final String name;
    protected final String type;
    protected final String defaultText;   // as declared by the engine, may be null
    protected final Engine engine;
    private String currentText;   // null = still at the default

    protected EngineProperty(String name, String type, String defaultText, Engine engine) {
        this.name = name;
        this.type = type;
        this.defaultText = defaultText;
        this.engine = engine;
    }

    public String getName() { return name; }
    public String getType() { return type; }

    //region CONVERSION
    /** T -> text. Throws IllegalArgumentException for null. */
    public final String toText(T value) {
        if (value == null) throw new IllegalArgumentException(name + ": value is null");
        return format(value);
    }

    /** text -> T. Returns null for null/blank text; throws IllegalArgumentException if malformed. */
    public final T fromText(String text) {
        if (text == null) return null;
        String trimmed = text.trim();
        if (trimmed.isEmpty() && !acceptsEmptyText()) return null;
        return parse(trimmed);
    }

    protected abstract String format(T value);
    protected abstract T parse(String text);

    /** Only string options treat an empty text as a real (empty) value. */
    protected boolean acceptsEmptyText() { return false; }

    /** Range / membership checks. Throw IllegalArgumentException when invalid. */
    protected void validate(T value) {}
    //endregion

    public String getDefaultText() { return defaultText; }
    public T getDefaultValue() { return fromText(defaultText); }

    /** Text we last sent to the engine, else the default. */
    public String getCurrentText() { return currentText != null ? currentText : defaultText; }
    public boolean isChanged() { return currentText != null; }

    /** Value we last sent to the engine, else the default. */
    public T get() { return fromText(getCurrentText()); }

    public void set(T value) throws IOException {
        if (value == null) throw new IllegalArgumentException(name + ": value is null");
        validate(value);
        String text = format(value);
        engine.sendOption(this, text);
        currentText = text;   // only after the engine accepted it
    }

    /** Convenience for GUIs / JSON: parses the text into T, validates, sends. */
    public void setFromText(String text) throws IOException {
        validateText(text);
        set(fromText(text));
    }

    /** Throws IllegalArgumentException if the text is missing, malformed or out of range. Sends nothing. */
    public void validateText(String text) {
        T value = fromText(text);
        if (value == null) throw new IllegalArgumentException(name + ": no value given");
        validate(value);
    }

    public void reset() throws IOException {
        if (currentText == null) return;
        if (defaultText == null) {
            throw new IllegalStateException("Option \"" + name + "\" has no default value");
        }
        engine.sendOption(this, defaultText);
        currentText = null;
    }

    public JsonObject toJson() {
        JsonObject s = new JsonObject();
        s.addProperty("name", name);
        s.addProperty("type", type);
        s.addProperty("default_value", defaultText);
        s.addProperty("value", getCurrentText());
        s.addProperty("current_value", getCurrentText());
        return s;
    }
}