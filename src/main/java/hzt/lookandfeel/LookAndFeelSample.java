package hzt.lookandfeel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Map;

public class LookAndFeelSample{

    private static final Logger LOGGER = LoggerFactory.getLogger(LookAndFeelSample.class);
    private static final String LABEL_PREFIX = "Number of button clicks: ";
    // Specify the look and feel to use by defining the LOOKANDFEEL constant
    // Valid values are: null (use the default), "Metal", "System", "Motif" and "GTK"
    private static final String METAL = "Default Metal";

    private int numClicks = 0;
    private final JComboBox<String> lookAndFeelComboBox = new JComboBox<>();
    private final JLabel label = new JLabel(LABEL_PREFIX + "0    ");

    public Component createComponents() {
        final var button = new JButton("I'm a Swing button!");
        button.setMnemonic(KeyEvent.VK_I);
        button.addActionListener(this::updateLabelText);
        label.setLabelFor(button);

        final var lookAndFeelMap = getLookAndFeelMap();
        configureLookAndFeelComboBox(lookAndFeelMap);
        final var pane = new JPanel(new GridLayout(0, 1));
        pane.add(new JLabel("Choose look and feel:"));
        pane.add(lookAndFeelComboBox);
        pane.add(button);
        pane.add(label);
        pane.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        return pane;
    }

    private void configureLookAndFeelComboBox(final Map<String, LookAndFeel> lookAndFeelMap) {
        lookAndFeelMap.keySet().forEach(lookAndFeelComboBox::addItem);
        lookAndFeelComboBox.addActionListener(e -> lookAndFeelComboboxAction(lookAndFeelMap));
        lookAndFeelComboBox.setSelectedItem("Motif");
    }

    private void lookAndFeelComboboxAction(final Map<String, LookAndFeel> lookAndFeelMap) {
        final var selected = (String) lookAndFeelComboBox.getSelectedItem();
        setLookAndFeel(lookAndFeelMap.get(selected));
    }

    public void updateLabelText(final ActionEvent e) {
        numClicks++;
        label.setText(LABEL_PREFIX + numClicks);
    }

    private static void setLookAndFeel(final LookAndFeel lookAndFeel) {
        try {
            LOGGER.info("setting look and feel to {}", lookAndFeel);
            UIManager.setLookAndFeel(lookAndFeel.theme);
        } catch (final UnsupportedLookAndFeelException | ClassNotFoundException |
                       InstantiationException | IllegalAccessException e) {
            LOGGER.error("Could not set look and feel", e);
        }
    }

    private static Map<String, LookAndFeel> getLookAndFeelMap() {
        return Map.of(
                METAL,
                new LookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName(), new DefaultMetalTheme()),
                "Metal ocean",
                new LookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName(), new OceanTheme()),
                "System",
                new LookAndFeel(UIManager.getSystemLookAndFeelClassName(), new DefaultMetalTheme()),
                "Motif",
                new LookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel", new DefaultMetalTheme()),
                "GTK",
                new LookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel", new DefaultMetalTheme()));
    }

    private static void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        final var frame = new JFrame("SwingApplication");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final var app = new LookAndFeelSample();
        final var contents = app.createComponents();
        frame.getContentPane().add(contents, BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(final String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(LookAndFeelSample::createAndShowGUI);
    }

    private record LookAndFeel(String theme, MetalTheme metalTheme) {

        @Override
        public String toString() {
            return "LookAndFeel{" +
                    "theme='" + theme + '\'' +
                    ", metalTheme=" + metalTheme +
                    '}';
        }
    }
}
