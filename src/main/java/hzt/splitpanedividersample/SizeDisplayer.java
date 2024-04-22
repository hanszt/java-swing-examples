/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package hzt.splitpanedividersample;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.util.Objects;

public final class SizeDisplayer extends JComponent {

    private static final int X_TEXT_PAD = 5;
    private static final int Y_TEXT_PAD = 5;

    private final transient Icon icon;
    private final String text;
    private final Rectangle textSizeR = new Rectangle();
    private final Dimension textSizeD = new Dimension();

    private Dimension userPreferredSize;
    private Dimension userMinimumSize;
    private Dimension userMaximumSize;

    private SizeDisplayer(final String text, final Icon icon) {
        this.text = text;
        this.icon = icon;
    }

    public static SizeDisplayer with(final String text, final Icon icon) {
        final var sizeDisplayer = new SizeDisplayer(text, icon);
        sizeDisplayer.setOpaque(true);
        return sizeDisplayer;
    }

    @Override
    protected void paintComponent(final Graphics graphics) {
        final var graphics2D = (Graphics2D) graphics.create();
        final var minSize = getMinimumSize();
        final var prefSize = getPreferredSize();
        final var size = getSize();
        setRenderingHints(graphics2D);
        drawMaxSizeRectangleIfOpaque(graphics2D, size);
        drawIcon(graphics2D, size);

        //Draw the preferred size rectangle.
        drawRectangle(graphics2D, prefSize, size, Color.RED);

        //Draw the minimum size rectangle.
        if (minSize.width != prefSize.width || minSize.height != prefSize.height) {
            drawRectangle(graphics2D, minSize, size, Color.CYAN);
        }
        drawText(graphics2D, size);
        graphics2D.dispose();
    }

    private static void drawRectangle(final Graphics2D graphics2D, final Dimension prefSize, final Dimension size, final Color red) {
        final var prefX = (size.width - prefSize.width) / 2;
        final var prefY = (size.height - prefSize.height) / 2;
        graphics2D.setColor(red);
        graphics2D.drawRect(prefX, prefY, prefSize.width - 1, prefSize.height - 1);
    }

    private static void setRenderingHints(final Graphics2D graphics2D) {
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private void drawMaxSizeRectangleIfOpaque(final Graphics2D graphics2D, final Dimension size) {
        if (isOpaque()) {
            graphics2D.setColor(getBackground());
            graphics2D.fillRect(0, 0, size.width, size.height);
        }
    }

    private void drawIcon(final Graphics2D graphics2D, final Dimension size) {
        if (icon != null) {
            final var oldComposite = graphics2D.getComposite();
            graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1F));
            icon.paintIcon(this, graphics2D,
                    (size.width - icon.getIconWidth()) / 2,
                    (size.height - icon.getIconHeight()) / 2);
            graphics2D.setComposite(oldComposite);
        }
    }

    private void drawText(final Graphics2D g2d, final Dimension size) {
        if (text != null) {
            final var textSize = getTextSize(g2d);
            g2d.setColor(getForeground());
            g2d.drawString(text,
                    (size.width - textSize.width) / 2,
                    (size.height - textSize.height) / 2
                            + g2d.getFontMetrics().getAscent());
        }
    }

    private Dimension getTextSize(final Graphics2D g2d) {
        if (text == null) {
            textSizeD.setSize(0, 0);
        } else {
            final var frc = g2d != null ? g2d.getFontRenderContext() :
                    new FontRenderContext(null, false, false);
            final var textRect = getFont().getStringBounds(text, frc);
            textSizeR.setRect(textRect);
            textSizeD.setSize(textSizeR.width, textSizeR.height);
        }

        return textSizeD;
    }

    @Override
    public Dimension getMinimumSize() {
        //user has set the min size
        return userMinimumSize != null ? userMinimumSize : getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        //user has set the pref size
        return Objects.requireNonNullElseGet(userPreferredSize, this::calculatePreferredSize);
    }

    @Override
    public Dimension getMaximumSize() {
        //user has set the max size
        return Objects.requireNonNullElseGet(userMaximumSize, () -> new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Override
    public void setMinimumSize(final Dimension newSize) {
        userMinimumSize = newSize;
    }

    @Override
    public void setPreferredSize(final Dimension newSize) {
        userPreferredSize = newSize;
    }

    @Override
    public void setMaximumSize(final Dimension newSize) {
        userMaximumSize = newSize;
    }

    private Dimension calculatePreferredSize() {
        final var insets = getInsets();
        final var textSize = getTextSize(null);
        var iconWidth = 0;
        var iconHeight = 0;

        if (icon != null) {
            iconWidth = icon.getIconWidth();
            iconHeight = icon.getIconHeight();
        }
        final var width = Math.max(iconWidth, textSize.width + 2 * X_TEXT_PAD) + insets.left + insets.right;
        final var height = Math.max(iconHeight, textSize.height + 2 * Y_TEXT_PAD) + insets.top + insets.bottom;
        return new Dimension(width, height);
    }
}
