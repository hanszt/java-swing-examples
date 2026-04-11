package hzt.samples3d;

import java.awt.*;
import java.util.function.Consumer;

/// A button that allows switching between different rendering modes
public final class RenderingModeButton extends Button {

    public enum Mode {WIRE_FRAME, FILLED_SHADED, FILLED_UNSHADED}
    private Mode mode = Mode.FILLED_SHADED;

    public RenderingModeButton(Consumer<Mode> modeConsumer) {
        setLabel(getLabel(mode));
        addActionListener(_ -> {
            final var newMode = switch (mode) {
                case WIRE_FRAME -> Mode.FILLED_UNSHADED;
                case FILLED_SHADED -> Mode.WIRE_FRAME;
                case FILLED_UNSHADED -> Mode.FILLED_SHADED;
            };
            modeConsumer.accept(newMode);
            mode = newMode;
            setLabel(getLabel(newMode));
        });
    }

    public Mode getMode() {
        return mode;
    }

    private static String getLabel(final Mode mode) {
        final var name = mode.name();
        return name.charAt(0) + name.substring(1).replace("_", " ").toLowerCase();
    }
}
