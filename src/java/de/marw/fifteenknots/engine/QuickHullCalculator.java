// $Id$
// Copyright © 2009 Martin Weber

package de.marw.fifteenknots.engine;

import java.util.List;
import java.util.concurrent.Callable;

import de.marw.fifteenknots.nmeareader.Position2D;
import de.marw.fifteenknots.nmeareader.TrackEvent;


/**
 * Calculates the convex hull of a set of points using the QuickHull algorithm.
 *
 * @author Martin Weber
 */
public class QuickHullCalculator implements Callable<List<Position2D>> {

  private List<TrackEvent> track;

  /**
   * Constructs a new object that calculates the convex hull the specified track
   * when method {@link #call()} is invoked.
   *
   * @param track
   *        the track points
   */
  public QuickHullCalculator( List<TrackEvent> track) {
    if (track == null) {
      throw new NullPointerException( "track");
    }
    this.track= track;
  }

  /**
   * Gets the convex hull of all points.
   *
   * @see Callable#call()
   */
  public List<Position2D> call() throws Exception {
    return QuickHull.quickHullOfTrack( track);
  }
}
