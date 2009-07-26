/**
 * File: 	PolyCirculinearCurve2D.java
 * Project: javaGeom
 * 
 * Distributed under the LGPL License.
 *
 * Created: 11 mai 09
 */
package math.geom2d.circulinear;

import java.util.ArrayList;
import java.util.Collection;

import math.geom2d.Box2D;
import math.geom2d.curve.Curve2D;
import math.geom2d.curve.Curve2DUtils;
import math.geom2d.curve.CurveSet2D;
import math.geom2d.domain.ContinuousOrientedCurve2D;
import math.geom2d.domain.PolyOrientedCurve2D;
import math.geom2d.transform.CircleInversion2D;


/**
 * A continuous curve which is composed of several continuous circulinear
 * curves.
 * @author dlegland
 *
 */
public class PolyCirculinearCurve2D<T extends ContinuousCirculinearCurve2D>
extends PolyOrientedCurve2D<T> implements ContinuousCirculinearCurve2D {

    // ===================================================================
    // constructors

    public PolyCirculinearCurve2D() {
        super();
    }

    public PolyCirculinearCurve2D(int size) {
        super(size);
    }

    public PolyCirculinearCurve2D(T[] curves) {
        super(curves);
    }

    public PolyCirculinearCurve2D(T[] curves, boolean closed) {
        super(curves, closed);
    }

    public PolyCirculinearCurve2D(Collection<? extends T> curves) {
        super(curves);
    }

    public PolyCirculinearCurve2D(Collection<? extends T> curves, boolean closed) {
        super(curves, closed);
    }

    // ===================================================================
    // methods implementing the CirculinearCurve2D interface

	/* (non-Javadoc)
	 * @see math.geom2d.circulinear.CirculinearCurve2D#getLength()
	 */
	public double getLength() {
		double sum = 0;
		for(CirculinearCurve2D curve : this.getCurves())
			sum += curve.getLength();
		return sum;
	}

	/* (non-Javadoc)
	 * @see math.geom2d.circulinear.CirculinearCurve2D#getLength(double)
	 */
	public double getLength(double pos) {
		return CirculinearCurve2DUtils.getLength(this, pos);
	}

	/* (non-Javadoc)
	 * @see math.geom2d.circulinear.CirculinearCurve2D#getPosition(double)
	 */
	public double getPosition(double length) {
		return CirculinearCurve2DUtils.getPosition(this, length);
	}

	/* (non-Javadoc)
	 * @see math.geom2d.circulinear.CirculinearShape2D#getBuffer(double)
	 */
	public CirculinearDomain2D getBuffer(double dist) {
		return CirculinearCurve2DUtils.computeBuffer(this, dist);
	}

	/* (non-Javadoc)
	 * @see math.geom2d.circulinear.ContinuousCirculinearCurve2D#getParallel(double)
	 */
	public ContinuousCirculinearCurve2D getParallel(double d) {
		return CirculinearCurve2DUtils.createContinuousParallel(this, d);
	}
	
	/* (non-Javadoc)
	 * @see math.geom2d.circulinear.CirculinearCurve2D#transform(math.geom2d.transform.CircleInversion2D)
	 */
	public PolyCirculinearCurve2D<? extends ContinuousCirculinearCurve2D>
	transform(CircleInversion2D inv) {
    	// Allocate array for result
		int n = curves.size();
		PolyCirculinearCurve2D<ContinuousCirculinearCurve2D> result = 
			new PolyCirculinearCurve2D<ContinuousCirculinearCurve2D>(n);
        
        // add each transformed curve
        for (ContinuousCirculinearCurve2D curve : curves)
            result.addCurve(curve.transform(inv));
        return result;
	}

    // ===================================================================
    // methods implementing the ContinuousCurve2D interface

    /*
     * (non-Javadoc)
     * 
     * @see math.geom2d.ContinuousCirculinearCurve2D#getSmoothPieces()
     */
    public Collection<? extends CirculinearElement2D> getSmoothPieces() {
    	// create array for storing result
    	ArrayList<CirculinearElement2D> result = 
    		new ArrayList<CirculinearElement2D>();
    	
    	// add elements of each curve
    	for(ContinuousCirculinearCurve2D curve : curves)
    		result.addAll(curve.getSmoothPieces());
    	
    	// return the collection
        return result;
    }

    // ===================================================================
    // methods implementing the Curve2D interface

    @Override
    public Collection<? extends PolyCirculinearCurve2D<?>> 
    getContinuousCurves() {
        ArrayList<PolyCirculinearCurve2D<T>> list = 
        	new ArrayList<PolyCirculinearCurve2D<T>>(1);
        list.add(this);
        return list;
    }

    public CirculinearCurveSet2D<? extends ContinuousCirculinearCurve2D> 
	clip(Box2D box) {
        // Clip the curve
        CurveSet2D<Curve2D> set = Curve2DUtils.clipCurve(this, box);

        // Stores the result in appropriate structure
        int n = set.getCurveNumber();
        CirculinearCurveSet2D<ContinuousCirculinearCurve2D> result = 
        	new CirculinearCurveSet2D<ContinuousCirculinearCurve2D>(n);

        // convert the result, class cast each curve
        for (Curve2D curve : set.getCurves()) {
            if (curve instanceof ContinuousCirculinearCurve2D)
                result.addCurve((ContinuousCirculinearCurve2D) curve);
        }
        
        // return the new set of curves
        return result;
	}
	
	public PolyCirculinearCurve2D<? extends ContinuousCirculinearCurve2D> 
	getReverseCurve() {
    	int n = curves.size();
        // create array of reversed curves
    	ContinuousCirculinearCurve2D[] curves2 = 
    		new ContinuousCirculinearCurve2D[n];
        
        // reverse each curve
        for (int i = 0; i<n; i++)
            curves2[i] = curves.get(n-1-i).getReverseCurve();
        
        // create the reversed final curve
        return new PolyCirculinearCurve2D<ContinuousCirculinearCurve2D>(curves2);
	}
	
	public PolyCirculinearCurve2D<? extends ContinuousCirculinearCurve2D>
	getSubCurve(double t0, double t1) {
		// Call the superclass method
		PolyOrientedCurve2D<? extends ContinuousOrientedCurve2D> subcurve =
			super.getSubCurve(t0, t1);
		
		// prepare result
		int n = subcurve.getCurveNumber();
		PolyCirculinearCurve2D<ContinuousCirculinearCurve2D> result = 
			new PolyCirculinearCurve2D<ContinuousCirculinearCurve2D>(n);
		
		// add each curve after class cast
		for(Curve2D curve : subcurve) {
			if(curve instanceof ContinuousCirculinearCurve2D)
				result.addCurve((ContinuousCirculinearCurve2D) curve);
		}
		
		// return the result
		return result;
	}

}