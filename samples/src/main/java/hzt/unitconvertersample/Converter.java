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

package hzt.unitconvertersample;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * An application that requires the following files:
 *   ConversionPanel.java
 *   ConverterRangeModel.java
 *   FollowerRangeModel.java
 *   Unit.java
 */
public final class Converter extends JPanel {

    private static final int MAX = 10_000;
    private static final boolean MULTICOLORED = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(Converter.class);
    //Specify the look and feel to use.  Valid values:
    //null (use the default), "Metal", "System", "Motif", "GTK+

    /**
     * Create the ConversionPanels (one for metric, another for U.S.).
     * I used "U.S." because although Imperial and U.S. distance
     * measurements are the same, this program could be extended to
     * include volume measurements, which aren't the same.
     */
    private final ConversionPanel metricPanel;
    private final ConversionPanel usaPanel;
    private final transient ConverterRangeModel dataModel;

    private Converter() {
        dataModel = new ConverterRangeModel();
        metricPanel = buildMetricConversionPanel(dataModel);
        //Create Unit objects for U.S. distances and then
        //instantiate a ConversionPanel with these Units.
        usaPanel = buildUsaConversionPanel(dataModel);
    }

    public Converter buildConverter() {
        //Create Unit objects for metric distances and then
        //instantiate a ConversionPanel with these Units.


        //Create a JPanel, and add the ConversionPanels to it.
        final var mainPane = new Converter();
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
        if (MULTICOLORED) {
            mainPane.setOpaque(true);
            mainPane.setBackground(new Color(255, 0, 0));
        }
        mainPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPane.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPane.add(metricPanel);
        mainPane.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPane.add(usaPanel);
        mainPane.add(Box.createGlue());
        final var maximum = calculateMaximum();
        dataModel.setMaximum(maximum);
        dataModel.setDoubleValue(maximum);
        return mainPane;
    }

    @NotNull
    private ConversionPanel buildMetricConversionPanel(final ConverterRangeModel dataModel) {
        final var metricDistances = List.of(
                new Unit("Centimeters", 0.01),
                new Unit("Meters", 1.0),
                new Unit("Kilometers", 1000.0)
        );
        final var panel = new ConversionPanel(dataModel).buildContent(metricDistances);
        panel.setOnUnitChanged(_ -> dataModel.setMaximum(calculateMaximum()));
        panel.setTittle("Metric System");
        return panel;
    }

    @NotNull
    private ConversionPanel buildUsaConversionPanel(final ConverterRangeModel dataModel) {
        final var usaDistances = List.of(
                new Unit("Inches", 0.0254),
                new Unit("Feet", 0.305),
                new Unit("Yards", 0.914),
                new Unit("Miles", 1613.0)
        );
        final var panel = new ConversionPanel(new FollowerRangeModel(dataModel)).buildContent(usaDistances);
        panel.setOnUnitChanged(_ -> dataModel.setMaximum(calculateMaximum()));
        panel.setTittle("U.S. System");
        return panel;
    }

    private int calculateMaximum() {
        final var metricMultiplier = metricPanel.getMultiplier();
        final var usaMultiplier = usaPanel.getMultiplier();
        return (metricMultiplier > usaMultiplier) ? (int) (MAX * (usaMultiplier / metricMultiplier)) : MAX;
    }

    private static void initLookAndFeel() {
        final var lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (final ClassNotFoundException | InstantiationException |
                       IllegalAccessException | UnsupportedLookAndFeelException e) {
            LOGGER.error("Can not set look and feel with class name {}", lookAndFeel, e);
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private  void createAndShowGUI() {
        //Set the look and feel.
        initLookAndFeel();

        //Create and set up the window.
        final var frame = new JFrame("Converter");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        final JPanel converter = buildConverter();
        converter.setOpaque(true); //content panes must be opaque
        frame.setContentPane(converter);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main() {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        final var converter = new Converter();
        SwingUtilities.invokeLater(converter::createAndShowGUI);
    }

}
