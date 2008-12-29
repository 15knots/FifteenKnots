// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.model;

import java.util.ArrayList;
import java.util.List;

import de.marw.fifteenknots.nmeareader.TrackEvent;


/**
 * A cruise of a boat with statistic data about the boat's speed and track
 * points {@link #getPolyLines() condensed by speed}. Each of the track points
 * in the list is guaranteed to hold information about
 * {@link TrackEvent#getSpeed() speed}, additionally to the basic
 * {@link TrackEvent#getDate() date} and {@link TrackEvent#getPosition()
 * position} information.
 *
 * @author Martin Weber
 */
public class SpeedCruise extends BasicCruise
{

  private final List<PolyLine> polylines= new ArrayList<PolyLine>();

  private float speedMin;

  private float speedMax;

  /**
   * Constructs a cruise object with the specified boat and track points.
   */
  public SpeedCruise( Boat boat, List<TrackEvent> track)
  {
    super( boat, track);
  }

  /**
   * Gets all lines made of consecutive track points with similiar speed.
   */
  public List<PolyLine> getPolyLines()
  {
    return polylines;
  }

  /**
   * Gets the maximum speed that the boat reached on the cruise.
   */
  public float getSpeedMax()
  {
    return this.speedMax;
  }

  /**
   * Sets the maximum speed that the boat reached on the cruise.
   *
   * @param speedMax
   *        the maximum speed that the boat reached on the cruise.
   */
  public void setSpeedMax( float speedMax)
  {
    this.speedMax= speedMax;
  }

  /**
   * Gets the minimum speed that the boat reached on the cruise.
   */
  public float getSpeedMin()
  {
    return this.speedMin;
  }

  /**
   * Sets the minimum speed that the boat reached on the cruise.
   *
   * @param speedMin
   *        the minimum speed that the boat reached on the cruise.
   */
  public void setSpeedMin( float speedMin)
  {
    this.speedMin= speedMin;
  }

}