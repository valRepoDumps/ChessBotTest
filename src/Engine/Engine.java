package Engine;

import ChessLogic.MinimalChessGame;
import ChessResources.GetMovesLogic.ChessMove;
import Engine.EngineProperty.EngineProperty;

import java.io.IOException;
import java.util.*;

public interface Engine {
    Map<String, EngineProperty<?>> getPropertiesMap();   // key: lower-case option name

    /** Wire level only: send "setoption" for this property. Tracking the value is the property's job. */
    void sendOption(EngineProperty<?> property, String value) throws IOException;

    ChessMove bestMove(MinimalChessGame game) throws IOException;

    default boolean hasOption(String name) {
        return getPropertiesMap().containsKey(key(name));
    }

    default List<EngineProperty<?>> getOptions() {
        return List.copyOf(getPropertiesMap().values());
    }

    /** Text form of the current value (last one set, else the engine's default). */
    default String getOption(String name) {
        EngineProperty<?> prop = getPropertiesMap().get(key(name));
        return prop == null ? null : prop.getCurrentText();
    }

    /** Text-level setter. Prefer the typed property's set(T), which validates first. */
    default void setOption(String name, String value) throws IOException {
        getTypedProperty(name).setFromText(value);   // parses, validates, sends, records
    }

    default void resetOption(String name) throws IOException {
        getTypedProperty(name).reset();
    }

    default void resetAllOptions() throws IOException {
        for (EngineProperty<?> prop : getPropertiesMap().values()) {
            prop.reset();
        }
    }

    default EngineProperty<?> getTypedProperty(String name) {
        EngineProperty<?> prop = getPropertiesMap().get(key(name));
        if (prop == null) throw new IllegalArgumentException("Engine has no option \"" + name + "\"");
        return prop;
    }

    /** e.g. engine.getTypedProperty("Hash", SpinProperty.class).set(256L) */
    default <P extends EngineProperty<?>> P getTypedProperty(String name, Class<P> type) {
        EngineProperty<?> prop = getTypedProperty(name);
        if (!type.isInstance(prop)) {
            throw new IllegalArgumentException("Option \"" + name + "\" is a " + prop.getType()
                    + " option, not " + type.getSimpleName());
        }
        return type.cast(prop);
    }

    static String key(String optionName) {
        return optionName.trim().toLowerCase(Locale.ROOT);
    }
}