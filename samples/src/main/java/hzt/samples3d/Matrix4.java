package hzt.samples3d;

import java.util.Arrays;

record Matrix4(double... values) {

    private static final int SLOT_COUNT = 16;

    public Matrix4 {
        if (values.length != SLOT_COUNT) {
            throw new IllegalArgumentException("Must have " + SLOT_COUNT + " values! (Had " + values.length + ")");
        }
        values = Arrays.copyOf(values, values.length);
    }

    public Matrix4 multiply(final Matrix4 other) {
        final var result = new double[SLOT_COUNT];
        for (var row = 0; row < 4; row++) {
            for (var col = 0; col < 4; col++) {
                for (var i = 0; i < 4; i++) {
                    result[row * 4 + col] += this.values[row * 4 + i] * other.values[i * 4 + col];
                }
            }
        }
        return new Matrix4(result);
    }

    /**
     * In 3D graphics, points are often represented using homogeneous coordinates (x, y, z, w) instead of just (x, y, z).
     * For a 3D point, 'w' is typically 1. This allows all transformations (translation, rotation, scaling, and even perspective projection)
     * to be represented as matrix multiplications. When the Vertex v is passed in, it's implicitly treated as (v.x(), v.y(), v.z(), 1).
     * <p>
     * The values array holds the 16 elements of a 4x4 transformation matrix in row-major order. The method performs the multiplication of this 4x4 matrix with the 4D
     * homogeneous coordinate vector of v.
     *
     * <ul>
     *      <li>final double x = v.x() * values[0] + v.y() * values[1] + v.z() * values[2] + values[3];
     *          This calculates the new x coordinate. It's the dot product of the input vertex (v.x, v.y, v.z, 1) with the first row of the matrix (values[0], values[1], values[2], values[3]).</li>
     *      <li>final double y = v.x() * values[4] + v.y() * values[5] + v.z() * values[6] + values[7];
     *          This calculates the new y coordinate using the second row of the matrix.</li>
     *      <li>final double z = v.x() * values[8] + v.y() * values[9] + v.z() * values[10] + values[11];
     *          This calculates the new z coordinate using the third row of the matrix.</li>
     *      <li>final double w = v.x() * values[12] + v.y() * values[13] + v.z() * values[14] + values[15];
     *          This calculates the new w component using the fourth row of the matrix. For purely affine transformations (rotation, translation, scaling), this w will remain 1. For perspective projection, w
     *          will change based on the original z coordinate.</li>
     * </ul>
     * <p>
     * Finally, x, y, and z are divided by w. This step is known as perspective division.
     *
     * <ul>
     *      <li>If w is 1 (for affine transformations), the coordinates remain unchanged.</li>
     *      <li>If w is something other than 1 (due to a perspective projection in the Matrix4), this division produces the foreshortening effect, making objects appear smaller as they move further away. This is
     *          how 3D perspective is achieved on a 2D screen.</li>
     * </ul>
     * In summary, this method applies a general 4x4 transformation (which can combine rotations, translations, scaling, and perspective) to a 3D point and returns its transformed 3D coordinates.
     *
     * @param v The vertex to apply the transformation to
     * @return the transformed vertex
     */
    public Vertex apply(final Vertex v) {
        final double x = v.x() * values[0] + v.y() * values[1] + v.z() * values[2] + values[3];
        final double y = v.x() * values[4] + v.y() * values[5] + v.z() * values[6] + values[7];
        final double z = v.x() * values[8] + v.y() * values[9] + v.z() * values[10] + values[11];
        final double w = v.x() * values[12] + v.y() * values[13] + v.z() * values[14] + values[15];
        return new Vertex(x / w, y / w, z / w);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Matrix4(double[] otherValues) && Arrays.equals(values, otherValues);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public String toString() {
        return "Matrix4{values=%s}".formatted(Arrays.toString(values));
    }
}
