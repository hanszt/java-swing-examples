package hzt.rangeslider;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

/**
 * UI delegate for the RangeSlider component.  RangeSliderUI paints two thumbs,
 * one for the lower value and one for the upper value.
 */
final class RangeSliderUI extends BasicSliderUI {

    /** Color of selected range. */
    private final Color rangeColor;

    /** Location and size of thumb for upper value. */
    private Rectangle upperThumbRect;
    /**
     * Indicator that determines whether upper thumb is selected.
     */
    private boolean upperThumbSelected;

    /**
     * Indicator that determines whether lower thumb is being dragged.
     */
    private boolean lowerDragging;
    /**
     * Indicator that determines whether upper thumb is being dragged.
     */
    private boolean upperDragging;

    /**
     * Constructs a RangeSliderUI for the specified slider component.
     *
     * @param rangeSlider RangeSlider
     */
    public RangeSliderUI(final RangeSlider rangeSlider) {
        this.rangeColor = rangeSlider.rangeColor;
        super(rangeSlider);
    }

    /**
     * Installs this UI delegate on the specified component.
     */
    @Override
    public void installUI(final JComponent c) {
        upperThumbRect = new Rectangle();
        super.installUI(c);
    }

    /**
     * Creates a listener to handle track events in the specified slider.
     */
    @Override
    protected TrackListener createTrackListener(final JSlider slider) {
        return new RangeTrackListener();
    }

    /**
     * Creates a listener to handle change events in the specified slider.
     */
    @Override
    protected ChangeListener createChangeListener(final JSlider slider) {
        return new ChangeHandler();
    }

    /**
     * Updates the dimensions for both thumbs.
     */
    @Override
    protected void calculateThumbSize() {
        // Call superclass method for lower thumb size.
        super.calculateThumbSize();

        // Set upper thumb size.
        upperThumbRect.setSize(thumbRect.width, thumbRect.height);
    }

