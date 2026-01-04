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

import org.hzt.swing_utils.function.document_listeners.InsertUpdateListener;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

public final class TextAreaDemo {

    public static final int MINIMUM_NR_OF_CHARS = 2;
    private static final String COMMIT_ACTION = "commit";

    private enum Mode {INSERT, COMPLETION}

    private final JFrame frame = new JFrame("TextAreaDemo");

    private final JTextArea textArea = new JTextArea();
    private final List<String> dictionaryWords = List.of("spark", "special", "spectacles", "spectacular", "swimming", "swing");
    private Mode mode = Mode.INSERT;

    public TextAreaDemo() {
        initComponents();
        textArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), COMMIT_ACTION);
        textArea.getActionMap().put(COMMIT_ACTION, new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent ev) {
                if (mode == Mode.COMPLETION) {
                    final var pos = textArea.getSelectionEnd();
                    textArea.insert(" ", pos);
                    textArea.setCaretPosition(pos + 1);
                    mode = Mode.INSERT;
                } else {
                    textArea.replaceSelection(String.format("%n"));
                }
            }
        });
    }

    private void initComponents() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        textArea.setColumns(20);
        textArea.setLineWrap(true);
        textArea.setRows(5);
        textArea.setWrapStyleWord(true);
        textArea.getDocument().addDocumentListener((InsertUpdateListener) this::lookForMatchingWord);

        final var label = new JLabel("Try typing 'spectacular' or 'Swing'...");
        final var jScrollPane = new JScrollPane(textArea);

        final var layout = new GroupLayout(frame.getContentPane());
        frame.getContentPane().setLayout(layout);

        configureHorizontalLayout(label, jScrollPane, layout);
        configureVerticalLayout(jScrollPane, layout, label);
        frame.pack();
    }

    private void lookForMatchingWord(final DocumentEvent ev) {
        if (ev.getLength() == 1) {
            final var textCursorPosition = ev.getOffset();
            lookForMatchingWordInDictionary(textCursorPosition);
        }
    }

    private static void configureHorizontalLayout(final JLabel label, final JScrollPane jScrollPane, final GroupLayout layout) {
        final var scrollPaneGroup = layout.createParallelGroup(Alignment.TRAILING);
        scrollPaneGroup.addComponent(jScrollPane, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE);
        scrollPaneGroup.addComponent(label, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE);

        final var sequentialGroup = layout.createSequentialGroup();
        sequentialGroup.addContainerGap();
        sequentialGroup.addGroup(scrollPaneGroup);
        sequentialGroup.addContainerGap();

        final var horizontalGroup = layout.createParallelGroup(Alignment.LEADING);
        horizontalGroup.addGroup(Alignment.TRAILING, sequentialGroup);
        layout.setHorizontalGroup(horizontalGroup);
    }

    private static void configureVerticalLayout(final JScrollPane jScrollPane, final GroupLayout layout, final JLabel label) {
        final var group = layout.createSequentialGroup();
        group.addContainerGap();
        group.addComponent(label);
        group.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
        group.addComponent(jScrollPane, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE);
        group.addContainerGap();

        final var verticalGroup = layout.createParallelGroup(Alignment.LEADING);
        verticalGroup.addGroup(group);

        layout.setVerticalGroup(verticalGroup);
    }

    private void lookForMatchingWordInDictionary(final int textCursorPosition) {
        try {
            final var content = textArea.getText(0, textCursorPosition + 1);
            final var wordStartPosition = startOfWord(textCursorPosition, content);
            if (textCursorPosition - wordStartPosition >= MINIMUM_NR_OF_CHARS) {
                final var prefix = content.substring(wordStartPosition + 1).toLowerCase();
                tryToFindMatch(textCursorPosition, wordStartPosition, prefix);
            }
        } catch (final BadLocationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void tryToFindMatch(final int textCursorPosition, final int wordStartPosition, final String prefix) {
        final var negativeIndex = Collections.binarySearch(dictionaryWords, prefix);
        if (negativeIndex < 0 && -negativeIndex <= dictionaryWords.size()) {
            final var match = dictionaryWords.get(-negativeIndex - 1);
            if (match.startsWith(prefix)) {
                // A completion is found
                final var completion = match.substring(textCursorPosition - wordStartPosition);
                // We cannot modify Document from within notification,
                // so we submit a task that does the change later
                SwingUtilities.invokeLater(() -> complete(completion, textCursorPosition + 1));
            }
        } else {
            mode = Mode.INSERT;
        }
    }

    private void complete(final String completion, final int position) {
        textArea.insert(completion, position);
        textArea.setCaretPosition(position + completion.length());
        textArea.moveCaretPosition(position);
        mode = Mode.COMPLETION;
    }

    private static int startOfWord(final int textCursorPosition, final String content) {
        var result = textCursorPosition;
        while (result >= 0) {
            if (!Character.isLetter(content.charAt(result))) {
                break;
            }
            result--;
        }
        return result;
    }

    private static void run() {
        //Turn off metal's use of bold fonts
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        final var textAreaDemo = new TextAreaDemo();
        textAreaDemo.frame.setVisible(true);
    }

    static void main(final String[] args) {
        SwingUtilities.invokeLater(TextAreaDemo::run);
    }

}
