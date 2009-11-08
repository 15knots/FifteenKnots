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
	final Position2D pt=
	  points.get( (i + 1) & (i + 1 < numPoints ? -1 : 0));

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
	  orientation= (convexity > 0) ? 1.0 : ( -1.0);
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
    // hull vectors
    final Point[] vect= new Point[numPoints];
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
	final Position2D pt= hull.get( (i + 1) & (i + 1 < numPoints ? -1 : 0));

	dx= pt.getLongitude() - pt0.getLongitude();
	dy= pt.getLatitude() - pt0.getLatitude();

	vect[i]= new Point( dx, dy);
	inv_vect_length[i]= 1.0 / Math.hypot( dx, dy);

	pt0= pt;
      }

      /* find convex hull orientation */
      double ax= vect[numPoints - 1].getX();
      double ay= vect[numPoints - 1].getY();
      double orientation= 0.0; // orientation of base vector

      for (int i= 0; i < numPoints; i++) {
	final double bx= vect[i].getX();
	final double by= vect[i].getY();

	final double convexity= ax * by - ay * bx;

	if (convexity != 0) {
	  orientation= convexity > 0 ? 1.0 : -1.0;
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
	base_a * vect[seq[0]].getX() + base_b * vect[seq[0]].getY();
      final double dp1=
	-base_b * vect[seq[1]].getX() + base_a * vect[seq[1]].getY();
      final double dp2=
	-base_a * vect[seq[2]].getX() - base_b * vect[seq[2]].getY();
      final double dp3=
	base_b * vect[seq[3]].getX() - base_a * vect[seq[3]].getY();

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
	final double lead_x= vect[pindex].getX() * inv_vect_length[pindex];
	final double lead_y= vect[pindex].getY() * inv_vect_length[pindex];
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
// System.out.println( "Length="+ Math.sqrt( base_a * base_a + base_b *
      // base_b));
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
   * @return an array containing the rectangle's coners or null, if hull
   *         contains less than two points.
   */
  public static Position2D[] mbb( final List<Position2D> hull) {

    final int numPoints= hull.size();
    if (numPoints < 2) {
      return null;
    }
    else if (numPoints == 2) {
      Position2D[] out=
	new Position2D[] { hull.get( 0), hull.get( 1), hull.get( 1),
	  hull.get( 0) };
      return out;
    }
    else { // numPoints > 2
      // rotate calipers
      final MinRectDescriptor minRect= rotating_calipers( hull);

      // caliper vertices...
      final Point c1= new Point( minRect.base_a, minRect.base_b);
      final Point c2= new Point( -minRect.base_b, minRect.base_a);
      final Point c3= new Point( -minRect.base_a, -minRect.base_b);
      final Point c4= new Point( minRect.base_b, -minRect.base_a);

      final Position2D leftist= hull.get( minRect.leftist_point_idx);
      final Position2D bottom= hull.get( minRect.bottom_point_idx);
      final Position2D rightist= hull.get( minRect.rightist_point_idx);
      final Position2D topist= hull.get( minRect.topist_point_idx);
      final double C1=
	c1.x * leftist.getLongitude() + c1.y * leftist.getLatitude();
      final double C2=
	c2.x * bottom.getLongitude() + c2.y * bottom.getLatitude();
      final double C3=
	c3.x * rightist.getLongitude() + c3.y * rightist.getLatitude();
      final double C4=
	c4.x * topist.getLongitude() + c4.y * topist.getLatitude();

      final double idet=
	1.f / (minRect.base_a * minRect.base_a + minRect.base_b
	  * minRect.base_b);
      final double px= (C1 * c2.y - C2 * c1.y) * idet;
      final double py= (c1.x * C2 - c2.x * C1) * idet;

      Position2D[] out=
	new Position2D[] {
// new Position2D( px, py), // corner
// new Position2D( px, py),// startkennung
// new Position2D( px - 0.001, py - 0.001), // startkennung
// new Position2D( px + 0.001, py - 0.001), // startkennung
	  new Position2D( px, py), // corner
	  new Position2D( (C2 * c3.y - C3 * c2.y) * idet, (c2.x * C3 - c3.x
	    * C2)
	    * idet),
	  new Position2D( (C3 * c4.y - C4 * c3.y) * idet, (c3.x * C4 - c4.x
	    * C3)
	    * idet),
	  new Position2D( (C4 * c1.y - C1 * c4.y) * idet, (c4.x * C1 - c1.x
	    * C4)
	    * idet) };

      return out;
    }
  }

  public static Position2D[] mbbSpherical( final List<Position2D> hull) {
    final int numPoints= hull.size();
    if (numPoints < 2) {
      return null;
    }
    else if (numPoints == 2) {
      Position2D[] out=
	new Position2D[] { hull.get( 0), hull.get( 1), hull.get( 1),
	  hull.get( 0) };
      return out;
    }
    else { // numPoints > 2

      /* whether we will use usual cartesian coordinates */
      final boolean stereographic= false;
      // hull after projection
      List<Position2D> points= new ArrayList<Position2D>( numPoints);

      final double r= 6371000.8; // earth radius in meter
      // projection of polar coordinates
      double avg_longitude;
      double avg_latitude;

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
	  final Position2D pt=
	    hull.get( (i + 1) & (i + 1 < numPoints ? -1 : 0));

	  pt0= pt;
	}

	// projection of polar coordinates, stereographic
	avg_longitude= left_x + (right_x - left_x) / 2;
	avg_latitude= bottom_y + (top_y - bottom_y) / 2;
	if (stereographic) {
	  // Stereographische Projektion, winkeltreu...
	  for (int i= 0; i < numPoints; i++) {
	    final Position2D pt= hull.get( i);
	    final double x=
	      2
		* r
		* Math
		  .tan( Math.toRadians( pt.getLongitude() - avg_longitude) / 2);
	    final double y=
	      2
		* r
		* Math
		  .tan( Math.toRadians( pt.getLatitude() - avg_latitude) / 2);
	    points.add( new Position2D( x, y));
	  }
	}
	else {
	  points= hull;
	}
      }

      /**************************************************************************/
      // rotate calipers
      final MinRectDescriptor minRect= rotating_calipers( points);
      System.out.println( "base_a=" + minRect.base_a + ",\tbase_b="
	+ minRect.base_b);

      // angle of tangent plane on sphere at average latitude (in radians)
      double thetaLat= Math.toRadians( 90.0 - avg_latitude);
      double thetaLon= Math.toRadians( avg_longitude);
// double hypot_a= Math.hypot( minRect.base_a ,minRect.base_a*Math.tan( theta));
// double hypot_b= Math.hypot( minRect.base_b ,minRect.base_b*Math.tan( theta));
      double cosLat= Math.cos( thetaLat);
      double cosLon= Math.cos( thetaLon);
      // caliper vertices...
      final Point c1= new Point( minRect.base_a, minRect.base_b);
      final Point c2= new Point( -minRect.base_b, minRect.base_a);
      final Point c3= new Point( -minRect.base_a, -minRect.base_b);
      final Point c4= new Point( minRect.base_b, -minRect.base_a);

      final double C1=
	c1.x * hull.get( minRect.leftist_point_idx).getLongitude() + c1.y
	  * hull.get( minRect.leftist_point_idx).getLatitude();
      final double C2=
	c2.x * hull.get( minRect.bottom_point_idx).getLongitude() + c2.y
	  * hull.get( minRect.bottom_point_idx).getLatitude();
      final double C3=
	c3.x * hull.get( minRect.rightist_point_idx).getLongitude() + c3.y
	  * hull.get( minRect.rightist_point_idx).getLatitude();
      final double C4=
	c4.x * hull.get( minRect.topist_point_idx).getLongitude() + c4.y
	  * hull.get( minRect.topist_point_idx).getLatitude();

      final double idet=
	1.f / (minRect.base_a * minRect.base_a + minRect.base_b
	  * minRect.base_b);
      final double px= (C1 * c2.y - C2 * c1.y) * idet;
      final double py= (c1.x * C2 - c2.x * C1) * idet;

      Position2D[] out;
      if (false) {
	final double dreieck= 0.02;
	out=
	  new Position2D[] {
	    new Position2D( c1.x + avg_longitude, c1.y + avg_latitude), // startkennung
	    new Position2D( c1.x + avg_longitude - dreieck, c1.y + avg_latitude
	      - dreieck),// startkennung
	    new Position2D( c1.x + avg_longitude + dreieck, c1.y + avg_latitude
	      - dreieck),// startkennung
	    new Position2D( c1.x + avg_longitude, c1.y + avg_latitude),
	    new Position2D( c2.x + avg_longitude, c2.y + avg_latitude),
	    new Position2D( c3.x + avg_longitude, c3.y + avg_latitude),
	    new Position2D( c4.x + avg_longitude, c4.y + avg_latitude), };
      }
      else {
	out=
	  new Position2D[] {

// new Position2D( px, py),// startkennung
// new Position2D( px - 0.001, py - 0.001), // startkennung
// new Position2D( px + 0.001, py - 0.001), // startkennung

	    new Position2D( px, py), // corner
	    new Position2D( (C2 * c3.y - C3 * c2.y) * idet, (c2.x * C3 - c3.x
	      * C2)
	      * idet),
	    new Position2D( (C3 * c4.y - C4 * c3.y) * idet, (c3.x * C4 - c4.x
	      * C3)
	      * idet),
	    new Position2D( (C4 * c1.y - C1 * c4.y) * idet, (c4.x * C1 - c1.x
	      * C4)
	      * idet), };

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
      }
      System.out.println( "out= " + Arrays.deepToString( out));
      return out;
    }
  }

  // //////////////////////////////////////////////////////////////////
  // inner classes
  // //////////////////////////////////////////////////////////////////

  /**
   * A two dimensional point.
   *
   * @author Martin Weber
   */
  private static class Point {

    final double x, y;

    /**
     * @param x
     * @param y
     */
    public Point( double x, double y) {
      this.x= x;
      this.y= y;
    }

    /**
     * Gets the x property.
     *
     * @return the current x property.
     */
    public final double getX() {
      return this.x;
    }

    /**
     * Gets the y property.
     *
     * @return the current y property.
     */
    public final double getY() {
      return this.y;
    }

  }

  /**
   * A three dimensional point.
   *
   * @author Martin Weber
   */
  private static class Point3D extends Point {

    final double z;

    /**
     * @param x
     * @param y
     */
    public Point3D( double x, double y, double z) {
      super( x, y);
      this.z= z;
    }

    public Point3D( double[] xyz) {
      this( xyz[0], xyz[2], xyz[2]);
    }

    public static Point3D fromPosition2D( Position2D pos, double radius) {
      final double sinLon= Math.sin( Math.toRadians( pos.getLongitude()));
      return new Point3D( radius * sinLon
	* Math.cos( Math.toRadians( 90.0 - pos.getLatitude())), radius * sinLon
	* Math.sin( Math.toRadians( 90.0 - pos.getLatitude())), radius
	* Math.cos( Math.toRadians( pos.getLongitude())));
    }

    public Position2D toPosition2D() {
      // http://de.wikipedia.org/wiki/Kugelkoordinaten
      return null; // TODO
    }

    /**
     * Gets the z property.
     *
     * @return the current z property.
     */
    public final double getZ() {
      return this.z;
    }

  }

  /**
   * Internal representation of the minimum bounding box.
   *
   * @author Martin Weber
   */
  private static class MinRectDescriptor {

    /* index of leftist point in list of hull points */
    int leftist_point_idx;

    int rightist_point_idx;

    /* bottom point */
    int bottom_point_idx;

    int topist_point_idx;

    /**
     * Caliper parameters. Rotating calipers sides will always have vertex
     * coordinates <code>(a,b) (-b,a) (-a,-b) (b, -a)</code> with
     * <code>sqrt(a*a+b*b) == 1</code>. Minimum bounding box has edges parallel
     * to rotating caliper.
     */
    double base_a, base_b;
  }

}
