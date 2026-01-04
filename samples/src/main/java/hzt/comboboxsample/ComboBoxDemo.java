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

package hzt.comboboxsample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.Optional;

/**
 * ComboBoxDemo.java uses these additional files:
 * images/Bird.gif
 * images/Cat.gif
 * images/Dog.gif
 * images/Rabbit.gif
 * images/Pig.gif
 */
public final class ComboBoxDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComboBoxDemo.class);

    private final JPanel mainPanel;
    private final JLabel picture;

    public ComboBoxDemo() {
        mainPanel = new JPanel(new BorderLayout());

        final var petStrings = new String[]{"Bird", "Cat", "Dog", "Rabbit", "Pig"};

        //Create the combo box, select the item at index 4.
        //Indices start at 0, so 4 specifies the pig.
        final var petList = new JComboBox<>(petStrings);
        petList.setSelectedItem("Pig");
        petList.addActionListener(e -> updateLabel(Objects.requireNonNull((String) petList.getSelectedItem())));

        //Set up the picture.
        picture = new JLabel();
        picture.setFont(picture.getFont().deriveFont(Font.ITALIC));
        picture.setHorizontalAlignment(SwingConstants.CENTER);
        updateLabel(petStrings[petList.getSelectedIndex()]);
        picture.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        //The preferred size is hard-coded to be the width of the
        //widest image and the height of the tallest image + the border.
        //A real program would compute this.
        picture.setPreferredSize(new Dimension(177, 122 + 10));

        //Lay out the demo.
        mainPanel.add(petList, BorderLayout.PAGE_START);
        mainPanel.add(picture, BorderLayout.PAGE_END);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }


    private void updateLabel(final String name) {
        final var path = "images/" + name + ".gif";
        Optional.ofNullable(ComboBoxDemo.class.getResource(path))
                .map(ImageIcon::new)
                .ifPresentOrElse(icon -> setIcon(name, icon),
                        () -> logAndDisplayErrorText(path));
    }

    private void logAndDisplayErrorText(final String path) {
        picture.setText("Image not found");
        LOGGER.error("Couldn't find file: {}", path);
    }

    private void setIcon(final String name, final ImageIcon icon) {
        picture.setToolTipText("A drawing of a " + name.toLowerCase());
        picture.setIcon(icon);
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        final var frame = new JFrame("ComboBoxDemo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        final var comboBoxDemo = new ComboBoxDemo();
        final JComponent newContentPane = comboBoxDemo.mainPanel;
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main() {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(ComboBoxDemo::createAndShowGUI);
    }
}
