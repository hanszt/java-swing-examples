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

package hzt.textareasample;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.lang.System.out;

public class TextAreaDemo extends JFrame implements DocumentListener {

    public static final int MINIMUM_NR_OF_CHARS = 2;
    private static final String COMMIT_ACTION = "commit";

    private enum Mode {INSERT, COMPLETION;}

    private final JTextArea textArea;
    private final List<String> dictionaryWords;
    private Mode mode = Mode.INSERT;

    public TextAreaDemo() {
        super("TextAreaDemo");
        this.textArea = new JTextArea();
        initComponents();
        InputMap inputMap = textArea.getInputMap();
        ActionMap actionMap = textArea.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), COMMIT_ACTION);
        actionMap.put(COMMIT_ACTION, new CommitAction());
        dictionaryWords = List.of("spark", "special", "spectacles", "spectacular", "swing");
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        textArea.setColumns(20);
        textArea.setLineWrap(true);
        textArea.setRows(5);
        textArea.setWrapStyleWord(true);
        textArea.getDocument().addDocumentListener(this);

        JLabel label = new JLabel("Try typing 'spectacular' or 'Swing'...");
        JScrollPane jScrollPane = new JScrollPane(textArea);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        configureHorizontalLayout(label, jScrollPane, layout);
        configureVerticalLayout(jScrollPane, layout, label);
        pack();
    }

    private void configureHorizontalLayout(JLabel label, JScrollPane jScrollPane, GroupLayout layout) {
        ParallelGroup scrollPaneGroup = layout.createParallelGroup(Alignment.TRAILING);
        scrollPaneGroup.addComponent(jScrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE);
        scrollPaneGroup.addComponent(label, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE);

        SequentialGroup sequentialGroup = layout.createSequentialGroup();
        sequentialGroup.addContainerGap();
        sequentialGroup.addGroup(scrollPaneGroup);
        sequentialGroup.addContainerGap();

        ParallelGroup horizontalGroup = layout.createParallelGroup(Alignment.LEADING);
        horizontalGroup.addGroup(Alignment.TRAILING, sequentialGroup);
        layout.setHorizontalGroup(horizontalGroup);
    }

    private void configureVerticalLayout(JScrollPane jScrollPane, GroupLayout layout, JLabel label) {
        SequentialGroup group = layout.createSequentialGroup();
        group.addContainerGap();
        group.addComponent(label);
        group.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        group.addComponent(jScrollPane, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE);
        group.addContainerGap();

        ParallelGroup verticalGroup = layout.createParallelGroup(Alignment.LEADING);
        verticalGroup.addGroup(group);

        layout.setVerticalGroup(verticalGroup);
    }

    // Listener methods

    @Override
    public void changedUpdate(DocumentEvent ev) {
        out.println("Changed update: " + ev);
    }
    @Override
    public void removeUpdate(DocumentEvent ev) {
        out.println("Remove update: " + ev);
    }

    @Override
    public void insertUpdate(DocumentEvent ev) {
        if (ev.getLength() == 1) {
            int textCursorPosition = ev.getOffset();
            lookForMatchingWordInDictionary(textCursorPosition);
        }
    }

    private void lookForMatchingWordInDictionary(int textCursorPosition) {
        try {
            String content = textArea.getText(0, textCursorPosition + 1);
            int wordStartPosition = startOfWord(textCursorPosition, content);
            if (textCursorPosition - wordStartPosition >= MINIMUM_NR_OF_CHARS) {
                String prefix = content.substring(wordStartPosition + 1).toLowerCase();
                tryToFindMatch(textCursorPosition, wordStartPosition, prefix);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void tryToFindMatch(int textCursorPosition, int wordStartPosition, String prefix) {
        int negativeIndex = Collections.binarySearch(dictionaryWords, prefix);
        if (negativeIndex < 0 && -negativeIndex <= dictionaryWords.size()) {
            String match = dictionaryWords.get(-negativeIndex - 1);
            if (match.startsWith(prefix)) {
                // A completion is found
                String completion = match.substring(textCursorPosition - wordStartPosition);
                // We cannot modify Document from within notification,
                // so we submit a task that does the change later
                SwingUtilities.invokeLater(new CompletionTask(completion, textCursorPosition + 1));
            }
        } else mode = Mode.INSERT;
    }

    private int startOfWord(int textCursorPosition, String content) {
        int result = textCursorPosition;
        while (result >= 0) {
            if (!Character.isLetter(Objects.requireNonNull(content).charAt(result))) break;
            result--;
        }
        return result;
    }

    private class CompletionTask implements Runnable {

        String completion;

        int position;
        CompletionTask(String completion, int position) {
            this.completion = completion;
            this.position = position;
        }

        public void run() {
            textArea.insert(completion, position);
            textArea.setCaretPosition(position + completion.length());
            textArea.moveCaretPosition(position);
            mode = Mode.COMPLETION;
        }

    }
    private class CommitAction extends AbstractAction {

        public void actionPerformed(ActionEvent ev) {
            if (mode == Mode.COMPLETION) {
                int pos = textArea.getSelectionEnd();
                textArea.insert(" ", pos);
                textArea.setCaretPosition(pos + 1);
                mode = Mode.INSERT;
            } else {
                textArea.replaceSelection(String.format("%n"));
            }
        }
    }

    private static void run() {
        //Turn off metal's use of bold fonts
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        new TextAreaDemo().setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TextAreaDemo::run);
    }

}
