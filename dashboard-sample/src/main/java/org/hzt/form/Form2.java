
package org.hzt.form;

import javax.swing.*;
import java.awt.*;

public final class Form2 extends JPanel {

    /**
     * Creates new form Form_1
     */
    public Form2() {
        initComponents();
    }

    private void initComponents() {
        final var jLabel1 = new JLabel();

        setBackground(new Color(242, 242, 242));

        jLabel1.setFont(new java.awt.Font("sansserif", 0, 36));
        jLabel1.setForeground(new Color(106, 106, 106));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Form 2");

        final var layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(128, 128, 128)
                .addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(125, 125, 125))
        );
    }


}
