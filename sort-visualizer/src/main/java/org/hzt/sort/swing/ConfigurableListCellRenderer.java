package org.hzt.sort.swing;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public class ConfigurableListCellRenderer<T> extends DefaultListCellRenderer {

    private final transient Function<T, String> nameExtractor;

    public ConfigurableListCellRenderer(final Function<T, String> nameExtractor) {
        this.nameExtractor = nameExtractor;
    }

    @Override
    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus
    ) {
        // Call the super method to handle background colors/selection logic
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        //noinspection unchecked
        setText(nameExtractor.apply((T) value));
        return this;
    }
}

