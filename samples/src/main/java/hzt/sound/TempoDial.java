package hzt.sound;
/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


import javax.sound.midi.Sequencer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;


/**
 * Midi tempo dial in beats per minute.
 *
 * @author Brian Lichtenwalter
 * @version @(#)TempoDial.java	1.9 02/02/06
 */
public class TempoDial extends JPanel {

    private static final int DOT_SIZE = 6;
    
    private final transient Ellipse2D ellipse;
    private final transient List<Data> data;
    private transient Data currentData;
    private transient Sequencer sequencer;

    public TempoDial() {
        ellipse = new Ellipse2D.Float(2, 20, 92, 120);
        final List<Ellipse2D.Float> dots = new ArrayList<>();
        final var pathIterator = ellipse.getPathIterator(null, 0.9);
        while (!pathIterator.isDone()) {
            final var pt = new float[6];
            final var segment = pathIterator.currentSegment(pt);
            if (segment == PathIterator.SEG_MOVETO || segment == PathIterator.SEG_LINETO) {
                dots.add(new Ellipse2D.Float(pt[0], pt[1], DOT_SIZE, DOT_SIZE));
            }
            pathIterator.next();
        }
        final List<Ellipse2D.Float> tmp = new ArrayList<>();
        for (final var dot : dots) {
            if (dot.getY() >= ellipse.getHeight() / 2) {
                tmp.add(dot);
            }
        }
        dots.removeAll(tmp);

        final var x = (float) (ellipse.getX() + ellipse.getWidth() / 2);
        final var y = (float) (ellipse.getY() + (ellipse.getHeight() / 2));
        final List<GeneralPath> paths = new ArrayList<>(dots.size());
        for (var i = 0; i < dots.size(); i++) {
            final var gp = new GeneralPath(Path2D.WIND_NON_ZERO);
            gp.moveTo(x, y);
            final Ellipse2D e1 = dots.get(i);
            gp.lineTo((float) e1.getX(), (float) e1.getY());
            if (i + 1 < dots.size()) {
                final Ellipse2D e2 = dots.get(i + 1);
                gp.lineTo((float) e2.getX(), (float) e2.getY());
            }
            gp.closePath();
            paths.add(gp);
        }

        data = new ArrayList<>(paths.size());
        for (int i = 0, tempo = 40; i < paths.size(); i++, tempo += 10) {
            data.add(new Data(tempo, dots.get(i), paths.get(i)));
            if (tempo == 120) {
                currentData = data.get(data.size() - 1);
            }
        }
        configureDial();
    }

    private void configureDial() {
        setBackground(new Color(20, 20, 20));
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(final MouseEvent e) {
                processMouse(e);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                processMouse(e);
            }
        });
    }


    private void processMouse(final MouseEvent e) {
        if (ellipse.contains(e.getPoint())) {
            for (final var datum : data) {
                currentData = datum;
                if (currentData.path.contains(e.getPoint())) {
                    break;
                }
            }
            repaint();
            if (sequencer != null) {
                sequencer.setTempoInBPM(getTempo());
            }
        }
    }


    public void setSequencer(final Sequencer sequencer) {
        this.sequencer = sequencer;
    }


    public float getTempo() {
        return currentData.tempo;
    }


    /**
     * Tempo value must match one found in data vector.
     * Acceptable tempo values start at 40 increment by 10 until 160.
     */
    public void setTempo(final double tempo) {
        for (final var datum : data) {
            currentData = datum;
            if (Double.compare(currentData.tempo, tempo) == 0) {
                break;
            }
        }
        repaint();
    }


    @Override
    public void paint(final Graphics g) {
        final var d = getSize();
        final var graphics = (Graphics2D) g;
        graphics.setBackground(getBackground());
        graphics.clearRect(0, 0, d.width, d.height);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        final var x = ellipse.getWidth() / 2 + ellipse.getX() + DOT_SIZE / 2.0;
        final var y = ellipse.getHeight() / 2;
        final var x2 = currentData.dot.getX() + DOT_SIZE / 2.0;
        final var y2 = currentData.dot.getY() + DOT_SIZE / 2.0;
        final Ellipse2D e = new Ellipse2D.Double(x - 5, y - 5, 10, 10);

        final var jfcBlue = new Color(204, 204, 255);
        graphics.setColor(jfcBlue);
        graphics.setStroke(new BasicStroke(3));
        graphics.draw(new Line2D.Double(e.getX() + 5, e.getY() + 5, x2, y2));
        graphics.fill(e);
        graphics.setFont(new Font("serif", Font.BOLD, 12));
        graphics.drawString(currentData.tempo + " bpm", 2, 12);

        graphics.fill(currentData.dot);
        graphics.setStroke(new BasicStroke(1.5F));
        graphics.setColor(jfcBlue.darker());
        for (final var datum : data) {
            graphics.draw(datum.dot);
        }
    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(105, 70);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }


    /**
     * Convenience storage class for our tempo dial data.
     */
    private static final class Data {
        private final int tempo;
        private final Ellipse2D dot;
        private final GeneralPath path;

        private Data(final int tempo, final Ellipse2D.Float dot, final GeneralPath path) {
            this.tempo = tempo;
            this.dot = dot;
            this.path = path;
        }
    }


    public static void main(final String[] args) {
        final var frame = new JFrame("Tempo Dial");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            @SuppressWarnings("all")
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.getContentPane().add("Center", new TempoDial());
        frame.pack();
        frame.setSize(new Dimension(200, 140));
        frame.setVisible(true);
    }
}
