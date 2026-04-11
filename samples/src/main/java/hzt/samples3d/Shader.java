package hzt.samples3d;

import java.awt.*;

@FunctionalInterface
public interface Shader {
    Color shade(Color color, double factor);
}
