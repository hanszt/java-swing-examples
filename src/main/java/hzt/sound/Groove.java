package hzt.sound;
/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Rhythm Groove Box.  Program any beat you like, click on a cell.
 * Channel 10 (the rhythm channel) supports the 47 instrument sounds.
 * These sounds are the result of a program change to instrument 1.
 * <p>
 * Beat Pattern 1
 * <p>
 * |  1 sec         | 2 sec
 * 1 e + a  2 e + a   3 e + a  4 e + a
 * hh  x   x    x   x     x   x    x   x
 * sn           x                  x
 * kk  x     x      x     x
 * 0 1 2 3  4 5 6 7   8 9 1011 12131415
 * <p>
 * Hi-hat
 * on-off : 0-1, 2-3, 4-5, 6-7, 8-9, 10-11, 12-13, 14-15
 * <p>
 * snare :
 * on-off : 4-5, 12-13
 * <p>
 * bass :
 * on-off : 0-1, 3-4, 6-7, 8-9
 *
 * @author Brian Lichtenwalter
 * @version @(#)Groove.java	1.17 02/02/06
 */
public final class Groove implements ControlContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(Groove.class);

    private static final int ACOUSTIC_BASS = 35;
    private static final int ACOUSTIC_SNARE = 38;
    private static final int PEDAL_HIHAT = 44;
    private static final int CLOSED_HIHAT = 42;

    private static final int PROGRAM = 192;
    private static final int NOTE_ON = 144;
    private static final int NOTE_OFF = 128;
    private static final String START = "Start";
    private static final String STOP = "Stop";
    private static final String ROCK_BEAT_1 = "Rock Beat 1";
    private static final String ROCK_BEAT_2 = "Rock Beat 2";
    private static final String ROCK_BEAT_3 = "Rock Beat 3";
    private static final int CHAN = 9;

    private final TempoDial tempoDial = new TempoDial();
    private final TableModel dataModel;
    private final JTable table;
    private final JButton loopButton;
    private final JButton startButton;
    private final String[] instruments =
            {"Acoustic bass drum", "Bass drum 1", "Side stick", "Acoustic snare",
                    "Hand clap", "Electric snare", "Low floor tom", "Closed hi-hat",
                    "High floor tom", "Pedal hi-hat", "Low tom", "Open hi-hat",
                    "Low-mid tom", "Hi-mid tom", "Crash cymbal 1", "High tom",
                    "Ride cymbal 1", "Chinese cymbal", "Ride bell", "Tambourine",
                    "Splash cymbal", "Cowbell", "Crash cymbal 2", "Vibraslap",
                    "Ride cymbal 2", "Hi bongo", "Low bongo", "Mute hi conga",
                    "Open hi conga", "Low conga", "High timbale", "Low timbale",
                    "High agogo", "Low agogo", "Cabasa", "Maracas",
                    "Short whistle", "Long whistle", "Short guiro", "Long guiro",
                    "Claves", "Hi wood block", "Low wood block", "Mute cuica",
                    "Open cuica", "Mute triangle", "Open triangle"};
    private final String[] names = {"Instrument",
            "1", "e", "+", "a",
            "2", "e", "+", "a",
            "3", "e", "+", "a",
            "4", "e", "+", "a"};
    private final List<Data> data = new ArrayList<>(instruments.length);
    private final JPanel mainPanel;

    private Sequencer sequencer;
    private Track track;
    private int row;
    private int col;

    public Groove() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(5, 0));
        EmptyBorder emptyBorder = new EmptyBorder(5, 5, 5, 5);
        mainPanel.setBorder(emptyBorder);

        for (int i = 0, id = 35; i < instruments.length; i++, id++) {
            data.add(new Data(instruments[i], id));
        }

        dataModel = new DataModel();

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                setBackground((Color) value);
            }
        };

        table = new JTable(dataModel);
        table.getColumn(names[0]).setMinWidth(120);
        TableColumnModel tcm = table.getColumnModel();
        for (int i = 1; i < names.length; i++) {
            tcm.getColumn(i).setCellRenderer(renderer);
        }

        // Listener for row changes
        ListSelectionModel listSelectionModel = table.getSelectionModel();
        listSelectionModel.addListSelectionListener(this::updateRowBySelectionIndex);
        // Listener for column changes
        listSelectionModel = table.getColumnModel().getSelectionModel();
        listSelectionModel.addListSelectionListener(this::updateColor);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        SoftBevelBorder sbb = new SoftBevelBorder(BevelBorder.RAISED);
        rightPanel.setBorder(new CompoundBorder(sbb, emptyBorder));
        rightPanel.add(tempoDial);
        rightPanel.add(Box.createVerticalStrut(10));

        JPanel leftPanel = new JPanel(new GridLayout(0, 1, 2, 10));
        final Color background = mainPanel.getBackground();
        startButton = makeButton(START, background);
        startButton.addActionListener(e -> start(startButton));
        loopButton = makeButton("Loop", background);
        loopButton.addActionListener(e -> loop(loopButton));
        leftPanel.add(startButton);
        leftPanel.add(loopButton);
        final JButton clearButton = makeButton("Clear Table", background);
        clearButton.addActionListener(this::clear);
        leftPanel.add(clearButton);

        JComboBox<String> combo = new JComboBox<>();
        combo.addActionListener(e -> selectBeat(combo));
        combo.addItem(ROCK_BEAT_1);
        combo.addItem(ROCK_BEAT_2);
        combo.addItem(ROCK_BEAT_3);
        leftPanel.add(combo);

        rightPanel.add(leftPanel);
        rightPanel.add(Box.createVerticalStrut(120));
        mainPanel.add("West", rightPanel);
        mainPanel.add("Center", new JScrollPane(table));
    }

    private final class DataModel extends AbstractTableModel {

        public int getColumnCount() {
            return names.length;
        }

        public int getRowCount() {
            return data.size();
        }

        public Object getValueAt(int row, int col) {
            final Data d = Groove.this.data.get(row);
            return col == 0 ? d.name : d.staff[col - 1];
        }

        @Override
        public String getColumnName(int col) {
            return names[col];
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col != 0;
        }

        @Override
        public void setValueAt(Object aValue, int row, int col) {
            if (col == 0) {
                (data.get(row)).name = (String) aValue;
            } else {
                (data.get(row)).staff[col - 1] = (Color) aValue;
            }
        }
    }

    private void updateColor(ListSelectionEvent e) {
        ListSelectionModel sm = (ListSelectionModel) e.getSource();
        if (!sm.isSelectionEmpty()) {
            col = sm.getMinSelectionIndex();
        }
        if (col != 0) {
            final Color[] staff = (data.get(row)).staff;
            Color c = staff[col - 1];
            if (c.equals(Color.white)) {
                staff[col - 1] = Color.black;
            } else {
                staff[col - 1] = Color.white;
            }
            table.tableChanged(new TableModelEvent(dataModel));
        }
    }

    private void updateRowBySelectionIndex(ListSelectionEvent e) {
        ListSelectionModel sm = (ListSelectionModel) e.getSource();
        if (!sm.isSelectionEmpty()) {
            row = sm.getMinSelectionIndex();
        }
    }


    public void open() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
        } catch (MidiUnavailableException e) {
            LOGGER.error("Could not open sequencer", e);
        }
        tempoDial.setSequencer(sequencer);
        sequencer.addMetaEventListener(this::startSequencerAndSetTempo);
    }


    public void close() {
        if (startButton.getText().startsWith(STOP)) {
            startButton.doClick(0);
        }
        if (sequencer != null) {
            sequencer.close();
        }
        sequencer = null;
    }


    private static JButton makeButton(String bName, Color c) {
        JButton button = new JButton(bName);
        button.setBackground(c);
        return button;
    }


    private void buildTrackThenStartSequencer() {
        try {
            Sequence sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            createEvent(PROGRAM, 1, 0);
            buildTrack();
            // so we always have a track from 0 to 15.
            createEvent(PROGRAM, 1, 15);

            // set and start the sequencer.
            sequencer.setSequence(sequence);
        } catch (InvalidMidiDataException e) {
            LOGGER.error("", e);
        }
        sequencer.start();
        sequencer.setTempoInBPM(tempoDial.getTempo());

    }

    private void buildTrack() {
        for (Data d : data) {
            final Color[] staff = d.staff;
            for (int j = 0; j < staff.length; j++) {
                if (staff[j].equals(Color.black)) {
                    createEvent(NOTE_ON, d.id, j);
                    createEvent(NOTE_OFF, d.id, j + 1L);
                }
            }
        }
    }


    private void presetTracks(String trackType) {
        clearTable();

        switch (trackType) {
            case ROCK_BEAT_1 -> configureRockBeat1();
            case ROCK_BEAT_2 -> configureRockBeat2();
            case ROCK_BEAT_3 -> configureRockBeat3();
            default -> LOGGER.error("Unknown track type: {}", trackType);
        }
        table.tableChanged(new TableModelEvent(dataModel));
    }

    private void configureRockBeat3() {
        final int HAND_CLAP = 39;
        final int LO_TOM = 45;
        final int HI_TOM = 50;
        final int RIDE_BELL = 53;
        for (int i = 0; i < 16; i += 4) {
            setCell(RIDE_BELL, i);
        }
        for (int i = 2; i < 16; i += 4) {
            setCell(PEDAL_HIHAT, i);
        }
        setCell(HAND_CLAP, 4);
        setCell(HAND_CLAP, 12);
        setCell(HI_TOM, 13);
        setCell(LO_TOM, 14);
        int[] bass3 = {0, 3, 6, CHAN, 15};
        for (int j : bass3) {
            setCell(ACOUSTIC_BASS + 1, j);
        }
    }

    private void configureRockBeat2() {
        final int CRASH_CYMBAL1 = 49;
        for (int i = 0; i < 16; i += 4) {
            setCell(CRASH_CYMBAL1, i);
        }
        for (int i = 0; i < 16; i += 2) {
            setCell(PEDAL_HIHAT, i);
        }
        setCell(ACOUSTIC_SNARE, 4);
        setCell(ACOUSTIC_SNARE, 12);
        int[] bass2 = {0, 2, 3, 7, CHAN, 10, 15};
        for (int j : bass2) {
            setCell(ACOUSTIC_BASS, j);
        }
    }

    private void configureRockBeat1() {
        for (int i = 0; i < 16; i += 2) {
            setCell(CLOSED_HIHAT, i);
        }
        setCell(ACOUSTIC_SNARE, 4);
        setCell(ACOUSTIC_SNARE, 12);
        int[] bass1 = {0, 3, 6, 8};
        for (int j : bass1) {
            setCell(ACOUSTIC_BASS, j);
        }
    }


    private void setCell(int id, int tick) {
        for (Data d : data) {
            if (d.id == id) {
                d.staff[tick] = Color.black;
                break;
            }
        }
    }


    private void clearTable() {
        for (Data d : data) {
            Arrays.fill(d.staff, Color.white);
        }
    }


    private void createEvent(int type, int num, long tick) {
        ShortMessage message = new ShortMessage();
        try {
            int velocity = 100;
            message.setMessage(type, Groove.CHAN, num, velocity);
            MidiEvent event = new MidiEvent(message, tick);
            track.add(event);
        } catch (InvalidMidiDataException e) {
            LOGGER.error("Invalid midi data", e);
        }
    }


    public void startSequencerAndSetTempo(MetaMessage message) {
        final int END_OF_TRACK = 47;
        if (message.getType() == END_OF_TRACK) {
            if (loopButton.getBackground().equals(Color.gray)) {
                if (sequencer != null && sequencer.isOpen()) {
                    sequencer.start();
                    sequencer.setTempoInBPM(tempoDial.getTempo());
                }
            } else {
                startButton.setText(START);
            }
        }
    }

    private void selectBeat(JComboBox<String> beatComboBox) {
        presetTracks((String) Objects.requireNonNull(beatComboBox.getSelectedItem()));
        if (startButton.getText().startsWith(STOP)) {
            sequencer.stop();
            buildTrackThenStartSequencer();
        }
    }

    private void start(JButton startButton) {
        if (startButton.getText().startsWith(START)) {
            buildTrackThenStartSequencer();
            startButton.setText(STOP);
        } else {
            sequencer.stop();
            startButton.setText(START);
        }
    }

    private void loop(JButton button) {
        button.setSelected(!button.isSelected());
        if (loopButton.getBackground().equals(Color.gray)) {
            loopButton.setBackground(mainPanel.getBackground());
        } else {
            loopButton.setBackground(Color.gray);
        }
    }

    private void clear(ActionEvent e) {
        clearTable();
        table.tableChanged(new TableModelEvent(dataModel));
    }

    /**
     * Storage class for instrument and musical staff represented by color.
     */
    private static class Data {

        private String name;
        private final int id;
        private final Color[] staff = new Color[16];

        public Data(String name, int id) {
            this.name = name;
            this.id = id;
            Arrays.fill(staff, Color.white);
        }
    }


    public static void main(String[] args) {
        final Groove groove = new Groove();
        JFrame frame = new JFrame("Rhythm Groove Box");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            @SuppressWarnings("all")
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.getContentPane().add("Center", groove.mainPanel);
        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 640;
        int h = 440;
        frame.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2);
        frame.setSize(w, h);
        frame.setVisible(true);
        groove.open();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
