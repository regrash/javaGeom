/* file : PolyOrientedCurve2D.java
 * 
 * Project : geometry
 *
 * ===========================================
 * 
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY, without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. if not, write to :
 * The Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Created on 1 mai 2006
 *
 */

package math.geom2d.domain;

import static java.lang.Math.PI;
import static java.lang.Math.round;

// Imports
import java.util.Collection;

import math.geom2d.Angle2DUtil;
import math.geom2d.Box2D;
import math.geom2d.IShape2D;
import math.geom2d.Vector2D;
import math.geom2d.curve.CurveArray2D;
import math.geom2d.curve.Curves2DUtil;
import math.geom2d.curve.IContinuousCurve2D;
import math.geom2d.curve.ICurve2D;
import math.geom2d.curve.ICurveSet2D;
import math.geom2d.curve.ISmoothCurve2D;
import math.geom2d.curve.PolyCurve2D;
import math.geom2d.line.StraightLine2D;
import math.geom2d.point.Point2D;
import math.geom2d.polygon.LinearCurve2D;
import math.geom2d.transform.AffineTransform2D;

/**
 * A PolyOrientedCurve2D is a set of piecewise smooth curve arcs, such that the end of a curve is the beginning of the next curve, and such that they do not intersect nor self-intersect.
 * <p>
 * 
 * @see BoundaryPolyCurve2D
 * @author dlegland
 */
public class PolyOrientedCurve2D<T extends IContinuousOrientedCurve2D> extends PolyCurve2D<T> implements IContinuousOrientedCurve2D {
    private static final long serialVersionUID = 1L;

    /**
     * Static factory for creating a new PolyOrientedCurve2D from an array of curves.
     * 
     * @since 0.8.1
     */
    @SafeVarargs
    public static <T extends IContinuousOrientedCurve2D> PolyOrientedCurve2D<T> create(T... curves) {
        return new PolyOrientedCurve2D<>(curves);
    }

    /**
     * Static factory for creating a new PolyOrientedCurve2D from an array of curves.
     * 
     * @since 0.8.1
     */
    @SafeVarargs
    public static <T extends IContinuousOrientedCurve2D> PolyOrientedCurve2D<T> createClosed(T... curves) {
        return new PolyOrientedCurve2D<>(curves, true);
    }

    /**
     * Static factory for creating a new PolyOrientedCurve2D from an array of curves and a flag indicating if the curve is closed or not.
     * 
     * @since 0.9.0
     */
    public static <T extends IContinuousOrientedCurve2D> PolyOrientedCurve2D<T> create(T[] curves, boolean closed) {
        return new PolyOrientedCurve2D<>(curves, closed);
    }

    // ===================================================================
    // Constructors

    public PolyOrientedCurve2D() {
        super();
    }

    public PolyOrientedCurve2D(int size) {
        super(size);
    }

    @SafeVarargs
    public PolyOrientedCurve2D(T... curves) {
        super(curves);
    }

    public PolyOrientedCurve2D(T[] curves, boolean closed) {
        super(curves, closed);
    }

    public PolyOrientedCurve2D(Collection<? extends T> curves) {
        super(curves);
    }

    public PolyOrientedCurve2D(Collection<? extends T> curves, boolean closed) {
        super(curves, closed);
    }

    // ===================================================================
    // Methods specific to PolyOrientedCurve2D

    @Override
    public double windingAngle(Point2D point) {
        double angle = 0;
        for (IOrientedCurve2D curve : this.curves)
            angle += curve.windingAngle(point);
        return angle;
    }

    @Override
    public double signedDistance(Point2D p) {
        return signedDistance(p.x(), p.y());
    }

    /*
     * (non-Javadoc)
     * 
     * @see math.geom2d.Shape2D#signedDistance(math.geom2d.Point2D)
     */
    @Override
    public double signedDistance(double x, double y) {
        double dist = this.distance(x, y);

        if (this.isInside(new Point2D(x, y)))
            dist = -dist;

        return dist;
    }

