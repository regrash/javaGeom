package math.geom2d.circulinear.buffer;

import junit.framework.TestCase;
import math.geom2d.domain.IBoundary2D;
import math.geom2d.domain.IDomain2D;
import math.geom2d.point.Point2D;
import math.geom2d.polygon.LinearRing2D;
import math.geom2d.polygon.Polyline2D;

public class RoundJoinFactoryTest extends TestCase {

    public void testGetBufferPolyline() {
        Polyline2D curve = new Polyline2D(new Point2D[] { new Point2D(50, 50), new Point2D(50, 100), new Point2D(100, 100), new Point2D(100, 50), new Point2D(150, 100), new Point2D(150, 50), });

        BufferCalculator bc1 = new BufferCalculator(new RoundJoinFactory(), new RoundCapFactory());
        IDomain2D domain = bc1.computeBuffer(curve, 20);

        assertTrue(domain.isBounded());
        assertFalse(domain.isEmpty());

        IBoundary2D boundary = domain.boundary();
        assertEquals(1, boundary.continuousCurves().size());
    }

    public void testGetBufferLinearRing() {
        LinearRing2D curve = new LinearRing2D(new Point2D[] { new Point2D(100, 100), new Point2D(200, 100), new Point2D(200, 200), new Point2D(150, 150), new Point2D(100, 200), });

        BufferCalculator bc1 = new BufferCalculator(new RoundJoinFactory(), new RoundCapFactory());
        IDomain2D domain = bc1.computeBuffer(curve, 10);

        assertTrue(domain.isBounded());
        assertFalse(domain.isEmpty());

        IBoundary2D boundary = domain.boundary();
        assertEquals(2, boundary.continuousCurves().size());
    }

    public void testGetParallels_SmallAnglePolyline() {
        Polyline2D polyline = new Polyline2D(new Point2D[] { new Point2D(200, 100), new Point2D(100, 100), new Point2D(180, 140) });
        double dist = 30;

        BufferCalculator bc = BufferCalculator.getDefaultInstance();
        IDomain2D buffer = bc.computeBuffer(polyline, dist);

        assertEquals(1, buffer.boundary().continuousCurves().size());
    }

}
