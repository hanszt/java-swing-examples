package com.stockviewer.ui.swing

import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Insets
import javax.swing.border.AbstractBorder

class MatteBorder(top: Int, left: Int, bottom: Int, right: Int, color: Color) : AbstractBorder() {
    private val insets = Insets(top, left, bottom, right)
    private val clr = color
    override fun getBorderInsets(c: Component?) = insets
    override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, w: Int, h: Int) {
        g.color = clr
        if (insets.top > 0) g.fillRect(x, y, w, insets.top)
        if (insets.bottom > 0) g.fillRect(x, y + h - insets.bottom, w, insets.bottom)
        if (insets.left > 0) g.fillRect(x, y, insets.left, h)
        if (insets.right > 0) g.fillRect(x + w - insets.right, y, insets.right, h)
    }
}
