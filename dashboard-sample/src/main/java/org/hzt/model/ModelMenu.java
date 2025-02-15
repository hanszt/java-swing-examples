package org.hzt.model;

import org.hzt.Resources;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public record ModelMenu(
        String icon,
        String name,
        MenuType type
) {

    public Icon toIcon() {
        return new ImageIcon(Resources.urlOrThrow("/org/hzt/icon/" + icon + ".png"));
    }

    public enum MenuType {
        TITLE, MENU, EMPTY
    }
}
