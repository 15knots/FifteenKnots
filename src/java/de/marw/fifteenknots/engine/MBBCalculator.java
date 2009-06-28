// $Id$
// Copyright © 2009 Martin Weber

package de.marw.fifteenknots.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.marw.fifteenknots.nmeareader.Position2D;


/**
 * Minimum Bounding Box
 *
 * @author Martin Weber
 */
public class MBBCalculator {

  /**
   * Rotating Calipers algorithm calculating the minimum area enclosing
   * rectangle. <a href="http://cgm.cs.mcgill.ca/~orm/maer.html"> Uses method
   * descibed here</a> with some applications.
   * <p>
   * This implementation is based on code borrowed from the <a
   * href="http://www.sourceforge.net/projects/opencvlibrary"> Open Source
   * Computer Vision Library</a>,
   * </p>
   *
   * @param points
   *        convex hull vertices ( any orientation )
   * @return output info (TODO).
   *
   *         <pre>
   * ((CvPoint2D32f*)out)[0] - corner
   * ((CvPoint2D32f*)out)[1] - vector1
   * ((CvPoint2D32f*)out)[0] - corner2
   *           &circ;
   *           |
   *   vector2 |
   *           |
   *           |____________\
   *         corner         /
   *                vector1
   * </pre>
   */
  public static Position2D[] mbbCart( final List<Position2D> points) {
    /* we will use usual cartesian coordinates */
    final int numPoints= points.size();
    final Position2D[] vect= new Position2D[numPoints];
    final double[] inv_vect_length= new double[numPoints];
    // indexes of extremal points
    int left= 0, bottom= 0, right= 0, top= 0;

    /*
     * rotating calipers sides will always have coordinates (a,b) (-b,a) (-a,-b)
     * (b, -a)
     */
    /* this is a first base bector (a,b) initialized by (1,0) */
    double base_a;
    double base_b= 0;

    {
      /* coordinates of extremal points */
      double left_x, right_x, top_y, bottom_y;

      // find extremal points...
      Position2D pt0= points.get( 0);
      left_x= right_x= pt0.getLongitude();
      top_y= bottom_y= pt0.getLatitude();
      for (int i= 0; i < numPoints; i++) {
	double dx, dy;

	if (pt0.getLongitude() < left_x) {
	  left_x= pt0.getLongitude();
	  left= i;
	}

	if (pt0.getLongitude() > right_x) {
	  right_x= pt0.getLongitude();
	  right= i;
	}
	if (pt0.getLatitude() > top_y) {
	  top_y= pt0.getLatitude();
	  top= i;
	}
	if (pt0.getLatitude() < bottom_y) {
	  bottom_y= pt0.getLatitude();
	  bottom= i;
	}
	final Position2D pt= points.get( (i + 1) & (i + 1 < numPoints
	  ? -1 : 0));

	dx= pt.getLongitude() - pt0.getLongitude();
	dy= pt.getLatitude() - pt0.getLatitude();

	vect[i]= new Position2D( dx, dy);
	inv_vect_length[i]= 1.0 / Math.hypot( dx, dy);

	pt0= pt;
      }

      /* find convex hull orientation */
      double ax= vect[numPoints - 1].getLongitude();
      double ay= vect[numPoints - 1].getLatitude();
      double orientation= 0.0; // orientation of base vector

      for (int i= 0; i < numPoints; i++) {
	final double bx= vect[i].getLongitude();
	final double by= vect[i].getLatitude();

	final double convexity= ax * by - ay * bx;

	if (convexity != 0) {
	  orientation= (convexity > 0)
	    ? 1.0 : ( -1.0);
	  break;
	}
	ax= bx;
	ay= by;
      }
      assert (orientation != 0);
      base_a= orientation;
    }

    /**************************************************************************/
    // init calipers position
    final int seq[]= new int[] { bottom, right, top, left };

    class MinRect {

      /* leftist point */
      int leftist_point_idx;

      /* bottom point */
      int bottom_point_idx;

      double base_a;

      double base_b;

      double width;

      double height;

      double area;
    }

    final MinRect minRect= new MinRect();

    /**************************************************************************/
    // Main loop - evaluate angles and rotate calipers
    double minarea= Float.MAX_VALUE;
    /* all of edges will be checked while rotating calipers by 90 degrees */
    for (int k= 0; k < numPoints; k++) {
      /* compute cosine of angle between calipers side and polygon edge */
      /* dp - dot product */
      final double dp0=
	base_a * vect[seq[0]].getLongitude() + base_b
	  * vect[seq[0]].getLatitude();
      final double dp1=
	-base_b * vect[seq[1]].getLongitude() + base_a
	  * vect[seq[1]].getLatitude();
      final double dp2=
	-base_a * vect[seq[2]].getLongitude() - base_b
	  * vect[seq[2]].getLatitude();
      final double dp3=
	base_b * vect[seq[3]].getLongitude() - base_a
	  * vect[seq[3]].getLatitude();

      double cosalpha= dp0 * inv_vect_length[seq[0]];
      double maxcos= cosalpha;

      /* number of calipers edges, that has minimal angle with edge */
      int main_element= 0;

      /* choose minimal angle */
      cosalpha= dp1 * inv_vect_length[seq[1]];
      if (cosalpha > maxcos) {
	main_element= 1;
	maxcos= cosalpha;
      }
      cosalpha= dp2 * inv_vect_length[seq[2]];
      if (cosalpha > maxcos) {
	main_element= 2;
	maxcos= cosalpha;
      }
      cosalpha= dp3 * inv_vect_length[seq[3]];
      if (cosalpha > maxcos) {
	main_element= 3;
	maxcos= cosalpha;
      }

      /* rotate calipers */
      {
	// get next base
	final int pindex= seq[main_element];
	final double lead_x=
	  vect[pindex].getLongitude() * inv_vect_length[pindex];
	final double lead_y=
	  vect[pindex].getLatitude() * inv_vect_length[pindex];
	switch (main_element) {
	  case 0:
	    base_a= lead_x;
	    base_b= lead_y;
	  break;
	  case 1:
	    base_a= lead_y;
	    base_b= -lead_x;
	  break;
	  case 2:
	    base_a= -lead_x;
	    base_b= -lead_y;
	  break;
	  case 3:
	    base_a= -lead_y;
	    base_b= lead_x;
	  break;
	  default:
	    assert true : "main_element > 3";
	}
      }
      /* change base point of main edge */
      seq[main_element]+= 1;
      if (seq[main_element] == numPoints)
	seq[main_element]= 0;
      {
	/* determine area of rectangle */

	/* find vector left-right */
	double dx=
	  points.get( seq[1]).getLongitude()
	    - points.get( seq[3]).getLongitude();
	double dy=
	  points.get( seq[1]).getLatitude() - points.get( seq[3]).getLatitude();

	/* dotproduct */
	final double width= dx * base_a + dy * base_b;

	/* find vector bottom-top */
	dx=
	  points.get( seq[2]).getLongitude()
	    - points.get( seq[0]).getLongitude();
	dy=
	  points.get( seq[2]).getLatitude() - points.get( seq[0]).getLatitude();

	/* dotproduct */
	final double height= -dx * base_b + dy * base_a;
	final double area= width * height;
	if (area <= minarea) {
	  minarea= area;

	  /* leftist point */
	  minRect.leftist_point_idx= seq[3];
	  /* bottom point */
	  minRect.bottom_point_idx= seq[0];
	  minRect.base_a= base_a;
	  minRect.base_b= base_b;
	  minRect.width= width;
	  minRect.height= height;
	  minRect.area= area;
	}
      }
    } /* for */

    final double A1= minRect.base_a;
    final double B1= minRect.base_b;

    final double A2= -minRect.base_b;
    final double B2= minRect.base_a;

    final double C1=
      A1 * points.get( minRect.leftist_point_idx).getLongitude() + B1
	* points.get( minRect.leftist_point_idx).getLatitude();
    final double C2=
      A2 * points.get( minRect.bottom_point_idx).getLongitude() + B2
	* points.get( minRect.bottom_point_idx).getLatitude();

    final double idet= 1.f / (A1 * B2 - A2 * B1);
    final double px= (C1 * B2 - C2 * B1) * idet;
    final double py= (A1 * C2 - A2 * C1) * idet;

    Position2D[] out;
    // this would be ok in cartesian coordinates, but we have polar coordinates
    out=
      new Position2D[] {
	new Position2D( px, py), // corner
	new Position2D( px, py),// startkennung
	new Position2D( px - 0.001, py - 0.001), // startkennung
	new Position2D( px + 0.001, py - 0.001), // startkennung
	new Position2D( px, py), // corner
	new Position2D( px + minRect.base_a * minRect.width, py
	  + minRect.base_b * minRect.width),
	new Position2D( px + minRect.base_a * minRect.width
	  + ( -minRect.base_b) * minRect.height, py + minRect.base_b
	  * minRect.width + minRect.base_a * minRect.height),
	new Position2D( px + ( -minRect.base_b) * minRect.height, py
	  + minRect.base_a * minRect.height),
// new Position2D( cx, cy),// center?
      };
// System.out.println( "out= " + Arrays.deepToString( out));
    return out;
  }

