package hzt.filechoosersample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class FileChooserSample {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileChooserSample.class);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileChooserSample::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("FileChooserSample");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel contentPane = createContentPane();
        contentPane.setOpaque(true);
        frame.setContentPane(contentPane);
        frame.pack();
        frame.setVisible(true);
    }

    private static JPanel createContentPane() {
        JPanel contentPane = new JPanel(new GridLayout(1, 0));
        JFileChooser fileChooser = configuredFileChooser();
        JTextArea textArea = new JTextArea();
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(fileChooser);
        splitPane.setBottomComponent(textArea);
        contentPane.add(splitPane);
        return contentPane;
    }

    private static JFileChooser configuredFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addActionListener(FileChooserSample::printContent);
        return fileChooser;
    }

    private static void printContent(ActionEvent e) {
        LOGGER.info("Start printing content");
        JFileChooser fileChooser = (JFileChooser) e.getSource();
        File file = fileChooser.getSelectedFile();
        try (Scanner input = new Scanner(file)) {
            while (input.hasNextLine()) {
                final var nextLine = input.nextLine();
                LOGGER.info(nextLine);
            }
        } catch (IOException io) {
            LOGGER.error("Something went wrong while printing content", io);
        }
        LOGGER.info("Content printed");
    }
}
