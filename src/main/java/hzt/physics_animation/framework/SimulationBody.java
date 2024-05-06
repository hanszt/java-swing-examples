package hzt.physics_animation.framework;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

/**
 * Custom Body class to add drawing functionality.
 * @author William Bittle
 * @version 3.2.1
 * @since 3.0.0
 */
public final class SimulationBody extends Body {
	/** The color of the object */
    private final Color color;
	
	/**
	 * Default constructor.
	 */
	public SimulationBody() {
		this.color = Graphics2DRenderer.getRandomColor();
	}

	/**
	 * Draws the body.
	 * <p>
	 * Only coded for polygons and circles.
	 * @param g the graphics object to render to
	 * @param scale the scaling factor
	 */
	public void render(final Graphics2D g, final double scale) {
		this.render(g, scale, this.color);
	}
	
	/**
	 * Draws the body.
	 * <p>
	 * Only coded for polygons and circles.
	 * @param g the graphics object to render to
	 * @param scale the scaling factor
	 * @param color the color to render the body
	 */
	public void render(final Graphics2D g, final double scale, final Color color) {
		// point radius
		final var pointRadius = 4;
		
		// save the original transform
        final var ot = g.getTransform();
		
		// transform the coordinate system from world coordinates to local coordinates
        final var lt = new AffineTransform();
		lt.translate(this.transform.getTranslationX() * scale, this.transform.getTranslationY() * scale);
		lt.rotate(this.transform.getRotationAngle());
		
		// apply the transform
		g.transform(lt);
		
		// loop over all the body fixtures for this body
		for (final var fixture : fixtures) {
			this.renderFixture(g, scale, fixture, color);
		}
		
		// draw a center point
        final var ce = new Ellipse2D.Double(
				this.getLocalCenter().x * scale - pointRadius * 0.5,
				this.getLocalCenter().y * scale - pointRadius * 0.5,
				pointRadius,
				pointRadius);
		g.setColor(Color.WHITE);
		g.fill(ce);
		g.setColor(Color.DARK_GRAY);
		g.draw(ce);
		
		// set the original transform
		g.setTransform(ot);
	}
	
	/**
	 * Renders the given fixture.
	 * @param g the graphics object to render to
	 * @param scale the scaling factor
	 * @param fixture the fixture to render
	 * @param color the color to render the fixture
	 */
	protected void renderFixture(final Graphics2D g, final double scale, final BodyFixture fixture, Color color) {
		// get the shape on the fixture
        final var convex = fixture.getShape();

		// brighten the color if asleep
		// render the fixture
		Graphics2DRenderer.render(g, convex, scale, this.isAtRest() ? color.brighter() : color);
	}
}
