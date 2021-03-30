package hzt.filechoosersample;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class FileChooserSample {

    public static void main(String[] args) {
        FileChooserSample fileChooserSample = new FileChooserSample();
        SwingUtilities.invokeLater(fileChooserSample::createAndShowGUI);
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("FileChooserSample");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel contentPane = createContentPane();
        contentPane.setOpaque(true);
        frame.setContentPane(contentPane);
        frame.pack();
        frame.setVisible(true);
    }

    private JPanel createContentPane() {
        JPanel contentPane = new JPanel(new GridLayout(1, 0));
        JFileChooser fileChooser = configuredFileChooser();
        JTextArea textArea = new JTextArea();
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(fileChooser);
        splitPane.setBottomComponent(textArea);
        contentPane.add(splitPane);
        return contentPane;
    }

    private JFileChooser configuredFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addActionListener(this::printContent);
        return fileChooser;
    }

    private void printContent(ActionEvent e) {
        System.out.println("Print content");
        JFileChooser fileChooser = (JFileChooser) e.getSource();
        File file = fileChooser.getSelectedFile();
        try (Scanner input = new Scanner(file)) {
            while (input.hasNextLine()) {
                System.out.println(input.nextLine());
            }
        } catch (IOException io) {
            io.printStackTrace();
        }

    }
}
