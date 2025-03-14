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

package hzt.treedemoproject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

/**
 * A 1.4 application that requires the following additional files:
 * TreeDemoHelp.html
 * arnold.html
 * bloch.html
 * chan.html
 * jls.html
 * swingtutorial.html
 * tutorial.html
 * tutorialcont.html
 * vm.html
 */
public final class TreeIconDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(TreeIconDemo.class);

    private final JTree tree;
    private final URL helpURL;

    public TreeIconDemo(final JPanel mainPanel) {
        final var top = new DefaultMutableTreeNode("The Java Series");
        addJavaProgrammerBooks(top);
        addJavaImplementerBooks(top);

        final var htmlPane = new JEditorPane();
        htmlPane.setEditable(false);

        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(e -> updateTree(htmlPane));
        setIconForLeaveNodes();

        helpURL = initHelp(htmlPane);
        final var htmlView = new JScrollPane(htmlPane);
        final var splitPane = buildSplitPane(htmlView);
        mainPanel.add(splitPane);
    }

    private JSplitPane buildSplitPane(final JScrollPane htmlView) {
        final var treeView = new JScrollPane(tree);
        final var splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(htmlView);

        final var minimumSize = new Dimension(100, 50);
        htmlView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(100);

        splitPane.setPreferredSize(new Dimension(500, 300));
        return splitPane;
    }

    private void setIconForLeaveNodes() {
        createImageIcon().ifPresentOrElse(this::setIcon,
                () -> LOGGER.error("Leaf icon missing; using default."));
    }

    private void setIcon(final ImageIcon leafIcon) {
        final var renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(leafIcon);
        tree.setCellRenderer(renderer);
    }

    public void updateTree(final JEditorPane htmlPane) {
        final var node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node != null) {
            final var nodeInfo = node.getUserObject();
            if (node.isLeaf()) {
                final var book = (BookInfo) nodeInfo;
                displayURL(book.getBookURL(), htmlPane);
            } else {
                displayURL(helpURL, htmlPane);
            }
        }
    }

    private static class BookInfo {

        private final String bookName;
        private final URL bookURL;

        public BookInfo(final String book, final String filename) {
            bookName = book;
            bookURL = TreeIconDemo.class.getResource(filename);
            if (bookURL == null) {
                LOGGER.error("Couldn't find file: {}", filename);
            }
        }

        public String toString() {
            return getBookName();
        }

        public String getBookName() {
            return bookName;
        }

        public URL getBookURL() {
            return bookURL;
        }
    }

    private static URL initHelp(final JEditorPane htmlPane) {
        final var fileName = "TreeDemoHelp.html";
        final var url = TreeIconDemo.class.getResource(fileName);
        if (url == null) {
            LOGGER.error("Couldn't open help file: {}", fileName);
        }
        displayURL(url, htmlPane);
        return url;
    }

    private static void displayURL(final URL url, final JEditorPane htmlPane) {
        try {
            if (url != null) {
                htmlPane.setPage(url);
            } else {
                htmlPane.setText("File Not Found");
            }
        } catch (final IOException e) {
            LOGGER.error("Attempted to read a bad URL: {}", url, e);
        }
    }

    private static void addJavaProgrammerBooks(final DefaultMutableTreeNode top) {
        final var category = new DefaultMutableTreeNode("Books for Java Programmers");
        top.add(category);
        category.add(new DefaultMutableTreeNode(
                new BookInfo("The Java Tutorial: A Short Course on the Basics", "tutorial.html")));
        category.add(new DefaultMutableTreeNode(
                new BookInfo("The Java Tutorial Continued: The Rest of the JDK", "tutorialcont.html")));
        category.add(new DefaultMutableTreeNode(
                new BookInfo("The JFC Swing Tutorial: A Guide to Constructing GUIs", "swingtutorial.html")));
        category.add(new DefaultMutableTreeNode(
                new BookInfo("Effective Java Programming Language Guide", "bloch.html")));
        category.add(new DefaultMutableTreeNode(
                new BookInfo("The Java Programming Language", "arnold.html")));
        category.add(new DefaultMutableTreeNode(
                new BookInfo("The Java Developers Almanac", "chan.html")));
    }

    private static void addJavaImplementerBooks(final DefaultMutableTreeNode top) {
        final var category = new DefaultMutableTreeNode("Books for Java Implementers");
        top.add(category);
        category.add(new DefaultMutableTreeNode(
                new BookInfo("The Java Virtual Machine Specification", "vm.html")));
        category.add(new DefaultMutableTreeNode(
                new BookInfo("The Java Language Specification", "jls.html")));
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    private static Optional<ImageIcon> createImageIcon() {
        return Optional.ofNullable(TreeIconDemo.class.getResource("images/middle.gif")).map(ImageIcon::new);
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        final var frame = new JFrame("TreeIconDemo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final var mainPanel = new JPanel(new GridLayout(1, 0));
        //Create and set up the content pane.
        new TreeIconDemo(mainPanel);
        mainPanel.setOpaque(true); //content panes must be opaque
        frame.setContentPane(mainPanel);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(final String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(TreeIconDemo::createAndShowGUI);
    }
}
