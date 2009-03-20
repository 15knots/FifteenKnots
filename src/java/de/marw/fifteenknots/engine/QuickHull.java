// $Id$
// Copyright © 2009 Martin Weber

package de.marw.fifteenknots.engine;

import java.util.ArrayList;
import java.util.List;

import de.marw.fifteenknots.nmeareader.Position2D;
import de.marw.fifteenknots.nmeareader.TrackEvent;


/**
 * Calculates the convex hull of a set of points using the QuickHull algorithm.
 *
 * @author Martin Weber
 */
public class QuickHull {

  /** nothing to instanciate */
  private QuickHull() {}

  /**
   * Calculates the convex hull of a set of points using the QuickHull
   * algorithm.
   */
  public static List<Position2D> quickHullOfTrack( final List<TrackEvent> track) {
    final List<Position2D> pts= new ArrayList<Position2D>( track.size());
    for (TrackEvent trackEvent : track) {
      pts.add( trackEvent.getPosition());
    }
    return quickHull( pts);
  }

  /**
   * Calculates the convex hull of a set of points using the QuickHull
   * algorithm.
   *
   * @param points
   *        the set of points. Note that the passed in List will be modified by
   *        the algorithm.
   */
  public static List<Position2D> quickHull( final List<Position2D> points) {
    final List<Position2D> convexHull= new ArrayList<Position2D>();
    int numPoints= points.size();
    if (numPoints < 3) {
      return new ArrayList<Position2D>( points);
    }
    // find extremals
    int minPointIdx= -1, maxPointIdx= -1;
    double minX= Double.MAX_VALUE;
    double maxX= Double.MIN_VALUE;
    for (int i= 0; i < numPoints; i++) {
      final Position2D point= points.get( i);
      if (point.getLatitude() < minX) {
	minX= point.getLatitude();
	minPointIdx= i;
      }
      if (point.getLatitude() > maxX) {
	maxX= point.getLatitude();
	maxPointIdx= i;
      }
    }
    final Position2D A= points.get( minPointIdx);
    final Position2D B= points.get( maxPointIdx);
    convexHull.add( A);
    convexHull.add( B);
    points.remove( A);
    points.remove( B);
    // Determine who's to the left or right of AB...
    final List<Position2D> leftSet= new ArrayList<Position2D>();
    final List<Position2D> rightSet= new ArrayList<Position2D>();
    numPoints= points.size();
    for (int i= 0; i < numPoints; i++) {
      final Position2D p= points.get( i);
      if (pointLocation( A, B, p) == -1)
	leftSet.add( p);
      else rightSet.add( p);
    }
    hullSet( convexHull, A, B, rightSet);
    hullSet( convexHull, B, A, leftSet);
    return convexHull;
  }

  /**
   * Checks whether a point lies on one side of a line segment or on the other
   * side.
   *
   * @param segmentStart
   *        the starting position of the segement
   * @param segmentEnd
   *        the ending position of the segement
   * @param point
   *        the point to test
   * @return -1, 1
   */
  private static int pointLocation( final Position2D segmentStart,
    final Position2D segmentEnd, final Position2D point) {
    // NOTE: This code implies a flat, rectangular world
    // compute cross product
    final double cp1=
      (segmentEnd.getLatitude() - segmentStart.getLatitude())
	* (point.getLongitude() - segmentStart.getLongitude())
	- (segmentEnd.getLongitude() - segmentStart.getLongitude())
	* (point.getLatitude() - segmentStart.getLatitude());
    return (cp1 > 0)
      ? 1 : -1;
  }

  /**
   * Gets the pseudo distance between a point and a line segment.
   *
   * @param segmentStart
   *        the starting position of the segement
   * @param segmentEnd
   *        the ending position of the segement
   * @param point
   *        the point to test
   * @return the pseudo distance (no square root is taken)
   */
  private static double distance( final Position2D segmentStart,
    final Position2D segmentEnd, final Position2D point) {
    // NOTE: This code implies a flat, rectangular world
    final double ABx= segmentEnd.getLatitude() - segmentStart.getLatitude();
    final double ABy= segmentEnd.getLongitude() - segmentStart.getLongitude();
    double num=
      ABx * (segmentStart.getLongitude() - point.getLongitude()) - ABy
	* (segmentStart.getLatitude() - point.getLatitude());
    if (num < 0)
      num= -num;
    return num;
  }

  private static void hullSet( final List<Position2D> hull,
    final Position2D segmentStart, final Position2D segmentEnd,
    final List<Position2D> set) {

    final int insertPosition= hull.indexOf( segmentEnd);
    int numPoints= set.size();
    if (numPoints == 0)
      return;
    if (numPoints == 1) {
      final Position2D p= set.get( 0);
      set.remove( p);
      hull.add( insertPosition, p);
      return;
    }
    double dist= Double.MIN_VALUE;
    int furthestPointIdx= -1;
    for (int i= 0; i < numPoints; i++) {
      final Position2D p= set.get( i);
      final double distance= distance( segmentStart, segmentEnd, p);
      if (distance > dist) {
	dist= distance;
	furthestPointIdx= i;
      }
    }
    final Position2D P= set.get( furthestPointIdx);
    set.remove( furthestPointIdx);
    hull.add( insertPosition, P);
    // Determine who's to the left of AP
    final ArrayList<Position2D> leftSetAP= new ArrayList<Position2D>();
    numPoints= set.size();
    for (int i= 0; i < numPoints; i++) {
      final Position2D M= set.get( i);
      if (pointLocation( segmentStart, P, M) == 1) {
	// set.remove(M);
	leftSetAP.add( M);
      }
    }
    // Determine who's to the left of PB
    final ArrayList<Position2D> leftSetPB= new ArrayList<Position2D>();
    for (int i= 0; i < numPoints; i++) {
      final Position2D M= set.get( i);
      if (pointLocation( P, segmentEnd, M) == 1) {
	// set.remove(M);
	leftSetPB.add( M);
      }
    }
    hullSet( hull, segmentStart, P, leftSetAP);
    hullSet( hull, P, segmentEnd, leftSetPB);
  }
}
