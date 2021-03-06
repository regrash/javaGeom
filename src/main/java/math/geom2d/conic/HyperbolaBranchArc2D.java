
package math.geom2d.conic;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Collection;

import math.geom2d.Box2D;
import math.geom2d.IGeometricObject2D;
import math.geom2d.Vector2D;
import math.geom2d.curve.AbstractSmoothCurve2D;
import math.geom2d.curve.CurveArray2D;
import math.geom2d.curve.Curves2DUtil;
import math.geom2d.curve.ICurve2D;
import math.geom2d.curve.ICurveSet2D;
import math.geom2d.curve.ISmoothCurve2D;
import math.geom2d.domain.ISmoothOrientedCurve2D;
import math.geom2d.exception.UnboundedShape2DException;
import math.geom2d.line.ILinearShape2D;
import math.geom2d.point.Point2D;
import math.geom2d.transform.AffineTransform2D;

/**
 * An arc of hyperbola, defined from the parent hyperbola branch, and two positions on the parent curve.
 * 
 * @author dlegland
 */
public class HyperbolaBranchArc2D extends AbstractSmoothCurve2D implements ISmoothOrientedCurve2D {
    private static final long serialVersionUID = 1L;

    public static HyperbolaBranchArc2D create(HyperbolaBranch2D branch, double t0, double t1) {
        return new HyperbolaBranchArc2D(branch, t0, t1);
    }

    // ===================================================================
    // class variables

    /** The supporting hyperbola branch */
    HyperbolaBranch2D branch = null;

    /**
     * The lower bound if the parameterization for this arc.
     */
    double t0 = 0;

    /**
     * The upper bound if the parameterization for this arc.
     */
    double t1 = 1;

    // ===================================================================
    // constructor

    /**
     * Constructor from a branch and two bounds.
     */
    public HyperbolaBranchArc2D(HyperbolaBranch2D branch, double t0, double t1) {
        this.branch = branch;
        this.t0 = t0;
        this.t1 = t1;
    }

    // ===================================================================
    // methods specific to the arc

    public HyperbolaBranch2D getHyperbolaBranch() {
        return branch;
    }

    // ===================================================================
    // methods inherited from SmoothCurve2D interface

    @Override
    public double curvature(double t) {
        return branch.curvature(t);
    }

    @Override
    public Vector2D tangent(double t) {
        return branch.tangent(t);
    }

    // ===================================================================
    // methods inherited from OrientedCurve2D interface

    @Override
    public double signedDistance(Point2D point) {
        return this.signedDistance(point.x(), point.y());
    }

    @Override
    public double signedDistance(double x, double y) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double windingAngle(Point2D point) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isInside(Point2D pt) {
        // TODO Auto-generated method stub
        return false;
    }

    // ===================================================================
    // methods inherited from ContinuousCurve2D interface

    @Override
    public java.awt.geom.GeneralPath appendPath(java.awt.geom.GeneralPath path) {
        return this.asPolyline(60).appendPath(path);
    }

    /** Returns false. */
    @Override
    public boolean isClosed() {
        return false;
    }

    // ===================================================================
    // methods inherited from Curve2D interface

    @Override
    public Collection<Point2D> intersections(ILinearShape2D line) {
        Collection<Point2D> inters0 = this.branch.intersections(line);
        ArrayList<Point2D> inters = new ArrayList<>();
        for (Point2D point : inters0) {
            double pos = this.branch.project(point);
            if (pos > this.t0 && pos < this.t1)
                inters.add(point);
        }

        return inters;
    }

    /**
     * If t0 equals minus infinity, throws an UnboundedShapeException.
     */
    @Override
    public Point2D point(double t) {
        if (Double.isInfinite(t))
            throw new UnboundedShape2DException(this);
        t = min(max(t, t0), t1);
        return branch.point(t);
    }

    @Override
    public double position(Point2D point) {
        if (!this.branch.contains(point))
            return Double.NaN;
        double t = this.branch.position(point);
        if (t - t0 < -ACCURACY)
            return Double.NaN;
        if (t1 - t < ACCURACY)
            return Double.NaN;
        return t;
    }

    @Override
    public double project(Point2D point) {
        double t = this.branch.project(point);
        return min(max(t, t0), t1);
    }

    @Override
    public HyperbolaBranchArc2D reverse() {
        Hyperbola2D hyper = branch.hyperbola;
        Hyperbola2D hyper2 = new Hyperbola2D(hyper.xc, hyper.yc, hyper.a, hyper.b, hyper.theta, !hyper.direct);
        return new HyperbolaBranchArc2D(new HyperbolaBranch2D(hyper2, branch.positive), -t1, -t0);
    }

