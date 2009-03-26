// $Id$
// Copyright © 2009 Martin Weber

package de.marw.fifteenknots.engine;

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
  public static Position2D[] mbb( List<Position2D> points) {
    /* we will use usual cartesian coordinates */
    int numPoints= points.size();
    Position2D[] vect= new Position2D[numPoints];
    double[] inv_vect_length= new double[numPoints];
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
	Position2D pt= points.get( (i + 1) & (i + 1 < numPoints
	  ? -1 : 0));

	dx= pt.getLongitude() - pt0.getLongitude();
	dy= pt.getLatitude() - pt0.getLatitude();

	vect[i]= new Position2D( dx, dy);
	inv_vect_length[i]= 1.0 / Math.hypot( dx, dy);

	pt0= pt;
      }
// cvbInvSqrt( inv_vect_length, inv_vect_length, n );

      /* find convex hull orientation */
      double ax= vect[numPoints - 1].getLongitude();
      double ay= vect[numPoints - 1].getLatitude();
      double orientation= 0.0; // orientation of base vector

      for (int i= 0; i < numPoints; i++) {
	double bx= vect[i].getLongitude();
	double by= vect[i].getLatitude();

	double convexity= ax * by - ay * bx;

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
    int seq[]= new int[] { bottom, right, top, left };

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

    MinRect minRect= new MinRect();

    /**************************************************************************/
    // Main loop - evaluate angles and rotate calipers
    double minarea= Float.MAX_VALUE;
    /* all of edges will be checked while rotating calipers by 90 degrees */
    for (int k= 0; k < numPoints; k++) {
      /* sinus of minimal angle */
      /* float sinus; */

      /* compute cosine of angle between calipers side and polygon edge */
      /* dp - dot product */
      double dp0=
	base_a * vect[seq[0]].getLongitude() + base_b
	  * vect[seq[0]].getLatitude();
      double dp1=
	-base_b * vect[seq[1]].getLongitude() + base_a
	  * vect[seq[1]].getLatitude();
      double dp2=
	-base_a * vect[seq[2]].getLongitude() - base_b
	  * vect[seq[2]].getLatitude();
      double dp3=
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
// maxcos = (cosalpha > maxcos) ? (main_element = 1, cosalpha) : maxcos;
      cosalpha= dp2 * inv_vect_length[seq[2]];
      if (cosalpha > maxcos) {
	main_element= 2;
	maxcos= cosalpha;
      }
// maxcos = (cosalpha > maxcos) ? (main_element = 2, cosalpha) : maxcos;
      cosalpha= dp3 * inv_vect_length[seq[3]];
      if (cosalpha > maxcos) {
	main_element= 3;
	maxcos= cosalpha;
      }
// maxcos = (cosalpha > maxcos) ? (main_element = 3, cosalpha) : maxcos;

      /* rotate calipers */
      {
	// get next base
	int pindex= seq[main_element];
	double lead_x= vect[pindex].getLongitude() * inv_vect_length[pindex];
	double lead_y= vect[pindex].getLatitude() * inv_vect_length[pindex];
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
	/* find area of rectangle */

	/* find vector left-right */
	double dx=
	  points.get( seq[1]).getLongitude()
	    - points.get( seq[3]).getLongitude();
	double dy=
	  points.get( seq[1]).getLatitude() - points.get( seq[3]).getLatitude();

	/* dotproduct */
	double width= dx * base_a + dy * base_b;

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

    Position2D[] out=
      new Position2D[] {
	new Position2D( px - 0.001, py - 0.001),
	new Position2D( px, py),
	new Position2D( px + A1 * minRect.width, py + B1 * minRect.width),
	new Position2D( px + A1 * minRect.width + A2 * minRect.height, py + B1
	  * minRect.width + B2 * minRect.height),
// new Position2D( px, py),
	new Position2D( px + A2 * minRect.height, py + B2 * minRect.height),
	new Position2D( px, py),
// dsadafsg
      };
// out[2]= A1 * minRect.width;
// out[3]= B1 * minRect.width;
//
// out[4]= A2 * minRect.height;
// out[5]= B2 * minRect.height;
    System.out.println( "out= " + Arrays.deepToString( out));
    return out;
  }
}
