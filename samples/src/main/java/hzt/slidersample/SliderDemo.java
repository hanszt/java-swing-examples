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

package hzt.slidersample;

import hzt.Loggers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * SliderDemo.java requires all the files in the images/doggy directory.
 */
public final class SliderDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(SliderDemo.class);
    //Set up animation parameters.
    static final int FPS_MIN = 0;
    static final int FPS_MAX = 30;
    static final int FPS_INIT = 15;    //initial frames per second
    private static final int NUM_FRAMES = 14;

    int frameNumber = 0;
    private final JPanel mainPanel;
    ImageIcon[] images = new ImageIcon[NUM_FRAMES];
    int delay;
    Timer timer;
    boolean frozen = false;

    //This label uses ImageIcon to show the doggy pictures.
    JLabel picture;

    public SliderDemo() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        delay = 1000 / FPS_INIT;

        //Create the label.
        final var sliderLabel = new JLabel("Frames Per Second", SwingConstants.CENTER);
        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        //Create the slider.
        final var framesPerSecond = new JSlider(SwingConstants.HORIZONTAL, FPS_MIN, FPS_MAX, FPS_INIT);


        framesPerSecond.addChangeListener(this::stateChanged);

        //Turn on labels at major tick marks.

        framesPerSecond.setMajorTickSpacing(10);
        framesPerSecond.setMinorTickSpacing(1);
        framesPerSecond.setPaintTicks(true);
        framesPerSecond.setPaintLabels(true);
        framesPerSecond.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        framesPerSecond.setFont(new Font("Serif", Font.ITALIC, 15));

        //Create the label that displays the animation.
        picture = new JLabel();
        picture.setHorizontalAlignment(SwingConstants.CENTER);
        picture.setAlignmentX(Component.CENTER_ALIGNMENT);
        picture.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        updatePicture(); //display first frame

        //Put everything together.
        mainPanel.add(sliderLabel);
        mainPanel.add(framesPerSecond);
        mainPanel.add(picture);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //Set up a timer that calls this object's action handler.
        timer = new Timer(delay, this::actionPerformed);
        timer.setInitialDelay(delay * 7); //We pause animation twice per cycle
        //by restarting the timer
        timer.setCoalesce(true);
    }

    /**
     * Listen to the slider.
     */
    public void stateChanged(final ChangeEvent e) {
        final var source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) {
            final var fps = source.getValue();
            if (fps == 0) {
                if (!frozen) {
                    stopAnimation();
                }
            } else {
                delay = 1000 / fps;
                timer.setDelay(delay);
                timer.setInitialDelay(delay * 10);
                if (frozen) {
                    startAnimation();
                }
            }
        }
    }

    public void startAnimation() {
        //Start (or restart) animating!
        timer.start();
        frozen = false;
    }

    public void stopAnimation() {
        //Stop the animating thread.
        timer.stop();
        frozen = true;
    }

    //Called when the Timer fires.
    public void actionPerformed(final ActionEvent e) {
        //Advance the animation frame.
        if (frameNumber == (NUM_FRAMES - 1)) {
            frameNumber = 0;
        } else {
            frameNumber++;
        }

        updatePicture(); //display the next picture

        if (frameNumber == (NUM_FRAMES - 1)
                || frameNumber == (NUM_FRAMES / 2 - 1)) {
            timer.restart();
        }
    }

    /**
     * Update the label to display the image for the current frame.
     */
    protected final void updatePicture() {
        //Get the image if we haven't already.
        if (images[frameNumber] == null) {
            images[frameNumber] = createImageIcon("images/doggy/T"
                    + frameNumber
                    + ".gif");
        }

        //Set the image.
        if (images[frameNumber] != null) {
            picture.setIcon(images[frameNumber]);
        } else { //image not found
            picture.setText("image #" + frameNumber + " not found");
        }
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    protected static ImageIcon createImageIcon(final String path) {
        final var imgURL = SliderDemo.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            LOGGER.error("Couldn't find file: {}", path);
            return null;
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        final var frame = new JFrame("SliderDemo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        final var demo = new SliderDemo();
        //Add content to the window.
        final var mainPanel = demo.mainPanel;
        frame.add(mainPanel, BorderLayout.CENTER);

        frame.addWindowListener(demo.new AnimationWindowListener());
        //Display the window.
        frame.pack();
        frame.setVisible(true);
        demo.startAnimation();
    }

    void main() {
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(SliderDemo::createAndShowGUI);
    }

    private class AnimationWindowListener implements WindowListener {

        public void windowIconified(final WindowEvent e) {
            stopAnimation();
        }

        public void windowDeiconified(final WindowEvent e) {
            startAnimation();
        }

        public void windowOpened(final WindowEvent e) {
            Loggers.logIfInfoEnabled(LOGGER, e::toString);
        }

        public void windowClosing(final WindowEvent e) {
            Loggers.logIfInfoEnabled(LOGGER, e::toString);
        }

        public void windowClosed(final WindowEvent e) {
            Loggers.logIfInfoEnabled(LOGGER, e::toString);
        }

        public void windowActivated(final WindowEvent e) {
            Loggers.logIfInfoEnabled(LOGGER, e::toString);
        }

        public void windowDeactivated(final WindowEvent e) {
            Loggers.logIfInfoEnabled(LOGGER, e::toString);
        }
    }

}

