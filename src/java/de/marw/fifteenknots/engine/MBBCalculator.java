// $Id$
// Copyright © 2009 Martin Weber

package de.marw.fifteenknots.engine;

import java.util.ArrayList;
import java.util.List;

import de.marw.fifteenknots.nmeareader.Position2D;
import de.marw.fifteenknots.nmeareader.TrackEvent;


/**
 * Minimum Bounding Box
 *
 * @author Martin Weber
 */
public class MBBCalculator {

  /**
   * RotatingCalipers <a href="http://cgm.cs.mcgill.ca/~orm/diam.html">Uses
   * method descibed here</a><br>
   * measures diameter (maximum) and width (minimum) distances between parallel
   * lines of support (~tangents)
   */
  public static void mbb( final List<TrackEvent> track) {
    final List<Position2D> pts= new ArrayList<Position2D>( track.size());
    for (TrackEvent trackEvent : track) {
      pts.add( trackEvent.getPosition());
    }
    List<Position2D> hull= QuickHull.quickHull( pts);

    final double pi= Math.PI;
    double dmax, dmin;
// run("Convex Hull");
// getSelectionCoordinates(perimx, perimy);

    // Compute the polygon's extreme points in the y direction
    double ymin= Double.MAX_VALUE;
    double ymax= Double.MIN_VALUE;
    final int npoints= hull.size();
    int podal=-1;
    int antipodal=-1;
    for (int n= 0; n < npoints; n++) {
      Position2D perim= hull.get( n);
      if (perim.getLatitude() < ymin) {
	ymin= perim.getLatitude();
	podal= n; // n is always 0 in ImageJ, start point of selection is
	// minimum x value within set with minimum y
      }
      if (perim.getLatitude() > ymax) {
	ymax= perim.getLatitude();
	antipodal= n;
      }
    }

    // create list of angles beween points
    double angles[]= new double[npoints];
    {
      for (int n= 0; n < npoints - 1; n++) {
	final Position2D perim1= hull.get( n);
	final Position2D perim2= hull.get( n + 1);
	angles[n]=
	  Math.atan2( perim1.getLatitude() - perim2.getLatitude(), perim1
	    .getLongitude()
	    - perim2.getLongitude());
      }
      // angle between last point and first point of selection
      final Position2D perim1= hull.get( npoints - 1);
      final Position2D perim2= hull.get( 0);
      angles[npoints - 1]=
	Math.atan2( perim1.getLatitude() - perim2.getLatitude(), perim1
	  .getLongitude()
	  - perim2.getLongitude());

      // correct any angles < -pi
      for (int n= 0; n < angles.length; n++) {
	if (angles[n] < -pi)
	  angles[n]= angles[n] + 2 * pi;
      }
    }
    // Construct two horizontal lines of support through ymin and ymax.
    // Since this is already an anti-podal pair, compute the distance, and keep
    // as maximum.
    dmax= ymax - ymin;
    dmin= dmax;

    final int end= antipodal;
    while (antipodal < npoints && podal <= end) {
      int podincr= 0;
      int antipodincr= 0;
      double testangle;
      double starttheta;
      double endtheta=0.0;
      // find the start condition for theta
      if (podal > 0) {
	starttheta= endtheta;
      }
      else {
	if (angles[antipodal - 1] > 0) {
	  testangle= angles[antipodal - 1] - pi;
	}
	else {
	  testangle= angles[antipodal - 1] + pi;
	}
	if (angles[npoints - 1] <= testangle) {
	  starttheta= angles[npoints - 1];
	}
	else {
	  starttheta= testangle;
	}
      }

      // Rotate the lines until one is flush with an edge of the polygon.
      // find the end condition for theta
      // increment the podal or antipodal point depending on
      // which caliper blade touches the next point first
      if (angles[antipodal] > 0) {
	testangle= angles[antipodal] - pi;
      }
      else {
	testangle= angles[antipodal] + pi;
      }
      if (antipodal <= npoints - 1) {
	if (angles[podal] > testangle && angles[podal] * testangle > 0) {
	  endtheta= angles[podal];
	  podincr= 1;
	}
	else if (angles[podal] < testangle) {
	  endtheta= testangle;
	  antipodincr= 1;
	}
	else if (angles[podal] > testangle && angles[podal] * testangle < 0) {
	  endtheta= testangle;
	  antipodincr= 1;
	}
	else {
	  endtheta= angles[podal];
	  podincr= 1;
	  antipodincr= 1;
	}
      }
      else {
	endtheta= 0;
      }

      // A new anti-podal pair is determined. Compute the new distance,
      // compare to old maximum, and update if necessary.
      double thetah=
	Math.atan2( hull.get( antipodal).getLatitude()
	  - hull.get( podal).getLatitude(), hull.get( antipodal).getLongitude()
	  - hull.get( podal).getLongitude());
      double distance=
	Math.sqrt( sqr( (hull.get( antipodal).getLongitude() - hull.get( podal)
	  .getLongitude()))
	  + sqr( (hull.get( antipodal).getLatitude() - hull.get( podal)
	    .getLatitude())));
      if (distance > dmax)
	dmax= distance;

      double ds;
      double de;
      if (starttheta >= endtheta) {
	ds= distance * Math.abs( Math.cos( -thetah + starttheta - pi / 2));
	de= distance * Math.abs( Math.cos( -thetah + endtheta - pi / 2));
	if (ds < dmin)
	  dmin= ds;
	else if (de < dmin)
	  dmin= de;
      }
      else if (starttheta < endtheta && starttheta * endtheta < 0) {
	ds= distance * Math.abs( Math.cos( -thetah + starttheta - pi / 2));
	de= distance * Math.abs( Math.cos( -thetah + -3 * pi / 2));
	if (ds < dmin)
	  dmin= ds;
	else if (de < dmin)
	  dmin= de;

	ds= distance * Math.abs( Math.cos( -thetah + pi / 2));
	de= distance * Math.abs( Math.cos( -thetah + endtheta - pi / 2));
	if (ds < dmin)
	  dmin= ds;
	else if (de < dmin)
	  dmin= de;
      }

      // increment either or both podal or antipodal
      podal= podal + podincr;
      antipodal= antipodal + antipodincr;
    }

//    setResult( "RCmax", 0, dmax);
//    setResult( "RCmin", 0, dmin);
  }

  private static double sqr( double n) {
    return n * n;
  }

}
