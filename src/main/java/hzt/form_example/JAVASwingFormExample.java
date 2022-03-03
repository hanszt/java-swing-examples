package hzt.form_example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;

public class JAVASwingFormExample {

	private static final Logger LOGGER = LoggerFactory.getLogger(JAVASwingFormExample.class);

	public static final int TEXT_FIELD_WIDTH = 86;
	public static final int TEXT_FIELD_HEIGHT = 20;
	public static final int LABEL_WIDTH = 46;
	public static final int LABEL_HEIGHT = 14;
	public static final int TEXT_FIELD_X = 128;
	public static final int LABEL_X = 65;
	public static final String DEFAULT_COMBOBOX_ITEM = "Select";

	private final JFrame frame = new JFrame();
	private final JTextField nameTextField = new JTextField();
	private final JTextField phoneNumberField = new JTextField();
	private final JTextArea addressField = new JTextArea();
	private final JTextField emailIdField = new JTextField();
	private final JRadioButton maleRadioButton = new JRadioButton("");
	private final JRadioButton femaleRadioButton = new JRadioButton("");
	private final JComboBox<String> occupationComboBox = new JComboBox<>();
	private final JButton submitButton = new JButton("submit");

	public JAVASwingFormExample() {
		initialize();
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(JAVASwingFormExample::run);
	}

	private static void run() {
			JAVASwingFormExample application = new JAVASwingFormExample();
			application.frame.setVisible(true);
	}

	private static void occupationComboBoxAction(ActionEvent actionEvent) {
		LOGGER.info("Occupation combobox action");
	}

	private void initialize() {
		frame.setBounds(100, 100, 730, 489);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		configureLabeledTextField("Name", nameTextField, 28, TEXT_FIELD_WIDTH);
		configureLabeledTextField("Phone", phoneNumberField, 65, TEXT_FIELD_WIDTH);
		configureLabeledTextField("Email", emailIdField, 112, 212);
		configuredAddressField();
		configureClearButton();

		configureLabel("Sex", LABEL_X, 228, LABEL_WIDTH);
		configureLabel("Male", TEXT_FIELD_X, 228, LABEL_WIDTH);
		configureLabel("Female", 292, 228, LABEL_WIDTH);

		maleRadioButton.setBounds(337, 224, 109, 23);
		frame.getContentPane().add(maleRadioButton);

		femaleRadioButton.setBounds(162, 224, 109, 23);
		frame.getContentPane().add(femaleRadioButton);

		configureLabel("Occupation", LABEL_X, 288, 67);

		configureOccupationCombobox();
		configureSubmitButton();
	}

	private void configureLabel(String text, int labelX, int labelY, int labelWidth) {
		JLabel sexLabel = new JLabel(text);
		sexLabel.setBounds(labelX, labelY, labelWidth, LABEL_HEIGHT);
		frame.getContentPane().add(sexLabel);
	}

	private void configuredAddressField() {
		configureLabel("Address", LABEL_X, 162, LABEL_WIDTH);
		addressField.setBounds(126, 157, 212, 40);
		frame.getContentPane().add(addressField);
	}

	private void configureLabeledTextField(String name, JTextField textField, int y, int textFieldWidth) {
		configureLabel(name, LABEL_X, y, LABEL_WIDTH);
		textField.setBounds(TEXT_FIELD_X, y, textFieldWidth, TEXT_FIELD_HEIGHT);
		textField.setColumns(10);
		frame.getContentPane().add(textField);
	}

	private void configureClearButton() {
		JButton clearButton = new JButton("Clear");
		clearButton.setBounds(312, 387, 89, 23);
		frame.getContentPane().add(clearButton);
		clearButton.addActionListener(this::clear);
	}

	private void configureSubmitButton() {
		submitButton.setBackground(Color.BLUE);
		submitButton.setForeground(Color.MAGENTA);
		submitButton.setBounds(LABEL_X, 387, 89, 23);
		submitButton.addActionListener(this::submit);
		frame.getContentPane().add(submitButton);
	}

	private void configureOccupationCombobox() {
		occupationComboBox.addItem(DEFAULT_COMBOBOX_ITEM);
		occupationComboBox.addItem("Business");
		occupationComboBox.addItem("Engineer");
		occupationComboBox.addItem("Doctor");
		occupationComboBox.addItem("Student");
		occupationComboBox.addItem("Others");
		occupationComboBox.addActionListener(JAVASwingFormExample::occupationComboBoxAction);
		occupationComboBox.setBounds(180, 285, 91, TEXT_FIELD_HEIGHT);
		frame.getContentPane().add(occupationComboBox);
	}

	private void submit(ActionEvent e) {
		final var textFieldsEmpty = nameTextField.getText().isEmpty() || (phoneNumberField.getText().isEmpty())
				|| (emailIdField.getText().isEmpty()) || (addressField.getText().isEmpty());

		final var maleAndFemaleRadioButtonSelected = (femaleRadioButton.isSelected()) && (maleRadioButton.isSelected());

		final var comboBoxInDefaultState = occupationComboBox.getSelectedItem() != null &&
				occupationComboBox.getSelectedItem().equals(DEFAULT_COMBOBOX_ITEM);

		final var isDataMissing = textFieldsEmpty || maleAndFemaleRadioButtonSelected || comboBoxInDefaultState;
		if (isDataMissing) {
			JOptionPane.showMessageDialog(null, "Data Missing");
		} else {
			displaySubmittedData();
		}
	}

	private void displaySubmittedData() {
		String message = String.format("Data submitted:%n" +
				"Name: %s%n" +
				"Phone number: %s%n" +
				"Email: %s%n" +
				"Address: %s%n" +
				"Sex: %s%n" +
				"Occupation: %s%n",
				nameTextField.getText(), phoneNumberField.getText(), emailIdField.getText(), addressField.getText(),
				getSex(), occupationComboBox.getSelectedItem());
		JOptionPane.showMessageDialog(null, message);
	}

	private String getSex() {
		if (femaleRadioButton.isSelected()) {
			return "Female";
		} else if (maleRadioButton.isSelected()) {
			return "Male";
		} else {
			return "Not defined";
		}
	}

	private void clear(ActionEvent e) {
		phoneNumberField.setText(null);
		emailIdField.setText(null);
		nameTextField.setText(null);
		addressField.setText(null);
		maleRadioButton.setSelected(false);
		femaleRadioButton.setSelected(false);
		occupationComboBox.setSelectedItem(DEFAULT_COMBOBOX_ITEM);
	}
}