  /**
   * Rotating Calipers algorithm calculating the minimum area enclosing
   * rectangle. <a href="http://cgm.cs.mcgill.ca/~orm/maer.html"> Uses method
   * descibed here</a> with some applications.
   * <p>
   * This implementation is based on code borrowed from the <a
   * href="http://www.sourceforge.net/projects/opencvlibrary"> Open Source
   * Computer Vision Library</a>,
   * </p>
   *
   * @param hull
   *        convex hull vertices ( any orientation )
   * @return output info (TODO).
   *
   *         <pre>
   * ((CvPoint2D32f*)out)[0] - corner
   * ((CvPoint2D32f*)out)[1] - vector1
   * ((CvPoint2D32f*)out)[0] - corner2
   *           &circ;
   *           |
   *   vector2 |
   *           |
   *           |____________\
   *         corner         /
   *                vector1
   * </pre>
   */
  private static MinRectDescriptor rotating_calipers( List<Position2D> hull) {
    /* we will use usual cartesian coordinates */
    final int numPoints= hull.size();

    final Position2D[] vect= new Position2D[numPoints];
    // inverse lengths of vectors..
    final double[] inv_vect_length= new double[numPoints];
    // indexes of extremal points
    int left= 0, bottom= 0, right= 0, top= 0;

    /*
     * rotating calipers sides will always have coordinates (a,b) (-b,a) (-a,-b)
     * (b, -a)
     */
    /* this is a first base bector (a,b) initialized by (1,0) */
    double base_a;
    double base_b= 0;

    {
      /* coordinates of extremal points */
      double left_x, right_x, top_y, bottom_y;

      // find extremal points...
      Position2D pt0= hull.get( 0);
      left_x= right_x= pt0.getLongitude();
      top_y= bottom_y= pt0.getLatitude();
      for (int i= 0; i < numPoints; i++) {
	double dx, dy;

	if (pt0.getLongitude() < left_x) {
	  left_x= pt0.getLongitude();
	  left= i;
	}

	if (pt0.getLongitude() > right_x) {
	  right_x= pt0.getLongitude();
	  right= i;
	}
	if (pt0.getLatitude() > top_y) {
	  top_y= pt0.getLatitude();
	  top= i;
	}
	if (pt0.getLatitude() < bottom_y) {
	  bottom_y= pt0.getLatitude();
	  bottom= i;
	}
	final Position2D pt= hull.get( (i + 1) & (i + 1 < numPoints
	  ? -1 : 0));

	dx= pt.getLongitude() - pt0.getLongitude();
	dy= pt.getLatitude() - pt0.getLatitude();

	vect[i]= new Position2D( dx, dy);
	inv_vect_length[i]= 1.0 / Math.hypot( dx, dy);

	pt0= pt;
      }

      /* find convex hull orientation */
      double ax= vect[numPoints - 1].getLongitude();
      double ay= vect[numPoints - 1].getLatitude();
      double orientation= 0.0; // orientation of base vector

      for (int i= 0; i < numPoints; i++) {
	final double bx= vect[i].getLongitude();
	final double by= vect[i].getLatitude();

	final double convexity= ax * by - ay * bx;

	if (convexity != 0) {
	  orientation= (convexity > 0)
	    ? 1.0 : ( -1.0);
	  break;
	}
	ax= bx;
	ay= by;
      }
      assert (orientation != 0);
      base_a= orientation;
    }

    /**************************************************************************/
    // init calipers position
    final int seq[]= new int[] { bottom, right, top, left };

    final MinRectDescriptor minRect= new MinRectDescriptor();

    /**************************************************************************/
    // Main loop - evaluate angles and rotate calipers
    double minarea= Float.MAX_VALUE;
    /* all of edges will be checked while rotating calipers by 90 degrees */
    for (int k= 0; k < numPoints; k++) {
      /* compute cosine of angle between calipers side and polygon edge */
      /* dp - dot product */
      final double dp0=
	base_a * vect[seq[0]].getLongitude() + base_b
	  * vect[seq[0]].getLatitude();
      final double dp1=
	-base_b * vect[seq[1]].getLongitude() + base_a
	  * vect[seq[1]].getLatitude();
      final double dp2=
	-base_a * vect[seq[2]].getLongitude() - base_b
	  * vect[seq[2]].getLatitude();
      final double dp3=
	base_b * vect[seq[3]].getLongitude() - base_a
	  * vect[seq[3]].getLatitude();

      double cosalpha= dp0 * inv_vect_length[seq[0]];
      double maxcos= cosalpha;

      /* index of calipers edge, that has minimal angle with edge */
      int main_element= 0;

      /* choose minimal angle */
      cosalpha= dp1 * inv_vect_length[seq[1]];
      if (cosalpha > maxcos) {
	main_element= 1;
	maxcos= cosalpha;
      }
      cosalpha= dp2 * inv_vect_length[seq[2]];
      if (cosalpha > maxcos) {
	main_element= 2;
	maxcos= cosalpha;
      }
      cosalpha= dp3 * inv_vect_length[seq[3]];
      if (cosalpha > maxcos) {
	main_element= 3;
	maxcos= cosalpha;
      }

      /* rotate calipers */
      {
	// get next base
	final int pindex= seq[main_element];
	final double lead_x=
	  vect[pindex].getLongitude() * inv_vect_length[pindex];
	final double lead_y=
	  vect[pindex].getLatitude() * inv_vect_length[pindex];
	switch (main_element) {
	  case 0:
	    base_a= lead_x;
	    base_b= lead_y;
	  break;
	  case 1:
	    base_a= lead_y;
	    base_b= -lead_x;
	  break;
	  case 2:
	    base_a= -lead_x;
	    base_b= -lead_y;
	  break;
	  case 3:
	    base_a= -lead_y;
	    base_b= lead_x;
	  break;
	  default:
	    assert true : "main_element > 3";
	}
      }
      /* change base point of main edge */
      seq[main_element]+= 1;
      if (seq[main_element] == numPoints)
	seq[main_element]= 0;
      // System.out.println( "base_a=" + base_a + ",\tbase_b=" + base_b
      // + "\tmain=" + main_element);
      // System.out.println( "Length="
      // + Math.sqrt( base_a * base_a + base_b * base_b));
      {
	/* determine area of rectangle */

	/* find vector left-right */
	double dx=
	  hull.get( seq[1]).getLongitude() - hull.get( seq[3]).getLongitude();
	double dy=
	  hull.get( seq[1]).getLatitude() - hull.get( seq[3]).getLatitude();

	/* dotproduct */
	final double width= dx * base_a + dy * base_b;

	/* find vector bottom-top */
	dx= hull.get( seq[2]).getLongitude() - hull.get( seq[0]).getLongitude();
	dy= hull.get( seq[2]).getLatitude() - hull.get( seq[0]).getLatitude();

	/* dotproduct */
	final double height= -dx * base_b + dy * base_a;
	final double area= width * height;
	if (area <= minarea) {
	  minarea= area;
	  /* leftist point */
	  minRect.leftist_point_idx= seq[3];
	  minRect.rightist_point_idx= seq[1];
	  /* bottom point */
	  minRect.bottom_point_idx= seq[0];
	  minRect.topist_point_idx= seq[2];
	  minRect.base_a= base_a;
	  minRect.base_b= base_b;
	}
      }
    } /* for */
    return minRect;
  }

