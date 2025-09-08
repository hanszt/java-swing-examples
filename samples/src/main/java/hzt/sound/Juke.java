package hzt.sound;
/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;


/**
 * A JukeBox for sampled and midi sound files.  Features duration progress,
 * seek slider, pan and volume controls.
 *
 * @author Brian Lichtenwalter
 * @version @(#)Juke.java	1.21 02/02/06
 */
public final class Juke extends JPanel implements ControlContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(Juke.class);
    public static final String START = "Start";
    public static final String PAUSE = "Pause";
    public static final String SOUTH = "South";
    private final PlaybackMonitor playbackMonitor = new PlaybackMonitor();

    private final transient List<File> sounds = new ArrayList<>();
    private transient Thread thread;
    private transient Sequencer sequencer;
    private boolean midiEOM;
    private boolean audioEOM;
    private transient MidiChannel[] channels;
    private transient Object currentSound;
    private String currentName;
    private double duration;
    private int num;
    private boolean bump;
    private boolean paused = false;
    private JButton startB;
    private JButton pauseB;
    private JButton loopB;
    private JTable table;
    private JSlider panSlider;
    private JSlider gainSlider;
    private JSlider seekSlider;
    private final JukeTable jukeTable;
    private transient Loading loading;
    private transient Credits credits;
    private String errStr;

    public Juke(final String dirName) {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(5, 5, 5, 5));

        if (dirName != null) {
            loadJuke(dirName);
        }
        jukeTable = new JukeTable();
        final var splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jukeTable, new JukeControls());
        splitPane.setContinuousLayout(true);
        add(splitPane);
    }

    public void open() {
        try {
            sequencer = MidiSystem.getSequencer();
            if (sequencer instanceof final Synthesizer synthesizer) {
                channels = synthesizer.getChannels();
            }
        } catch (final MidiUnavailableException ex) {
            LOGGER.error("Midi unavailable", ex);
            return;
        }
        sequencer.addMetaEventListener(this::meta);
        credits = new Credits();
        credits.start();
    }


    public void close() {
        if (credits != null && credits.isAlive()) {
            credits.interrupt();
        }
        if (thread != null && startB != null) {
            startB.doClick(0);
        }
        if (jukeTable != null && jukeTable.frame != null) {
            jukeTable.frame.dispose();
            jukeTable.frame = null;
        }
        if (sequencer != null) {
            sequencer.close();
        }
    }

    public void loadJuke(final String name) {
        try {
            final var file = new File(name);
            if (file.isDirectory()) {
                final var files = file.list();
                for (final var s : Objects.requireNonNull(files)) {
                    final var leafFile = new File(file.getAbsolutePath(), s);
                    if (leafFile.isDirectory()) {
                        loadJuke(leafFile.getAbsolutePath());
                    } else {
                        addSound(leafFile);
                    }
                }
            } else if (file.exists()) {
                addSound(file);
            } else {
                LOGGER.error("file with name {} not processed", name);
            }
        } catch (final SecurityException ex) {
            LOGGER.debug("Security exception", ex);
            reportStatus(ex.toString());
            JavaSound.showInfoDialog();
        }
    }

    private void addSound(final File file) {
        final var s = file.getName().toLowerCase();
        final var b = s.endsWith(".wav") || s.endsWith(".rmf") || s.endsWith(".mid");
        final var b1 = s.endsWith(".au") || s.endsWith(".aif") || s.endsWith(".aiff");
        if (b || b1) {
            sounds.add(file);
        }
    }

    public boolean loadSound(final File file) {
        duration = 0.0;
        loading = new Loading();
        loading.start();
        currentName = file.getName();
        playbackMonitor.repaint();
        try {
            currentSound = AudioSystem.getAudioInputStream(file);
        } catch (final UnsupportedAudioFileException | IOException e1) {
            LOGGER.debug("UnsupportedAudioFileException | IOException", e1);
            try (final var is = new FileInputStream(file)) {
                currentSound = new BufferedInputStream(is, 1024);
            } catch (final IOException e3) {
                LOGGER.error("IOException", e3);
                currentSound = null;
                return false;
            }
        }
        loading.interrupt();

        // user pressed stop or changed tabs while loading
        if (sequencer == null) {
            currentSound = null;
            return false;
        }

        if (currentSound instanceof AudioInputStream stream) {
            try {
                var format = stream.getFormat();
                if ((format.getEncoding() == AudioFormat.Encoding.ULAW) ||
                        (format.getEncoding() == AudioFormat.Encoding.ALAW)) {
                    final var tmp = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            format.getSampleRate(),
                            format.getSampleSizeInBits() * 2,
                            format.getChannels(),
                            format.getFrameSize() * 2,
                            format.getFrameRate(),
                            true);
                    stream = AudioSystem.getAudioInputStream(tmp, stream);
                    format = tmp;
                }
                final var info = new DataLine.Info(
                        Clip.class,
                        stream.getFormat(),
                        ((int) stream.getFrameLength() *
                                format.getFrameSize()));

                final var clip = (Clip) AudioSystem.getLine(info);
                clip.addLineListener(this::update);
                clip.open(stream);
                currentSound = clip;
                seekSlider.setMaximum((int) stream.getFrameLength());
            } catch (final LineUnavailableException | IOException ex) {
                LOGGER.error("Could not open clip", ex);
                currentSound = null;
                return false;
            }
        } else if (currentSound instanceof Sequence || currentSound instanceof BufferedInputStream) {
            try {
                sequencer.open();
                if (currentSound instanceof final Sequence soundSequence) {
                    sequencer.setSequence(soundSequence);
                } else {
                    sequencer.setSequence((BufferedInputStream) currentSound);
                }
                seekSlider.setMaximum((int) (sequencer.getMicrosecondLength() / 1000));

            } catch (final InvalidMidiDataException imde) {
                LOGGER.error("Unsupported audio file.", imde);
                currentSound = null;
                return false;
            } catch (final IOException | MidiUnavailableException ex) {
                LOGGER.error("IOException | MidiUnavailableException", ex);
                currentSound = null;
                return false;
            }
        }

        seekSlider.setValue(0);

        // enable seek, pan, and gain sliders for sequences as well as clips
        seekSlider.setEnabled(true);
        panSlider.setEnabled(true);
        gainSlider.setEnabled(true);

        duration = getDuration();

        return true;
    }

    public void playSound() {
        playbackMonitor.start();
        setGain();
        setPan();
        midiEOM = audioEOM = bump = false;
        if (currentSound instanceof Sequence ||
                (currentSound instanceof BufferedInputStream && thread != null)) {
            sequencer.start();
            while (!midiEOM && thread != null && !bump) {
                try {
                    thread.sleep(99);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            sequencer.stop();
            sequencer.close();
        } else if (currentSound instanceof final Clip clip && thread != null) {
            clip.start();
            try {
                Thread.sleep(99);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            while ((paused || clip.isActive()) && thread != null && !bump) {
                try {
                    Thread.sleep(99);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            clip.stop();
            clip.close();
        }
        currentSound = null;
        playbackMonitor.stop();
    }


    public double getDuration() {
        var duration = 0.0;
        if (currentSound instanceof final Sequence sequence) {
            duration = sequence.getMicrosecondLength() / 1000000.0;
        } else if (currentSound instanceof BufferedInputStream) {
            duration = sequencer.getMicrosecondLength() / 1000000.0;
        } else if (currentSound instanceof final Clip clip) {
            duration = clip.getBufferSize() /
                    (clip.getFormat().getFrameSize() * clip.getFormat().getFrameRate());
        }
        return duration;
    }

    public double getSeconds() {
        var seconds = 0.0;
        if (currentSound instanceof final Clip clip) {
            seconds = clip.getFramePosition() / clip.getFormat().getFrameRate();
        } else if ((currentSound instanceof Sequence) || (currentSound instanceof BufferedInputStream)) {
            try {
                seconds = sequencer.getMicrosecondPosition() / 1000000.0;
            } catch (final IllegalStateException e) {
                LOGGER.error("TEMP: IllegalStateException on sequencer.getMicrosecondPosition(): ", e);
            }
        }
        return seconds;
    }


    public void update(final LineEvent event) {
        if (event.getType() == LineEvent.Type.STOP && !paused) {
            audioEOM = true;
        }
    }


    public void meta(final MetaMessage message) {
        final var END_OF_TRACK = 47;
        if (message.getType() == END_OF_TRACK) {
            midiEOM = true;
        }
    }


    private void reportStatus(final String msg) {
        if ((errStr = msg) != null) {
            LOGGER.error(errStr);
            playbackMonitor.repaint();
        }
        if (credits != null && credits.isAlive()) {
            credits.interrupt();
        }
    }


    public Thread getThread() {
        return thread;
    }

    public void startJuke() {
        thread = new Thread(this::run);
        thread.setName("Juke");
        thread.start();
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
        }
        thread = null;
    }

    public void run() {
        do {
            table.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
            for (; num < sounds.size() && thread != null; num++) {
                table.scrollRectToVisible(new Rectangle(0, (num + 2) * (table.getRowHeight() + table.getRowMargin()), 1, 1));
                table.setRowSelectionInterval(num, num);
                if (loadSound(sounds.get(num))) {
                    playSound();
                }
                // take a little break between sounds
                try {
                    thread.sleep(222);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            num = 0;
        } while (loopB.isSelected() && thread != null);

        if (thread != null) {
            startB.doClick();
        }
        thread = null;
        currentName = null;
        currentSound = null;
        playbackMonitor.repaint();
    }


    public void setPan() {
        final var value = panSlider.getValue();

        if (currentSound instanceof final Clip clip) {
            final var panControl = (FloatControl) clip.getControl(FloatControl.Type.PAN);
            panControl.setValue((float) (value / 100.0));
        } else if (currentSound instanceof Sequence || currentSound instanceof BufferedInputStream) {
            for (final var channel : channels) {
                channel.controlChange(10, (int) (((double) value + 100.0) / 200.0 * 127.0));
            }
        }
    }


    public void setGain() {
        final var value = gainSlider.getValue() / 100.0;
        if (currentSound instanceof final Clip clip) {
            final var gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            final var dB = (float)
                    (Math.log(value == 0.0 ? 0.0001 : value) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        } else if (currentSound instanceof Sequence || currentSound instanceof BufferedInputStream) {
            for (var i = 0; i < channels.length; i++) {
                channels[i].controlChange(7, (int) (value * 127.0));
            }
        }
    }


    /**
     * GUI controls for start, stop, previous, next, pan and gain.
     */
    private final class JukeControls extends JPanel implements ChangeListener {

        public JukeControls() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            final var p1 = new JPanel();
            p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
            p1.setBorder(new EmptyBorder(10, 0, 5, 0));

            final var p3 = new JPanel();
            final var prevB = createButton("<<", false);
            prevB.addActionListener(this::actionPerformed);
            p3.add(prevB);
            final var nextB = createButton(">>", false);
            nextB.addActionListener(this::actionPerformed);
            p3.add(nextB);
            startB = createButton(START, sounds.size() != 0);
            startB.addActionListener(e -> startButtonAction(prevB, nextB));

            final var p2 = new JPanel();
            p2.add(startB);
            pauseB = createButton(PAUSE, false);
            pauseB.addActionListener(e -> pauseButtonAction());
            p2.add(pauseB);
            p1.add(p2);
            p1.add(p3);
            add(p1);

            final var p4 = new JPanel(new BorderLayout());
            final var eb = new EmptyBorder(5, 20, 10, 20);
            final var bb = new BevelBorder(BevelBorder.LOWERED);
            p4.setBorder(new CompoundBorder(eb, bb));
            p4.add(playbackMonitor);
            seekSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
            seekSlider.setEnabled(false);
            seekSlider.addChangeListener(this);
            p4.add(SOUTH, seekSlider);
            add(p4);

            final var p5 = new JPanel();
            p5.setLayout(new BoxLayout(p5, BoxLayout.X_AXIS));
            p5.setBorder(new EmptyBorder(5, 5, 10, 5));
            panSlider = new JSlider(-100, 100, 0);
            panSlider.addChangeListener(this);
            var tb = new TitledBorder(new EtchedBorder());
            tb.setTitle("Pan = 0.0");
            panSlider.setBorder(tb);
            p5.add(panSlider);
            gainSlider = new JSlider(0, 100, 80);
            gainSlider.addChangeListener(this);
            tb = new TitledBorder(new EtchedBorder());
            tb.setTitle("Gain = 80");
            gainSlider.setBorder(tb);
            p5.add(gainSlider);
            add(p5);
        }

        private JButton createButton(final String name, final boolean state) {
            final var b = new JButton(name);
            b.setEnabled(state);
            return b;
        }

        public void stateChanged(final ChangeEvent e) {
            final var slider = (JSlider) e.getSource();
            final var value = slider.getValue();
            if (slider.equals(seekSlider)) {
                if (currentSound instanceof Clip) {
                    ((Clip) currentSound).setFramePosition(value);
                } else if (currentSound instanceof Sequence) {
                    final var dur = ((Sequence) currentSound).getMicrosecondLength();
                    sequencer.setMicrosecondPosition(value * 1000);
                } else if (currentSound instanceof BufferedInputStream) {
                    final var dur = sequencer.getMicrosecondLength();
                    sequencer.setMicrosecondPosition(value * 1000);
                }
                playbackMonitor.repaint();
                return;
            }
            final var tb = (TitledBorder) slider.getBorder();
            var s = tb.getTitle();
            if (s.startsWith("Pan")) {
                s = s.substring(0, s.indexOf('=') + 1) + value / 100.0;
                if (currentSound != null) {
                    setPan();
                }
            } else if (s.startsWith("Gain")) {
                s = s.substring(0, s.indexOf('=') + 1) + value;
                if (currentSound != null) {
                    setGain();
                }
            }
            tb.setTitle(s);
            slider.repaint();
        }


        public void setComponentsEnabled(final boolean state, final JButton prevB, final JButton nextB) {
            seekSlider.setEnabled(state);
            pauseB.setEnabled(state);
            prevB.setEnabled(state);
            nextB.setEnabled(state);
        }

        private void startButtonAction(final JButton prevButton, final JButton nextButton) {
            if (startB.getText().equals(START)) {
                if (credits != null) {
                    credits.interrupt();
                }
                paused = false;
                num = table.getSelectedRow();
                num = num == -1 ? 0 : num;
                startJuke();
                startB.setText("Stop");
                setComponentsEnabled(true, prevButton, nextButton);
            } else if (startB.getText().equals("Stop")) {
                credits = new Credits();
                credits.start();
                paused = false;
                stop();
                startB.setText(START);
                pauseB.setText(PAUSE);
                setComponentsEnabled(false, prevButton, nextButton);
            }
        }

        private void pauseButtonAction() {
            if (pauseB.getText().equals(PAUSE)) {
                paused = true;
                if (currentSound instanceof Clip) {
                    ((Clip) currentSound).stop();
                } else if (currentSound instanceof Sequence || currentSound instanceof BufferedInputStream) {
                    sequencer.stop();
                }
                playbackMonitor.stop();
                pauseB.setText("Resume");
            } else if (pauseB.getText().equals("Resume")) {
                paused = false;
                if (currentSound instanceof Clip) {
                    ((Clip) currentSound).start();
                } else if (currentSound instanceof Sequence || currentSound instanceof BufferedInputStream) {
                    sequencer.start();
                }
                playbackMonitor.start();
                pauseB.setText(PAUSE);
            }
        }


        public void actionPerformed(final ActionEvent e) {
            final var button = (JButton) e.getSource();
            if (button.getText().equals("<<")) {
                paused = false;
                pauseB.setText(PAUSE);
                num = num - 1 < 0 ? sounds.size() - 1 : num - 2;
                bump = true;
            } else if (button.getText().equals(">>")) {
                paused = false;
                pauseB.setText(PAUSE);
                num = num + 1 == sounds.size() ? -1 : num;
                bump = true;
            }
        }
    }


    /**
     * Displays current sound and time elapsed.
     */
    public class PlaybackMonitor extends JPanel implements Runnable {

        String welcomeStr = "Welcome to Java Sound";
        Thread pbThread;
        Color black = new Color(20, 20, 20);
        Color jfcBlue = new Color(204, 204, 255);
        Color jfcDarkBlue = jfcBlue.darker();
        Font font24 = new Font("serif", Font.BOLD, 24);
        Font font28 = new Font("serif", Font.BOLD, 28);
        Font font42 = new Font("serif", Font.BOLD, 42);
        FontMetrics fm28, fm42;

        public PlaybackMonitor() {
            fm28 = getFontMetrics(font28);
            fm42 = getFontMetrics(font42);
        }

        public void paint(final Graphics g) {
            final var g2 = (Graphics2D) g;
            final var d = getSize();
            g2.setBackground(black);
            g2.clearRect(0, 0, d.width, d.height);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(jfcBlue);

            if (errStr != null) {
                g2.setFont(new Font("serif", Font.BOLD, 18));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.drawString("ERROR", 5, 20);
                final var as = new AttributedString(errStr);
                final var font12 = new Font("serif", Font.PLAIN, 12);
                as.addAttribute(TextAttribute.FONT, font12, 0, errStr.length());
                final var aci = as.getIterator();
                final var frc = g2.getFontRenderContext();
                final var lbm = new LineBreakMeasurer(aci, frc);
                float x = 5, y = 25;
                lbm.setPosition(0);
                while (lbm.getPosition() < errStr.length()) {
                    final var tl = lbm.nextLayout(d.width - x - 5);
                    if (!tl.isLeftToRight()) {
                        x = d.width - tl.getAdvance();
                    }
                    tl.draw(g2, x, y += tl.getAscent());
                    y += tl.getDescent() + tl.getLeading();
                }
            } else if (currentName == null) {
                final var frc = g2.getFontRenderContext();
                final var tl = new TextLayout(welcomeStr, font28, frc);
                final var x = (float) (d.width / 2 - tl.getBounds().getWidth() / 2);
                tl.draw(g2, x, d.height / 2);
                if (credits != null) {
                    credits.render(d, g2);
                }
            } else {
                g2.setFont(font24);
                g2.drawString(currentName, 5, fm28.getHeight() - 5);
                if (duration <= 0.0) {
                    loading.render(d, g2);
                } else {
                    var seconds = getSeconds();
                    if (midiEOM || audioEOM) {
                        seconds = duration;
                    }
                    if (seconds > 0.0) {
                        g2.setFont(font42);
                        var s = String.valueOf(seconds);
                        s = s.substring(0, s.indexOf('.') + 2);
                        final var strW = (int) fm42.getStringBounds(s, g2).getWidth();
                        g2.drawString(s, d.width - strW - 9, fm42.getAscent());

                        final var num = 30;
                        final var progress = (int) (seconds / duration * num);
                        final var ww = ((double) (d.width - 10) / (double) num);
                        final double hh = (int) (d.height * 0.25);
                        var x = 0.0;
                        for (; x < progress; x += 1.0) {
                            g2.fill(new Rectangle2D.Double(x * ww + 5, d.height - hh - 5, ww - 1, hh));
                        }
                        g2.setColor(jfcDarkBlue);
                        for (; x < num; x += 1.0) {
                            g2.fill(new Rectangle2D.Double(x * ww + 5, d.height - hh - 5, ww - 1, hh));
                        }
                    }
                }
            }
        }

        public void start() {
            pbThread = new Thread(this);
            pbThread.setName("PlaybackMonitor");
            pbThread.start();
        }

        public void stop() {
            if (pbThread != null) {
                pbThread.interrupt();
            }
            pbThread = null;
        }

        public void run() {
            while (pbThread != null) {
                try {
                    pbThread.sleep(99);
                } catch (final Exception e) {
                    break;
                }
                repaint();
            }
            pbThread = null;
        }
    } // End PlaybackMonitor


    /**
     * Table to display the name of the sound.
     */
    private final class JukeTable extends JPanel {

        private final transient TableModel dataModel;
        private JFrame frame;
        private JTextField textField;
        private JButton applyB;

        public JukeTable() {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(260, 300));

            final var names = new String[]{"#", "Name"};

            dataModel = new AbstractTableModel() {
                public int getColumnCount() {
                    return names.length;
                }

                public int getRowCount() {
                    return sounds.size();
                }

                public Object getValueAt(final int row, final int col) {
                    if (col == 0) {
                        return row;
                    } else if (col == 1) {
                        final var file = sounds.get(row);
                        return file.getName();
                    }
                    return null;
                }

                @Override
                public String getColumnName(final int col) {
                    return names[col];
                }

                @Override
                public Class<?> getColumnClass(final int c) {
                    return Optional.ofNullable(getValueAt(0, c)).map(Object::getClass).orElseThrow();
                }
            };

            table = new JTable(dataModel);
            final var col = table.getColumn("#");
            col.setMaxWidth(20);
            table.sizeColumnsToFit(0);

            final var scrollPane = new JScrollPane(table);
            final var eb = new EmptyBorder(5, 5, 2, 5);
            scrollPane.setBorder(new CompoundBorder(eb, new EtchedBorder()));
            add(scrollPane);

            final var p1 = new JPanel();
            var menuBar = new JMenuBar();
            menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
            var menu = (JMenu) menuBar.add(new JMenu("Add"));
            final var items = new String[]{"File or Directory of Files", "URL"};
            for (var i = 0; i < items.length; i++) {
                final var item = menu.add(new JMenuItem(items[i]));
                item.addActionListener(this::actionPerformed);
            }
            p1.add(menuBar);

            menuBar = new JMenuBar();
            menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
            menu = menuBar.add(new JMenu("Remove"));
            var item = menu.add(new JMenuItem("Selected"));
            item.addActionListener(this::actionPerformed);
            item = menu.add(new JMenuItem("All"));
            item.addActionListener(this::actionPerformed);
            p1.add(menuBar);

            loopB = addButton("loop", p1);
            loopB.setBackground(Color.gray);
            loopB.setSelected(true);

            add(SOUTH, p1);
        }


        private JButton addButton(final String name, final JPanel p) {
            final var b = new JButton(name);
            b.addActionListener(this::actionPerformed);
            p.add(b);
            return b;
        }


        private void doFrame(final String titleName) {
            final var w = 500;
            final var h = 130;
            final var panel = new JPanel(new BorderLayout());
            final var p1 = new JPanel();
            if (titleName.endsWith("URL")) {
                p1.add(new JLabel("URL :"));
                textField = new JTextField("http://foo.bar.com/foo.wav");
                textField.addActionListener(this::actionPerformed);
            } else {
                p1.add(new JLabel("File or Dir :"));
                final var sep = String.valueOf(System.getProperty("file.separator").toCharArray()[0]);
                final String text;
                try {
                    text = System.getProperty("user.dir") + sep;
                } catch (final SecurityException ex) {
                    reportStatus(ex.toString());
                    JavaSound.showInfoDialog();
                    LOGGER.debug("Security exception", ex);
                    return;
                }
                textField = new JTextField(text);
                textField.setPreferredSize(new Dimension(w - 100, 30));
                textField.addActionListener(this::actionPerformed);
            }
            p1.add(textField);
            panel.add(p1);
            final var p2 = new JPanel();
            applyB = addButton("Apply", p2);
            addButton("Cancel", p2);
            panel.add(SOUTH, p2);
            frame = new JFrame(titleName);
            frame.getContentPane().add("Center", panel);
            frame.pack();
            final var d = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setLocation(d.width / 2 - w / 2, d.height / 2 - h / 2);
            frame.setSize(w, h);
            frame.setVisible(true);
        }


        public void actionPerformed(final ActionEvent e) {
            final var object = e.getSource();
            if (object instanceof JTextField) {
                applyB.doClick();
            } else if (object instanceof JMenuItem) {
                final var mi = (JMenuItem) object;
                if (mi.getText().startsWith("File")) {
                    doFrame("Add File or Directory");
                } else if (mi.getText().equals("URL")) {
                    doFrame("Add URL");
                } else if (mi.getText().equals("Selected")) {
                    final var rows = table.getSelectedRows();
                    final List<File> tmp = new Vector();
                    for (final var row : rows) {
                        tmp.add(sounds.get(row));
                    }
                    sounds.removeAll(tmp);
                    tableChanged();
                } else if (mi.getText().equals("All")) {
                    sounds.clear();
                    tableChanged();
                }
            } else if (object instanceof JButton) {
                final var button = (JButton) e.getSource();
                if (button.getText().equals("Apply")) {
                    final var name = textField.getText().trim();
                    if (name.startsWith("http") || name.startsWith("file")) {
                        try {
                            sounds.add(new File(name));
                        } catch (final RuntimeException ex) {
                            LOGGER.error("", ex);
                        }
                    } else {
                        loadJuke(name);
                    }
                    tableChanged();
                } else if (button.getText().equals("Cancel")) {
                    frame.dispose();
                    frame = null;
                    errStr = null;
                    playbackMonitor.repaint();
                } else if (button.getText().equals("loop")) {
                    loopB.setSelected(!loopB.isSelected());
                    loopB.setBackground(loopB.isSelected() ? Color.gray : Color.lightGray);
                }
                startB.setEnabled(sounds.size() != 0);
            }
        }

        public void tableChanged() {
            table.tableChanged(new TableModelEvent(dataModel));
        }
    }


    /**
     * Animation thread for when an audio file loads.
     */
    private final class Loading extends Thread {

        private double extent;
        private int incr;

        public void run() {
            extent = 360.0;
            incr = 10;
            while (true) {
                try {
                    sleep(99);
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
                playbackMonitor.repaint();
            }
        }

        public void render(final Dimension d, final Graphics2D g2) {
            if (isAlive()) {
                final var frc = g2.getFontRenderContext();
                final var tl = new TextLayout("Loading", g2.getFont(), frc);
                final var sw = (float) tl.getBounds().getWidth();
                tl.draw(g2, d.width - sw - 45, d.height - 10);
                final double x = d.width - 33;
                final double y = d.height - 30;
                final double ew = 25;
                final double eh = 25;
                g2.draw(new Ellipse2D.Double(x, y, ew, eh));
                g2.fill(new Arc2D.Double(x, y, ew, eh, 90, extent, Arc2D.PIE));
                if ((extent -= incr) < 0) {
                    extent = 350.0;
                }
            }
        }
    }


    /**
     * Animation thread for the contributors of Java Sound.
     */
    private final class Credits extends Thread {

        private int x;
        private Font font16 = new Font("serif", Font.PLAIN, 16);
        private String contributors = "Contributors : Kara Kytle, " +
                "Jan Borgersen, " + "Brian Lichtenwalter";
        private int strWidth = getFontMetrics(font16).stringWidth(contributors);

        @Override
        public void run() {
            x = -999;
            while (!playbackMonitor.isShowing()) {
                try {
                    sleep(999);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            for (var i = 0; i < 100; i++) {
                try {
                    sleep(99);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            while (true) {
                if (--x < -strWidth) {
                    x = playbackMonitor.getSize().width;
                }
                playbackMonitor.repaint();
                try {
                    sleep(99);
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        public void render(final Dimension d, final Graphics2D g2) {
            if (isAlive()) {
                g2.setFont(font16);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.drawString(contributors, x, d.height - 5);
            }
        }
    }


    public static void main(final String[] args) {
        final var media = "media";
        final var juke = new Juke(args.length == 0 ? media : args[0]);
        juke.open();
        final var f = new JFrame("Juke Box");
        f.addWindowListener(new WindowAdapter() {
            @Override
            @SuppressWarnings("all")
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

            @Override
            public void windowIconified(final WindowEvent e) {
                juke.credits.interrupt();
            }
        });
        f.getContentPane().add("Center", juke);
        f.pack();
        final var screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final var w = 750;
        final var h = 340;
        f.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2);
        f.setSize(w, h);
        f.setVisible(true);
        if (args.length > 0) {
            final var file = new File(args[0]);
            if (!file.isDirectory()) {
                LOGGER.info("usage: java Juke audioDirectory");
            }
        }
    }
} 