    /**
     * Returns a new HyperbolaBranchArc2D, with same parent hyperbola branch, and with new parameterization bounds. The new bounds are constrained to belong to the old bounds interval. If t1<t0, returns null.
     */
    @Override
    public HyperbolaBranchArc2D subCurve(double t0, double t1) {
        if (t1 < t0)
            return null;
        t0 = max(this.t0, t0);
        t1 = min(this.t1, t1);
        return new HyperbolaBranchArc2D(branch, t0, t1);
    }

    @Override
    public double t0() {
        return t0;
    }

    @Override
    public double t1() {
        return t1;
    }

    // ===================================================================
    // methods inherited from Shape2D interface

    @Override
    public Box2D boundingBox() {
        if (!this.isBounded())
            throw new UnboundedShape2DException(this);
        return this.asPolyline(100).boundingBox();
    }

    /**
     * Clips the hyperbola branch arc by a box. The result is an instance of CurveSet2D<HyperbolaBranchArc2D>, which contains only instances of HyperbolaBranchArc2D. If the shape is not clipped, the result is an instance of CurveSet2D<HyperbolaBranchArc2D> which contains 0 curves.
     */
    @Override
    public ICurveSet2D<? extends HyperbolaBranchArc2D> clip(Box2D box) {
        // Clip the curve
        ICurveSet2D<ISmoothCurve2D> set = Curves2DUtil.clipSmoothCurve(this, box);

        // Stores the result in appropriate structure
        CurveArray2D<HyperbolaBranchArc2D> result = new CurveArray2D<>(set.size());

        // convert the result
        for (ICurve2D curve : set.curves()) {
            if (curve instanceof HyperbolaBranchArc2D)
                result.add((HyperbolaBranchArc2D) curve);
        }
        return result;
    }

    @Override
    public double distance(Point2D point) {
        Point2D p = point(project(point));
        return p.distance(point);
    }

    @Override
    public double distance(double x, double y) {
        Point2D p = point(project(new Point2D(x, y)));
        return p.distance(x, y);
    }

    @Override
    public boolean isBounded() {
        if (t0 == Double.NEGATIVE_INFINITY)
            return false;
        if (t1 == Double.POSITIVE_INFINITY)
            return false;
        return true;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public HyperbolaBranchArc2D transform(AffineTransform2D trans) {
        // transform the parent branch
        HyperbolaBranch2D branch2 = branch.transform(trans);

        // Compute position of end points on the transformed parabola
        double startPos = Double.isInfinite(t0) ? Double.NEGATIVE_INFINITY : branch2.project(this.firstPoint().transform(trans));
        double endPos = Double.isInfinite(t1) ? Double.POSITIVE_INFINITY : branch2.project(this.lastPoint().transform(trans));

        // Compute the new arc
        if (startPos > endPos) {
            return new HyperbolaBranchArc2D(branch2.reverse(), endPos, startPos);
        } else {
            return new HyperbolaBranchArc2D(branch2, startPos, endPos);
        }
    }

    @Override
    public boolean contains(Point2D p) {
        return this.contains(p.x(), p.y());
    }

    @Override
    public boolean contains(double x, double y) {
        if (!branch.contains(x, y))
            return false;
        double t = branch.position(new Point2D(x, y));
        if (t < t0)
            return false;
        if (t > t1)
            return false;
        return true;
    }

    public java.awt.geom.GeneralPath getGeneralPath() {
        if (!this.isBounded())
            throw new UnboundedShape2DException(this);
        return this.asPolyline(100).asGeneralPath();
    }

    // ===================================================================
    // methods implementing the GeometricObject2D interface

    /*
     * (non-Javadoc)
     * 
     * @see math.geom2d.GeometricObject2D#almostEquals(math.geom2d.GeometricObject2D, double)
     */
    @Override
    public boolean almostEquals(IGeometricObject2D obj, double eps) {
        if (this == obj)
            return true;

        if (!(obj instanceof HyperbolaBranchArc2D))
            return false;
        HyperbolaBranchArc2D arc = (HyperbolaBranchArc2D) obj;

        if (!branch.almostEquals(arc.branch, eps))
            return false;
        if (Math.abs(t0 - arc.t0) > eps)
            return false;
        if (Math.abs(t1 - arc.t1) > eps)
            return false;
        return true;
    }

    // ===================================================================
    // methods overriding object

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((branch == null) ? 0 : branch.hashCode());
        long temp;
        temp = Double.doubleToLongBits(t0);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(t1);
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
        HyperbolaBranchArc2D other = (HyperbolaBranchArc2D) obj;
        if (branch == null) {
            if (other.branch != null)
                return false;
        } else if (!branch.equals(other.branch))
            return false;
        if (Double.doubleToLongBits(t0) != Double.doubleToLongBits(other.t0))
            return false;
        if (Double.doubleToLongBits(t1) != Double.doubleToLongBits(other.t1))
            return false;
        return true;
    }

}
