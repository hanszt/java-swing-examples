package hzt.gui_form_sample;

import javax.swing.*;

public final class GuiFormExample {

    private JPanel mainPanel;
    private JButton button;
    private JTextField textField;
    private JCheckBox checkBox;

    private GuiFormExample() {
    }

    private void start() {
        buildFrame();
        button.addActionListener(e -> textField.setText("Hello!"));
        checkBox.addActionListener(e ->
                textField.setText(checkBox.isSelected() ? "Checkbox selected" : "Checkbox not selected"));
    }

    private void buildFrame() {
        final var frame = new JFrame("Gui form sample");
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(final String[] args) {
        new GuiFormExample().start();
    }
}
