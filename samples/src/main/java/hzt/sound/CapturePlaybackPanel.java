package hzt.sound;
/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


import org.hzt.utils.sequences.Sequence;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.geom.Line2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Capture/Playback sample.  Record audio in different formats
 * and then playback the recorded audio.  The captured audio can
 * be saved either as a WAVE, AU or AIFF.  Or load an audio file
 * for streaming playback.
 *
 * @author Brian Lichtenwalter
 * @version @(#)CapturePlayback.java	1.12	02/02/06
 */
public final class CapturePlaybackPanel extends JPanel implements ControlContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(CapturePlaybackPanel.class);

    private static final int BUF_SIZE = 16384;
    private static final String RECORD = "Record";
    private static final String PAUSE = "Pause";

    private final FormatControls formatControls = new FormatControls();
    private final transient Capture capture = new Capture();
    private final transient Playback playback = new Playback();

    private transient AudioInputStream audioInputStream;
    private final SamplingGraph samplingGraph;

    private final JButton playButton;
    private final JButton recordButton;
    private final JButton pauseButton;
    private final JButton loadButton;
    private final JButton audioFileSaveButton;
    private final JButton aiffFileSaveButton;
    private final JButton waveFileSaveButton;
    private final JTextField textField;

    private String errStr;
    private double duration;
    private double seconds;
    private File file;
    private String fileName = "untitled";
    private final List<Line2D.Double> lines = new ArrayList<>();

    public CapturePlaybackPanel() {
        setLayout(new BorderLayout());
        final var sbb = new SoftBevelBorder(BevelBorder.LOWERED);
        setBorder(new EmptyBorder(5, 5, 5, 5));

        final var p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.add(formatControls);

        final var mainPanel = new JPanel();
        mainPanel.setBorder(sbb);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        final var buttonsPanel = new JPanel();
        buttonsPanel.setBorder(new EmptyBorder(10, 0, 5, 0));
        playButton = addButton("Play", buttonsPanel, false);
        recordButton = addButton(RECORD, buttonsPanel, true);
        pauseButton = addButton(PAUSE, buttonsPanel, false);
        loadButton = addButton("Load...", buttonsPanel, true);
        mainPanel.add(buttonsPanel);

        final var samplingPanel = new JPanel(new BorderLayout());
        samplingPanel.setBorder(new CompoundBorder(new EmptyBorder(10, 20, 20, 20), sbb));
        samplingGraph = new SamplingGraph();
        samplingPanel.add(samplingGraph);
        mainPanel.add(samplingPanel);

        final var savePanel = new JPanel();
        savePanel.setLayout(new BoxLayout(savePanel, BoxLayout.Y_AXIS));

        final var saveTFpanel = new JPanel();
        saveTFpanel.add(new JLabel("File to save:  "));
        textField = new JTextField(fileName);
        saveTFpanel.add(textField);
        textField.setPreferredSize(new Dimension(140, 25));
        savePanel.add(saveTFpanel);

        final var saveBpanel = new JPanel();
        audioFileSaveButton = addButton("Save AU", saveBpanel, false);
        aiffFileSaveButton = addButton("Save AIFF", saveBpanel, false);
        waveFileSaveButton = addButton("Save WAVE", saveBpanel, false);
        savePanel.add(saveBpanel);

        mainPanel.add(savePanel);

        p1.add(mainPanel);
        add(p1);
    }


    public void open() {
        //no action required
    }


    public void close() {
        if (playback.thread != null) {
            playButton.doClick(0);
        }
        if (capture.thread != null) {
            recordButton.doClick(0);
        }
    }


    private JButton addButton(final String name, final JPanel p, final boolean state) {
        final var b = new JButton(name);
        b.addActionListener(this::actionPerformed);
        b.setEnabled(state);
        p.add(b);
        return b;
    }

    private void actionPerformed(final ActionEvent e) {
        final var obj = e.getSource();
        if (obj.equals(audioFileSaveButton)) {
            saveToFile(textField.getText().trim(), AudioFileFormat.Type.AU);
        } else if (obj.equals(aiffFileSaveButton)) {
            saveToFile(textField.getText().trim(), AudioFileFormat.Type.AIFF);
        } else if (obj.equals(waveFileSaveButton)) {
            saveToFile(textField.getText().trim(), AudioFileFormat.Type.WAVE);
        } else if (obj.equals(playButton)) {
            playButtonAction();
        } else if (obj.equals(recordButton)) {
            recordButtonAction();
        } else if (obj.equals(pauseButton)) {
            pauseButtonAction();
        } else if (obj.equals(loadButton)) {
            loadFile();
        }
    }

    private void loadFile() {
        try {
            final var file = new File(System.getProperty("user.dir"));
            final var fc = new JFileChooser(file);
            fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(final File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    final var name = f.getName();
                    return name.endsWith(".au") || name.endsWith(".wav") || name.endsWith(".aiff") || name.endsWith(".aif");
                }

                public String getDescription() {
                    return ".au, .wav, .aif";
                }
            });
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                createAudioInputStream(fc.getSelectedFile(), true);
            }
        } catch (final SecurityException ex) {
            JavaSound.showInfoDialog();
            LOGGER.error("Security exception", ex);
        }
    }

    private void pauseButtonAction() {
        if (pauseButton.getText().startsWith(PAUSE)) {
            if (capture.thread != null) {
                capture.line.stop();
            } else {
                if (playback.thread != null) {
                    playback.line.stop();
                }
            }
            pauseButton.setText("Resume");
        } else {
            if (capture.thread != null) {
                capture.line.start();
            } else {
                if (playback.thread != null) {
                    playback.line.start();
                }
            }
            pauseButton.setText(PAUSE);
        }
    }

    private void recordButtonAction() {
        if (recordButton.getText().startsWith(RECORD)) {
            file = null;
            capture.start();
            fileName = "untitled";
            samplingGraph.start();
            loadButton.setEnabled(false);
            playButton.setEnabled(false);
            pauseButton.setEnabled(true);
            audioFileSaveButton.setEnabled(false);
            aiffFileSaveButton.setEnabled(false);
            waveFileSaveButton.setEnabled(false);
            recordButton.setText("Stop");
        } else {
            lines.clear();
            capture.stop();
            samplingGraph.stop();
            loadButton.setEnabled(true);
            playButton.setEnabled(true);
            pauseButton.setEnabled(false);
            audioFileSaveButton.setEnabled(true);
            aiffFileSaveButton.setEnabled(true);
            waveFileSaveButton.setEnabled(true);
            recordButton.setText(RECORD);
        }
    }

    private void playButtonAction() {
        if (playButton.getText().startsWith("Play")) {
            playback.start();
            samplingGraph.start();
            recordButton.setEnabled(false);
            pauseButton.setEnabled(true);
            playButton.setText("Stop");
        } else {
            playback.stop();
            samplingGraph.stop();
            recordButton.setEnabled(true);
            pauseButton.setEnabled(false);
            playButton.setText("Play");
        }
    }

    public void createAudioInputStream(final File file, final boolean updateComponents) {
        if (file != null && file.isFile()) {
            try {
                this.file = file;
                errStr = null;
                audioInputStream = AudioSystem.getAudioInputStream(file);
                playButton.setEnabled(true);
                fileName = file.getName();
                final var milliseconds = (long) ((audioInputStream.getFrameLength() * 1000) / audioInputStream.getFormat().getFrameRate());
                duration = milliseconds / 1000.0;
                audioFileSaveButton.setEnabled(true);
                aiffFileSaveButton.setEnabled(true);
                waveFileSaveButton.setEnabled(true);
                if (updateComponents) {
                    formatControls.setFormat(audioInputStream.getFormat());
                    samplingGraph.createWaveForm(null);
                }
            } catch (final Exception ex) {
                reportStatus(ex.toString());
            }
        } else {
            reportStatus("Audio file required.");
        }
    }


    public void saveToFile(final String name, final AudioFileFormat.Type fileType) {
        if (audioInputStream == null) {
            reportStatus("No loaded audio to save");
            return;
        } else if (file != null) {
            createAudioInputStream(file, false);
        }

        // reset to the beginnning of the captured data
        try {
            audioInputStream.reset();
        } catch (final Exception e) {
            reportStatus("Unable to reset stream " + e);
            return;
        }

        final var file = new File(fileName = name);
        try {
            if (AudioSystem.write(audioInputStream, fileType, file) == -1) {
                throw new IOException("Problems writing to file");
            }
        } catch (final Exception ex) {
            reportStatus(ex.toString());
        }
        samplingGraph.repaint();
    }


    private void reportStatus(final String msg) {
        if ((errStr = msg) != null) {
            LOGGER.error(errStr);
            samplingGraph.repaint();
        }
    }


    /**
     * Write data to the OutputChannel.
     */
    private final class Playback {

        private SourceDataLine line;
        private Thread thread;

        public void start() {
            errStr = null;
            thread = new Thread(this::run);
            thread.setName("Playback");
            thread.start();
        }

        public void stop() {
            thread = null;
        }

        private void shutDown(final String message) {
            if ((errStr = message) != null) {
                LOGGER.error(errStr);
                samplingGraph.repaint();
            }
            if (thread != null) {
                thread = null;
                samplingGraph.stop();
                recordButton.setEnabled(true);
                pauseButton.setEnabled(false);
                playButton.setText("Play");
            }
        }

        public void run() {

            // reload the file if loaded by file
            if (file != null) {
                createAudioInputStream(file, false);
            }

            // make sure we have something to play
            if (audioInputStream == null) {
                shutDown("No loaded audio to play back");
                return;
            }
            // reset to the beginnning of the stream
            try {
                audioInputStream.reset();
            } catch (final Exception e) {
                shutDown("Unable to reset the stream\n" + e);
                return;
            }

            // get an AudioInputStream of the desired format for playback
            final var format = formatControls.getFormat();
            final var playbackInputStream = AudioSystem.getAudioInputStream(format, audioInputStream);

            if (playbackInputStream == null) {
                shutDown("Unable to convert stream of format " + audioInputStream + " to format " + format);
                return;
            }

            // define the required attributes for our line, 
            // and make sure a compatible line is supported.

            final var info = new DataLine.Info(SourceDataLine.class,
                    format);
            if (!AudioSystem.isLineSupported(info)) {
                shutDown("Line matching " + info + " not supported.");
                return;
            }

            // get and open the source data line for playback.

            try {
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format, BUF_SIZE);
            } catch (final LineUnavailableException ex) {
                shutDown("Unable to open the line: " + ex);
                return;
            }

            // play back the captured audio data

            final var frameSizeInBytes = format.getFrameSize();
            final var bufferLengthInFrames = line.getBufferSize() / 8;
            final var bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            final var data = new byte[bufferLengthInBytes];
            var numBytesRead = 0;

            // start the source data line
            line.start();

            while (thread != null) {
                try {
                    if ((numBytesRead = playbackInputStream.read(data)) == -1) {
                        break;
                    }
                    var numBytesRemaining = numBytesRead;
                    while (numBytesRemaining > 0) {
                        numBytesRemaining -= line.write(data, 0, numBytesRemaining);
                    }
                } catch (final Exception e) {
                    shutDown("Error during playback: " + e);
                    break;
                }
            }
            // we reached the end of the stream.  let the data play out, then
            // stop and close the line.
            if (thread != null) {
                line.drain();
            }
            line.stop();
            line.close();
            line = null;
            shutDown(null);
        }
    }

    /**
     * Reads data from the input channel and writes to the output stream
     */
    private final class Capture {

        private TargetDataLine line;
        private Thread thread;

        public void start() {
            errStr = null;
            thread = new Thread(this::run);
            thread.setName("Capture");
            thread.start();
        }

        public void stop() {
            thread = null;
        }

        private void shutDown(final String message) {
            if ((errStr = message) != null && thread != null) {
                thread = null;
                samplingGraph.stop();
                loadButton.setEnabled(true);
                playButton.setEnabled(true);
                pauseButton.setEnabled(false);
                audioFileSaveButton.setEnabled(true);
                aiffFileSaveButton.setEnabled(true);
                waveFileSaveButton.setEnabled(true);
                recordButton.setText(RECORD);
                LOGGER.error(errStr);
                samplingGraph.repaint();
            }
        }

        public void run() {
            duration = 0;
            audioInputStream = null;

            // define the required attributes for our line, 
            // and make sure a compatible line is supported.

            final var format = formatControls.getFormat();
            final var info = new DataLine.Info(TargetDataLine.class,
                    format);

            if (!AudioSystem.isLineSupported(info)) {
                shutDown("Line matching " + info + " not supported.");
                return;
            }

            // get and open the target data line for capture.

            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format, line.getBufferSize());
            } catch (final LineUnavailableException ex) {
                shutDown("Unable to open the line: " + ex);
                return;
            } catch (final SecurityException ex) {
                shutDown(ex.toString());
                JavaSound.showInfoDialog();
                return;
            } catch (final Exception ex) {
                shutDown(ex.toString());
                return;
            }

            // play back the captured audio data
            final var out = new ByteArrayOutputStream();
            final var frameSizeInBytes = format.getFrameSize();
            final var bufferLengthInFrames = line.getBufferSize() / 8;
            final var bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            final var data = new byte[bufferLengthInBytes];
            int numBytesRead;

            line.start();

            while (thread != null) {
                if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
                    break;
                }
                out.write(data, 0, numBytesRead);
            }

            // we reached the end of the stream.  stop and close the line.
            line.stop();
            line.close();
            line = null;

            // stop and close the output stream
            try {
                out.flush();
                out.close();
            } catch (final IOException ex) {
                LOGGER.error("Something went wrong while closing out", ex);
            }

            // load bytes into the audio input stream for playback

            final var audioBytes = out.toByteArray();
            final var bais = new ByteArrayInputStream(audioBytes);
            audioInputStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);

            final var milliseconds = (long) ((audioInputStream.getFrameLength() * 1000) / format.getFrameRate());
            duration = milliseconds / 1000.0;

            try {
                audioInputStream.reset();
            } catch (final Exception ex) {
                LOGGER.error("Error", ex);
                return;
            }

            samplingGraph.createWaveForm(audioBytes);
        }
    }

    /**
     * Controls for the AudioFormat.
     */
    private static final class FormatControls extends JPanel {

        private final List<ButtonGroup> buttonGroups = new ArrayList<>();
        private final JToggleButton linrB;
        private final JToggleButton ulawB;
        private final JToggleButton alawB;
        private final JToggleButton rate8B;
        private final JToggleButton rate11B;
        private final JToggleButton rate16B;
        private final JToggleButton rate22B;
        private final JToggleButton rate44B;
        private final JToggleButton size8B;
        private final JToggleButton size16B;
        private final JToggleButton signB;
        private final JToggleButton unsignB;
        private final JToggleButton litB;
        private final JToggleButton bigB;
        private final JToggleButton monoB;
        private final JToggleButton sterB;

        public FormatControls() {
            setLayout(new GridLayout(0, 1));
            final var eb = new EmptyBorder(0, 0, 0, 5);
            final var bb = new BevelBorder(BevelBorder.LOWERED);
            final var cb = new CompoundBorder(eb, bb);
            setBorder(new CompoundBorder(cb, new EmptyBorder(8, 5, 5, 5)));
            final var p1 = new JPanel();
            final var encodingGroup = new ButtonGroup();
            linrB = addToggleButton(p1, encodingGroup, "linear", true);
            ulawB = addToggleButton(p1, encodingGroup, "ulaw", false);
            alawB = addToggleButton(p1, encodingGroup, "alaw", false);
            add(p1);
            buttonGroups.add(encodingGroup);

            final var p2 = new JPanel();
            final var p2b = new JPanel();
            final var sampleRateGroup = new ButtonGroup();
            rate8B = addToggleButton(p2, sampleRateGroup, "8000", false);
            rate11B = addToggleButton(p2, sampleRateGroup, "11025", false);
            rate16B = addToggleButton(p2b, sampleRateGroup, "16000", false);
            rate22B = addToggleButton(p2b, sampleRateGroup, "22050", false);
            rate44B = addToggleButton(p2b, sampleRateGroup, "44100", true);
            add(p2);
            add(p2b);
            buttonGroups.add(sampleRateGroup);

            final var p3 = new JPanel();
            final var sampleSizeInBitsGroup = new ButtonGroup();
            size8B = addToggleButton(p3, sampleSizeInBitsGroup, "8", false);
            size16B = addToggleButton(p3, sampleSizeInBitsGroup, "16", true);
            add(p3);
            buttonGroups.add(sampleSizeInBitsGroup);

            final var p4 = new JPanel();
            final var signGroup = new ButtonGroup();
            signB = addToggleButton(p4, signGroup, "signed", true);
            unsignB = addToggleButton(p4, signGroup, "unsigned", false);
            add(p4);
            buttonGroups.add(signGroup);

            final var p5 = new JPanel();
            final var endianGroup = new ButtonGroup();
            litB = addToggleButton(p5, endianGroup, "little endian", false);
            bigB = addToggleButton(p5, endianGroup, "big endian", true);
            add(p5);
            buttonGroups.add(endianGroup);

            final var p6 = new JPanel();
            final var channelsGroup = new ButtonGroup();
            monoB = addToggleButton(p6, channelsGroup, "mono", false);
            sterB = addToggleButton(p6, channelsGroup, "stereo", true);
            add(p6);
            buttonGroups.add(channelsGroup);
        }

        private static JToggleButton addToggleButton(final JPanel p, final ButtonGroup g,
                                                     final String name, final boolean state) {
            final var b = new JToggleButton(name, state);
            p.add(b);
            g.add(b);
            return b;
        }

        public AudioFormat getFormat() {
            final var texts = getSelectedButtonTexts();
            var encoding = AudioFormat.Encoding.ULAW;
            final var encString = texts.get(0);
            final var rate = Float.parseFloat(texts.get(1));
            final var sampleSize = Integer.parseInt(texts.get(2));
            final var signedString = texts.get(3);
            final var bigEndian = texts.get(4).startsWith("big");
            final var channels = "mono".equals(texts.get(5)) ? 1 : 2;

            if ("linear".equals(encString)) {
                if ("signed".equals(signedString)) {
                    encoding = AudioFormat.Encoding.PCM_SIGNED;
                } else {
                    encoding = AudioFormat.Encoding.PCM_UNSIGNED;
                }
            } else if ("alaw".equals(encString)) {
                encoding = AudioFormat.Encoding.ALAW;
            }
            return new AudioFormat(encoding, rate, sampleSize,
                    channels, (sampleSize / 8) * channels, rate, bigEndian);
        }

        @NotNull
        private List<String> getSelectedButtonTexts() {
            return Sequence.of(buttonGroups)
                    .map(ButtonGroup::getElements)
                    .map(Enumeration::asIterator)
                    .flatMap(i -> () -> i)
                    .filter(AbstractButton::isSelected)
                    .map(AbstractButton::getText)
                    .toList();
        }

        public void setFormat(final AudioFormat format) {
            final var type = format.getEncoding();
            if (type == AudioFormat.Encoding.ULAW) {
                ulawB.doClick();
            } else if (type == AudioFormat.Encoding.ALAW) {
                alawB.doClick();
            } else if (type == AudioFormat.Encoding.PCM_SIGNED) {
                linrB.doClick();
                signB.doClick();
            } else if (type == AudioFormat.Encoding.PCM_UNSIGNED) {
                linrB.doClick();
                unsignB.doClick();
            }
            final var rate = (int) format.getFrameRate();
            if (rate == 8000) {
                rate8B.doClick();
            } else if (rate == 11025) {
                rate11B.doClick();
            } else if (rate == 16000) {
                rate16B.doClick();
            } else if (rate == 22050) {
                rate22B.doClick();
            } else if (rate == 44100) {
                rate44B.doClick();
            }
            switch (format.getSampleSizeInBits()) {
                case 8 -> size8B.doClick();
                case 16 -> size16B.doClick();
            }
            if (format.isBigEndian()) {
                bigB.doClick();
            } else {
                litB.doClick();
            }
            if (format.getChannels() == 1) {
                monoB.doClick();
            } else {
                sterB.doClick();
            }
        }
    }

    /**
     * Render a WaveForm.
     */
    private final class SamplingGraph extends JPanel {

        private static final Color jfcBlue = new Color(204, 204, 255);
        private static final Color pink = new Color(255, 175, 175);

        private transient Thread thread;
        private final Font font12 = new Font("serif", Font.PLAIN, 12);

        public SamplingGraph() {
            setBackground(new Color(20, 20, 20));
        }

        public void createWaveForm(byte[] audioBytes) {
            lines.clear();
            final var format = audioInputStream.getFormat();
            if (audioBytes == null) {
                try {
                    final var size = (int) (audioInputStream.getFrameLength() * format.getFrameSize());
                    audioBytes = new byte[size];
                    audioInputStream.read(audioBytes);
                } catch (final IOException ex) {
                    reportStatus(ex.toString());
                    return;
                }
            }
            final var d = getSize();
            final int[] audioData;
            if (format.getSampleSizeInBits() == 16) {
                audioData = get16BitAudioData(audioBytes, format);
            } else if (format.getSampleSizeInBits() == 8) {
                audioData = get8BitAudioData(audioBytes, format);
            } else {
                return;
            }

            displayAudioTrace(audioBytes, format, d, audioData);
        }

        private void displayAudioTrace(final byte[] audioBytes, final AudioFormat format, final Dimension d, final int[] audioData) {
            final var w = d.width;
            final var h = d.height - 15;
            final var frames_per_pixel = audioBytes.length / format.getFrameSize() / w;
            final var numChannels = format.getChannels();
            double y_last = 0;
            for (double x = 0; x < w; x++) {
                final var idx = (int) (frames_per_pixel * numChannels * x);
                final var my_byte = format.getSampleSizeInBits() == 8 ?
                        (byte) audioData[idx] : (byte) (128 * audioData[idx] / 32768);
                final double y_new = (h * (128 - my_byte) / 256);
                lines.add(new Line2D.Double(x, y_last, x, y_new));
                y_last = y_new;
            }
            repaint();
        }

        private int @NotNull [] get8BitAudioData(final byte[] audioBytes, final AudioFormat format) {
            final var audioData = new int[audioBytes.length];
            if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
                for (var i = 0; i < audioBytes.length; i++) {
                    audioData[i] = audioBytes[i];
                }
            } else {
                for (var i = 0; i < audioBytes.length; i++) {
                    audioData[i] = audioBytes[i] - 128;
                }
            }
            return audioData;
        }

        private int @NotNull [] get16BitAudioData(final byte[] audioBytes, final AudioFormat format) {
            final int[] audioData;
            final var nlengthInSamples = audioBytes.length / 2;
            audioData = new int[nlengthInSamples];
            if (format.isBigEndian()) {
                for (var i = 0; i < nlengthInSamples; i++) {
                    /* First byte is MSB (high order) */
                    final int MSB = audioBytes[2 * i];
                    /* Second byte is LSB (low order) */
                    final int LSB = audioBytes[2 * i + 1];
                    audioData[i] = MSB << 8 | (255 & LSB);
                }
            } else {
                for (var i = 0; i < nlengthInSamples; i++) {
                    /* First byte is LSB (low order) */
                    final int LSB = audioBytes[2 * i];
                    /* Second byte is MSB (high order) */
                    final int MSB = audioBytes[2 * i + 1];
                    audioData[i] = MSB << 8 | (255 & LSB);
                }
            }
            return audioData;
        }

        @Override
        public void paint(final Graphics g) {
            final var d = getSize();
            final var w = d.width;
            final var h = d.height;
            final var INFO_PAD = 15;

            final var g2 = (Graphics2D) g;
            g2.setBackground(getBackground());
            g2.clearRect(0, 0, w, h);
            g2.setColor(Color.white);
            g2.fillRect(0, h - INFO_PAD, w, INFO_PAD);

            if (errStr != null) {
                g2.setColor(jfcBlue);
                g2.setFont(new Font("serif", Font.BOLD, 18));
                g2.drawString("ERROR", 5, 20);
                final var as = new AttributedString(errStr);
                as.addAttribute(TextAttribute.FONT, font12, 0, errStr.length());
                final var aci = as.getIterator();
                final var frc = g2.getFontRenderContext();
                final var lbm = new LineBreakMeasurer(aci, frc);
                float x = 5, y = 25;
                lbm.setPosition(0);
                while (lbm.getPosition() < errStr.length()) {
                    final var textLayout = lbm.nextLayout(w - x - 5);
                    if (!textLayout.isLeftToRight()) {
                        x = w - textLayout.getAdvance();
                    }
                    textLayout.draw(g2, x, y += textLayout.getAscent());
                    y += textLayout.getDescent() + textLayout.getLeading();
                }
            } else if (capture.thread != null) {
                g2.setColor(Color.black);
                g2.setFont(font12);
                g2.drawString("Length: " + seconds, 3, h - 4);
            } else {
                g2.setColor(Color.black);
                g2.setFont(font12);
                g2.drawString("File: " + fileName + "  Length: " + duration + "  Position: " + seconds, 3, h - 4);

                if (audioInputStream != null) {
                    // .. render sampling graph ..
                    g2.setColor(jfcBlue);
                    for (var i = 1; i < lines.size(); i++) {
                        g2.draw(lines.get(i));
                    }
                    // .. draw current position ..
                    if (Double.compare(seconds, 0.0) != 0) {
                        final var loc = seconds / duration * w;
                        g2.setColor(pink);
                        g2.setStroke(new BasicStroke(3));
                        g2.draw(new Line2D.Double(loc, 0, loc, h - INFO_PAD - 2.0));
                    }
                }
            }
        }

        public void start() {
            thread = new Thread(this::run);
            thread.setName("SamplingGraph");
            thread.start();
            seconds = 0;
        }

        public void stop() {
            if (thread != null) {
                thread.interrupt();
            }
            thread = null;
        }

        public void run() {
            seconds = 0;
            while (thread != null) {
                if ((playback.line != null) && (playback.line.isOpen())) {

                    final var milliseconds = (playback.line.getMicrosecondPosition() / 1000);
                    seconds = milliseconds / 1000.0;
                } else if ((capture.line != null) && (capture.line.isActive())) {
                    final var milliseconds = (capture.line.getMicrosecondPosition() / 1000);
                    seconds = milliseconds / 1000.0;
                }

                try {
                    //noinspection BusyWait
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                repaint();

                while ((capture.line != null && !capture.line.isActive()) ||
                        (playback.line != null && !playback.line.isOpen())) {
                    try {
                        //noinspection BusyWait
                        Thread.sleep(10);
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            seconds = 0;
            repaint();
        }
    }

    public static void main(final String[] args) {
        final var capturePlaybackPanel = new CapturePlaybackPanel();
        capturePlaybackPanel.open();
        final var f = new JFrame("Capture/Playback");
        f.addWindowListener(new WindowAdapter() {
            @Override
            @SuppressWarnings("all")
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        f.getContentPane().add("Center", capturePlaybackPanel);
        f.pack();
        final var screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final var w = 720;
        final var h = 340;
        f.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2);
        f.setSize(w, h);
        f.setVisible(true);
    }
} 
