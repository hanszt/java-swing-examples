/*
 * Copyright (c) 2010-2016 William Bittle  http://www.dyn4j.org/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this list of conditions
 *     and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *     and the following disclaimer in the documentation and/or other materials provided with the
 *     distribution.
 *   * Neither the name of dyn4j nor the names of its contributors may be used to endorse or
 *     promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package hzt.physics_animation.framework;

import org.dyn4j.world.World;

import javax.swing.JPanel;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;

/**
 * A simple framework for building samples.
 *
 * @version 3.2.0
 * @since 3.2.0
 */
@SuppressWarnings("unused")
public abstract class SimulationPanel extends JPanel {

    public static final double NANO_TO_BASE = 1.0e9;

    protected final Canvas canvas;
    protected final transient World<SimulationBody> world;
    protected final double scale;

    private boolean stopped;
    private boolean paused;
    private long last;

    protected SimulationPanel(double scale) {
        this.scale = scale;
        this.world = new World<>();
        this.canvas = new Canvas();
        this.canvas.setPreferredSize(new Dimension(800, 600));
    }

    /**
     * Creates game objects and adds them to the world.
     */
    protected abstract void initializeWorld();

    /**
     * Start active rendering the simulation.
     * <p>
     * This should be called after the JFrame has been shown.
     */
    public void start() {
        // initialize the last update time
        this.last = System.nanoTime();
        // don't allow AWT to paint the canvas since we are
        this.canvas.setIgnoreRepaint(true);
        // enable double buffering (the JFrame has to be
        // visible before this can be done)
        this.canvas.createBufferStrategy(2);
        // run a separate thread to do active rendering
        // because we don't want to do it on the EDT
        Thread thread = new Thread(this::startGameLoop);
        // set the game loop thread to a daemon thread so that
        // it cannot stop the JVM from exiting
        thread.setDaemon(true);
        // start the game loop
        thread.start();
    }

    private void startGameLoop() {
        // perform an infinite loop stopped
        // render as fast as possible
        while (!isStopped()) {
            gameLoop();
            // you could add a Thread.yield(); or
            // Thread.sleep(long) here to give the
            // CPU some breathing room
            try {
                //noinspection BusyWait
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * The method calling the necessary methods to update
     * the game, graphics, and poll for input.
     */
    private void gameLoop() {
        // get the graphics object to render to
        Graphics2D graphics2D = (Graphics2D) this.canvas.getBufferStrategy().getDrawGraphics();

        // by default, set (0, 0) to be the center of the screen with the positive x-axis
        // pointing right and the positive y-axis pointing up
        this.transform(graphics2D);

        // reset the view
        this.clear(graphics2D);

        // get the current time
        long time = System.nanoTime();
        // get the elapsed time from the last iteration
        long diff = time - this.last;
        // set the last time
        this.last = time;
        // convert from nanoseconds to seconds
        double elapsedTime = diff / NANO_TO_BASE;

        // render anything about the simulation (will render the World objects)
        this.render(graphics2D);

        if (!paused) {
            // update the World
            this.update(graphics2D, elapsedTime);
        }

        // dispose of the graphics object
        graphics2D.dispose();

        // blit/flip the buffer
        BufferStrategy strategy = this.canvas.getBufferStrategy();
        if (!strategy.contentsLost()) {
            strategy.show();
        }

        // Sync the display on some systems.
        // (on Linux, this fixes event queue problems)
        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Performs any transformations to the graphics.
     * <p>
     * By default, this method puts the origin (0,0) in the center of the window
     * and points the positive y-axis pointing up.
     *
     * @param g the graphics object to render to
     */
    protected void transform(Graphics2D g) {
        final int w = this.canvas.getWidth();
        final int h = this.canvas.getHeight();

        // before we render everything im going to flip the y axis and move the
        // origin to the center (instead of it being in the top left corner)
        AffineTransform yFlip = AffineTransform.getScaleInstance(1, -1);
        AffineTransform move = AffineTransform.getTranslateInstance(w / 2D, -h / 2D);
        g.transform(yFlip);
        g.transform(move);
    }

    /**
     * Clears the previous frame.
     *
     * @param graphics the graphics object to render to
     */
    protected void clear(Graphics2D graphics) {
        final int w = this.canvas.getWidth();
        final int h = this.canvas.getHeight();

        graphics.setColor(Color.WHITE);
        graphics.fillRect(-w / 2, -h / 2, w, h);
    }

    /**
     * Renders the example.
     *
     * @param graphics the graphics object to render to
     */
    protected void render(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // draw all the objects in the world
        for (int i = 0; i < this.world.getBodyCount(); i++) {
            // get the object
            SimulationBody body = this.world.getBody(i);
            body.render(graphics, this.scale);
        }
    }

    /**
     * Updates the world.
     *
     * @param g           the graphics object to render to
     * @param elapsedTime the elapsed time from the last update
     */
    protected void update(Graphics2D g, double elapsedTime) {
        this.world.update(elapsedTime);
    }

    /**
     * Stops the simulation.
     */
    public synchronized void stop() {
        this.stopped = true;
    }

    /**
     * Returns true if the simulation is stopped.
     *
     * @return boolean true if stopped
     */
    public boolean isStopped() {
        return this.stopped;
    }

    /**
     * Pauses the simulation.
     */
    public synchronized void pause() {
        this.paused = true;
    }

    /**
     * Pauses the simulation.
     */
    public synchronized void resume() {
        this.paused = false;
    }

    /**
     * Returns true if the simulation is paused.
     *
     * @return boolean true if paused
     */
    public boolean isPaused() {
        return this.paused;
    }
}
