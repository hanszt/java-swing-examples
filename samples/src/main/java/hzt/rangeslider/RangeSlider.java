package hzt.rangeslider;

import javax.swing.JSlider;
import java.awt.*;

/**
 * An extension of JSlider to select a range of values using two thumb controls.
 * The thumb controls are used to select the lower and upper value of a range
 * with predetermined minimum and maximum values.
 * 
 * <p>Note that RangeSlider makes use of the default BoundedRangeModel, which 
 * supports an inner range defined by a value and an extent.  The upper value
 * returned by RangeSlider is simply the lower value plus the extent.</p>
 */
public final class RangeSlider extends JSlider {

    /**
     * Color of selected range.
     */
    final Color rangeColor;

    /**
     * Constructs a RangeSlider with default minimum and maximum values of 0 and 100.
     * @param rangeColor the range color
     */
    public RangeSlider(final Color rangeColor) {
        this.rangeColor = rangeColor;
        super();
        initSlider();
    }

    /**
     * Initializes the slider by setting default properties.
     */
    private void initSlider() {
        setOrientation(HORIZONTAL);
    }

    /**
     * Overrides the superclass method to install the UI delegate to draw two
     * thumbs.
     */
    @Override
    public void updateUI() {
        setUI(new RangeSliderUI(this));
        // Update UI for slider labels.  This must be called after updating the
        // UI of the slider.  Refer to JSlider.updateUI().
        updateLabelUIs();
    }

    /**
     * Sets the lower value in the range.
     */
    @Override
    public void setValue(final int value) {
        final var oldValue = getValue();
        if (oldValue == value) {
            return;
        }

        // Compute new value and extent to maintain upper value.
        final var oldExtent = getExtent();
        final var newValue = Math.clamp(value, getMinimum(), oldValue + oldExtent);
        final var newExtent = oldExtent + oldValue - newValue;

        // Set new value and extent, and fire a single change event.
        getModel().setRangeProperties(newValue, newExtent, getMinimum(), 
            getMaximum(), getValueIsAdjusting());
    }

    /**
     * Returns the upper value in the range.
     */
    public int getUpperValue() {
        return getValue() + getExtent();
    }

    /**
     * Sets the upper value in the range.
     */
    public void setUpperValue(final int value) {
        // Compute new extent.
        final var lowerValue = getValue();
        final var newExtent = Math.clamp(0, value - lowerValue, getMaximum() - lowerValue);
        
        // Set extent to set upper value.
        setExtent(newExtent);
    }
}
