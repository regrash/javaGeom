/**
 * File: 	AbstractSmoothCurve2D.java
 * Project: javaGeom
 * 
 * Distributed under the LGPL License.
 *
 * Created: 21 mai 09
 */
package math.geom2d.curve;

import java.util.ArrayList;
import java.util.Collection;

import math.geom2d.Vector2D;
import math.geom2d.point.Point2D;

/**
 * Provides a base implementation for smooth curves.
 * 
 * @author dlegland
 */
public abstract class AbstractSmoothCurve2D extends AbstractContinuousCurve2D implements ISmoothCurve2D {
    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * 
     * @see math.geom2d.curve.ContinuousCurve2D#leftTangent(double)
     */
    @Override
    public Vector2D leftTangent(double t) {
        return this.tangent(t);
    }

    /*
     * (non-Javadoc)
     * 
     * @see math.geom2d.curve.ContinuousCurve2D#rightTangent(double)
     */
    @Override
    public Vector2D rightTangent(double t) {
        return this.tangent(t);
    }

    /*
     * (non-Javadoc)
     * 
     * @see math.geom2d.curve.ContinuousCurve2D#normal(double)
     */
    @Override
    public Vector2D normal(double t) {
        return this.tangent(t).rotate(-Math.PI / 2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see math.geom2d.curve.ContinuousCurve2D#smoothPieces()
     */
    @Override
    public Collection<? extends ISmoothCurve2D> smoothPieces() {
        return wrapCurve(this);
    }

    /**
     * Returns an empty set of Point2D, as a smooth curve does not have singular points by definition.
     * 
     * @see math.geom2d.curve.ICurve2D#singularPoints()
     */
    @Override
    public Collection<Point2D> singularPoints() {
        return new ArrayList<>(0);
    }

    /**
     * Returns a set of Point2D, containing the extremities of the curve if they are not infinite.
     * 
     * @see math.geom2d.curve.ICurve2D#vertices()
     */
    @Override
    public Collection<Point2D> vertices() {
        ArrayList<Point2D> array = new ArrayList<>(2);
        if (!Double.isInfinite(this.t0()))
            array.add(this.firstPoint());
        if (!Double.isInfinite(this.t1()))
            array.add(this.lastPoint());
        return array;
    }

    /**
     * Returns always false, as a smooth curve does not have singular points by definition.
     * 
     * @see math.geom2d.curve.ICurve2D#isSingular(double)
     */
    @Override
    public boolean isSingular(double pos) {
        return false;
    }

}
