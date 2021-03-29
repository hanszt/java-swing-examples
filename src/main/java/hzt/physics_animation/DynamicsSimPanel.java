package hzt.physics_animation;

import hzt.physics_animation.framework.SimulationPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class DynamicsSimPanel {

    private final JPanel simulationPanel = new JPanel();

    public static void main(String[] args) {
        new DynamicsSimPanel().start();
    }

    private void start() {
        PhysicsPanel physicsPanel = new PhysicsPanel();
        simulationPanel.add(physicsPanel);
        buildMainFrame(physicsPanel);
        physicsPanel.run();
    }

    private void buildMainFrame(SimulationPanel simulationPanel) {
        JFrame jFrame = new JFrame("Dynamics sim 2D");
        // setup the JFrame
        jFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        // add a window listener
        jFrame.addWindowListener(new WindowAdapter() {
            /* (non-Javadoc)
             * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
             */
            @Override
            public void windowClosing(WindowEvent e) {
                // before we stop the JVM stop the simulation
                simulationPanel.stop();
                super.windowClosing(e);
            }
        });
        // size everything
        jFrame.pack();
        // create the size of the window
        Dimension size = new Dimension(800, 600);
        jFrame.setSize(size);
		jFrame.setContentPane(this.simulationPanel);
		jFrame.setVisible(true);
    }
}
