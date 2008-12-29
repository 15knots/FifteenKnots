// $Id$
// Copyright © 2008 Martin Weber

package de.marw.fifteenknots.model;

import java.util.List;

import de.marw.fifteenknots.nmeareader.TrackEvent;


/**
 * A cruise of a boat. Contains boat metadata and the list of track points. None
 * of the track points in the list is guaranteed to hold information about
 * {@link TrackEvent#getSpeed() speed} or {@link TrackEvent#getBearing()
 * bearing}, only {@link TrackEvent#getDate() date} and
 * {@link TrackEvent#getPosition() position} is provided.
 *
 * @author Martin Weber
 */
public class BasicCruise implements Cruise
{
  private final List<TrackEvent> track;

  private final Boat boat;

  /**
   * Constructs a cruise object with the specified boat and track points.
   */
  public BasicCruise( Boat boat, List<TrackEvent> track)
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

  /**
   * Gets the metadata about the boat.
   */
  public Boat getBoat()
  {
    return boat;
  }

  /**
   * Gets all track points recorded for this cruise.
   *
   * @return all track points ordered ascending by time.
   */
  public List<TrackEvent> getTrackpoints()
  {
    return track;
  }

}
