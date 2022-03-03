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

package hzt.spinnersample;

/*
 * This application  demonstrates using spinners.
 * Other files required:
 *   SpringUtilities.java
 */

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;

public class SpinnerDemo4 {

    private final JPanel mainPanel;

    public SpinnerDemo4() {
        mainPanel = new JPanel(new SpringLayout());

        String[] labels = {"Shade of Gray: "};
        int numPairs = labels.length;

        JSpinner spinner = addLabeledSpinner(mainPanel, labels[0], new GrayModel(170));
        spinner.setEditor(new GrayEditor().withConfigurationFor(spinner));

        //Lay out the
        SpringUtilities.makeCompactGrid(mainPanel, numPairs, 2, //rows, cols
                10, 10,        //initX, initY
                6, 10);       //xPad, yPad
    }

    protected static JSpinner addLabeledSpinner(Container container, String text, SpinnerModel model) {
        JLabel label = new JLabel(text);
        container.add(label);

        JSpinner spinner = new JSpinner(model);
        label.setLabelFor(spinner);
        container.add(spinner);

        return spinner;
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("SpinnerDemo4");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        final var spinnerDemo4 = new SpinnerDemo4();
        JComponent newContentPane = spinnerDemo4.mainPanel;
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(SpinnerDemo4::run);
    }

    private static void run() {
        //Turn off metal's use of bold fonts
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        createAndShowGUI();
    }

    private static class GrayModel extends SpinnerNumberModel {
        public GrayModel(int value) {
            super(value, 0, 255, 5);
        }

        public int getIntValue() {
            return (int) getValue();
        }

        public Color getColor() {
            int intValue = getIntValue();
            return new Color(intValue, intValue, intValue);
        }
    }

    private static class GrayEditor extends JLabel {

        private GrayEditor withConfigurationFor(JSpinner spinner) {
            setOpaque(true);

            //Get info from the model.
            GrayModel myModel = (GrayModel) (spinner.getModel());
            setBackground(myModel.getColor());
            spinner.addChangeListener(this::stateChanged);

            //Set tool tip text.
            updateToolTipText(spinner);

            //Set size info.
            Dimension size = new Dimension(60, 15);
            setMinimumSize(size);
            setPreferredSize(size);
            return this;
        }

        protected final void updateToolTipText(JSpinner spinner) {
            String toolTipText = spinner.getToolTipText();
            if (toolTipText != null) {
                //JSpinner has tool tip text.  Use it.
                if (!toolTipText.equals(getToolTipText())) {
                    setToolTipText(toolTipText);
                }
            } else {
                //Define our own tool tip text.
                GrayModel myModel = (GrayModel) (spinner.getModel());
                int rgb = myModel.getIntValue();
                setToolTipText("(" + rgb + "," + rgb + "," + rgb + ")");
            }
        }

        public void stateChanged(ChangeEvent e) {
            JSpinner mySpinner = (JSpinner) (e.getSource());
            GrayModel myModel = (GrayModel) (mySpinner.getModel());
            final var color = myModel.getColor();
            setBackground(color);
            updateToolTipText(mySpinner);
        }
    }
}
