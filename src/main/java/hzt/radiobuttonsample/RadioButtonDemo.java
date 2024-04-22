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

package hzt.radiobuttonsample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Optional;

/**
 * RadioButtonDemo.java requires these files:
 *   images/Bird.gif
 *   images/Cat.gif
 *   images/Dog.gif
 *   images/Rabbit.gif
 *   images/Pig.gif
 */
public class RadioButtonDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(RadioButtonDemo.class);

    private static final String BIRD_STRING = "Bird";
    private static final String CAT_STRING = "Cat";
    private static final String DOG_STRING = "Dog";
    private static final String RABBIT_STRING = "Rabbit";
    private static final String PIG_STRING = "Pig";

    private final JPanel mainPanel;
    private final JLabel picture;

    public RadioButtonDemo() {
        mainPanel = new JPanel(new BorderLayout());

        //Create the radio buttons.
        final var birdButton = new JRadioButton(BIRD_STRING);
        birdButton.setMnemonic(KeyEvent.VK_B);
        birdButton.setActionCommand(BIRD_STRING);
        birdButton.setSelected(true);

        final var catButton = new JRadioButton(CAT_STRING);
        catButton.setMnemonic(KeyEvent.VK_C);
        catButton.setActionCommand(CAT_STRING);

        final var dogButton = new JRadioButton(DOG_STRING);
        dogButton.setMnemonic(KeyEvent.VK_D);
        dogButton.setActionCommand(DOG_STRING);

        final var rabbitButton = new JRadioButton(RABBIT_STRING);
        rabbitButton.setMnemonic(KeyEvent.VK_R);
        rabbitButton.setActionCommand(RABBIT_STRING);

        final var pigButton = new JRadioButton(PIG_STRING);
        pigButton.setMnemonic(KeyEvent.VK_P);
        pigButton.setActionCommand(PIG_STRING);

        //Group the radio buttons.
        final var group = new ButtonGroup();
        group.add(birdButton);
        group.add(catButton);
        group.add(dogButton);
        group.add(rabbitButton);
        group.add(pigButton);

        //Register a listener for the radio buttons.
        birdButton.addActionListener(this::actionPerformed);
        catButton.addActionListener(this::actionPerformed);
        dogButton.addActionListener(this::actionPerformed);
        rabbitButton.addActionListener(this::actionPerformed);
        pigButton.addActionListener(this::actionPerformed);

        //Set up the picture label.
        picture = createImageIcon("images/" + BIRD_STRING + ".gif")
                .map(JLabel::new)
                .orElseThrow();

        //The preferred size is hard-coded to be the width of the
        //widest image and the height of the tallest image.
        //A real program would compute this.
        picture.setPreferredSize(new Dimension(177, 122));


        //Put the radio buttons in a column in a panel.
        final var radioPanel = new JPanel(new GridLayout(0, 1));
        radioPanel.add(birdButton);
        radioPanel.add(catButton);
        radioPanel.add(dogButton);
        radioPanel.add(rabbitButton);
        radioPanel.add(pigButton);

        mainPanel.add(radioPanel, BorderLayout.LINE_START);
        mainPanel.add(picture, BorderLayout.CENTER);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }

    /**
     * Listens to the radio buttons.
     */
    public void actionPerformed(final ActionEvent e) {
        final var actionCommand = e.getActionCommand();
        createImageIcon("images/" + actionCommand + ".gif")
                .ifPresentOrElse(picture::setIcon, () -> LOGGER.error("Couldn't find file: {}", actionCommand));
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    protected static Optional<ImageIcon> createImageIcon(final String path) {
        return Optional.ofNullable(RadioButtonDemo.class.getResource(path))
                .map(ImageIcon::new);
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        final var frame = new JFrame("RadioButtonDemo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        final var radioButtonDemo = new RadioButtonDemo();
        final JComponent newContentPane = radioButtonDemo.mainPanel;
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(final String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(RadioButtonDemo::createAndShowGUI);
    }
}
