package hzt.lookandfeel;

import javax.swing.*;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Map;

public class LookAndFeelSample implements ActionListener {

    private static final String LABEL_PREFIX = "Number of button clicks: ";

    private int numClicks = 0;
    private final JComboBox<String> lookAndFeelComboBox = new JComboBox<>();
    private final JLabel label = new JLabel(LABEL_PREFIX + "0    ");

    // Specify the look and feel to use by defining the LOOKANDFEEL constant
    // Valid values are: null (use the default), "Metal", "System", "Motif",
    // and "GTK"
    private static final String METAL = "Default Metal";

    // If you choose the Metal L&F, you can also choose a theme.
    // Specify the theme to use by defining the THEME constant
    // Valid values are: "DefaultMetal", "Ocean",  and "Test"

    public Component createComponents() {
        JButton button = new JButton("I'm a Swing button!");
        button.setMnemonic(KeyEvent.VK_I);
        button.addActionListener(this);
        label.setLabelFor(button);

        Map<String, LookAndFeel> lookAndFeelMap = getLookAndFeelMap();
        configureLookAndFeelComboBox(lookAndFeelMap);
        JPanel pane = new JPanel(new GridLayout(0, 1));
        pane.add(new JLabel("Choose look and feel:"));
        pane.add(lookAndFeelComboBox);
        pane.add(button);
        pane.add(label);
        pane.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        return pane;
    }

    private void configureLookAndFeelComboBox(Map<String, LookAndFeel> lookAndFeelMap) {
        lookAndFeelMap.keySet().forEach(lookAndFeelComboBox::addItem);
        lookAndFeelComboBox.addActionListener(e -> lookAndFeelComboboxAction(lookAndFeelMap));
        lookAndFeelComboBox.setSelectedItem("Motif");
    }

    private void lookAndFeelComboboxAction(Map<String, LookAndFeel> lookAndFeelMap) {
        String selected = (String) lookAndFeelComboBox.getSelectedItem();
        setLookAndFeel(lookAndFeelMap.get(selected));
    }

    public void actionPerformed(ActionEvent e) {
        numClicks++;
        label.setText(LABEL_PREFIX + numClicks);
    }

    private static void setLookAndFeel(LookAndFeel lookAndFeel) {
        try {
            System.out.println("setting look and feel to " + lookAndFeel);
            UIManager.setLookAndFeel(lookAndFeel.theme);
//            MetalLookAndFeel.setCurrentTheme(lookAndFeel.metalTheme);
        } catch (ClassNotFoundException e) {
            System.err.println("Couldn't find class for specified look and feel:" + lookAndFeel);
            System.err.println("Did you include the L&F library in the class path?");
            System.err.println("Using the default look and feel.");
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Can't use the specified look and feel (" + lookAndFeel + ") on this platform.");
            System.err.println("Using the default look and feel.");
        } catch (Exception e) {
            System.err.println("Couldn't get specified look and feel (" + lookAndFeel + "), for some reason.");
            System.err.println("Using the default look and feel.");
            e.printStackTrace();
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
        JFrame frame = new JFrame("SwingApplication");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        LookAndFeelSample app = new LookAndFeelSample();
        Component contents = app.createComponents();
        frame.getContentPane().add(contents, BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(LookAndFeelSample::createAndShowGUI);
    }

    private static class LookAndFeel {

        private final String theme;
        private final MetalTheme metalTheme;

        public LookAndFeel(String theme, MetalTheme metalTheme) {
            this.theme = theme;
            this.metalTheme = metalTheme;
        }

        @Override
        public String toString() {
            return "LookAndFeel{" +
                    "theme='" + theme + '\'' +
                    ", metalTheme=" + metalTheme +
                    '}';
        }
    }
}
