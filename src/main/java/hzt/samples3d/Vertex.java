package hzt.samples3d;

record Vertex(double x, double y, double z) {

    static final Vertex ZERO = new Vertex(0, 0, 0);

    Vertex plus(final double x, final double y, final double z) {
        return new Vertex(this.x + x, this.y + y, this.z + z);
    }

    Vertex minus(final double x, final double y, final double z) {
        return new Vertex(this.x - x, this.y - y, this.z - z);
    }

    Vertex minus(Vertex other) {
        return minus(other.x, other.y, other.z);
    }

    Vertex multiplied(final double factor) {
        return new Vertex(x * factor, y * factor, z * factor);
    }

    Vertex divided(final double n) {
        return Double.compare(n, 0.0) == 0 ? ZERO : new Vertex(x / n, y / n, z / n);
    }

    Vertex crossProduct(final Vertex other) {
        return new Vertex(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
        );
    }

    Vertex midPoint(final Vertex other) {
        return new Vertex(other.x + (x - other.x) / 2.0, other.y + (y - other.y) / 2.0, other.z + (z - other.z) / 2.0);
    }

    Vertex normalized() {
        final var mag = magnitude();
        return Double.compare(mag, 0.0) == 0 ? ZERO : divided(mag);
    }

    double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }
}