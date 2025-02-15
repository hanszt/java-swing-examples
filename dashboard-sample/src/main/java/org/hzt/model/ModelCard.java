package org.hzt.model;

import javax.swing.*;

public record ModelCard(
        Icon icon,
        String title,
        String values,
        String description
) {
}