  /**
   * Rotating Calipers algorithm calculating the minimum area enclosing
   * rectangle. <a href="http://cgm.cs.mcgill.ca/~orm/maer.html"> Uses method
   * descibed here</a> with some applications.
   * <p>
   * This implementation is based on code borrowed from the <a
   * href="http://www.sourceforge.net/projects/opencvlibrary"> Open Source
   * Computer Vision Library</a>,
   * </p>
   *
   * @param hull
   *        convex hull vertices ( any orientation )
   * @return an array containing the rectangles coners.
   */
  public static Position2D[] mbb( final List<Position2D> hull) {
    // rotate calipers
    final MinRectDescriptor minRect= rotating_calipers( hull);

    // caliper edgess...
    final double A1= minRect.base_a;
    final double B1= minRect.base_b;

    final double A2= -minRect.base_b;
    final double B2= minRect.base_a;

    final double A3= -minRect.base_a;
    final double B3= -minRect.base_b;
    final double A4= minRect.base_b;
    final double B4= -minRect.base_a;
    final double C1=
      A1 * hull.get( minRect.leftist_point_idx).getLongitude() + B1
	* hull.get( minRect.leftist_point_idx).getLatitude();
    final double C2=
      A2 * hull.get( minRect.bottom_point_idx).getLongitude() + B2
	* hull.get( minRect.bottom_point_idx).getLatitude();
    final double C3=
      A3 * hull.get( minRect.rightist_point_idx).getLongitude() + B3
	* hull.get( minRect.rightist_point_idx).getLatitude();
    final double C4=
      A4 * hull.get( minRect.topist_point_idx).getLongitude() + B4
	* hull.get( minRect.topist_point_idx).getLatitude();

    final double idet=
      1.f / (minRect.base_a * minRect.base_a + minRect.base_b * minRect.base_b);
    final double px= (C1 * B2 - C2 * B1) * idet;
    final double py= (A1 * C2 - A2 * C1) * idet;

    Position2D[] out;
    out=
      new Position2D[] {
// new Position2D( px, py), // corner
// new Position2D( px, py),// startkennung
// new Position2D( px - 0.001, py - 0.001), // startkennung
// new Position2D( px + 0.001, py - 0.001), // startkennung
	new Position2D( px, py), // corner
	new Position2D( (C2 * B3 - C3 * B2) * idet, (A2 * C3 - A3 * C2) * idet),
	new Position2D( (C3 * B4 - C4 * B3) * idet, (A3 * C4 - A4 * C3) * idet),
	new Position2D( (C4 * B1 - C1 * B4) * idet, (A4 * C1 - A1 * C4) * idet),

// new Position2D( px + minRect.base_a * minRect.width, py
// + minRect.base_b * minRect.width),
// new Position2D( px + minRect.base_a * minRect.width
// + ( -minRect.base_b) * minRect.height, py + minRect.base_b
// * minRect.width + minRect.base_a * minRect.height),
// new Position2D( px + ( -minRect.base_b) * minRect.height, py
// + minRect.base_a * minRect.height), new Position2D( px, py), // corner
// new Position2D( cx, cy),// center?
      };
    System.out.println( "out= " + Arrays.deepToString( out));

    return out;
  }

