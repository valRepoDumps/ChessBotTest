package Engine.EngineProperty;

import Engine.Engine;

public class CheckProperty extends EngineProperty<Boolean> {
    public CheckProperty(String name, String defaultText, Engine eng) { super(name, "check", defaultText, eng); }

    @Override
    protected String format(Boolean value) { return value ? "true" : "false"; }

    /** Strict: Boolean.parseBoolean would turn any typo into false. */
    @Override
    protected Boolean parse(String text) {
        if (text.equalsIgnoreCase("true")) return true;
        if (text.equalsIgnoreCase("false")) return false;
        throw new IllegalArgumentException(name + " must be true or false, got \"" + text + "\"");
    }
}