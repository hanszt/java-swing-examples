package hzt.samples3d;

import java.awt.*;

@FunctionalInterface
interface Shader {
    Color shade(Color color, double factor);
}
