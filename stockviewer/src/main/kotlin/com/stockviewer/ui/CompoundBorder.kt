package com.stockviewer.ui

import java.awt.Component
import java.awt.Graphics
import java.awt.Insets
import javax.swing.border.AbstractBorder
import javax.swing.border.Border

class CompoundBorder(private val outer: Border, private val inner: Border) : AbstractBorder() {
    override fun getBorderInsets(c: Component?): Insets {
        val o = outer.getBorderInsets(c)
        val i = inner.getBorderInsets(c)
        return Insets(o.top + i.top, o.left + i.left, o.bottom + i.bottom, o.right + i.right)
    }

    override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, w: Int, h: Int) {
        outer.paintBorder(c, g, x, y, w, h)
        val ins = outer.getBorderInsets(c)
        inner.paintBorder(c, g, x + ins.left, y + ins.top, w - ins.left - ins.right, h - ins.top - ins.bottom)
    }
}