  public static Position2D[] mbbSperical( final List<Position2D> hull) {
    final boolean stereographic= false;
    /* we will use usual cartesian coordinates */
    final int numPoints= hull.size();
    // hull after projection
    List<Position2D> points= new ArrayList<Position2D>( numPoints);

    final double r= 6371000.8; // earth radius in meter
    // projection of polar coordinates
    double axis_x;
    double axis_y;

    if (stereographic) {
      {
	/* coordinates of extremal points */
	double left_x, right_x, top_y, bottom_y;

	// find extremal points...
	Position2D pt0= hull.get( 0);
	left_x= right_x= pt0.getLongitude();
	top_y= bottom_y= pt0.getLatitude();
	for (int i= 0; i < numPoints; i++) {
	  if (pt0.getLongitude() < left_x) {
	    left_x= pt0.getLongitude();
	  }

	  if (pt0.getLongitude() > right_x) {
	    right_x= pt0.getLongitude();
	  }
	  if (pt0.getLatitude() > top_y) {
	    top_y= pt0.getLatitude();
	  }
	  if (pt0.getLatitude() < bottom_y) {
	    bottom_y= pt0.getLatitude();
	  }
	  final Position2D pt= hull.get( (i + 1) & (i + 1 < numPoints
	    ? -1 : 0));

	  pt0= pt;
	}

	// projection of polar coordinates, stereographic
	axis_x= left_x + (right_x - left_x) / 2;
	axis_y= bottom_y + (top_y - bottom_y) / 2;
	// Stereographische Projektion, winkeltreu...
	for (int i= 0; i < numPoints; i++) {
	  final Position2D pt= hull.get( i);
	  final double x=
	    2 * r * Math.tan( Math.toRadians( pt.getLongitude() - axis_x) / 2);
	  final double y=
	    2 * r * Math.tan( Math.toRadians( pt.getLatitude() - axis_y) / 2);
	  points.add( new Position2D( x, y));
	}
      }
    }
    else {
      points= hull;
    }
    // rotate calipers
    final MinRectDescriptor minRect= rotating_calipers( points);

    Position2D[] out= null;

    if (stereographic) {
      // TODO
// rückrechnen
      out= new Position2D[] {
// new Position2D( Math.toDegrees( Math.atan( px / (2 * r)) * 2)
// + axis_x, Math.toDegrees( Math.atan( py / (2 * r)) * 2) + axis_y),
// new Position2D( Math.toDegrees( Math.atan( px / (2 * r)) * 2)
// + axis_x - 0.001, Math.toDegrees( Math.atan( py / (2 * r)) * 2)
// + axis_y - 0.001), // startkennung
// new Position2D( Math.toDegrees( Math.atan( px / (2 * r)) * 2)
// + axis_x + 0.001, Math.toDegrees( Math.atan( py / (2 * r)) * 2)
// + axis_y - 0.001), // startkennung
	};
    }
    return out;
  }

  // //////////////////////////////////////////////////////////////////
  // inner classes
  // //////////////////////////////////////////////////////////////////
  static private class MinRectDescriptor {

    /* index of leftist point in list of hull points */
    int leftist_point_idx;

    int rightist_point_idx;

    /* bottom point */
    int bottom_point_idx;

    int topist_point_idx;

    /**
     * Caliper parameters. Rotating calipers sides will always have coordinates
     * <code>(a,b) (-b,a) (-a,-b) (b, -a)</code> with
     * <code>sqrt(a*a+b*b) == 1</code>. Minimum bounding box has edges parallel
     * to rotating caliper.
     */
    double base_a, base_b;
  }

}
