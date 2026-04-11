package hzt.samples3d;

public record Vertex(double x, double y, double z) {

    public static final Vertex ZERO = new Vertex(0, 0, 0);

    public Vertex plus(final double x, final double y, final double z) {
        return new Vertex(this.x + x, this.y + y, this.z + z);
    }

    public Vertex minus(final double x, final double y, final double z) {
        return new Vertex(this.x - x, this.y - y, this.z - z);
    }

    public Vertex minus(final Vertex other) {
        return minus(other.x, other.y, other.z);
    }

    public Vertex scale(final double scalar) {
        return new Vertex(x * scalar, y * scalar, z * scalar);
    }

    public Vertex divided(final double n) {
        return Double.compare(n, 0.0) == 0 ? ZERO : new Vertex(x / n, y / n, z / n);
    }

    public Vertex crossProduct(final Vertex other) {
        return new Vertex(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
        );
    }

    public Vertex midPoint(final Vertex other) {
        return new Vertex(other.x + (x - other.x) / 2.0, other.y + (y - other.y) / 2.0, other.z + (z - other.z) / 2.0);
    }

    public Vertex normalized() {
        final var mag = magnitude();
        return Double.compare(mag, 0.0) == 0 ? ZERO : divided(mag);
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }
}