    /**
     * Updates the locations for both thumbs.
     */
    @Override
    protected void calculateThumbLocation() {
        // Call superclass method for lower thumb location.
        super.calculateThumbLocation();

        // Adjust upper value to snap to ticks if necessary.
        if (slider.getSnapToTicks()) {
            final var upperValue = slider.getValue() + slider.getExtent();
            final var majorTickSpacing = slider.getMajorTickSpacing();
            final var minorTickSpacing = slider.getMinorTickSpacing();
            var tickSpacing = 0;

            if (minorTickSpacing > 0) {
                tickSpacing = minorTickSpacing;
            } else if (majorTickSpacing > 0) {
                tickSpacing = majorTickSpacing;
            }

            if (tickSpacing != 0) {
                // If it's not on a tick, change the value
                var snappedValue = upperValue;
                if ((upperValue - slider.getMinimum()) % tickSpacing != 0) {
                    final var temp = (float) (upperValue - slider.getMinimum()) / (float) tickSpacing;
                    final var whichTick = Math.round(temp);
                    snappedValue = slider.getMinimum() + (whichTick * tickSpacing);
                }

                if (snappedValue != upperValue) {
                    slider.setExtent(snappedValue - slider.getValue());
                }
            }
        }

        // Calculate upper thumb location.  The thumb is centered over its 
        // value on the track.
        if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
            final var upperPosition = xPositionForValue(slider.getValue() + slider.getExtent());
            upperThumbRect.x = upperPosition - (upperThumbRect.width / 2);
            upperThumbRect.y = trackRect.y;

        } else {
            final var upperPosition = yPositionForValue(slider.getValue() + slider.getExtent());
            upperThumbRect.x = trackRect.x;
            upperThumbRect.y = upperPosition - (upperThumbRect.height / 2);
        }
    }

    /**
     * Returns the size of a thumb.
     */
    @Override
    protected Dimension getThumbSize() {
        return new Dimension(12, 12);
    }

    /**
     * Paints the slider.  The selected thumb is always painted on top of the
     * other thumb.
     */
    @Override
    public void paint(final Graphics g, final JComponent c) {
        super.paint(g, c);

        final var clipRect = g.getClipBounds();
        if (upperThumbSelected) {
            // Paint lower thumb first, then upper thumb.
            if (clipRect.intersects(thumbRect)) {
                paintLowerThumb(g);
            }
            if (clipRect.intersects(upperThumbRect)) {
                paintUpperThumb(g);
            }

        } else {
            // Paint upper thumb first, then lower thumb.
            if (clipRect.intersects(upperThumbRect)) {
                paintUpperThumb(g);
            }
            if (clipRect.intersects(thumbRect)) {
                paintLowerThumb(g);
            }
        }
    }

    /**
     * Paints the track.
     */
    @Override
    public void paintTrack(final Graphics g) {
        // Draw track.
        super.paintTrack(g);

        final var trackBounds = trackRect;

        if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
            // Determine position of selected range by moving from the middle
            // of one thumb to the other.
            final var lowerX = thumbRect.x + (thumbRect.width / 2);
            final var upperX = upperThumbRect.x + (upperThumbRect.width / 2);

            // Determine track position.
            final var cy = (trackBounds.height / 2) - 2;

            // Save color and shift position.
            final var oldColor = g.getColor();
            g.translate(trackBounds.x, trackBounds.y + cy);

            // Draw selected range.
            g.setColor(rangeColor);
            for (var y = 0; y <= 3; y++) {
                g.drawLine(lowerX - trackBounds.x, y, upperX - trackBounds.x, y);
            }

            // Restore position and color.
            g.translate(-trackBounds.x, -(trackBounds.y + cy));
            g.setColor(oldColor);

        } else {
            // Determine position of selected range by moving from the middle
            // of one thumb to the other.
            final var lowerY = thumbRect.x + (thumbRect.width / 2);
            final var upperY = upperThumbRect.x + (upperThumbRect.width / 2);

            // Determine track position.
            final var cx = (trackBounds.width / 2) - 2;

            // Save color and shift position.
            final var oldColor = g.getColor();
            g.translate(trackBounds.x + cx, trackBounds.y);

            // Draw selected range.
            g.setColor(rangeColor);
            for (var x = 0; x <= 3; x++) {
                g.drawLine(x, lowerY - trackBounds.y, x, upperY - trackBounds.y);
            }

            // Restore position and color.
            g.translate(-(trackBounds.x + cx), -trackBounds.y);
            g.setColor(oldColor);
        }
    }

    /**
     * Overrides superclass method to do nothing.  Thumb painting is handled
     * within the <code>paint()</code> method.
     */
    @Override
    public void paintThumb(final Graphics g) {
        // Do nothing.
    }

    /**
     * Paints the thumb for the lower value using the specified graphics object.
     */
    private void paintLowerThumb(final Graphics g) {
        final var knobBounds = thumbRect;
        final var w = knobBounds.width;
        final var h = knobBounds.height;

        // Create graphics copy.
        final var g2d = (Graphics2D) g.create();

        // Create default thumb shape.
        final var thumbShape = createThumbShape(w - 1, h - 1);

        // Draw thumb.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate(knobBounds.x, knobBounds.y);

        g2d.setColor(Color.CYAN);
        g2d.fill(thumbShape);

        g2d.setColor(Color.BLUE);
        g2d.draw(thumbShape);

        // Dispose graphics.
        g2d.dispose();
    }

    /**
     * Paints the thumb for the upper value using the specified graphics object.
     */
    private void paintUpperThumb(final Graphics g) {
        final var knobBounds = upperThumbRect;
        final var w = knobBounds.width;
        final var h = knobBounds.height;

        // Create graphics copy.
        final var g2d = (Graphics2D) g.create();

        // Create default thumb shape.
        final var thumbShape = createThumbShape(w - 1, h - 1);

        // Draw thumb.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate(knobBounds.x, knobBounds.y);

        g2d.setColor(Color.PINK);
        g2d.fill(thumbShape);

        g2d.setColor(Color.RED);
        g2d.draw(thumbShape);

        // Dispose graphics.
        g2d.dispose();
    }

    /**
     * Returns a Shape representing a thumb.
     */
    private Shape createThumbShape(final int width, final int height) {
        // Use circular shape.
        return new Ellipse2D.Double(0, 0, width, height);
    }

    /**
     * Moves the selected thumb in the specified direction by a block increment.
     * This method is called when the user presses the Page Up or Down keys.
     */
    @Override
    public void scrollByBlock(final int direction) {
        synchronized (slider) {
            var blockIncrement = (slider.getMaximum() - slider.getMinimum()) / 10;
            if (blockIncrement <= 0 && slider.getMaximum() > slider.getMinimum()) {
                blockIncrement = 1;
            }
            final var delta = blockIncrement * ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);

            if (upperThumbSelected) {
                final var oldValue = ((RangeSlider) slider).getUpperValue();
                ((RangeSlider) slider).setUpperValue(oldValue + delta);
            } else {
                final var oldValue = slider.getValue();
                slider.setValue(oldValue + delta);
            }
        }
    }

    /**
     * Moves the selected thumb in the specified direction by a unit increment.
     * This method is called when the user presses one of the arrow keys.
     */
    @Override
    public void scrollByUnit(final int direction) {
        synchronized (slider) {
            final var delta = (direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL;

            if (upperThumbSelected) {
                final var oldValue = ((RangeSlider) slider).getUpperValue();
                ((RangeSlider) slider).setUpperValue(oldValue + delta);
            } else {
                final var oldValue = slider.getValue();
                slider.setValue(oldValue + delta);
            }
        }
    }

    /**
     * Listener to handle model change events.  This calculates the thumb
     * locations and repaints the slider if the value change is not caused by
     * dragging a thumb.
     */
    public final class ChangeHandler implements ChangeListener {
        public void stateChanged(final ChangeEvent arg0) {
            if (!lowerDragging && !upperDragging) {
                calculateThumbLocation();
                slider.repaint();
            }
        }
    }

    /**
     * Listener to handle mouse movements in the slider track.
     */
    public final class RangeTrackListener extends TrackListener {

        @Override
        public void mousePressed(final MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }

            currentMouseX = e.getX();
            currentMouseY = e.getY();

            if (slider.isRequestFocusEnabled()) {
                slider.requestFocus();
            }

            // Determine which thumb is pressed.  If the upper thumb is 
            // selected (last one dragged), then check its position first;
            // otherwise check the position of the lower thumb first.
            var lowerPressed = false;
            var upperPressed = false;
            if (upperThumbSelected || slider.getMinimum() == slider.getValue()) {
                if (upperThumbRect.contains(currentMouseX, currentMouseY)) {
                    upperPressed = true;
                } else if (thumbRect.contains(currentMouseX, currentMouseY)) {
                    lowerPressed = true;
                }
            } else {
                if (thumbRect.contains(currentMouseX, currentMouseY)) {
                    lowerPressed = true;
                } else if (upperThumbRect.contains(currentMouseX, currentMouseY)) {
                    upperPressed = true;
                }
            }

            // Handle lower thumb pressed.
            if (lowerPressed) {
                switch (slider.getOrientation()) {
                    case SwingConstants.VERTICAL -> offset = currentMouseY - thumbRect.y;
                    case SwingConstants.HORIZONTAL -> offset = currentMouseX - thumbRect.x;
                }
                upperThumbSelected = false;
                lowerDragging = true;
                return;
            }
            lowerDragging = false;

            // Handle upper thumb pressed.
            if (upperPressed) {
                switch (slider.getOrientation()) {
                    case SwingConstants.VERTICAL -> offset = currentMouseY - upperThumbRect.y;
                    case SwingConstants.HORIZONTAL -> offset = currentMouseX - upperThumbRect.x;
                }
                upperThumbSelected = true;
                upperDragging = true;
                return;
            }
            upperDragging = false;
        }

        @Override
        public void mouseReleased(final MouseEvent e) {
            lowerDragging = false;
            upperDragging = false;
            slider.setValueIsAdjusting(false);
            super.mouseReleased(e);
        }

        @Override
        public void mouseDragged(final MouseEvent e) {
            if (!slider.isEnabled()) {
                return;
            }

            currentMouseX = e.getX();
            currentMouseY = e.getY();

            if (lowerDragging) {
                slider.setValueIsAdjusting(true);
                moveLowerThumb();

            } else if (upperDragging) {
                slider.setValueIsAdjusting(true);
                moveUpperThumb();
            }
        }

        @Override
        public boolean shouldScroll(final int direction) {
            return false;
        }

        /**
         * Moves the location of the lower thumb, and sets its corresponding
         * value in the slider.
         */
        private void moveLowerThumb() {
            switch (slider.getOrientation()) {
                case SwingConstants.VERTICAL -> {
                    final var halfThumbHeight = thumbRect.height / 2;
                    var thumbTop = currentMouseY - offset;
                    var trackTop = trackRect.y;
                    var trackBottom = trackRect.y + (trackRect.height - 1);
                    final var vMax = yPositionForValue(slider.getValue() + slider.getExtent());

                    // Apply bounds to thumb position.
                    if (drawInverted()) {
                        trackBottom = vMax;
                    } else {
                        trackTop = vMax;
                    }
                    thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
                    thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

                    setThumbLocation(thumbRect.x, thumbTop);

                    // Update slider value.
                    final var thumbMiddle = thumbTop + halfThumbHeight;
                    slider.setValue(valueForYPosition(thumbMiddle));
                }
                case SwingConstants.HORIZONTAL -> {
                    final var halfThumbWidth = thumbRect.width / 2;
                    var thumbLeft = currentMouseX - offset;
                    var trackLeft = trackRect.x;
                    var trackRight = trackRect.x + (trackRect.width - 1);
                    final var hMax = xPositionForValue(slider.getValue() + slider.getExtent());

                    // Apply bounds to thumb position.
                    if (drawInverted()) {
                        trackLeft = hMax;
                    } else {
                        trackRight = hMax;
                    }
                    thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
                    thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

                    setThumbLocation(thumbLeft, thumbRect.y);

                    // Update slider value.
                    final var thumbMiddle = thumbLeft + halfThumbWidth;
                    slider.setValue(valueForXPosition(thumbMiddle));
                }
            }
        }

        /**
         * Moves the location of the upper thumb, and sets its corresponding
         * value in the slider.
         */
        private void moveUpperThumb() {
            switch (slider.getOrientation()) {
                case SwingConstants.VERTICAL -> {
                    final var halfThumbHeight = thumbRect.height / 2;
                    var thumbTop = currentMouseY - offset;
                    var trackTop = trackRect.y;
                    var trackBottom = trackRect.y + (trackRect.height - 1);
                    final var vMin = yPositionForValue(slider.getValue());

                    // Apply bounds to thumb position.
                    if (drawInverted()) {
                        trackTop = vMin;
                    } else {
                        trackBottom = vMin;
                    }
                    thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
                    thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

                    setUpperThumbLocation(thumbRect.x, thumbTop);

                    // Update slider extent.
                    final var thumbMiddle = thumbTop + halfThumbHeight;
                    slider.setExtent(valueForYPosition(thumbMiddle) - slider.getValue());
                }
                case SwingConstants.HORIZONTAL -> {
                    final var halfThumbWidth = thumbRect.width / 2;
                    var thumbLeft = currentMouseX - offset;
                    var trackLeft = trackRect.x;
                    var trackRight = trackRect.x + (trackRect.width - 1);
                    final var hMin = xPositionForValue(slider.getValue());

                    // Apply bounds to thumb position.
                    if (drawInverted()) {
                        trackRight = hMin;
                    } else {
                        trackLeft = hMin;
                    }
                    thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
                    thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

                    setUpperThumbLocation(thumbLeft, thumbRect.y);

                    // Update slider extent.
                    final var thumbMiddle = thumbLeft + halfThumbWidth;
                    slider.setExtent(valueForXPosition(thumbMiddle) - slider.getValue());
                }
            }
        }

        /**
         * Sets the location of the upper thumb, and repaints the slider.  This is
         * called when the upper thumb is dragged to repaint the slider.  The
         * <code>setThumbLocation()</code> method performs the same task for the
         * lower thumb.
         */
        private void setUpperThumbLocation(final int x, final int y) {
            final var upperUnionRect = new Rectangle();
            upperUnionRect.setBounds(upperThumbRect);

            upperThumbRect.setLocation(x, y);

            SwingUtilities.computeUnion(upperThumbRect.x, upperThumbRect.y, upperThumbRect.width, upperThumbRect.height, upperUnionRect);
            slider.repaint(upperUnionRect.x, upperUnionRect.y, upperUnionRect.width, upperUnionRect.height);
        }
    }
}
