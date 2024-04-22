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

/*
 * A 1.4 class used by the Converter example.
 */

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.text.NumberFormat;
import java.util.List;

public final class ConversionPanel extends JPanel {

    private final transient ConverterRangeModel converterRangeModel;
    private transient ChangeListener onUnitChanged;

    ConversionPanel(final ConverterRangeModel rangeModel) {
        converterRangeModel = rangeModel;
    }

    public ConversionPanel buildContent(final List<Unit> units) {
        //Put everything together.
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(buildUnitGroup());
        add(createChooserPanel(units));
        return this;
    }

    @NotNull
    private JPanel buildUnitGroup() {
        final var unitGroup = getUnitGroup();
        unitGroup.setLayout(new BoxLayout(unitGroup, BoxLayout.PAGE_AXIS));
        unitGroup.setOpaque(true);
        unitGroup.setBackground(new Color(0, 0, 255));
        unitGroup.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        final var textField = buildTextField();
        final var slider = new JSlider(converterRangeModel);
        converterRangeModel.addChangeListener(e -> updateTextAndSlider(textField, slider));
        unitGroup.add(textField);
        unitGroup.add(slider);
        unitGroup.setAlignmentY(TOP_ALIGNMENT);
        return unitGroup;
    }

    @NotNull
    private JFormattedTextField buildTextField() {
        final var numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(2);
        return formattedTextField(numberFormat);
    }

    @NotNull
    private static JPanel getUnitGroup() {
        return new JPanel() {
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(150, super.getPreferredSize().height);
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
    }

    @NotNull
    private JPanel createChooserPanel(final List<Unit> units) {
        //Create a sub-panel so the combo box isn't too tall and is sufficiently wide.
        final var chooserPanel = new JPanel();
        chooserPanel.setLayout(new BoxLayout(chooserPanel, BoxLayout.PAGE_AXIS));
        chooserPanel.setOpaque(true);
        chooserPanel.setBackground(new Color(255, 0, 255));
        chooserPanel.add(buildUnitChooser(units));
        chooserPanel.add(Box.createHorizontalStrut(100));
        chooserPanel.setAlignmentY(TOP_ALIGNMENT);
        return chooserPanel;
    }

    private JComboBox<String> buildUnitChooser(final List<Unit> units) {
        final var unitChooser = new JComboBox<>(units.stream()
                .map(Unit::description)
                .toArray(String[]::new));
        converterRangeModel.setMultiplier(units.get(unitChooser.getSelectedIndex()).multiplier());
        unitChooser.addActionListener(e -> updateSliderModel(units.get(unitChooser.getSelectedIndex())));
        return unitChooser;
    }

    private void updateSliderModel(final Unit unit) {
        converterRangeModel.setMultiplier(unit.multiplier());
        onUnitChanged.stateChanged(new ChangeEvent(this));
    }

    private JFormattedTextField formattedTextField(final NumberFormat numberFormat) {
        final var formatter = new NumberFormatter(numberFormat);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);
        final var textField = new JFormattedTextField(formatter);
        textField.setColumns(10);
        textField.setValue(converterRangeModel.getDoubleValue());
        textField.addPropertyChangeListener(this::propertyChange);
        return textField;
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    /**
     * Returns the multiplier (units/meter) for the currently
     * selected unit of measurement.
     */
    public double getMultiplier() {
        return converterRangeModel.getMultiplier();
    }

    /**
     * Updates the text field when the main data model is updated.
     */
    public void updateTextAndSlider(final JFormattedTextField textField, final JSlider slider) {
        final var min = converterRangeModel.getMinimum();
        final var max = converterRangeModel.getMaximum();
        final var value = converterRangeModel.getDoubleValue();
        final var formatter = (NumberFormatter) textField.getFormatter();

        formatter.setMinimum((double) min);
        formatter.setMaximum((double) max);
        textField.setValue(value);
        slider.repaint();
    }

    /**
     * Detects when the value of the text field (not necessarily the same
     * number as you'd get from getText) changes.
     */
    public void propertyChange(final PropertyChangeEvent e) {
        if ("value".equals(e.getPropertyName())) {
            final var value = (Number) e.getNewValue();
            converterRangeModel.setDoubleValue(value.doubleValue());
        }
    }

    public void setTittle(final String myTitle) {
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(myTitle),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    public void setOnUnitChanged(final ChangeListener changedListener) {
        onUnitChanged = changedListener;
    }
}
