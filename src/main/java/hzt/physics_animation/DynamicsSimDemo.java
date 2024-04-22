package hzt.physics_animation;

import hzt.physics_animation.framework.SimulationPanel;
import org.hzt.swing_utils.function.window_listeners.WindowClosingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public final class DynamicsSimDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicsSimDemo.class);
    private final JPanel simulationPanel = new JPanel();

    public static void main(final String[] args) {
        new DynamicsSimDemo().start();
    }

    private void start() {
        final var physicsPanel = new PhysicsPanel();
        simulationPanel.add(physicsPanel);
        buildMainFrame(physicsPanel);
        final var lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (final ClassNotFoundException | InstantiationException |
                       IllegalAccessException | UnsupportedLookAndFeelException e) {
            LOGGER.error("Could not set look and feel with name {}", lookAndFeel, e);
        }
        physicsPanel.start();
    }

    private void buildMainFrame(final SimulationPanel simulationPanel) {
        final var jFrame = new JFrame("Dynamics sim 2D");
        jFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        jFrame.addWindowListener((WindowClosingListener) e -> stopSimulation(simulationPanel));

        jFrame.pack();
        jFrame.setSize(new Dimension(800, 600));
        jFrame.setContentPane(this.simulationPanel);
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
    }

    private static void stopSimulation(final SimulationPanel simulationPanel) {
        simulationPanel.stop();
        LOGGER.info("simulation stopped");
    }
}
