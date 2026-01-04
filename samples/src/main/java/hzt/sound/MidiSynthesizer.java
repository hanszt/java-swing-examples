package hzt.sound;
/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


import org.hzt.swing_utils.function.mouse_listeners.MouseMovedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;

/**
 * Illustrates general MIDI melody instruments and MIDI controllers.
 *
 * @author Brian Lichtenwalter
 * @version @(#)MidiSynth.java	1.16 02/02/06
 */
public final class MidiSynthesizer extends JPanel implements ControlContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(MidiSynthesizer.class);

    private static final int PROGRAM = 192;
    private static final int NOTE_ON = 144;
    private static final int NOTE_OFF = 128;
    private static final int SUSTAIN = 64;
    private static final int REVERB = 91;
    private static final int ON = 0;
    private static final int OFF = 1;
    private static final String RECORD = "Record";

    private static final Color jfcBlue = new Color(204, 204, 255);
    private static final Color pink = new Color(255, 175, 175);

    private final Piano piano;

    private transient Sequencer sequencer;
    private transient Sequence sequence;
    private transient Synthesizer synthesizer;
    // current channel
    private transient ChannelData channelData;
    private transient Instrument[] instruments;
    private transient ChannelData[] channels;

    private final JCheckBox mouseOverCB = new JCheckBox("mouseOver", true);

    private JTable table;
    private boolean isRecord;
    private transient Track track;
    private long startTime;
    private RecordFrame recordFrame;
    private final ControlsPanel controlsPanel;


    public MidiSynthesizer() {
        setLayout(new BorderLayout());
        final var p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        final var eb = new EmptyBorder(5, 5, 5, 5);
        final var bb = new BevelBorder(BevelBorder.LOWERED);
        final var cb = new CompoundBorder(eb, bb);
        p.setBorder(new CompoundBorder(cb, eb));
        final var pp = new JPanel(new BorderLayout());
        pp.setBorder(new EmptyBorder(10, 20, 10, 5));
        piano = new Piano();
        pp.add(piano);
        p.add(pp);
        controlsPanel = new ControlsPanel();
        p.add(controlsPanel);
        p.add(new InstrumentsTable());
        add(p);
    }


    public void open() {
        try {
            if (synthesizer == null) {
                synthesizer = MidiSystem.getSynthesizer();
                if (synthesizer == null) {
                    LOGGER.error("getSynthesizer() failed!");
                    return;
                }
            }
            synthesizer.open();
            sequencer = MidiSystem.getSequencer();
            sequence = new Sequence(Sequence.PPQ, 10);
        } catch (final InvalidMidiDataException | MidiUnavailableException ex) {
            LOGGER.error("", ex);
            return;
        }

        final var sb = synthesizer.getDefaultSoundbank();
        if (sb != null) {
            instruments = synthesizer.getDefaultSoundbank().getInstruments();
            synthesizer.loadInstrument(instruments[0]);
        }
        final var midiChannels = synthesizer.getChannels();
        channels = new ChannelData[midiChannels.length];
        for (var i = 0; i < channels.length; i++) {
            final var data = new ChannelData(midiChannels[i], i);
            ChannelData.configureSliders(controlsPanel.getSliders());
            channels[i] = data;
        }
        channelData = channels[0];

        var lsm = table.getSelectionModel();
        lsm.setSelectionInterval(0, 0);
        lsm = table.getColumnModel().getSelectionModel();
        lsm.setSelectionInterval(0, 0);
    }

    public void close() {
        if (synthesizer != null) {
            synthesizer.close();
        }
        if (sequencer != null) {
            sequencer.close();
        }
        sequencer = null;
        synthesizer = null;
        instruments = null;
        channels = null;
        if (recordFrame != null) {
            recordFrame.dispose();
            recordFrame = null;
        }
    }

    /**
     * given 120 bpm:
     * (120 bpm) / (60 seconds per minute) = 2 beats per second
     * 2 / 1000 beats per millisecond
     * (2 * resolution) ticks per second
     * (2 * resolution)/1000 ticks per millisecond, or
     * (resolution / 500) ticks per millisecond
     * ticks = milliseconds * resolution / 500
     */
    public void createShortEvent(final int type, final int num) {
        final var message = new ShortMessage();
        try {
            final var millis = System.currentTimeMillis() - startTime;
            final var tick = millis * sequence.getResolution() / 500;
            message.setMessage(type + channelData.num, num, channelData.volume);
            final var event = new MidiEvent(message, tick);
            track.add(event);
        } catch (final InvalidMidiDataException ex) {
            LOGGER.error("Error creating short event", ex);
        }
    }

    /**
     * Black and white keys or notes on the piano.
     */
    private final class Key extends Rectangle {

        private int noteState = OFF;
        private final int keyNumber;

        public Key(final int x, final int y, final int width, final int height, final int num) {
            super(x, y, width, height);
            keyNumber = num;
        }

        public boolean isNoteOn() {
            return noteState == ON;
        }

        public void on() {
            setNoteState(ON);
            channelData.channel.noteOn(keyNumber, channelData.volume);
            if (isRecord) {
                createShortEvent(NOTE_ON, keyNumber);
            }
        }

        public void off() {
            setNoteState(OFF);
            channelData.channel.noteOff(keyNumber, channelData.volume);
            if (isRecord) {
                createShortEvent(NOTE_OFF, keyNumber);
            }
        }

        public void setNoteState(final int state) {
            noteState = state;
        }
    }

    /**
     * Piano renders black & white keys and plays the notes for a MIDI
     * channel.
     */
    private final class Piano extends JPanel {

        private static final int TRANSPOSE = 24;
        private static final int NOTES_IN_OCTAVE = 12;
        private static final int NR_OF_OCTAVES = 6;

        private static final int WHITE_KEY_WIDTH = 16;
        private static final int WHITE_KEY_HEIGHT = 80;
        private static final int WHITE_KEY_COUNT = 42;

        private static final int BLACK_KEY_WIDTH = WHITE_KEY_WIDTH / 2;
        private static final int BLACK_KEY_HEIGHT = WHITE_KEY_HEIGHT / 2;

        private final List<Key> pianoKeys = new ArrayList<>();
        private final List<Key> whiteKeys;
        private final List<Key> blackKeys;

        private Key prevKey;

        private Piano() {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(WHITE_KEY_COUNT * WHITE_KEY_WIDTH, WHITE_KEY_HEIGHT + 1));
            whiteKeys = createWhiteKeys();
            blackKeys = createBlackKeys();
            pianoKeys.addAll(blackKeys);
            pianoKeys.addAll(whiteKeys);

            addMouseMotionListener((MouseMovedListener) this::playKeyIfMouseMovedOver);
            addMouseListener(new PianoMouseMouseListener());
        }

        private void turnAllNotesOff() {
            for (final var channel : channels) {
                channel.channel.allNotesOff();
            }
            for (final var key : pianoKeys) {
                key.setNoteState(OFF);
            }
        }

        private void playKeyIfMouseMovedOver(final MouseEvent e) {
            if (mouseOverCB.isSelected()) {
                final var key = getKey(e.getPoint());
                if (prevKey != null && !prevKey.equals(key)) {
                    prevKey.off();
                }
                if (key != null && !key.equals(prevKey)) {
                    key.on();
                }
                prevKey = key;
                repaint();
            }
        }

        private class PianoMouseMouseListener extends MouseAdapter {
            @Override
            public void mousePressed(final MouseEvent e) {
                prevKey = getKey(e.getPoint());
                if (prevKey != null) {
                    prevKey.on();
                    repaint();
                }
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                if (prevKey != null) {
                    prevKey.off();
                    repaint();
                }
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                if (prevKey != null) {
                    prevKey.off();
                    repaint();
                    prevKey = null;
                }
            }
        }

        private List<Key> createBlackKeys() {
            final List<Key> keys = new ArrayList<>();
            var x = 0;
            for (var i = 0; i < NR_OF_OCTAVES; i++) {
                final var keyNum = i * NOTES_IN_OCTAVE + Piano.TRANSPOSE;
                x += WHITE_KEY_WIDTH;
                final var X_OFFSET = 4;
                keys.add(new Key(x - X_OFFSET, 0, BLACK_KEY_WIDTH, BLACK_KEY_HEIGHT, keyNum + 1));
                x += WHITE_KEY_WIDTH;
                keys.add(new Key(x - X_OFFSET, 0, BLACK_KEY_WIDTH, BLACK_KEY_HEIGHT, keyNum + 3));
                x += WHITE_KEY_WIDTH;
                x += WHITE_KEY_WIDTH;
                keys.add(new Key(x - X_OFFSET, 0, BLACK_KEY_WIDTH, BLACK_KEY_HEIGHT, keyNum + 6));
                x += WHITE_KEY_WIDTH;
                keys.add(new Key(x - X_OFFSET, 0, BLACK_KEY_WIDTH, BLACK_KEY_HEIGHT, keyNum + 8));
                x += WHITE_KEY_WIDTH;
                keys.add(new Key(x - X_OFFSET, 0, BLACK_KEY_WIDTH, BLACK_KEY_HEIGHT, keyNum + 10));
                x += WHITE_KEY_WIDTH;
            }
            return keys;
        }

        private List<Key> createWhiteKeys() {
            final List<Key> keys = new ArrayList<>();
            final var whiteIDs = new int[]{0, 2, 4, 5, 7, 9, 11};
            var x = 0;
            for (var i = 0; i < NR_OF_OCTAVES; i++) {
                for (var j = 0; j < NR_OF_OCTAVES + 1; j++) {
                    final var keyNum = i * NOTES_IN_OCTAVE + whiteIDs[j] + Piano.TRANSPOSE;
                    keys.add(new Key(x, 0, WHITE_KEY_WIDTH, WHITE_KEY_HEIGHT, keyNum));
                    x += WHITE_KEY_WIDTH;
                }
            }
            return keys;
        }

        public Key getKey(final Point point) {
            return pianoKeys.stream()
                    .filter(key -> key.contains(point))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public void paint(final Graphics g) {
            final var g2 = (Graphics2D) g;
            final var d = getSize();

            g2.setBackground(getBackground());
            g2.clearRect(0, 0, d.width, d.height);

            g2.setColor(Color.white);
            g2.fillRect(0, 0, WHITE_KEY_COUNT * WHITE_KEY_WIDTH, WHITE_KEY_HEIGHT);

            for (final var whiteKey : whiteKeys) {
                if (whiteKey.isNoteOn()) {
                    g2.setColor(isRecord ? pink : jfcBlue);
                    g2.fill(whiteKey);
                }
                g2.setColor(Color.black);
                g2.draw(whiteKey);
            }
            for (final var key : blackKeys) {
                if (key.isNoteOn()) {
                    g2.setColor(isRecord ? pink : jfcBlue);
                    g2.fill(key);
                    g2.setColor(Color.black);
                    g2.draw(key);
                } else {
                    g2.setColor(Color.black);
                    g2.fill(key);
                }
            }
        }
    }


    /**
     * Stores MidiChannel information.
     */
    private static final class ChannelData {

        private final MidiChannel channel;
        private final int num;

        private int volume = 64;
        private int row;
        private int col;

        public ChannelData(final MidiChannel channel, final int num) {
            this.channel = channel;
            this.num = num;
        }

        public void setComponentStates(final JTable table, final JCheckBox soloCB, final JCheckBox monoCB, final JCheckBox muteCB) {
            table.setRowSelectionInterval(row, row);
            table.setColumnSelectionInterval(col, col);

            soloCB.setSelected(channel.getSolo());
            monoCB.setSelected(channel.getMono());
            muteCB.setSelected(channel.getMute());
        }

        private static void configureSliders(final JSlider... sliders) {
            for (final var slider : sliders) {
                final var titledBorder = (TitledBorder) slider.getBorder();
                final var s = titledBorder.getTitle();
                titledBorder.setTitle(s.substring(0, s.indexOf('=') + 1) + slider.getValue());
                slider.repaint();
            }
        }

        public void setRow(final int row) {
            this.row = row;
        }

        public void setCol(final int col) {
            this.col = col;
        }
    }


    /**
     * Table for 128 general MIDI melody instruments.
     */
    private final class InstrumentsTable extends JPanel {

        private static final String[] names = {
                "Piano", "Chromatic Perc.", "Organ", "Guitar",
                "Bass", "Strings", "Ensemble", "Brass",
                "Reed", "Pipe", "Synth Lead", "Synth Pad",
                "Synth Effects", "Ethnic", "Percussive", "Sound Effects"};
        private static final int N_ROWS = 8;
        // just show 128 instruments
        private static final int N_COLS = names.length;

        private InstrumentsTable() {
            setLayout(new BorderLayout());
            final TableModel dataModel = new InstrumentTableModel();

            table = new JTable(dataModel);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            final var rowChangeListener = table.getSelectionModel();
            rowChangeListener.addListSelectionListener(e -> newSelectionAction(e, channelData::setRow));

            final var columnChangeListener = table.getColumnModel().getSelectionModel();
            columnChangeListener.addListSelectionListener(e -> newSelectionAction(e, channelData::setCol));

            table.setPreferredScrollableViewportSize(new Dimension(N_COLS * 110, 200));
            table.setCellSelectionEnabled(true);
            table.setColumnSelectionAllowed(true);
            for (final var name : names) {
                final var column = table.getColumn(name);
                column.setPreferredWidth(110);
            }
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            final var scrollPane = new JScrollPane(table);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            add(scrollPane);
        }

        private final class InstrumentTableModel extends AbstractTableModel {

            public int getColumnCount() {
                return N_COLS;
            }

            public int getRowCount() {
                return N_ROWS;
            }

            public Object getValueAt(final int r, final int c) {
                return instruments != null ? instruments[c * N_ROWS + r].getName() : Integer.toString(c * N_ROWS + r);
            }

            @Override
            public String getColumnName(final int c) {
                return names[c];
            }

            @Override
            public Class<?> getColumnClass(final int c) {
                return getValueAt(0, c).getClass();
            }
        }

        private void newSelectionAction(final ListSelectionEvent e, final IntConsumer consumer) {
            final var model = (ListSelectionModel) e.getSource();
            if (!model.isSelectionEmpty()) {
                consumer.accept(model.getMinSelectionIndex());
            }
            programChange(channelData.col * N_ROWS + channelData.row);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(800, 170);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(800, 170);
        }

        private void programChange(final int program) {
            if (instruments != null) {
                synthesizer.loadInstrument(instruments[program]);
            }
            channelData.channel.programChange(program);
            if (isRecord) {
                createShortEvent(PROGRAM, program);
            }
        }
    }


    /**
     * A collection of MIDI controllers.
     */
    private final class ControlsPanel extends JPanel {

        private final JSlider velocitySlider;
        private final JSlider pressureSlider;
        private final JSlider pitchBendSlider;
        private final JSlider reverbSlider;
        private final JCheckBox soloCB;
        private final JCheckBox monoCB;
        private final JCheckBox muteCB;

        public ControlsPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(5, 10, 5, 10));

            final var sliderPanel = new JPanel();
            sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.X_AXIS));

            velocitySlider = addSlider(sliderPanel, "Volume", 127, 64, this::adjustVolume);
            pressureSlider = addSlider(sliderPanel, "Pressure", 127, 64, this::adjustPressure);
            reverbSlider = addSlider(sliderPanel, "Reverb", 127, 64, this::adjustReverb);
            pitchBendSlider = addSlider(sliderPanel, "Bend", 16383, 8192, this::adjustBend);

            sliderPanel.add(pitchBendSlider);
            sliderPanel.add(Box.createHorizontalStrut(5));
            sliderPanel.add(Box.createHorizontalStrut(10));
            add(sliderPanel);

            final var panel = new JPanel();
            panel.setBorder(new EmptyBorder(10, 0, 10, 0));
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

            final var combo = new JComboBox<String>();
            combo.setPreferredSize(new Dimension(120, 25));
            combo.setMaximumSize(new Dimension(120, 25));
            for (var i = 1; i <= 16; i++) {
                combo.addItem("Channel " + i);
            }
            combo.addItemListener(this::comboBoxAction);
            panel.add(combo);
            panel.add(Box.createHorizontalStrut(20));

            muteCB = new JCheckBox("Mute");
            muteCB.addItemListener(e -> channelData.channel.setMute(muteCB.isSelected()));
            panel.add(muteCB);
            soloCB = new JCheckBox("Solo");
            soloCB.addItemListener(e -> channelData.channel.setSolo(soloCB.isSelected()));
            panel.add(soloCB);
            monoCB = new JCheckBox("Mono");
            monoCB.addItemListener(e -> channelData.channel.setMono(monoCB.isSelected()));

            panel.add(monoCB);
            final var sustain = new JCheckBox("Sustain");
            sustain.addItemListener(e -> channelData.channel.controlChange(SUSTAIN, sustain.isSelected() ? 127 : 0));
            panel.add(sustain);

            final var notesOff = new JButton("All Notes Off");
            notesOff.addActionListener(e -> piano.turnAllNotesOff());
            panel.add(notesOff);
            panel.add(Box.createHorizontalStrut(10));
            panel.add(mouseOverCB);
            panel.add(Box.createHorizontalStrut(10));
            final var recordB = new JButton("Record...");
            recordB.addActionListener(e -> recordButtonAction());
            panel.add(recordB);
            add(panel);
        }

        private JSlider addSlider(final JPanel panel, final String name, final int max, final int value, final ChangeListener changeListener) {
            final var slider = new JSlider(SwingConstants.HORIZONTAL, 0, max, value);
            slider.addChangeListener(changeListener);
            final var tb = new TitledBorder(new EtchedBorder());
            tb.setTitle(name + " = " + value);
            slider.setBorder(tb);
            panel.add(slider);
            panel.add(Box.createHorizontalStrut(5));
            return slider;
        }

        private void adjustVolume(final ChangeEvent e) {
            channelData.volume = updateSliderAndGetValue((JSlider) e.getSource());
        }

        private void adjustPressure(final ChangeEvent e) {
            final var pressure = updateSliderAndGetValue((JSlider) e.getSource());
            channelData.channel.setChannelPressure(pressure);
        }

        private void adjustBend(final ChangeEvent e) {
            final var pitchBend = updateSliderAndGetValue((JSlider) e.getSource());
            channelData.channel.setPitchBend(pitchBend);
        }

        private void adjustReverb(final ChangeEvent e) {
            final var value = updateSliderAndGetValue((JSlider) e.getSource());
            channelData.channel.controlChange(REVERB, value);
        }

        private int updateSliderAndGetValue(final JSlider slider) {
            final var value = slider.getValue();
            final var tb = (TitledBorder) slider.getBorder();
            final var title = tb.getTitle();
            tb.setTitle(title.substring(0, title.indexOf('=') + 1) + value);
            slider.repaint();
            return value;
        }

        private void comboBoxAction(final ItemEvent e) {
            //noinspection unchecked
            final var combo = (JComboBox<String>) e.getSource();
            channelData = channels[combo.getSelectedIndex()];
            channelData.setComponentStates(table, soloCB, monoCB, muteCB);
        }

        private void recordButtonAction() {
            if (recordFrame != null) {
                recordFrame.toFront();
            } else {
                recordFrame = new RecordFrame();
            }
        }

        private JSlider[] getSliders() {
            return new JSlider[] {velocitySlider, pitchBendSlider, reverbSlider, pressureSlider};
        }
    }


    /**
     * A frame that allows for midi capture & saving the captured data.
     */
    private final class RecordFrame extends JFrame {

        private final transient List<TrackData> tracks = new ArrayList<>();
        private final transient TableModel dataModel;
        private final JTable table;

        public RecordFrame() {
            super("Midi Capture");
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(final WindowEvent e) {
                    recordFrame = null;
                }
            });
            final var recordButton = createButton(RECORD, true);
            final var playButton = createButton("Play", false);
            final var saveButton = createButton("Save...", false);

            sequencer.addMetaEventListener(e -> updateButtons(e, playButton, recordButton));
            try {
                sequence = new Sequence(Sequence.PPQ, 10);
            } catch (final InvalidMidiDataException e) {
                LOGGER.error("Invalid midi data", e);
            }
            final var recordPanel = new JPanel();
            recordPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            recordPanel.setLayout(new BoxLayout(recordPanel, BoxLayout.X_AXIS));

            recordPanel.add(recordButton);
            recordPanel.add(playButton);
            recordPanel.add(saveButton);

            recordButton.addActionListener(e -> recordAction(recordButton, playButton, saveButton));
            playButton.addActionListener(e -> playAction(recordButton, playButton));
            saveButton.addActionListener(e -> saveAction());

            getContentPane().add("North", recordPanel);

            final var names = new String[]{"Channel #", "Instrument"};

            dataModel = new AbstractTableModel() {
                public int getColumnCount() {
                    return names.length;
                }

                public int getRowCount() {
                    return tracks.size();
                }

                public Object getValueAt(final int row, final int col) {
                    if (col == 0) {
                        return (tracks.get(row)).chanNum;
                    } else if (col == 1) {
                        return (tracks.get(row)).name;
                    } else {
                        return null;
                    }
                }

                @Override
                public String getColumnName(final int col) {
                    return names[col];
                }

                @Override
                public Class<?> getColumnClass(final int c) {
                    return Objects.requireNonNull(getValueAt(0, c)).getClass();
                }

                @Override
                public void setValueAt(final Object val, final int row, final int col) {
                    if (col == 0) {
                        (tracks.get(row)).chanNum = (int) val;
                    }
                    if (col == 1) {
                        (tracks.get(row)).name = (String) val;
                    }
                }
            };

            table = new JTable(dataModel);
            final var col = table.getColumn("Channel #");
            col.setMaxWidth(65);
            table.sizeColumnsToFit(0);

            final var scrollPane = new JScrollPane(table);
            final var eb = new EmptyBorder(0, 5, 5, 5);
            scrollPane.setBorder(new CompoundBorder(eb, new EtchedBorder()));

            getContentPane().add("Center", scrollPane);
            pack();
            final var d = Toolkit.getDefaultToolkit().getScreenSize();
            final var w = 210;
            final var h = 160;
            setLocation(d.width / 2 - w / 2, d.height / 2 - h / 2);
            setSize(w, h);
            setVisible(true);
        }

        public JButton createButton(final String name, final boolean state) {
            final var button = new JButton(name);
            button.setFont(new Font("serif", Font.PLAIN, 10));
            button.setEnabled(state);
            return button;
        }

        private void recordAction(final JButton recordButton, final JButton playButton, final JButton saveButton) {
            isRecord = recordButton.getText().startsWith(RECORD);
            if (isRecord) {
                track = sequence.createTrack();
                startTime = System.currentTimeMillis();

                // add a program change right at the beginning of
                // the track for the current instrument
                createShortEvent(PROGRAM, channelData.col * 8 + channelData.row);

                recordButton.setText("Stop");
                playButton.setEnabled(false);
                saveButton.setEnabled(false);
            } else {
                final var name = instruments != null ?
                        instruments[channelData.col * 8 + channelData.row].getName() :
                        Integer.toString(channelData.col * 8 + channelData.row);

                tracks.add(new TrackData(channelData.num + 1, name));
                table.tableChanged(new TableModelEvent(dataModel));
                recordButton.setText(RECORD);
                playButton.setEnabled(true);
                saveButton.setEnabled(true);
            }
        }

        private void playAction(final JButton recordButton, final JButton playButton) {
            if (playButton.getText().startsWith("Play")) {
                try {
                    sequencer.open();
                    sequencer.setSequence(sequence);
                } catch (final MidiUnavailableException | InvalidMidiDataException ex) {
                    LOGGER.error("Midi unavailable or invalid", ex);
                }
                sequencer.start();
                playButton.setText("Stop");
                recordButton.setEnabled(false);
            } else {
                sequencer.stop();
                playButton.setText("Play");
                recordButton.setEnabled(true);
            }
        }

        private void saveAction() {
            try {
                final var file = new File(System.getProperty("user.dir"));
                final var fc = new JFileChooser(file);
                fc.setFileFilter(new FileFilter() {
                    public boolean accept(final File f) {
                        return f.isDirectory();
                    }

                    public String getDescription() {
                        return "Save as .mid file.";
                    }
                });
                if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    saveMidiFile(fc.getSelectedFile());
                }
            } catch (final SecurityException ex) {
                JavaSound.showInfoDialog();
                LOGGER.debug("Not the proper rights", ex);
            }
        }

        private void updateButtons(final MetaMessage message, final JButton playButton, final JButton recordButton) {
            final var END_OF_TRACK = 47;
            if (message.getType() == END_OF_TRACK) {
                playButton.setText("Play");
                recordButton.setEnabled(true);
            }
        }


        public void saveMidiFile(final File file) {
            try {
                final var fileTypes = MidiSystem.getMidiFileTypes(sequence);
                if (fileTypes.length == 0) {
                    LOGGER.warn("Can't save sequence");
                } else {
                    if (MidiSystem.write(sequence, fileTypes[0], file) == -1) {
                        throw new IllegalStateException("Problems writing to file");
                    }
                }
            } catch (final SecurityException ex) {
                JavaSound.showInfoDialog();
                LOGGER.debug("No access", ex);
            } catch (final IOException e) {
                LOGGER.error("Problem while saving file...", e);
            }
        }

        private static class TrackData {
            private int chanNum;
            private String name;

            public TrackData(final int chanNum, final String name) {
                this.chanNum = chanNum;
                this.name = name;
            }
        }
    }

    static void main(final String[] args) {
        final var midiSynthesizer = new MidiSynthesizer();
        midiSynthesizer.open();
        final var frame = new JFrame("Midi Synthesizer");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            @SuppressWarnings("all")
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.getContentPane().add("Center", midiSynthesizer);
        frame.pack();
        final var screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final var w = 760;
        final var h = 470;
        frame.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2);
        frame.setSize(w, h);
        frame.setVisible(true);
    }
} 
