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
package hzt.physics_animation;

import hzt.physics_animation.framework.SimulationBody;
import hzt.physics_animation.framework.SimulationPanel;
import org.dyn4j.dynamics.joint.RevoluteJoint;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isMiddleMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;

public final class PhysicsPanel extends SimulationPanel {

	/** A point for tracking the mouse click */
	private Point point;
	
	/**
	 * A custom mouse adapter for listening for mouse clicks.
	 * @author William Bittle
	 * @version 3.2.1
	 * @since 3.2.0
	 */
	private final class CustomMouseAdapter extends MouseAdapter {
		@Override
		public void mousePressed(final MouseEvent e) {
			// store the mouse click position for use later
			if (isRightMouseButton(e)) {
				point = new Point(e.getX(), e.getY());
			}
		}

		@Override
		public void mouseDragged(final MouseEvent e) {
			if (isLeftMouseButton(e) || isMiddleMouseButton(e)) {
				point = new Point(e.getX(), e.getY());
			}
		}
	}

	public PhysicsPanel() {
		super(32.0);
		final MouseAdapter mouseAdapter = new CustomMouseAdapter();
		this.canvas.addMouseMotionListener(mouseAdapter);
		this.canvas.addMouseWheelListener(mouseAdapter);
		this.canvas.addMouseListener(mouseAdapter);
		initializeWorld();
	}

	protected void initializeWorld() {
		add(canvas);
        final var anchor = new SimulationBody();
		anchor.addFixture(Geometry.createCircle(.5));
		anchor.setMass(MassType.INFINITE);
        final var floor = new SimulationBody();
	    floor.addFixture(Geometry.createRectangle(20, 1));
	    floor.setMass(MassType.NORMAL);
        final var revoluteJoint = new RevoluteJoint<>(anchor, floor, new Vector2());
		revoluteJoint.setMotorEnabled(true);
		revoluteJoint.setMotorSpeed(1);
		revoluteJoint.setMaximumMotorTorque(2500);
		world.addBody(floor);
		world.addBody(anchor);
		world.addJoint(revoluteJoint);
	}

	@Override
	protected void update(final Graphics2D g, final double elapsedTime) {

		if (this.point != null) {
            final var vector2 = convertFromScreenSpaceToWorldSpaceCoordinates();
            final var body = new SimulationBody();
			body.addFixture(Geometry.createRectangle(Math.random(), Math.random()));
			body.translate(vector2);
			body.setMass(MassType.NORMAL);
			this.world.addBody(body);

			this.point = null;
		}
		super.update(g, elapsedTime);
	}

	private Vector2 convertFromScreenSpaceToWorldSpaceCoordinates() {
        final var x = (this.point.getX() - this.canvas.getWidth() / 2.0) / this.scale;
        final var y = -(this.point.getY() - this.canvas.getHeight() / 2.0) / this.scale;
		return new Vector2(x, y);
	}


}
