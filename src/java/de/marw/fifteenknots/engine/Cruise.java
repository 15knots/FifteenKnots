// $Header$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import de.marw.fifteenknots.model.Boat;
import de.marw.fifteenknots.model.ICruise;
import de.marw.fifteenknots.model.IPolyLine;
import de.marw.fifteenknots.nmeareader.TrackEvent;


class Cruise implements ICruise
{

  private final List<TrackEvent> track;

  private final List<IPolyLine> polylines= new ArrayList<IPolyLine>();

  private final Boat boat;

  private float speedMin;

  private float speedMax;

  /**
   * @param boat
   * @param track
   */
  public Cruise( Boat boat, Vector<TrackEvent> track)
  {
    if (track == null) {
      throw new NullPointerException( "track");
    }
    this.track= track;
    if (boat == null) {
      throw new NullPointerException( "boat");
    }
    this.boat= boat;

  }

  /*-
   * @see de.marw.fifteenknots.model.ICruise#getBoat()
   */
  public Boat getBoat()
  {
    return boat;
  }

  /*-
   * @see de.marw.fifteenknots.engine.ICruise#getTrackpoints()
   */
  public List<TrackEvent> getTrackpoints()
  {
    return track;
  }

  /*-
   * @see de.marw.fifteenknots.engine.ICruise#getPolyLines()
   */
  public List<IPolyLine> getPolyLines()
  {
    return polylines;
  }

  /**
   * Gets the maximum speed that reached on the cruise.
   */
  public float getSpeedMax()
  {
    return this.speedMax;
  }

  /**
   * Sets the maximum speed that the boat reached on the cruise.
   * 
   * @param speedMax
   *        the maximum speed that reached on the cruise.
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