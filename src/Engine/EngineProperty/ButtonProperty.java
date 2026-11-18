package Engine.EngineProperty;

import Engine.Engine;

import java.io.IOException;

/** A button carries no value: nothing to convert, trigger() just fires it. */
public class ButtonProperty extends EngineProperty<Void> {
    public ButtonProperty(String name, Engine eng) { super(name, "button", null, eng); }

    public void trigger() throws IOException {
        engine.sendOption(this, null);
    }

    @Override
    protected String format(Void value) { return ""; }

    @Override
    protected Void parse(String text) { return null; }

    @Override
    public Void get() { return null; }

    @Override
    public void set(Void data) throws IOException { trigger(); }

    @Override
    public void reset() {}

    @Override
    public void validateText(String text) {}

    @Override
    public void setFromText(String text) throws IOException { trigger(); }
}