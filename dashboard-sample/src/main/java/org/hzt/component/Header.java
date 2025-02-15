package org.hzt.component;

import org.hzt.Resources;
import org.hzt.swing.SearchText;

import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import static javax.swing.GroupLayout.DEFAULT_SIZE;

public final class Header extends JPanel {

    public Header() {
        initComponents();
        setOpaque(false);
    }

    private void initComponents() {

        JLabel jLabel1 = new JLabel();
        SearchText searchText1 = new SearchText();
        JLabel jLabel2 = new JLabel();

        setBackground(new Color(255, 255, 255));

        jLabel1.setIcon(new ImageIcon(Resources.urlOrThrow("/org/hzt/icon/search.png")));

        jLabel2.setIcon(new ImageIcon(Resources.urlOrThrow("/org/hzt/icon/menu.png")));
        jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(searchText1, DEFAULT_SIZE, 606, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(jLabel1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(searchText1, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel2, DEFAULT_SIZE, 45, Short.MAX_VALUE)
        );
    }

    @Override
    protected void paintComponent(Graphics grphcs) {
        Graphics2D g2 = (Graphics2D) grphcs;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        g2.fillRect(0, 0, 25, getHeight());
        g2.fillRect(getWidth() - 25, getHeight() - 25, getWidth(), getHeight());
        super.paintComponent(grphcs);
    }
}
