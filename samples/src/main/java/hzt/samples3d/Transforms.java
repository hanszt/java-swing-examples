package hzt.samples3d;

import static java.lang.Math.*;
import static java.lang.Math.toRadians;

final class Transforms {

    private Transforms() {
    }

    static Matrix4 translationTransform(double tx, double ty, double tz) {
        return new Matrix4(
                1, 0, 0, tx,
                0, 1, 0, ty,
                0, 0, 1, tz,
                0, 0, 0, 1
        );
    }

    static Matrix4 pitchTransform(final double pitch) {
        return new Matrix4(
                1.0, 0.0, 0.0, 0.0,
                0.0, cos(pitch), -sin(pitch), 0.0,
                0.0, sin(pitch), cos(pitch), 0.0,
                0.0, 0.0, 0.0, 1.0
        );
    }

    static Matrix4 headingTransform(final double heading) {
        return new Matrix4(
                cos(heading), 0.0, sin(heading), 0.0,
                0.0, 1.0, 0.0, 0.0,
                -sin(heading), 0.0, cos(heading), 0.0,
                0.0, 0.0, 0.0, 1.0
        );
    }

    static Matrix4 buildTransform(
            final double translateX,
            final double translateY,
            final double translateZ,
            final int headingDeg,
            final int pitchDeg
    ) {
        final var heading = toRadians(headingDeg);
        final var pitch = toRadians(pitchDeg);
        final var rotation = headingTransform(heading).multiply(pitchTransform(pitch));
        return translationTransform(translateX, translateY, translateZ).multiply(rotation);
    }


}
