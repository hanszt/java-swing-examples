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

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

import static java.lang.System.err;

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
public class TreeIconDemo extends JPanel implements TreeSelectionListener {

    private final JEditorPane htmlPane;
    private final JTree tree;
    private final URL helpURL;

    public TreeIconDemo() {
        super(new GridLayout(1, 0));
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("The Java Series");
        createNodes(top);

        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);

        tree = new JTree(top);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        setIconForLeaveNodes();

        helpURL = initHelp();
        JScrollPane htmlView = new JScrollPane(htmlPane);
        JSplitPane splitPane = buildSplitPane(htmlView);
        this.add(splitPane);
    }

    private JSplitPane buildSplitPane(JScrollPane htmlView) {
        JScrollPane treeView = new JScrollPane(tree);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(treeView);
        splitPane.setBottomComponent(htmlView);

        Dimension minimumSize = new Dimension(100, 50);
        htmlView.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation(100);

        splitPane.setPreferredSize(new Dimension(500, 300));
        return splitPane;
    }

    private void setIconForLeaveNodes() {
        ImageIcon leafIcon = createImageIcon();
        if (leafIcon != null) {
            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
            renderer.setLeafIcon(leafIcon);
            tree.setCellRenderer(renderer);
        } else {
            err.println("Leaf icon missing; using default.");
        }
    }

    /**
     * Required by TreeSelectionListener interface.
     */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node != null) {
            Object nodeInfo = node.getUserObject();
            if (node.isLeaf()) {
                BookInfo book = (BookInfo) nodeInfo;
                displayURL(book.getBookURL());
            } else {
                displayURL(helpURL);
            }
        }
    }

    private static class BookInfo {

        private String bookName;
        private URL bookURL;

        public BookInfo(String book, String filename) {
            setBookName(book);
            setBookURL(TreeIconDemo.class.getResource(filename));
            if (getBookURL() == null) {
                err.println("Couldn't find file: " + filename);
            }
        }

        public String toString() {
            return getBookName();
        }

        public String getBookName() {
            return bookName;
        }

        public void setBookName(String bookName) {
            this.bookName = bookName;
        }

        public URL getBookURL() {
            return bookURL;
        }

        public void setBookURL(URL bookURL) {
            this.bookURL = bookURL;
        }
    }

    private URL initHelp() {
        String fileName = "TreeDemoHelp.html";
        URL url = TreeIconDemo.class.getResource(fileName);
        if (url == null) err.println("Couldn't open help file: " + fileName);
        displayURL(url);
        return url;
    }

    private void displayURL(URL url) {
        try {
            if (url != null) htmlPane.setPage(url);
            else htmlPane.setText("File Not Found");
        } catch (IOException e) {
            err.println("Attempted to read a bad URL: " + url);
        }
    }

    private void createNodes(DefaultMutableTreeNode top) {
        addJavaProgrammerBooks(top);
        addJavaImplementerBooks(top);
    }

    private void addJavaProgrammerBooks(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode category = new DefaultMutableTreeNode("Books for Java Programmers");
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

    private void addJavaImplementerBooks(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode category = new DefaultMutableTreeNode("Books for Java Implementers");
        top.add(category);
        category.add(new DefaultMutableTreeNode(
                new BookInfo("The Java Virtual Machine Specification", "vm.html")));
        category.add(new DefaultMutableTreeNode(
                new BookInfo("The Java Language Specification", "jls.html")));
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    protected static ImageIcon createImageIcon() {
        URL imgURL = TreeIconDemo.class.getResource("images/middle.gif");
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            err.println("Couldn't find file: " + "images/middle.gif");
            return null;
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("TreeIconDemo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        TreeIconDemo newContentPane = new TreeIconDemo();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(TreeIconDemo::createAndShowGUI);
    }
}
