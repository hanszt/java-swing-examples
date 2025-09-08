package org.hzt.component;

import org.hzt.Resources;
import org.hzt.model.ModelCard;

import javax.swing.*;
import java.awt.*;

public final class Card extends JPanel {

    private final JLabel lbDescription = new JLabel();
    private final JLabel lbIcon = new JLabel();
    private final JLabel lbTitle = new JLabel();
    private final JLabel lbValues = new JLabel();
    private final Color color1;
    private final Color color2;

    public Card(final Color color1, final Color color2) {
        initComponents();
        setOpaque(false);
        this.color1 = color1;
        this.color2 = color2;
    }

    public void setData(final ModelCard data) {
        lbIcon.setIcon(data.icon());
        lbTitle.setText(data.title());
        lbValues.setText(data.values());
        lbDescription.setText(data.description());
    }

    private void initComponents() {

        lbIcon.setIcon(new ImageIcon(Resources.urlOrThrow("/org/hzt/icon/stock.png")));

        lbTitle.setFont(new Font("sansserif", Font.BOLD, 14));
        lbTitle.setForeground(new java.awt.Color(255, 255, 255));
        lbTitle.setText("Title");

        lbValues.setFont(new Font("sansserif", Font.BOLD, 18));
        lbValues.setForeground(new java.awt.Color(255, 255, 255));
        lbValues.setText("Values");

        lbDescription.setFont(new Font("sansserif", Font.PLAIN, 14));
        lbDescription.setForeground(new java.awt.Color(255, 255, 255));
        lbDescription.setText("Description");

        final var layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbDescription)
                    .addComponent(lbValues)
                    .addComponent(lbTitle)
                    .addComponent(lbIcon))
                .addContainerGap(283, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(lbIcon)
                .addGap(18, 18, 18)
                .addComponent(lbTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbValues)
                .addGap(18, 18, 18)
                .addComponent(lbDescription)
                .addContainerGap(25, Short.MAX_VALUE))
        );
    }

    @Override
    protected void paintComponent(final Graphics grphcs) {
        final var g2 = (Graphics2D) grphcs;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final var g = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
        g2.setPaint(g);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        g2.setColor(new Color(255, 255, 255, 50));
        g2.fillOval(getWidth() - (getHeight() / 2), 10, getHeight(), getHeight());
        g2.fillOval(getWidth() - (getHeight() / 2) - 20, getHeight() / 2 + 20, getHeight(), getHeight());
        super.paintComponent(grphcs);
    }
}
