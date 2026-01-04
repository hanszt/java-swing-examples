package hzt.sound;
/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * The Java Sound Samples : MidiSynth, Juke, CapturePlayback, Groove.
 *
 * @author Brian Lichtenwalter
 * @version @(#)JavaSound.java	1.17 02/02/06
 */
public final class JavaSound extends JPanel implements ControlContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaSound.class);
    
    private static final int SCENE_WIDTH = 760;
    private static final int SCENE_HEIGHT = 500;
    
    private final JTabbedPane tabPane = new JTabbedPane();
    private final transient List<ControlContext> demos = new ArrayList<>(4);
    private int index;


    public JavaSound(final String audioDirectory) {

        setLayout(new BorderLayout());

        final var menuBar = new JMenuBar();
        final var options = menuBar.add(new JMenu("Options"));
        final var item = options.add(new JMenuItem("Applet Info"));
        item.addActionListener(e -> showInfoDialog());
        add(menuBar, BorderLayout.NORTH);

        tabPane.addChangeListener(this::changeTab);

        final var eb = new EmptyBorder(5, 5, 5, 5);
        final var bb = new BevelBorder(BevelBorder.LOWERED);
        final var cb = new CompoundBorder(eb, bb);
        final var p = new JPanel(new BorderLayout());
        p.setBorder(new CompoundBorder(cb, new EmptyBorder(0, 0, 90, 0)));
        final var juke = new Juke(audioDirectory);
        p.add(juke);
        demos.add(juke);
        tabPane.addTab("Juke Box", p);

        new Thread(this::run).start();

        add(tabPane, BorderLayout.CENTER);
    }


    public void changeTab(final ChangeEvent e) {
        close();
        index = tabPane.getSelectedIndex();
        open();
    }


    public void close() {
        demos.get(index).close();
    }


    public void open() {
        demos.get(index).open();
    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(SCENE_WIDTH, SCENE_HEIGHT);
    }

    /**
     * Lazy load the tabbed pane with CapturePlayback, MidiSynth and Groove.
     */
    public void run() {
        final var eb = new EmptyBorder(5, 5, 5, 5);
        final var bb = new BevelBorder(BevelBorder.LOWERED);
        final var cb = new CompoundBorder(eb, bb);
        var p = new JPanel(new BorderLayout());
        p.setBorder(new CompoundBorder(cb, new EmptyBorder(0, 0, 90, 0)));
        final var capturePlaybackPanel = new CapturePlaybackPanel();
        demos.add(capturePlaybackPanel);
        p.add(capturePlaybackPanel);
        tabPane.addTab("Capture/Playback", p);

        final var midiSynthesizer = new MidiSynthesizer();
        demos.add(midiSynthesizer);
        tabPane.addTab("Midi Synthesizer", midiSynthesizer);

        p = new JPanel(new BorderLayout());
        p.setBorder(new CompoundBorder(cb, new EmptyBorder(0, 0, 5, 20)));
        final var groove = new Groove();
        demos.add(groove);
        p.add(groove.getMainPanel());
        tabPane.addTab("Groove Box", p);
    }


    static void main(final String[] args) {
        try {
            if (MidiSystem.getSequencer() == null) {
                LOGGER.error("MidiSystem Sequencer Unavailable, exiting!");
                System.exit(1);
            } 
            if (AudioSystem.getMixer(null) == null) {
                LOGGER.error("AudioSystem Unavailable, exiting!");
                System.exit(1);
            }
        } catch (final MidiUnavailableException ex) {
            LOGGER.error("Midi unavailable", ex);
            System.exit(1);
        }

        var media = "media";
        if (args.length > 0) {
            final var file = new File(args[0]);
            if (file.isDirectory()) {
                media = args[0];
            } else {
                LOGGER.info("usage: java JavaSound audioDirectory");
            }
        }

        final var demo = new JavaSound(media);
        final var f = new JFrame("Java Sound Demo");
        f.addWindowListener(new WindowAdapter() {
            @Override
            @SuppressWarnings("all")
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

            @Override
            public void windowDeiconified(final WindowEvent e) {
                demo.open();
            }

            @Override
            public void windowIconified(final WindowEvent e) {
                demo.close();
            }
        });
        f.getContentPane().add("Center", demo);
        f.pack();
        final var d = Toolkit.getDefaultToolkit().getScreenSize();
        f.setLocation(d.width / 2 - JavaSound.SCENE_WIDTH / 2, d.height / 2 - JavaSound.SCENE_HEIGHT / 2);
        f.setSize(new Dimension(JavaSound.SCENE_WIDTH, JavaSound.SCENE_HEIGHT));
        f.setVisible(true);
    }

    public static void showInfoDialog() {
        final var msg = """
                When running the Java Sound demo as an applet these permissions
                are necessary in order to load/save files and record audio :
                   
                    grant {
                      permission java.io.FilePermission "<<ALL FILES>>", "read", "write"
                      permission javax.sound.sampled.AudioPermission "record";
                      permission java.util.PropertyPermission "user.dir", "read"
                    };
                    
                The permissions need to be added to the .java.policy file.
                """;
        new Thread(() -> JOptionPane.showMessageDialog(null, msg,
                "Applet Info", JOptionPane.INFORMATION_MESSAGE)).start();
    }
}
