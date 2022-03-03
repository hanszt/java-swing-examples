package hzt.colorpickersample;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ColorPickerSampleTest {

    @Test
    void testJPanelIsOpaque() {
        final var jPanel = ColorPickerSample.buildContent();
        assertTrue(jPanel.isOpaque());
    }
}
