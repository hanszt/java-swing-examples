package hzt.filechoosersample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Scanner;

public record FileChooserSample() {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileChooserSample.class);

    public static void main() {
        SwingUtilities.invokeLater(new FileChooserSample()::createAndShowGUI);
    }

    private void createAndShowGUI() {
        final var frame = new JFrame("FileChooserSample");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        final var contentPane = createContentPane();
        contentPane.setOpaque(true);
        frame.setContentPane(contentPane);
        frame.pack();
        frame.setVisible(true);
    }

    private JPanel createContentPane() {
        final var splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(configuredFileChooser());
        splitPane.setBottomComponent(new JTextArea());
        final var contentPane = new JPanel(new GridLayout(1, 0));
        contentPane.add(splitPane);
        return contentPane;
    }

    private JFileChooser configuredFileChooser() {
        final var fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addActionListener(this::printContent);
        return fileChooser;
    }

    private void printContent(final ActionEvent e) {
        LOGGER.info("Start printing content");
        final var fileChooser = (JFileChooser) e.getSource();
        final var file = fileChooser.getSelectedFile();
        try (final var input = new Scanner(file)) {
            while (input.hasNextLine()) {
                final var nextLine = input.nextLine();
                LOGGER.info(nextLine);
            }
        } catch (final IOException io) {
            LOGGER.error("Something went wrong while printing content", io);
        }
        LOGGER.info("Content printed");
    }
}
