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
import java.util.Optional;

/*
 * SplitPaneDividerDemo.java requires the following files:
 *   SizeDisplayer.java
 *   images/Cat.gif
 *   images/Dog.gif
 */
public class SplitPaneDividerDemo {

    private final JPanel mainPanel;
    private final JSplitPane splitPane;
    
    public SplitPaneDividerDemo() {
        mainPanel = new JPanel(new BorderLayout());

        final var font = new Font("Serif", Font.ITALIC, 24);

        var icon = createImageIcon("images/Cat.gif").orElseThrow();
        final var sd1 = SizeDisplayer.with("left", icon);
        sd1.setMinimumSize(new Dimension(30,30));
        sd1.setFont(font);
        
        icon = createImageIcon("images/Dog.gif").orElseThrow();
        final var sd2 = SizeDisplayer.with("right", icon);
        sd2.setMinimumSize(new Dimension(60,60));
        sd2.setFont(font);
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sd1, sd2);
        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(createControlPanel(), BorderLayout.PAGE_END);
    }

    private JComponent createControlPanel() {
        final var panel = new JPanel();
        final var reset = new JButton("Reset");
        reset.addActionListener(e -> splitPane.resetToPreferredSizes());
        panel.add(reset);
        return panel;
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    private static Optional<ImageIcon> createImageIcon(final String path) {
        return Optional.ofNullable(SplitPaneDividerDemo.class.getResource(path)).map(ImageIcon::new);
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        final var frame = new JFrame("SplitPaneDividerDemo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        final var splitPaneDividerDemo = new SplitPaneDividerDemo();
        final var contentPane = splitPaneDividerDemo.mainPanel;
        contentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(contentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    void main() {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(SplitPaneDividerDemo::createAndShowGUI);
    }
}
