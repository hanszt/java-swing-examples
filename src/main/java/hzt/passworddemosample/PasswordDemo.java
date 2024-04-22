/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package hzt.passworddemosample;

import org.hzt.swing_utils.function.window_listeners.WindowActivatedListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Base64;

/* PasswordDemo.java requires no other files. */

public class PasswordDemo {

    private static final String OK = "ok";
    private static final String HELP = "help";

    private final JFrame controllingFrame;
    private final JPasswordField passwordField;

    public PasswordDemo(final JFrame frame) {
        //Use the default FlowLayout.
        this.controllingFrame = frame;
        passwordField = new JPasswordField(10);
    }

    private JPanel buildContentPane() {
        passwordField.setActionCommand(OK);
        passwordField.addActionListener(this::checkEnteredPassword);

        final var label = new JLabel("Enter the password: ");
        label.setLabelFor(passwordField);

        final var buttonPane = createButtonPanel();

        //Lay out everything.
        final var textPane = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        textPane.add(label);
        textPane.add(passwordField);
        final var passwordPanel = new JPanel();
        passwordPanel.add(textPane);
        passwordPanel.add(buttonPane);
        return passwordPanel;
    }

    private static void run() {
        //Turn off metal's use of bold fonts
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        createAndShowGUI();
    }

    protected final JComponent createButtonPanel() {
        final var p = new JPanel(new GridLayout(0, 1));
        final var okButton = new JButton("OK");
        final var helpButton = new JButton("Help");

        okButton.setActionCommand(OK);
        helpButton.setActionCommand(HELP);
        okButton.addActionListener(this::checkEnteredPassword);
        helpButton.addActionListener(this::checkEnteredPassword);

        p.add(okButton);
        p.add(helpButton);

        return p;
    }

    public void checkEnteredPassword(final ActionEvent e) {
        final var cmd = e.getActionCommand();

        if (OK.equals(cmd)) { //Process the password.
            final var input = passwordField.getPassword();
            if (isPasswordCorrect(input)) {
                JOptionPane.showMessageDialog(controllingFrame,
                        "Success! You typed the right password.");
            } else {
                JOptionPane.showMessageDialog(controllingFrame,
                        "Invalid password. Try again.",
                        "Error Message",
                        JOptionPane.ERROR_MESSAGE);
            }

            //Zero out the possible password, for security.
            Arrays.fill(input, '0');

            passwordField.selectAll();
            resetFocus();
        } else { //The user has asked for help.
            JOptionPane.showMessageDialog(controllingFrame, """
                            You can get the password by searching this example's
                            source code for the string "correctPassword".
                            Or look at the section How to Use Password Fields in
                            the components section of The Java Tutorial.
                            """);
        }
    }

    /**
     * Checks the passed-in array against the correct password.
     * After this method returns, you should invoke eraseArray
     * on the passed-in array.
     */
    private static boolean isPasswordCorrect(final char[] input) {
        final byte[] correctPasswordEncoded = {89, 110, 86, 110, 89, 87, 74, 118, 98, 119, 61, 61};
        final var decoded = new String(Base64.getDecoder().decode(correctPasswordEncoded));
        return Arrays.equals(input, decoded.toCharArray());
    }

    //Must be called from the event dispatch thread.
    protected void resetFocus() {
        passwordField.requestFocusInWindow();
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        final var frame = new JFrame("PasswordDemo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        final var passwordDemo = new PasswordDemo(frame);
        final var contentPane = passwordDemo.buildContentPane();
        contentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(contentPane);

        //Make sure the focus goes to the right component
        //whenever the frame is initially given the focus.
        //Display the window.
        frame.addWindowListener((WindowActivatedListener) e -> passwordDemo.resetFocus());
        frame.pack();
        frame.setVisible(true);
    }

        public static void main(final String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(PasswordDemo::run);
    }
}
