package org.hzt.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class SearchText extends JTextField {

    public SearchText() {
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setSelectionColor(new Color(220, 204, 182));
    }

    @Override
    public void paint(final Graphics g) {
        super.paint(g);
        if (getText().isEmpty()) {
            final var h = getHeight();
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            final var ins = getInsets();
            final var fm = g.getFontMetrics();
            final var c0 = getBackground().getRGB();
            final var c1 = getForeground().getRGB();
            final var m = 0xfefefefe;
            final var c2 = ((c0 & m) >>> 1) + ((c1 & m) >>> 1);
            g.setColor(new Color(c2, true));
            final var hint = "Search here ...";
            g.drawString(hint, ins.left, h / 2 + fm.getAscent() / 2 - 2);
        }
    }
}
