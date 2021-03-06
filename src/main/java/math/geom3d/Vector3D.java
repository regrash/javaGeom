/**
 * 
 */

package math.geom3d;

import java.io.Serializable;

import math.geom3d.point.Point3D;
import math.geom3d.transform.AffineTransform3D;

/**
 * Define a vector in 3 dimensions. Provides methods to compute cross product and dot product, addition and subtraction of vectors.
 */
public class Vector3D implements IGeometricObject3D, Serializable {
    private static final long serialVersionUID = 1L;

    protected double x = 1;
    protected double y = 0;
    protected double z = 0;

    /**
     * Computes the dot product of the two vectors, defined by :
     * <p>
     * <code> x1*x2 + y1*y2 + z1*z2</code>
     * <p>
     * Dot product is zero if the vectors defined by the 2 vectors are orthogonal. It is positive if vectors are in the same direction, and negative if they are in opposite direction.
     */
    public final static double dotProduct(Vector3D v1, Vector3D v2) {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }

    /**
     * Computes the cross product of the two vectors. Cross product is zero for colinear vectors. It is positive if angle between vector 1 and vector 2 is comprised between 0 and PI, and negative otherwise.
     */
    public final static Vector3D crossProduct(Vector3D v1, Vector3D v2) {
        return new Vector3D(v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, v1.x * v2.y - v1.y * v2.x);
    }

    /**
     * test if the two vectors are colinear
     * 
     * @return true if the vectors are colinear
     */
    public final static boolean isColinear(Vector3D v1, Vector3D v2) {
        v1 = v1.normalize();
        v2 = v2.normalize();
        return Vector3D.crossProduct(v1, v2).norm() < IShape3D.ACCURACY;
    }

    /**
     * test if the two vectors are orthogonal
     * 
     * @return true if the vectors are orthogonal
     */
    public final static boolean isOrthogonal(Vector3D v1, Vector3D v2) {
        v1 = v1.normalize();
        v2 = v2.normalize();
        double dot = Vector3D.dotProduct(v1, v2);
        return Math.abs(dot) < IShape3D.ACCURACY;
    }

    // ===================================================================
    // constructors

    /** Constructs a new Vector3D initialized with x=1, y=0 and z=0. */
    public Vector3D() {
        this(1, 0, 0);
    }

    /** Base constructor, using coordinates in each direction. */
    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Construct a new vector between origin and a 3D point.
     */
    public Vector3D(Point3D point) {
        this(point.x(), point.y(), point.z());
    }

    /**
     * construct a new vector between two points
     */
    public Vector3D(Point3D point1, Point3D point2) {
        this(point2.x() - point1.x(), point2.y() - point1.y(), point2.z() - point1.z());
    }

    // ===================================================================
    // accessors

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    // ===================================================================
    // basic arithmetic on vectors

    /**
     * Return the sum of current vector with vector given as parameter. Inner fields are not modified.
     */
    public Vector3D plus(Vector3D v) {
        return new Vector3D(x + v.x, y + v.y, z + v.z);
    }

    /**
     * Return the subtraction of current vector with vector given as parameter. Inner fields are not modified.
     */
    public Vector3D minus(Vector3D v) {
        return new Vector3D(x - v.x, y - v.y, z - v.z);
    }

    /**
     * Multiplies this vector by a constant.
     */
    public Vector3D times(double k) {
        return new Vector3D(k * x, k * y, k * z);
    }

    // ===================================================================
    // general operations on vectors

    /**
     * Returns the opposite vector v2 of this, such that the sum of this and v2 equals the null vector.
     * 
     * @return the vector opposite to <code>this</code>.
     */
    public Vector3D opposite() {
        return new Vector3D(-x, -y, -z);
    }

    /**
     * Computes the norm of the vector
     * 
     * @return the euclidean norm of the vector
     */
    public double norm() {
        return Math.hypot(Math.hypot(x, y), z);
    }

    /**
     * Computes the square of the norm of the vector. This avoids to compute the square root.
     * 
     * @return the euclidean norm of the vector
     */
    public double normSq() {
        return x * x + y * y + z * z;
    }

    /**
     * Returns the vector with same direction as this one, but with norm equal to 1.
     */
    public Vector3D normalize() {
        double r = this.norm();
        return new Vector3D(this.x / r, this.y / r, this.z / r);
    }

    /**
     * Transform the vector, by using only the first 4 parameters of the transform. Translation of a vector returns the same vector.
     * 
     * @param trans
     *            an affine transform
     * @return the transformed vector.
     */
    public Vector3D transform(AffineTransform3D trans) {
        double[] tab = trans.coefficients();
        return new Vector3D(x * tab[0] + y * tab[1] + z * tab[2], x * tab[4] + y * tab[5] + z * tab[6], x * tab[8] + y * tab[9] + z * tab[10]);
    }

    /**
     * Test whether this object is the same as another vector, with respect to a given threshold.
     */
    @Override
    public boolean almostEquals(IGeometricObject3D obj, double eps) {
        if (this == obj)
            return true;

        if (!(obj instanceof Vector3D))
            return false;
        Vector3D v = (Vector3D) obj;

        if (Math.abs(this.x - v.x) > eps)
            return false;
        if (Math.abs(this.y - v.y) > eps)
            return false;
        if (Math.abs(this.z - v.z) > eps)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Vector3D other = (Vector3D) obj;
        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
            return false;
        if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
            return false;
        return true;
    }
}
