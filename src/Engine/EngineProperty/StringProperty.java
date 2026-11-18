package Engine.EngineProperty;

import Engine.Engine;

/**
 * UCI cannot send an empty value, so engines (Stockfish, ...) use the literal "<empty>".
 * The text form keeps that token; the Java value is a normal empty string.
 */
public class StringProperty extends EngineProperty<String> {
    public static final String EMPTY_TOKEN = "<empty>";

    public StringProperty(String name, String defaultText, Engine eng) { super(name, "string", defaultText, eng); }

    @Override
    protected String format(String value) {
        String v = value.trim();
        return v.isEmpty() ? EMPTY_TOKEN : v;
    }

    @Override
    protected String parse(String text) {
        return text.equals(EMPTY_TOKEN) ? "" : text;
    }

    @Override
    protected boolean acceptsEmptyText() { return true; }
}