    /**
     * Determines if the given point lies within the domain bounded by this curve.
     */
    @Override
    public boolean isInside(Point2D point) {
        double pos = this.project(point);

        if (!this.isSingular(pos)) {
            // Simply call the method isInside on the child curve
            return this.childCurve(pos).isInside(point);
        }

        // number of curves
        int n = this.size();

        // vertex index and position
        int i = this.curveIndex(pos);
        if (pos / 2 - i > .25)
            i++;

        // Test case of point equal to last position
        if (round(pos) == 2 * n - 1) {
            pos = 0;
            i = 0;
        }

        Point2D vertex = this.point(pos);

        // indices of previous and next curves
        int iPrev = i > 0 ? i - 1 : n - 1;
        int iNext = i;

        // previous and next curves
        T prev = this.curves.get(iPrev);
        T next = this.curves.get(iNext);

        // tangent vectors of the 2 neighbor curves
        Vector2D v1 = computeTangent(prev, prev.t1());
        Vector2D v2 = computeTangent(next, next.t0());

        // compute on which side of each ray the test point lies
        boolean in1 = new StraightLine2D(vertex, v1).isInside(point);
        boolean in2 = new StraightLine2D(vertex, v2).isInside(point);

        // check if angle between vectors is acute or obtuse
        double diff = Angle2DUtil.angle(v1, v2);
        double eps = 1e-12;
        if (diff < PI - eps) {
            // Acute angle
            return in1 && in2;
        }

        if (diff > PI + eps) {
            // obtuse angle
            return in1 || in2;
        }

        // Extract curvatures of both curves around singular point
        ISmoothCurve2D smoothPrev = Curves2DUtil.getLastSmoothCurve(prev);
        ISmoothCurve2D smoothNext = Curves2DUtil.getFirstSmoothCurve(next);
        double kappaPrev = smoothPrev.curvature(smoothPrev.t1());
        double kappaNext = smoothNext.curvature(smoothNext.t0());

        // get curvature signs
        double sp = Math.signum(kappaPrev);
        double sn = Math.signum(kappaNext);

        // Both curvatures have same sign
        // -> point is inside if both curvature are positive
        if (sn * sp > 0) {
            return kappaPrev > 0 && kappaNext > 0;
        }

        // One of the curvature is zero (straight curve)
        if (sn * sp == 0) {
            if (sn == 0 && sp == 0) {
                throw new IllegalArgumentException("colinear lines...");
            }

            if (sp == 0)
                return kappaNext > 0;
            else
                return kappaPrev > 0;
        }

        // if curvatures have opposite signs, curves point in the same
        // direction but with opposite direction.
        if (kappaPrev > 0 && kappaNext < 0) {
            return Math.abs(kappaPrev) > Math.abs(kappaNext);
        } else {
            return Math.abs(kappaPrev) < Math.abs(kappaNext);
        }
    }

    /**
     * Computes the tangent of the curve at the given position.
     */
    private static Vector2D computeTangent(IContinuousCurve2D curve, double pos) {
        // For smooth curves, simply call the getTangent() method
        if (curve instanceof ISmoothCurve2D) {
            return ((ISmoothCurve2D) curve).tangent(pos);
        } else if (curve instanceof ICurveSet2D<?>) {
            // Extract sub curve and recursively call this method on the sub curve
            ICurveSet2D<?> curveSet = (ICurveSet2D<?>) curve;
            double pos2 = curveSet.localPosition(pos);
            ICurve2D subCurve = curveSet.childCurve(pos);
            return computeTangent((IContinuousCurve2D) subCurve, pos2);
        } else if (curve instanceof LinearCurve2D) {
            if (pos <= IShape2D.ACCURACY) {
                return ((LinearCurve2D) curve).rightTangent(pos);
            } else {
                return ((LinearCurve2D) curve).leftTangent(pos);
            }
        }

        throw new IllegalArgumentException("Unknown type of curve: should be either continuous or curveset");
    }

    @Override
    public PolyOrientedCurve2D<? extends IContinuousOrientedCurve2D> reverse() {
        IContinuousOrientedCurve2D[] curves2 = new IContinuousOrientedCurve2D[curves.size()];
        int n = curves.size();
        for (int i = 0; i < n; i++)
            curves2[i] = curves.get(n - 1 - i).reverse();
        return new PolyOrientedCurve2D<>(curves2);
    }

    /**
     * Returns a portion of this curve as an instance of PolyOrientedCurve2D.
     */
    @Override
    public PolyOrientedCurve2D<? extends IContinuousOrientedCurve2D> subCurve(double t0, double t1) {
        PolyCurve2D<?> set = super.subCurve(t0, t1);
        PolyOrientedCurve2D<IContinuousOrientedCurve2D> subCurve = new PolyOrientedCurve2D<>();
        subCurve.setClosed(false);

        // convert to PolySmoothCurve by adding curves.
        for (ICurve2D curve : set.curves())
            subCurve.add((IContinuousOrientedCurve2D) curve);

        return subCurve;
    }

    /**
     * Clips the PolyCurve2D by a box. The result is an instance of CurveSet2D, which contains only instances of ContinuousOrientedCurve2D. If the PolyCurve2D is not clipped, the result is an instance of CurveSet2D which contains 0 curves.
     */
    @Override
    public ICurveSet2D<? extends IContinuousOrientedCurve2D> clip(Box2D box) {
        // Clip the curve
        ICurveSet2D<? extends ICurve2D> set = Curves2DUtil.clipCurve(this, box);

        // Stores the result in appropriate structure
        int n = set.size();
        CurveArray2D<IContinuousOrientedCurve2D> result = new CurveArray2D<>(n);

        // convert the result
        for (ICurve2D curve : set.curves()) {
            if (curve instanceof IContinuousOrientedCurve2D)
                result.add((IContinuousOrientedCurve2D) curve);
        }
        return result;
    }

    @Override
    public PolyOrientedCurve2D<? extends IContinuousOrientedCurve2D> transform(AffineTransform2D trans) {
        PolyOrientedCurve2D<IContinuousOrientedCurve2D> result = new PolyOrientedCurve2D<>();
        for (IContinuousOrientedCurve2D curve : curves)
            result.add(curve.transform(trans));
        result.setClosed(this.closed);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        // check class
        if (!(obj instanceof ICurveSet2D<?>))
            return false;
        // call superclass method
        return super.equals(obj);
    }

}
